package company.vk.edu.distrib.compute.mrglaster.service;

import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.mrglaster.controller.LinkController;
import company.vk.edu.distrib.compute.mrglaster.controller.StatusController;
import company.vk.edu.distrib.compute.mrglaster.dao.UrlDao;
import company.vk.edu.distrib.compute.mrglaster.network.ControllerManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class EPUrlShortenerService implements company.vk.edu.distrib.compute.urlshortener.UrlShortenerService {

    private static final Logger logger = Logger.getLogger(EPUrlShortenerService.class.getName());
    private final int shutdownDelay = 5;
    private final int port;
    private final HttpServer httpServer;

    public EPUrlShortenerService(int port) throws IOException {
        this.port = port;
        this.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);

        String baseUrl = "http://127.0.0.1:" + port;
        UrlDao urlDao = new UrlDao();

        ControllerManager routingManager = new ControllerManager();
        routingManager.addController(new StatusController());
        routingManager.addController(new LinkController(urlDao, baseUrl));
        routingManager.register(httpServer);
    }

    @Override
    public void start() {
        httpServer.start();
    }

    @Override
    public void stop() {
        httpServer.stop(shutdownDelay);
    }
}