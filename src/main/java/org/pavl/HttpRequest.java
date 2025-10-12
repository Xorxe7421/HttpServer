package org.pavl;

import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class HttpRequest {

    private final HttpRequestLine requestLine;
    private final HttpHeaders headers;
    private final byte[] body;

    public HttpRequest(InputStream in) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(in);

        String requestLineString = readLine(inputStream);
        if (requestLineString == null) {
            throw new IOException("End of the stream reached");
        }

        requestLine = HttpRequestLine.parseRequestLine(requestLineString);

        List<String> headerLines = getHeaderLines(inputStream);
        headers = parseHeaderLines(headerLines);

        body = setBody(inputStream);
    }

    private List<String> getHeaderLines(InputStream in) throws IOException {
        List<String> result = new ArrayList<>();

        String line;
        while (true) {
            line = readLine(in);
            if (line == null) {
                throw new IOException("End of the stream reached");
            }

            if (line.isBlank()) {
                break;
            }
            result.add(line);
        }

        return result;
    }

    public static HttpHeaders parseHeaderLines(List<String> headerLines) {
        Map<String, String> rawHeaderMap = headerLines.stream().collect(Collectors.toMap(headerLine -> {
            int colonIndex = headerLine.indexOf(":");
            return headerLine.substring(0, colonIndex);
        }, headerLine -> {
            int colonIndex = headerLine.indexOf(":");
            return headerLine.substring(colonIndex + 2);
        }));

        List<String> filteredHeaderKeys = new ArrayList<>();
        rawHeaderMap.keySet().forEach(key -> {
            try {
                HttpHeaders.HttpHeaderKey.fromString(key);
                filteredHeaderKeys.add(key);
            } catch (IllegalArgumentException ignored) {}
        });

        Map<HttpHeaders.HttpHeaderKey, String> headerMap =
                filteredHeaderKeys.stream().collect(Collectors.toMap(
                        HttpHeaders.HttpHeaderKey::fromString,
                        rawHeaderMap::get)
                );

        return new HttpHeaders(headerMap);
    }

    private byte[] setBody(InputStream in) throws IOException {
        Integer contentLengthAsInt = getContentLengthAsInt();
        if (contentLengthAsInt == null) return null;

        byte[] result = new byte[contentLengthAsInt];
        int totalRead = 0;

        while (totalRead < contentLengthAsInt) {
            int read = in.read(result, totalRead, contentLengthAsInt - totalRead);
            if (read == -1) {
                break;
            }
            totalRead += read;
        }

        return result;
    }

    private Integer getContentLengthAsInt() {
        String contentLength = headers.getValue(HttpHeaders.HttpHeaderKey.CONTENT_LENGTH);
        if (contentLength == null) {
            return null;
        }

        int contentLengthAsInt;
        try {
            contentLengthAsInt = Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Client provided invalid content length");
        }
        return contentLengthAsInt;
    }

    private String readLine(InputStream in) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        int newLine = '\n';
        int carriageReturn = '\r';
        while (true) {
            int i = in.read();
            if (i == -1) {
                return null;
            }

            if (i == newLine) {
                break;
            }

            if (i == carriageReturn) {
                int temp = i;
                i = in.read();
                if (i == -1) {
                    return null;
                }
                if (i == newLine) {
                    break;
                }
                stringBuilder.append(temp);
            }
            stringBuilder.append((char) i);
        }

        return stringBuilder.toString();
    }

    public String toString() {
        return "HttpRequest {" + "\n" +
                "   requestLine = " + requestLine + "," + "\n" +
                "   headers = " + headers + "," + "\n" +
                "   body = " + (body != null ? new String(body, StandardCharsets.UTF_8) : null) + "\n" +
                '}';
    }
}
