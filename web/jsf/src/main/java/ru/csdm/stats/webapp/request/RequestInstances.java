package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;

@RequestScoped
@Named
@Slf4j
public class RequestInstances {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private DependentUtil util;

    @Getter
    private List<Instance> instances;
    @Getter
    private Map<UInteger, Integer> knownServersAtAllInstances;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit");

        FacesContext fc = FacesContext.getCurrentInstance();
        if(fc.isPostback()) {
            if(util.trySendRedirect("showInstForm", "instTblId", "editInstance", "instanceId"))
                return;
        }
    }

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        instances = collectorDsl.selectFrom(INSTANCE)
                .orderBy(INSTANCE.REG_DATETIME.desc())
                .fetchInto(Instance.class);

        knownServersAtAllInstances = collectorDsl.select(INSTANCE.ID, DSL.countDistinct(KNOWN_SERVER.ID))
                .from(INSTANCE)
                .leftJoin(KNOWN_SERVER).on(INSTANCE.ID.eq(KNOWN_SERVER.INSTANCE_ID))
                .groupBy(INSTANCE.ID)
                .fetchMap(INSTANCE.ID, DSL.count());
    }
}