package company.vk.edu.distrib.compute.mrglaster.dao;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class UrlDao implements Dao<String> {
    private final Map<String, String> shortenedUrlInfoStorage = new ConcurrentHashMap<>();

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        String value = shortenedUrlInfoStorage.get(key);
        if (value == null) {
            throw new NoSuchElementException();
        }
        return value;
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {
        shortenedUrlInfoStorage.put(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        shortenedUrlInfoStorage.remove(key);
    }

    @Override
    public void close() throws IOException {

    }
}
