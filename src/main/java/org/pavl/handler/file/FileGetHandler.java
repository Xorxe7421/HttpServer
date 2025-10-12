package org.pavl.handler.file;

import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.HttpResponseLine;
import org.pavl.handler.HttpRequestHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;

import static org.pavl.HttpUtils.*;

public class FileGetHandler implements HttpRequestHandler {

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {

        if (!isGetRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        String fileName = requestLine.getQueryKeyValues().get("filename");
        byte[] fileContent = getFileContent(fileName);

        if (fileContent == null) {
            return HttpRequestHandler.createBadRequestErrorResponse(0, "File is invalid or not found");
        }

        HttpResponseLine responseLine = new HttpResponseLine(0, OK_STATUS_CODE, OK);
        Map<HttpHeaders.HttpHeaderKey, String> headerMap = Map.of(
                HttpHeaders.HttpHeaderKey.CONTENT_TYPE, BYTES_CONTENT_TYPE,
                HttpHeaders.HttpHeaderKey.CONTENT_LENGTH, String.valueOf(fileContent.length),
                HttpHeaders.HttpHeaderKey.CONTENT_DISPOSITION, String.format("attachment; filename=%s", fileName)
        );
        HttpHeaders responseHeaders = new HttpHeaders(headerMap);

        return new HttpResponse(responseLine, responseHeaders, fileContent);
    }

    private byte[] getFileContent(String fileName) {
        Path filePath;

        try {
            filePath = Path.of("./files", fileName);
        } catch (InvalidPathException e) {
            return null;
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            return null;
        }
    }
}
