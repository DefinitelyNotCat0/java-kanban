package kanban.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

public class BaseHttpHandler {
    protected void sendSuccess(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendSuccess(HttpExchange h) throws IOException {
        h.sendResponseHeaders(201, 0);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String message) throws IOException {
        sendError(h, message, 404);
    }

    protected void sendHasInteractions(HttpExchange h, String message) throws IOException {
        sendError(h, message, 406);
    }

    protected void sendInternalServerError(HttpExchange h, String message) throws IOException {
        sendError(h, message, 500);
    }

    protected void sendError(HttpExchange h, String message, int errorCode) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(errorCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected String getRequestBody(HttpExchange h) throws IOException {
        InputStream bodyInputStream = h.getRequestBody();
        return new String(bodyInputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
