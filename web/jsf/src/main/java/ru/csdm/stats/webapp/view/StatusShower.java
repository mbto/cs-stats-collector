package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Getter
@Setter
@ViewScoped
@Named
@Slf4j
public class StatusShower {
    private int processors;
    private String freeMemory;
    private String maxMemory;
    private String allocatedMemory;
    private String totalFreeMemory;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit()");

        calculate();
    }

    public void calculate() {
        Runtime runtime = Runtime.getRuntime();

        long freeMemoryL = runtime.freeMemory();
        long maxMemoryL = runtime.maxMemory();
        long allocatedMemoryL = runtime.totalMemory();

        processors = runtime.availableProcessors();
        freeMemory = String.format("%.2f", freeMemoryL / 1024f / 1024f);
        maxMemory = String.format("%.2f", maxMemoryL / 1024f / 1024f);
        allocatedMemory = String.format("%.2f", allocatedMemoryL / 1024f / 1024f);

        totalFreeMemory = String.format("%.2f", (freeMemoryL + (maxMemoryL - allocatedMemoryL)) / 1024f / 1024f);
    }
}