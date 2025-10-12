package org.pavl;

public class HttpUtils {

    public static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=UTF-8";
    public static final String BYTES_CONTENT_TYPE = "application/octet-stream";

    public static boolean isGetRequest(HttpRequestLine requestLine) {
        return requestLine.getHttpMethod() == HttpMethod.GET;
    }

    public static boolean isPostRequest(HttpRequestLine requestLine) {
        return requestLine.getHttpMethod() == HttpMethod.POST;
    }

    public static boolean isFormRequest(HttpHeaders headers) {
        return headers.getValue(HttpHeaders.HttpHeaderKey.CONTENT_TYPE).equals(FORM_CONTENT_TYPE);
    }

    public static boolean isJsonRequest(HttpHeaders headers) {
        return headers.getValue(HttpHeaders.HttpHeaderKey.CONTENT_TYPE).equals(JSON_CONTENT_TYPE);
    }
}
