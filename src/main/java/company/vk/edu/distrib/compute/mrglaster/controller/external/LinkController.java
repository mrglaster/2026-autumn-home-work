package company.vk.edu.distrib.compute.mrglaster.controller.external;

import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.mrglaster.annotation.Route;
import company.vk.edu.distrib.compute.mrglaster.controller.model.BaseController;
import company.vk.edu.distrib.compute.mrglaster.controller.model.StatusCode;
import company.vk.edu.distrib.compute.mrglaster.dao.UrlDao;
import company.vk.edu.distrib.compute.mrglaster.service.ShortLinksGeneratorService;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.NoSuchElementException;

public class LinkController extends BaseController {

    private final UrlDao urlDao;
    private final ShortLinksGeneratorService shortLinksGeneratorService;

    public LinkController(UrlDao urlDao, String baseUrl) {
        this.urlDao = urlDao;
        this.shortLinksGeneratorService = new ShortLinksGeneratorService(baseUrl);
    }

    @Route(method = "GET", path = "/v0/links/{id}", requiresAuthorization = true)
    public void getFullUrl(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String id = pathParams.get("id");
        try {
            if (!isValidID(id)) {
                exchange.sendResponseHeaders(422, -1);
                return;
            }
            String longUrl = urlDao.get(id);
            this.sendStringResponse(exchange, longUrl, StatusCode.HTTP_OK);
        } catch (NoSuchElementException e) {
            exchange.sendResponseHeaders(404, -1);
        } catch (IllegalArgumentException | IOException e) {
            exchange.sendResponseHeaders(500, -1);
        }
    }

    @Route(method = "GET", path = "/{id}", requiresAuthorization = true)
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

    @Route(method = "POST", path = "/v0/links", requiresAuthorization = true)
    public void createLink(HttpExchange exchange) throws IOException, NoSuchAlgorithmException {
        String longUrl = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (longUrl.isEmpty() || !isValidUrl(longUrl)) {
            exchange.sendResponseHeaders(422, -1);
            return;
        }
        String shortId = shortLinksGeneratorService.generateLinkID(longUrl);
        String shortUrl = shortLinksGeneratorService.generateShortURL(shortId);
        urlDao.upsert(shortId, longUrl);
        this.sendStringResponse(exchange, shortUrl, StatusCode.HTTP_CREATED);
    }

    @Route(method = "PUT", path = "/v0/links/{id}", requiresAuthorization = true)
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
            this.sendStringResponse(exchange, "OK", StatusCode.HTTP_OK);
        } catch (NoSuchElementException e) {
            this.sendStatusCodeResponse(exchange, StatusCode.HTTP_NOT_FOUND);
        } catch (IllegalArgumentException | IOException e) {
            this.sendStatusCodeResponse(exchange, StatusCode.HTTP_INTERNAL_SERVER_ERROR);
        }
    }

    @Route(method = "DELETE", path = "/v0/links/{id}", requiresAuthorization = true)
    public void deleteLink(HttpExchange exchange, Map<String, String> pathParams) throws IOException {
        String id = pathParams.get("id");
        if (!isValidID(id)) {
            this.sendStatusCodeResponse(exchange, StatusCode.HTTP_UNPROCESSABLE_ENTITY);
            return;
        }
        urlDao.delete(id);
        this.sendStatusCodeResponse(exchange, StatusCode.HTTP_ACCEPTED);
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
