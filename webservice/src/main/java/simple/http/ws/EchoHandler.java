package simple.http.ws;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import simple.http.business.base.SimpleHttpHandlerAdapter;

public class EchoHandler extends SimpleHttpHandlerAdapter {

    @Override
    public final void doGet(String path, Map<String, String> queryParams, Map<String, List<String>> headers, byte[] body, OutputStream outputStream) {
        set200OK(outputStream);
        setCookie(outputStream, "echo", "echoGet");
        setBody(outputStream, headers.toString());
        
        setEndOfContent(outputStream);
    }
    
    @Override
    public final void doPut(String path, Map<String, String> queryParams, Map<String, List<String>> headers, byte[] body, OutputStream outputStream) {
        set200OK(outputStream);
        setCookie(outputStream, "echo", "echoPut");
        setBody(outputStream, headers.toString());
        
        setEndOfContent(outputStream);
    }

}
