package ru.csdm.stats.modules.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.modules.collector.handlers.Listener;
import ru.csdm.stats.service.CollectorService;

import javax.annotation.PostConstruct;
import java.net.DatagramSocket;

@Service
@Lazy(false)
@Slf4j
public class Module {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Listener listener;
    @Autowired
    private CollectorService collectorService;
    @Value("${collector.listener.port:8888}")
    private int listenerPort;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("init() start");

        log.info("Activating listener at port " + listenerPort);

        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket(listenerPort);
        } catch (Throwable e) {
            log.warn("Failed initialize listener at port " + listenerPort, e);
            int code = SpringApplication.exit(applicationContext, () -> 1);
            System.exit(code);
            return;
        }

        try {
            collectorService.refresh(null);
        } catch (Throwable e) {
            try {
                datagramSocket.close();
            } catch (Throwable ignored) {}
            throw e;
        }

        listener.setDatagramSocket(datagramSocket);
        listener.setupConsumers();
        listener.launchReceiverAsync();
        listener.launchDistributorAsync();

        if(log.isDebugEnabled())
            log.debug("init() finish");
    }
}