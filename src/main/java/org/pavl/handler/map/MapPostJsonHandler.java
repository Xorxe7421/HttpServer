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

public class MapPostJsonHandler implements HttpRequestHandler {

    private final Map<String, String> map;

    public MapPostJsonHandler(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {
        if (!isJsonRequest(headers)) {
            return HttpRequestHandler.createBadRequestErrorResponse(0);
        }

        if (!isPostRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        Map<String, String> parsedBody;

        try {
            parsedBody = parseBody(body);
        } catch (JsonProcessingException e) {
            return HttpRequestHandler.createBadRequestErrorResponse(0, "Failed to process request body");
        }

        map.putAll(parsedBody);

        String bodyAsString = "OK";
        return HttpRequestHandler.createSuccessfulResponse(bodyAsString.getBytes(StandardCharsets.UTF_8), 1, DEFAULT_CONTENT_TYPE);
    }

    private Map<String, String> parseBody(byte[] body) throws JsonProcessingException {
        String bodyAsString = new String(body, StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(bodyAsString, new TypeReference<>() {});
    }
}
