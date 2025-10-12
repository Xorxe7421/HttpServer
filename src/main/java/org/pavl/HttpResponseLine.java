package org.pavl;

public record HttpResponseLine(int httpVersion, int statusCode, String statusMessage) {

    @Override
    public String toString() {
        String httpVersionStart = "HTTP/1.";
        return httpVersionStart + httpVersion + " " + statusCode + " " + statusMessage;
    }
}
