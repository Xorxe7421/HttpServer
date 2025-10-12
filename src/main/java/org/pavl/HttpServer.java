package org.pavl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.pavl.config.BodySerializerModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private final int port;
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private static final ObjectMapper objectMapper;
    private static final int NUM_THREADS = 50;

    static {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BodySerializerModifier());
        objectMapper.registerModule(module);

        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port);
             ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS)) {
            logger.info("Server started");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    pool.submit(() -> handleConnection(clientSocket));
                } catch (IOException e) {
                    logger.error("IO exception occurred", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (clientSocket) {
            logger.info("Client request accepted");
            HttpRequest request = new HttpRequest(clientSocket.getInputStream());
            logger.info("Request: {}", objectMapper.writeValueAsString(request));
            HttpResponse response = HttpRequestDelegator.delegate(request);
            logger.info("Response: {}", objectMapper.writeValueAsString(response));
            sendResponse(clientSocket.getOutputStream(), response);
        } catch (IOException e) {
            logger.error("Exception occurred during request handling", e);
        }
    }

    private void sendResponse(OutputStream outputStream, HttpResponse response) throws IOException {
        logger.info("Sending response to the client");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        if (response != null) {
            writer.write(response.toString());
        }
        writer.flush();
        writer.close();
    }
}
