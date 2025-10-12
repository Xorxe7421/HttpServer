package org.pavl;

import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
public class SinglePartRequestBody {

    private final HttpHeaders headers;
    private final byte[] body;

    public static List<SinglePartRequestBody> parseBody(HttpHeaders headers, byte[] body) {
        String contentType = headers.getValue(HttpHeaders.HttpHeaderKey.CONTENT_TYPE);
        if (contentType == null) {
            throw new IllegalArgumentException("Wrong content type, expecting \"multipart/form-data\" ");
        }

        String boundary = getBoundary(contentType);
        if (boundary == null) {
            throw new IllegalArgumentException("Invalid request header");
        }

        List<List<Byte>> individualParts = getIndividualParts(body, boundary);
        List<SinglePartRequestBody> result = new ArrayList<>();

        for (List<Byte> individualPart : individualParts) {
            SinglePartRequestBody singlePartRequestBody = parseIndividualPart(individualPart);
            result.add(singlePartRequestBody);
        }

        return result;
    }

    private static List<List<Byte>> getIndividualParts(byte[] body, String boundary) {
        List<List<Byte>> byteList = new ArrayList<>();

        List<Byte> bodyAsList = convertBytesToList(body);
        byte[] boundaryAsBytes = boundary.getBytes(StandardCharsets.UTF_8);
        List<Byte> boundaryByteList = convertBytesToList(boundaryAsBytes);

        while (!isLastBoundary(bodyAsList, boundaryByteList)) {
            bodyAsList = bodyAsList.subList(boundaryByteList.size() + 2, bodyAsList.size());
            int rightIndex = Collections.indexOfSubList(bodyAsList, boundaryByteList);

            List<Byte> singleBodyByteList = bodyAsList.subList(0, rightIndex - 2);
            byteList.add(singleBodyByteList);
            bodyAsList = bodyAsList.subList(rightIndex, bodyAsList.size());
        }

        return byteList;
    }

    private static boolean isLastBoundary(List<Byte> bodyAsList, List<Byte> boundaryByteList) {
        int bodySize = bodyAsList.size();
        return bodyAsList.subList(0, bodySize - 4).equals(boundaryByteList) &&
                bodyAsList.get(bodySize - 1) == ("\n".getBytes(StandardCharsets.UTF_8)[0]) &&
                bodyAsList.get(bodySize - 2) == ("\r".getBytes(StandardCharsets.UTF_8)[0]) &&
                bodyAsList.get(bodySize - 3) == ("-".getBytes(StandardCharsets.UTF_8)[0]) &&
                bodyAsList.get(bodySize - 4) == ("-".getBytes(StandardCharsets.UTF_8)[0]);
    }

    private static String getBoundary(String contentType) {
        String boundaryKey = "boundary=";
        int index = contentType.indexOf(boundaryKey);

        if (index == -1) {
            return null;
        }

        int boundaryBeginIndex = index + boundaryKey.length();
        return "--" + contentType.substring(boundaryBeginIndex);
    }

    private static SinglePartRequestBody parseIndividualPart(List<Byte> singlePart) {
        String boundary = "\r\n";
        byte[] boundaryAsBytes = boundary.getBytes(StandardCharsets.UTF_8);
        List<Byte> boundaryByteList = convertBytesToList(boundaryAsBytes);

        List<String> lines = new ArrayList<>();

        while (true) {
            int rightIndex = Collections.indexOfSubList(singlePart, boundaryByteList);
            List<Byte> line = singlePart.subList(0, rightIndex);
            singlePart = singlePart.subList(rightIndex + 2, singlePart.size());

            String lineString = new String(convertListToBytes(line), StandardCharsets.UTF_8);

            if (lineString.isBlank()) {
                return new SinglePartRequestBody(HttpRequest.parseHeaderLines(lines), convertListToBytes(singlePart));
            }

            lines.add(lineString);
        }
    }

    private static List<Byte> convertBytesToList(byte[] bytes) {
        final List<Byte> list = new ArrayList<>();
        for (byte b : bytes) {
            list.add(b);
        }
        return list;
    }

    private static byte[] convertListToBytes(List<Byte> list) {
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }

    private SinglePartRequestBody(HttpHeaders headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }
}
