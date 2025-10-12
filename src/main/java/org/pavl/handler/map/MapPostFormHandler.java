package org.pavl.handler.map;

import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.handler.HttpRequestHandler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.pavl.HttpUtils.*;

public class MapPostFormHandler implements HttpRequestHandler {

    private final Map<String, String> map;

    public MapPostFormHandler(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {
        if (!isFormRequest(headers)) {
            return HttpRequestHandler.createBadRequestErrorResponse(0);
        }

        if (!isPostRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        Map<String, String> parsedBody = parseBody(body);
        map.putAll(parsedBody);

        String bodyAsString = "OK";
        return HttpRequestHandler.createSuccessfulResponse(bodyAsString.getBytes(StandardCharsets.UTF_8), 1, DEFAULT_CONTENT_TYPE);
    }

    private Map<String, String> parseBody(byte[] body) {
        String bodyAsString = new String(body, StandardCharsets.UTF_8);
        StringTokenizer tokenizer = new StringTokenizer(bodyAsString, "&");

        Map<String, String> result = new HashMap<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int divisionIndex = token.indexOf("=");
            String key = token.substring(0, divisionIndex);
            String value = token.substring(divisionIndex + 1);

            result.put(key, value);
        }

        return result;
    }
}
