package org.pavl;

public class Main {
    public static void main(String[] args) {
        HttpServer server = new HttpServer(8000);
        server.start();
    }
}
