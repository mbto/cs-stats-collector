package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.List;

import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@RequestScoped
@Named
@Slf4j
public class RequestProjects {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private DependentUtil util;

    @Getter
    private List<Project> projects;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit");

        FacesContext fc = FacesContext.getCurrentInstance();
        if(fc.isPostback()) {
            if(util.trySendRedirect("showProjForm", "projTblId", "editProject", "projectId"))
                return;
        }
    }

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        projects = collectorDsl.selectFrom(PROJECT)
                .orderBy(PROJECT.REG_DATETIME.desc())
                .fetchInto(Project.class);
    }
}