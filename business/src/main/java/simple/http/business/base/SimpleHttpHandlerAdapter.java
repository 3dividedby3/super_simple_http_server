package simple.http.business.base;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import simple.http.business.exc.SuperSimpleException;

public class SimpleHttpHandlerAdapter implements SimpleHttpHandler {

    @Override
    public void doGet(String path, Map<String, String> queryParams, Map<String, List<String>> headers, byte[] body, OutputStream outputStream) {
        // TODO Auto-generated method stub
    }

    @Override
    public void doPut(String path, Map<String, String> queryParams, Map<String, List<String>> headers, byte[] body, OutputStream outputStream) {
        // TODO Auto-generated method stub
    }
    
    protected void setBody(OutputStream outputStream, String body) throws SuperSimpleException {
        try {
            outputStream.write((HTTP_ELEMENT_SEPARATOR + HTTP_ELEMENT_SEPARATOR + body).getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting body", e);
        }
    }
    
    protected void setContentTypeJson(OutputStream outputStream) throws SuperSimpleException {
        setContentTypeTo(outputStream, "application/json; charset=utf-8");
    }
    
    protected void setContentTypeTextHtml(OutputStream outputStream) throws SuperSimpleException {
        setContentTypeTo(outputStream, "text/html; charset=UTF-8");
    }
    
    protected void setContentTypeTo(OutputStream outputStream, String contentType) throws SuperSimpleException {
        try {
            outputStream.write((HTTP_ELEMENT_SEPARATOR + "Content-Type: " + contentType).getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting ContentType: " + contentType, e);
        }
    }
    
    protected void setCookie(OutputStream outputStream, String cookieName, String cookieValue) throws SuperSimpleException {
        String authCookieValue = HTTP_ELEMENT_SEPARATOR + "Set-Cookie: " + cookieName + "=" + cookieValue;
        try {
            outputStream.write(authCookieValue.getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting Cookie: " + cookieName + "=" + cookieValue, e);
        }
    }

    protected void set500InternalServerError(OutputStream outputStream) throws SuperSimpleException {
        try {
            outputStream.write(("HTTP/1.1 500 Internal Server Error").getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting InternalServerError", e);
        }
    }
    
    protected void set401Unauthorized(OutputStream outputStream) throws SuperSimpleException {
        try {
            outputStream.write(("HTTP/1.1 401 Unauthorized").getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting Unauthorized", e);
        }
    }
    
    protected void set400BadRequest(OutputStream outputStream) throws SuperSimpleException {
        try {
            outputStream.write(("HTTP/1.1 400 Bad Request").getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting BadRequest", e);
        }
    }
    
    protected void set200OK(OutputStream outputStream) throws SuperSimpleException {
        try {
            outputStream.write(("HTTP/1.1 200 OK").getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting OK", e);
        }
    }
    
    protected void set204NoContent(OutputStream outputStream) throws SuperSimpleException {
        try {
            outputStream.write(("HTTP/1.1 204 No Content").getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting NoContent", e);
        }
    }
    
    protected void setEndOfContent(OutputStream outputStream) throws SuperSimpleException {
        try {
            outputStream.write((HTTP_ELEMENT_SEPARATOR + HTTP_ELEMENT_SEPARATOR).getBytes());
        } catch (IOException e) {
            throw new SuperSimpleException("Error setting EndOfContent", e);
        }
    }
    
}
