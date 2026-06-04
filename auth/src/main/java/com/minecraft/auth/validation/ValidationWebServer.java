package com.minecraft.auth.validation;

import com.minecraft.auth.Core;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * HTTP embarcado para endpoints de validacao (port de Savage
 * {@code core/.../ValidationWebServer}).
 *
 * <p>Habilitado quando {@code auth.validation-port > 0} no config.
 */
public final class ValidationWebServer {

    @Getter
    private final int port;

    private HttpServer server;

    public ValidationWebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        if (port <= 0) return;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());

        server.createContext("/health", exchange -> {
            byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
        });

        server.createContext("/api/validate", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            String response = "{\"valid\":true,\"query\":\"" + (query == null ? "" : query) + "\"}";
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
        });

        server.start();
        Core.getLogger().info("ValidationWebServer ouvindo em :" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            Core.getLogger().info("ValidationWebServer encerrado.");
        }
    }
}
