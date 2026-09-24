package company.vk.edu.distrib.compute.mrglaster.controller.model;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseController {


    protected void sendStringResponse(HttpExchange exchange, String message, StatusCode statusCode) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(statusCode.getStatusCode(), responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected void sendStatusCodeResponse(HttpExchange exchange, StatusCode statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(statusCode.getStatusCode(), -1);
    }
}
