package ru.csdm.stats.modules.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.DatagramSocket;

@Service
@Lazy(false)
@Slf4j
public class Module {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Broker broker;
    @Value("${collector.broker.port:8888}")
    private int brokerPort;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("init() start");

        log.info("Binding broker port " + brokerPort);

        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket(brokerPort);
        } catch (Throwable e) {
            log.warn("Failed binding broker port " + brokerPort, e);
            int code = SpringApplication.exit(applicationContext, () -> 1);
            System.exit(code);
            return;
        }

        try {
            broker.refresh(null);
        } catch (Throwable e) {
            try {
                datagramSocket.close();
            } catch (Throwable ignored) {}
            throw e;
        }

        broker.setDatagramSocket(datagramSocket);
        broker.launchReceiverAsync();
        broker.launchDistributorAsync();

        if(log.isDebugEnabled())
            log.debug("init() finish");
    }
}