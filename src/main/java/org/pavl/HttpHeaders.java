package org.pavl;

import lombok.Getter;

import java.util.*;

@Getter
public class HttpHeaders {

    private EnumMap<HttpHeaderKey, String> headerMap;

    public HttpHeaders() {

    }

    public HttpHeaders(Map<HttpHeaderKey, String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("Invalid constructor input: " + headers);
        }
        this.headerMap = new EnumMap<>(headers);
    }

    public String getValue(HttpHeaderKey key) {
        return headerMap.get(key);
    }

    @Override
    public String toString() {
        String result =  headerMap
                .keySet()
                .stream()
                .reduce("",
                        (current, key) -> current + key.toString() + ": " + headerMap.get(key) + "\r\n",
                        (first, second) -> first + second
                );

        return result.substring(0, result.length() - 2);
    }

    public enum HttpHeaderKey {
        USER_AGENT,
        HOST,
        CONNECTION,
        KEEP_ALIVE,
        ACCEPT_LANGUAGE,
        ACCEPT_ENCODING,
        ACCEPT,
        DATE,
        SERVER,
        CONTENT_TYPE,
        CONTENT_LENGTH,
        CONTENT_ENCODING,
        AUTHORIZATION,
        COOKIE,
        SET_COOKIE,
        LAST_MODIFIED,
        EXPIRES,
        CACHE_CONTROL,
        CONTENT_DISPOSITION;

        public static HttpHeaderKey fromString(String value) {
            String upperCaseValue = value.toUpperCase();
            String finalResult = upperCaseValue.replace('-', '_');
            return HttpHeaderKey.valueOf(finalResult);
        }

        @Override
        public String toString() {
            String value = super.toString();
            List<String> tokens = tokenizeValue(value);
            List<String> capitalisedTokens = capitaliseTokens(tokens);
            return String.join("-", capitalisedTokens);
        }

        private List<String> tokenizeValue(String value) {
            StringTokenizer tokenizer = new StringTokenizer(value, "_");
            List<String> result = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                result.add(tokenizer.nextToken());
            }
            return result;
        }

        private List<String> capitaliseTokens(List<String> tokens) {
            return tokens.stream().map(token -> {
                String lowerCaseToken = token.toLowerCase();
                return Character.toUpperCase(lowerCaseToken.charAt(0)) + lowerCaseToken.substring(1);
            }) .toList();
        }
    }
}
