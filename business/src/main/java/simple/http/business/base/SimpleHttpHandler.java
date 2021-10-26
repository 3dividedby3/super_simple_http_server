package simple.http.business.base;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface SimpleHttpHandler {
    
    String HTTP_ELEMENT_SEPARATOR = "\r\n";

    void doGet(String path, Map<String, String> queryParams, Map<String, List<String>> headers, byte[] body, OutputStream outputStream);
    void doPut(String path, Map<String, String> queryParams, Map<String, List<String>> headers, byte[] body, OutputStream outputStream);
}
