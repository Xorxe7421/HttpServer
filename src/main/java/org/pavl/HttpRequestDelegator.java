package org.pavl;

import org.pavl.handler.*;
import org.pavl.handler.file.FileGetHandler;
import org.pavl.handler.file.FilePostHandler;
import org.pavl.handler.map.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpRequestDelegator {

    private static final Map<String, String> map = new ConcurrentHashMap<>();

    private static final Map<String, HttpRequestHandler> requestHandlerMap = Map.of(
            "/map/get/form", new MapGetFormHandler(map),
            "/map/post/form", new MapPostFormHandler(map),
            "/map/get/all/form", new MapGetAllFormHandler(map),
            "/map/get/json", new MapGetJsonHandler(map),
            "/map/post/json", new MapPostJsonHandler(map),
            "/map/get/all/json", new MapGetAllJsonHandler(map),
            "/file/get", new FileGetHandler(),
            "/file/post", new FilePostHandler()
    );

    public static HttpResponse delegate(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        HttpRequestLine requestLine = request.getRequestLine();
        byte[] body = request.getBody();

        String path = requestLine.getPath();
        if (!requestHandlerMap.containsKey(path)) {
            return null;
        }

        return requestHandlerMap.get(path).handle(requestLine, headers, body);
    }
}
