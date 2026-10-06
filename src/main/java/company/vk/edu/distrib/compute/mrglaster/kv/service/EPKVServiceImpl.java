package company.vk.edu.distrib.compute.mrglaster.kv.service;

import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.kv.KVService;
import company.vk.edu.distrib.compute.kv.KVServiceTest;
import company.vk.edu.distrib.compute.mrglaster.kv.controller.EntityController;
import company.vk.edu.distrib.compute.mrglaster.kv.controller.KVStatusController;
import company.vk.edu.distrib.compute.mrglaster.urlshortener.controller.manager.ControllerManager;
import company.vk.edu.distrib.compute.mrglaster.urlshortener.controller.route.external.StatusController;
import company.vk.edu.distrib.compute.mrglaster.urlshortener.dao.PersistentDao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class EPKVServiceImpl implements KVService {

    private final HttpServer httpServer;
    private static final int SHUTDOWN_DELAY = 5;
    private final PersistentDao<byte[]> entityDao;
    private static final String ENTITY_STORAGE_FILE = "/tmp/entity";
    public EPKVServiceImpl(int port) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        ControllerManager routingManager = new ControllerManager(null);
        this.entityDao = new PersistentDao<>(ENTITY_STORAGE_FILE, PersistentDao.byteArraySerializer());;
        routingManager.addController(new EntityController(entityDao));
        routingManager.addController(new KVStatusController());
        routingManager.register(this.httpServer);
    }

    @Override
    public void start() {
        httpServer.start();
    }

    @Override
    public void stop() {
        httpServer.stop(SHUTDOWN_DELAY);
    }

    @Override
    public CompletableFuture<Void> awaitTermination() {
        return KVService.super.awaitTermination();
    }
}
