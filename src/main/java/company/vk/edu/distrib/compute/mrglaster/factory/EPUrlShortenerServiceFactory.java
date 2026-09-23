package company.vk.edu.distrib.compute.mrglaster.factory;

import company.vk.edu.distrib.compute.AbstractHttpServiceFactory;
import company.vk.edu.distrib.compute.mrglaster.service.EPUrlShortenerService;

import java.io.IOException;

public class EPUrlShortenerServiceFactory extends AbstractHttpServiceFactory<EPUrlShortenerService> {

    @Override
    protected EPUrlShortenerService doCreate(int port) throws IOException {
        return new EPUrlShortenerService(port);
    }
}
