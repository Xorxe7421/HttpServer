package org.pavl.handler.map;

import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.handler.HttpRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.pavl.HttpUtils.DEFAULT_CONTENT_TYPE;
import static org.pavl.HttpUtils.isGetRequest;

public class MapGetFormHandler implements HttpRequestHandler {

    private final Map<String, String> map;

    public MapGetFormHandler(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {
        if (!isGetRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        String key = requestLine.getQueryKeyValues().get("key");
        String value = map.get(key);

        if (value == null) {
            return HttpRequestHandler.createBadRequestErrorResponse(0, "Key Not Found");
        }

        return HttpRequestHandler.createSuccessfulResponse(value.getBytes(StandardCharsets.UTF_8), 0, DEFAULT_CONTENT_TYPE);
    }
}
