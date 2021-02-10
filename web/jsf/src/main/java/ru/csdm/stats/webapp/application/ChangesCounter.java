package ru.csdm.stats.webapp.application;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Named
@Slf4j
public class ChangesCounter {
    @Getter
    private final AtomicInteger counter = new AtomicInteger(0);

    public void increment(int value) {
        counter.addAndGet(value);
    }
}