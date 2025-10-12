package org.pavl;

import java.nio.charset.StandardCharsets;

public record HttpResponse(HttpResponseLine responseLine, HttpHeaders headers, byte[] body) {

    @Override
    public String toString() {
        return responseLine.toString() + "\r\n" + headers.toString() + "\r\n" + "\r\n" + new String(body, StandardCharsets.UTF_8);
    }
}
