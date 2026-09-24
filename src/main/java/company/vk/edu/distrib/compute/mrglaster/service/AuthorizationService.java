package company.vk.edu.distrib.compute.mrglaster.service;

import company.vk.edu.distrib.compute.mrglaster.dao.UserDao;
import com.sun.net.httpserver.HttpExchange;
import company.vk.edu.distrib.compute.mrglaster.security.PasswordHasher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthorizationService {
    private final UserDao userDao;

    public AuthorizationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean checkBasicAuth(String authHeader) throws IOException {


        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }

        try {
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);

            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                return false;
            }

            String username = parts[0];
            String password = parts[1];

            return verify(username, password);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean verify(String login, String password){
        try {
            String storedHash = userDao.get(login);
            return PasswordHasher.verify(password, storedHash);
        } catch (IOException e) {
            return false;
        }
    }
}