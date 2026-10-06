package company.vk.edu.distrib.compute.mrglaster.kv.dao;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.NoSuchElementException;

public class EPRemoteDao implements Dao<String> {

    private final HttpClient httpClient;
    private final int port;

    public EPRemoteDao(HttpClient httpClient, int port) {
        this.httpClient = httpClient;
        this.port = port;
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        return "";
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {

    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
