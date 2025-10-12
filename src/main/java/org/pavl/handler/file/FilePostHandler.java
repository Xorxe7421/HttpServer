package org.pavl.handler.file;

import org.pavl.HttpHeaders;
import org.pavl.HttpRequestLine;
import org.pavl.HttpResponse;
import org.pavl.SinglePartRequestBody;
import org.pavl.handler.HttpRequestHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.pavl.HttpUtils.DEFAULT_CONTENT_TYPE;
import static org.pavl.HttpUtils.isPostRequest;

public class FilePostHandler implements HttpRequestHandler {

    @Override
    public HttpResponse handle(HttpRequestLine requestLine, HttpHeaders headers, byte[] body) {

        if (!isPostRequest(requestLine)) {
            return HttpRequestHandler.createMethodNotAllowedErrorResponse(0);
        }

        List<SinglePartRequestBody> multiPartBody;
        try {
            multiPartBody = SinglePartRequestBody.parseBody(headers, body);
        }catch (IllegalArgumentException e) {
            return HttpRequestHandler.createBadRequestErrorResponse(0, e.getMessage());
        }

        for (SinglePartRequestBody singlePartRequestBody : multiPartBody) {
            try {
                saveFile(singlePartRequestBody);
            } catch (IOException | IllegalArgumentException e) {
                return HttpRequestHandler.createBadRequestErrorResponse(0, e.getMessage());
            }
        }

        String bodyAsString = "OK";
        return HttpRequestHandler.createSuccessfulResponse(bodyAsString.getBytes(StandardCharsets.UTF_8), 1, DEFAULT_CONTENT_TYPE);
    }

    private void saveFile(SinglePartRequestBody singlePartRequestBody) throws IOException {
        String fileName = getFileName(singlePartRequestBody);
        Path file = Path.of("./files", fileName);
        Files.write(file, singlePartRequestBody.getBody());
    }

    private static String getFileName(SinglePartRequestBody singlePartRequestBody) {
        HttpHeaders headers = singlePartRequestBody.getHeaders();
        String headerValue = headers.getValue(HttpHeaders.HttpHeaderKey.CONTENT_DISPOSITION);

        String fileNameKey = "filename=";
        int keyIndex = headerValue.indexOf(fileNameKey);

        if (keyIndex == -1) {
            throw new IllegalArgumentException("Wrong multipart type, file expected");
        }

        String fileName = headerValue.substring(keyIndex + fileNameKey.length() + 1);
        return fileName.replaceAll("\"", "");
    }
}
