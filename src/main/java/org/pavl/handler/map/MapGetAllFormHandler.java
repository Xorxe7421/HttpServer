package org.pavl.handler.map;

import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.handler.HttpRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.pavl.HttpUtils.*;

public class MapGetAllFormHandler implements HttpRequestHandler {

    private final Map<String, String> map;

    public MapGetAllFormHandler(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {
        if (!isGetRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        String bodyAsString = toFormBody(map);
        return HttpRequestHandler.createSuccessfulResponse(bodyAsString.getBytes(StandardCharsets.UTF_8), 0, DEFAULT_CONTENT_TYPE);
    }

    private String toFormBody(Map<String, String> map) {
        return map
                .keySet()
                .stream()
                .reduce("", (current, key) -> current + key + "=" + map.get(key) + '\n');
    }
}
