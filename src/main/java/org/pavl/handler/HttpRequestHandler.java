package org.pavl.handler;

import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.HttpResponseLine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.pavl.HttpUtils.DEFAULT_CONTENT_TYPE;

public interface HttpRequestHandler {

    String OK = "OK";
    int OK_STATUS_CODE = 200;

    String BAD_REQUEST = "Bad Request";
    int BAD_REQUEST_ERROR_CODE = 400;

    String METHOD_NOT_ALLOWED = "Method Not Allowed";
    int METHOD_NOT_ALLOWED_ERROR_CODE = 405;

    String INTERNAL_SERVER_ERROR = "Internal Server Error";
    int SERVER_ERROR_CODE = 500;

    HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body);

    static HttpResponse createSuccessfulResponse(byte[] body, int httpVersion, String contentType) {
        HttpResponseLine responseLine = new HttpResponseLine(httpVersion, OK_STATUS_CODE, OK);
        return getHttpResponse(body, responseLine, contentType);
    }

    static HttpResponse createBadRequestErrorResponse(int httpVersion) {
        return createBadRequestErrorResponse(httpVersion, BAD_REQUEST);
    }

    static HttpResponse createBadRequestErrorResponse(int httpVersion, String message) {
        byte[] bodyAsBytes = message.getBytes(StandardCharsets.UTF_8);
        HttpResponseLine responseLine = new HttpResponseLine(httpVersion, BAD_REQUEST_ERROR_CODE, BAD_REQUEST);
        return getHttpResponse(bodyAsBytes, responseLine, DEFAULT_CONTENT_TYPE);
    }

    static HttpResponse createMethodNotAllowedErrorResponse(int httpVersion) {
        byte[] bodyAsBytes = METHOD_NOT_ALLOWED.getBytes(StandardCharsets.UTF_8);
        HttpResponseLine responseLine = new HttpResponseLine(httpVersion, METHOD_NOT_ALLOWED_ERROR_CODE, METHOD_NOT_ALLOWED);
        return getHttpResponse(bodyAsBytes, responseLine, DEFAULT_CONTENT_TYPE);
    }

    static HttpResponse createServerErrorResponse(int httpVersion) {
        byte[] bodyAsBytes = INTERNAL_SERVER_ERROR.getBytes(StandardCharsets.UTF_8);
        HttpResponseLine responseLine = new HttpResponseLine(httpVersion, SERVER_ERROR_CODE, INTERNAL_SERVER_ERROR);
        return getHttpResponse(bodyAsBytes, responseLine, DEFAULT_CONTENT_TYPE);
    }

    private static HttpResponse getHttpResponse(byte[] bodyAsBytes, HttpResponseLine responseLine, String contentType) {
        Map<HttpHeaders.HttpHeaderKey, String> headerMap = Map.of(
                HttpHeaders.HttpHeaderKey.CONTENT_TYPE, contentType,
                HttpHeaders.HttpHeaderKey.CONTENT_LENGTH, String.valueOf(bodyAsBytes.length)
        );
        HttpHeaders headers = new HttpHeaders(headerMap);

        return new HttpResponse(responseLine, headers, bodyAsBytes);
    }
}
