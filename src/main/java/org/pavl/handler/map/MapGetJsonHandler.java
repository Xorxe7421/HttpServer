package org.pavl.handler.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.handler.HttpRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.pavl.HttpUtils.*;

public class MapGetJsonHandler implements HttpRequestHandler {

    private final Map<String, String> map;

    public MapGetJsonHandler(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {
        if (!isGetRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        Map<String, String> parsedBody;

        try {
            parsedBody = parseBody(body);
        } catch (JsonProcessingException e) {
            return HttpRequestHandler.createBadRequestErrorResponse(0, "Failed to process request body");
        }

        if (!parsedBody.containsKey("key")) {
            return HttpRequestHandler.createBadRequestErrorResponse(0, "Invalid request body");
        }

        String key = parsedBody.get("key");
        String value = map.get(key);

        if (value == null) {
            return HttpRequestHandler.createBadRequestErrorResponse(0, "Key Not Found");
        }

        Map<String, String> valueMap = Map.of(key, value);

        String bodyAsString;
        try {
            bodyAsString = toJsonBody(valueMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return HttpRequestHandler.createSuccessfulResponse(bodyAsString.getBytes(StandardCharsets.UTF_8), 0, JSON_CONTENT_TYPE);
    }

    private String toJsonBody(Map<String, String> valueMap) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(valueMap);
    }

    private Map<String, String> parseBody(byte[] body) throws JsonProcessingException {
        String bodyAsString = new String(body, StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(bodyAsString, new TypeReference<>() {});
    }
}
