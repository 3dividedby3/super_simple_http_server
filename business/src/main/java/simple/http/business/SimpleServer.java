package simple.http.business;

import static simple.http.business.PropertiesReader.CHARSET_UTF8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import simple.http.business.base.SimpleHttpHandler;
import simple.http.business.logging.LoggerHelper;

public class SimpleServer {
    private static final LoggerHelper LOG = new LoggerHelper(SimpleServer.class.getName());
    
    public static final String SPACE = " ";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_ELEMENT_SEPARATOR = "\r\n";
    public static final String HTTP_BODY_SEPARATOR = HTTP_ELEMENT_SEPARATOR + HTTP_ELEMENT_SEPARATOR;
    public static final String HTTP_HEADER_VALUE_SEPARATOR = ": ";
    
    //TODO: extract to properties file
    private static final int THREAD_POOL_SIZE = 7;
    
    private final Map<String, SimpleHttpHandler> handlerMap;
    private final long maxExecutionTimeMillis;
    private final int serverPort;
    
    public SimpleServer(Map<String, SimpleHttpHandler> handlerMap, long maxExecutionTimeMillis, int serverPort) {
        this.handlerMap = handlerMap;
        this.maxExecutionTimeMillis = maxExecutionTimeMillis;
        this.serverPort = serverPort;
    }
    
    public void startHandlingWithTimeout() throws IOException {
        LOG.info("Starting server with handlers: " + handlerMap);
        
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE); 
        ServerSocket serverSocket = new ServerSocket(serverPort);
        LOG.info("Server listening on port: " + serverPort);
        while (true) {
            LOG.info("*** Waiting for new connection");
            Socket socket = serverSocket.accept();
            LOG.info("New connection from getLocalSocketAddress: " + socket.getLocalSocketAddress());
            LOG.info("New connection from getRemoteSocketAddress: " + socket.getRemoteSocketAddress());
            Future<?> handler = executor.submit(new Runnable(){
                @Override
                public void run() {
                    handleSocketConnWithErrorManager(socket);
                }
            });
            executor.schedule(new Runnable(){
                public void run() {
                    handler.cancel(true);
                }      
            }, maxExecutionTimeMillis, TimeUnit.MILLISECONDS);
        }
    }
    
    private void handleSocketConnWithErrorManager(Socket socket) {
        try {
            handleSocketConn(socket);
        } catch (Exception e) {
            LOG.error("Error handling socket", e);
        }
    }  

    private void handleSocketConn(Socket socket) throws InterruptedException, IOException {
        LOG.info("Handling conn from getRemoteSocketAddress: " + socket.getRemoteSocketAddress());
        InputStream inputStream = socket.getInputStream();
        String method = null;
        String path = null;
        byte[] body = null;
        StringBuilder currentLine = new StringBuilder();
        Map<String, List<String>> headers = new HashMap<>();
        int currentReadInt;
        while (true) {
            currentReadInt = inputStream.read();
            if (currentReadInt == -1) {
                LOG.debug("Received end of stream");
                break;
            }
            currentLine.append((char)currentReadInt);
            if (currentLine.length() == HTTP_ELEMENT_SEPARATOR.length() 
                    && currentLine.toString().equals(HTTP_ELEMENT_SEPARATOR)
                    && headers.containsKey(HEADER_CONTENT_LENGTH)) {
                LOG.debug("*** Prepare to read body");
                body = readBody(inputStream, Integer.valueOf(headers.get(HEADER_CONTENT_LENGTH).get(0)));
                break;
            } else if (currentLine.length() > HTTP_ELEMENT_SEPARATOR.length() 
                    && HTTP_ELEMENT_SEPARATOR.equals(currentLine.substring(currentLine.length() - HTTP_ELEMENT_SEPARATOR.length()))) {
                currentLine.setLength(currentLine.length() - HTTP_ELEMENT_SEPARATOR.length());
                LOG.debug("currentLine: " + currentLine.toString());
                if (path == null) {
                    int startPath = currentLine.indexOf(SPACE);
                    method = currentLine.substring(0, startPath);
                    path = currentLine.substring(startPath + 1, currentLine.indexOf(SPACE, startPath + 1));
                } else if (currentLine.length() > 2) {
                    int idxSeparatorHeaderNameValue = currentLine.indexOf(HTTP_HEADER_VALUE_SEPARATOR);
                    String headerName = currentLine.substring(0, idxSeparatorHeaderNameValue);
                    String headerValue = currentLine.substring(idxSeparatorHeaderNameValue + HTTP_HEADER_VALUE_SEPARATOR.length()
                        , currentLine.length());
                    if (headers.containsKey(headerName)) {
                        headers.get(headerName).add(headerValue);
                    } else {
                        List<String> headerValues = new ArrayList<>();
                        headerValues.add(headerValue);
                        headers.put(headerName, headerValues);
                    }
                    
                    if ("Expect".equals(headerName) && "100-continue".equals(headerValue)) {
                        socket.getOutputStream().write(("HTTP/1.1 100 Continue").getBytes());
                        LOG.debug("Sent to client: HTTP/1.1 100 Continue");
                    }
                }
                currentLine.setLength(0);
            }
            if (inputStream.available() == 0) {
                LOG.debug("No more data available");
                break;
            }
        }
        LOG.info("method: " + method);
        LOG.info("path: " + path);
        LOG.info("headers: " + headers);
        if (body != null) {
            LOG.info("body with value");
            LOG.debug("body: " + new String(body, CHARSET_UTF8));
        } else {
            LOG.info("body is null");
        }
        
        String[] pathSplit = path.split("\\?");
        String uriPartOfPath = pathSplit[0];
        Map<String, String> queryParams = new HashMap<>();
        if (pathSplit.length != 2) {
            LOG.debug("path does not contain query string");
        } else {
            String queryStringOfPath = pathSplit.length == 2 ? pathSplit[1] : "";
            extractQueryParams(queryParams, queryStringOfPath);
        }
        handleRequest(method, uriPartOfPath, queryParams, headers, body, socket.getOutputStream());
        socket.close();
        LOG.info("Done");
    }

    private void extractQueryParams(Map<String, String> queryParams, String queryStringOfPath) {
        Arrays.stream(queryStringOfPath.split("&")).forEach(queryKeyValue -> {
            String[] keyValueSplit = queryKeyValue.split("=");
            if (keyValueSplit.length != 2) {
                LOG.error("invalid query parameter: " + queryKeyValue);
            } else {
                queryParams.put(keyValueSplit[0], keyValueSplit[1]);
            }
        });
    }

    private void handleRequest(String method, String path, Map<String, String> queryParams, Map<String, List<String>> headers, byte[] body,
            OutputStream outputStream) {
        SimpleHttpHandler httpHandler = handlerMap.get(path);
        if (httpHandler == null) {
            LOG.error("No handler mapped for path: " + path);
            
            return;
        }
        switch(method) {
            case "GET": httpHandler.doGet(path, queryParams, headers, body, outputStream);
                break;
            case "PUT": httpHandler.doPut(path, queryParams, headers, body, outputStream);
                break;
            default: LOG.warn("No handler mapped for method: [" + method + "] with valid path: " + path);
        }
             
    }

    private byte[] readBody(InputStream inputStream, int length) throws IOException {
        LOG.info("Reading body...");
        byte[] body = new byte[length];
        int nrOfReadBytes = 0;
        while (inputStream.available() > 0 || nrOfReadBytes < length) {
            nrOfReadBytes += inputStream.read(body, nrOfReadBytes, length - nrOfReadBytes);
            LOG.debug("read " + nrOfReadBytes + " out of a total of " + length);
        }

        return body;
    }

}
