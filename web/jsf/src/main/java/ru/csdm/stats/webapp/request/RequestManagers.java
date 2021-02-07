package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Manager;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.List;

import static ru.csdm.stats.common.model.collector.tables.Manager.MANAGER;

@RequestScoped
@Named
@Slf4j
public class RequestManagers {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private DependentUtil util;

    @Getter
    private List<Manager> managers;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit");

        FacesContext fc = FacesContext.getCurrentInstance();
        if(fc.isPostback()) {
            if(util.trySendRedirect("showManagForm", "managTblId", "editManager", "managerId"))
                return;
        }
    }

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        managers = collectorDsl.selectFrom(MANAGER)
                .orderBy(MANAGER.REG_DATETIME.desc())
                .fetchInto(Manager.class);
    }
}