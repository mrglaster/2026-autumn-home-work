package company.vk.edu.distrib.compute.mrglaster.controller.internal;

import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.mrglaster.annotation.Route;
import company.vk.edu.distrib.compute.mrglaster.controller.model.BaseController;
import company.vk.edu.distrib.compute.mrglaster.controller.model.StatusCode;
import company.vk.edu.distrib.compute.mrglaster.dao.UserDao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UserController extends BaseController {

    private final UserDao userDao;

    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @Route(method = "POST", path = "/internal/users", requiresAuthorization = false)
    public void createOrUpdateUser(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();

        String[] parts = body.split(":", 2);
        if (parts.length != 2 || parts[0].trim().isEmpty()) {
            this.sendStatusCodeResponse(exchange, StatusCode.HTTP_BAD_REQUEST);
            return;
        }

        String username = parts[0].trim();
        String password = parts[1];

        try {
            userDao.upsert(username, password);
            this.sendStringResponse(exchange, "OK", StatusCode.HTTP_OK);
        } catch (IllegalArgumentException e) {
            exchange.sendResponseHeaders(400, 0);
        }
    }
}