package company.vk.edu.distrib.compute.mrglaster.kv.factory;

import company.vk.edu.distrib.compute.AbstractHttpServiceFactory;
import company.vk.edu.distrib.compute.kv.KVServiceTest;
import company.vk.edu.distrib.compute.mrglaster.kv.service.EPKVServiceImpl;

import java.io.IOException;

@KVServiceTest
public class EPKVServiceFactory extends AbstractHttpServiceFactory<EPKVServiceImpl> {
    @Override
    protected EPKVServiceImpl doCreate(int port) throws IOException {
        return new EPKVServiceImpl(port);
    }
}
