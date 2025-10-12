package org.pavl.handler.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.handler.HttpRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.pavl.HttpUtils.*;

public class MapGetAllJsonHandler implements HttpRequestHandler {

    private final Map<String, String> map;

    public MapGetAllJsonHandler(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {
        if (!isGetRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        String bodyAsString;

        try {
            bodyAsString = toJsonBody(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return HttpRequestHandler.createSuccessfulResponse(bodyAsString.getBytes(StandardCharsets.UTF_8), 0, JSON_CONTENT_TYPE);
    }

    private String toJsonBody(Map<String, String> map) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(map);
    }
}
