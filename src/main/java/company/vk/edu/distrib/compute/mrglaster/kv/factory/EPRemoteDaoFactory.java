package company.vk.edu.distrib.compute.mrglaster.kv.factory;

import company.vk.edu.distrib.compute.Dao;
import company.vk.edu.distrib.compute.kv.RemoteDaoFactory;
import company.vk.edu.distrib.compute.kv.RemoteDaoFactoryTest;
import company.vk.edu.distrib.compute.mrglaster.kv.service.EPKVServiceImpl;
import company.vk.edu.distrib.compute.mrglaster.kv.dao.EPRemoteDao;

import java.io.IOException;
import java.net.http.HttpClient;

@RemoteDaoFactoryTest
public class EPRemoteDaoFactory implements RemoteDaoFactory<String> {

    @Override
    public Dao<String> create(int... ports) throws IOException {
        final var port = ports[0];
        final EPKVServiceImpl service = new EPKVServiceImpl(port);
        service.start();
        return new EPRemoteDao(HttpClient.newHttpClient(), port);
    }
}
