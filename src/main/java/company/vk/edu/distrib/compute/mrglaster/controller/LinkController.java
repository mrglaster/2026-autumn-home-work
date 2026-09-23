package company.vk.edu.distrib.compute.mrglaster.controller;

import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.mrglaster.annotation.Route;
import company.vk.edu.distrib.compute.mrglaster.dao.UrlDao;
import company.vk.edu.distrib.compute.mrglaster.service.ShortLinksGeneratorService;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.NoSuchElementException;

public class LinkController {

    private final UrlDao urlDao;
    private final ShortLinksGeneratorService shortLinksGeneratorService;

    public LinkController(UrlDao urlDao, String baseUrl) {
        this.urlDao = urlDao;
        this.shortLinksGeneratorService = new ShortLinksGeneratorService(baseUrl);
    }

    @Route(method = "GET", path = "/v0/links/{id}")
    public void getFullUrl(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String id = pathParams.get("id");

        try {
            if (!isValidID(id)) {
                exchange.sendResponseHeaders(422, -1);
                return;
            }
            String longUrl = urlDao.get(id);
            byte[] responseBytes = longUrl.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        } catch (NoSuchElementException e) {
            exchange.sendResponseHeaders(404, -1);
        } catch (IllegalArgumentException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
        }
    }

    @Route(method = "GET", path = "/{id}")
    public void redirectLink(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String id = pathParams.get("id");
        try {
            if (!isValidID(id)) {
                exchange.sendResponseHeaders(422, -1);
                return;
            }
            String longUrl = urlDao.get(id);
            exchange.getResponseHeaders().set("Location", longUrl);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(301, -1);
        } catch (NoSuchElementException e) {
            exchange.sendResponseHeaders(404, -1);
        } catch (IllegalArgumentException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
        }
    }

    @Route(method = "POST", path = "/v0/links")
    public void createLink(HttpExchange exchange) throws IOException, NoSuchAlgorithmException {
        String longUrl = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (longUrl.isEmpty() || !isValidUrl(longUrl)) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }
        String shortId = shortLinksGeneratorService.generateLinkID(longUrl);
        String shortUrl = shortLinksGeneratorService.generateShortURL(shortId);
        urlDao.upsert(shortId, longUrl);
        byte[] responseBytes = shortUrl.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(201, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    @Route(method = "PUT", path = "/v0/links/{id}")
    public void updateLink(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String id = pathParams.get("id");
        try {
            String newLongUrl = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (!isValidUrl(newLongUrl) || !isValidID(id)) {
                exchange.sendResponseHeaders(422, -1);
                return;
            }
            urlDao.get(id);
            urlDao.upsert(id, newLongUrl);
            byte[] responseBytes = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        } catch (NoSuchElementException e) {
            exchange.sendResponseHeaders(404, -1);
        } catch (IllegalArgumentException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
        }
    }

    @Route(method = "DELETE", path = "/v0/links/{id}")
    public void deleteLink(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String id = pathParams.get("id");
        if (!isValidID(id)) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }
        urlDao.delete(id);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(202, -1);
    }

    private boolean isValidUrl(String urlString) {
        try {
            URL uri = URI.create(urlString).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidID(String id) {
        if (id == null) return false;
        return id.matches("[0-9a-zA-Z]{10}");
    }
}
