package company.vk.edu.distrib.compute.mrglaster.dao;

import company.vk.edu.distrib.compute.Dao;
import company.vk.edu.distrib.compute.mrglaster.security.PasswordHasher;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class UserDao implements Dao<String> {
    private final Map<String, String> userCredentials = new ConcurrentHashMap<>();

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        String value = userCredentials.get(key);
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {
        if (key == null || value.trim().isEmpty() || value == null) {
            throw new IllegalArgumentException("Username and password must not be empty");
        }
        String hashedPassword = PasswordHasher.hash(value);
        userCredentials.put(key.trim(), hashedPassword);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        userCredentials.remove(key);
    }

    @Override
    public void close() throws IOException {

    }

    public boolean verify(String username, String password) {
        String storedHash = userCredentials.get(username);
        if (storedHash == null) {
            return false;
        }
        return PasswordHasher.verify(password, storedHash);
    }

}
