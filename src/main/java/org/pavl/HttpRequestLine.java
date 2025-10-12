package org.pavl;

import lombok.Getter;
import lombok.ToString;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
@ToString
public class HttpRequestLine {

    private HttpMethod httpMethod;
    private String path;
    private Map<String, String> queryKeyValues;
    private int httpVersion;

    private static final int TOKEN_AMOUNT = 3;
    private static final String PROTOCOL_VERSION_PREFIX = "HTTP/1.";

    public static HttpRequestLine parseRequestLine(String requestLine) {
        StringTokenizer tokenizer = new StringTokenizer(requestLine, " ");
        List<String> tokens = tokenizeRequestLine(tokenizer);

        if (tokens.size() != TOKEN_AMOUNT) {
            throw new IllegalArgumentException("Invalid request line: " + requestLine);
        }

        HttpRequestLine httpRequestLine = new HttpRequestLine();

        String firstToken = tokens.get(0);
        httpRequestLine.httpMethod = HttpMethod.valueOf(firstToken);

        String secondToken = tokens.get(1);
        if (!secondToken.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + secondToken);
        }

        if (!secondToken.contains("?")) {
            httpRequestLine.path = secondToken;
        }else {
            int dividerIndex = secondToken.indexOf('?');
            httpRequestLine.path = secondToken.substring(0, dividerIndex);
            String queryString = secondToken.substring(dividerIndex + 1);
            String decodedQueryString = URLDecoder.decode(queryString, StandardCharsets.UTF_8);
            httpRequestLine.queryKeyValues = parseQueryString(decodedQueryString);
        }

        String thirdToken = tokens.get(2);
        if (!thirdToken.startsWith(PROTOCOL_VERSION_PREFIX)
                || thirdToken.length() != PROTOCOL_VERSION_PREFIX.length() + 1) {
            throw new IllegalArgumentException("Invalid protocol version: " + thirdToken);
        }
        int lastIndex = thirdToken.length() - 1;
        httpRequestLine.httpVersion = Integer.parseInt(thirdToken.substring(lastIndex, lastIndex + 1));

        return httpRequestLine;
    }

    private static Map<String, String> parseQueryString(String decodedQueryString) {
        Map<String, String> result = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(decodedQueryString, "=");

        while (tokenizer.hasMoreTokens()) {
            String key = tokenizer.nextToken();

            if (tokenizer.hasMoreTokens()) {
                String value = tokenizer.nextToken();

                result.put(key, value);
            }
        }

        return result;
    }

    private static List<String> tokenizeRequestLine(StringTokenizer tokenizer) {
        List<String> result = new ArrayList<>();
        while (tokenizer.hasMoreElements()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }

    private HttpRequestLine() {

    }
}
