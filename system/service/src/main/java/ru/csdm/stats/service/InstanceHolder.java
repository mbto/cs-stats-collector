package ru.csdm.stats.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;

@Service
@Lazy(false)
@Slf4j
public class InstanceHolder {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DSLContext collectorDsl;

    @Getter
    private UInteger currentInstanceId;
    @Getter
    private boolean isDevEnvironment;

    private Map<UInteger, Instance> availableInstances;
    private LocalDateTime nextRefreshAvailableInstances;

    @PostConstruct
    public void init() {
        isDevEnvironment = Arrays.asList(applicationContext
                .getEnvironment()
                .getActiveProfiles()).contains("dev");

        reload();
    }

    public void reload() {
        try {
            String tempValue = applicationContext.getEnvironment()
                    .getProperty("collector.instance.name", String.class);

            if(StringUtils.isBlank(tempValue)) {
                throw new IllegalStateException("empty property 'collector.instance.name'");
            }

            final String collectorInstanceName = tempValue.trim();
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                Instance currentInstance = transactionalDsl.selectFrom(INSTANCE)
                        .where(INSTANCE.NAME.eq(collectorInstanceName))
                        .fetchOneInto(Instance.class);

                if (Objects.nonNull(currentInstance)) {
                    currentInstanceId = currentInstance.getId();
                } else {
                    currentInstanceId = transactionalDsl.insertInto(INSTANCE)
                            .set(INSTANCE.NAME, collectorInstanceName)
                            .set(INSTANCE.DESCRIPTION, "Auto-added")
                            .returning(INSTANCE.ID)
                            .fetchOne().getId();
                }
            });

            getAvailableInstances(true);
        } catch (Throwable e) {
            log.warn("Shutdown module, due " + e.toString(), e);

            int code = SpringApplication.exit(applicationContext, () -> 1);
            System.exit(code);
            return;
        }
    }

    public void setCurrentInstanceId(UInteger currentInstanceId) {
        if(isDevEnvironment) {
            this.currentInstanceId = currentInstanceId;
        }
    }

    public Map<UInteger, Instance> getAvailableInstances() {
        return getAvailableInstances(false);
    }

    public synchronized Map<UInteger, Instance> getAvailableInstances(boolean needRefresh) {
        LocalDateTime now = LocalDateTime.now();
        if (needRefresh || Objects.isNull(nextRefreshAvailableInstances) || now.isAfter(nextRefreshAvailableInstances)) {
            availableInstances = collectorDsl.selectFrom(INSTANCE)
                    .fetchMap(INSTANCE.ID, Instance.class);

            nextRefreshAvailableInstances = now.plusMinutes(10);
        }

        return availableInstances;
    }
}