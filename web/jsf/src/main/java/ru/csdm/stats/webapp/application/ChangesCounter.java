package ru.csdm.stats.webapp.application;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@Named
public class ChangesCounter {
//    @Autowired
//    private SettingsService settingsService;

    @Getter
    private final AtomicInteger counter = new AtomicInteger(0);

    public void increment(int value) {
        counter.addAndGet(value);
    }
}
