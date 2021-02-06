package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@RequestScoped
@Named
@Slf4j
public class ProjectOperations {
    @Autowired
    private DSLContext collectorDsl;

    @Getter
    private List<Project> projects;
    @PostConstruct
    public void init() {
        log.debug("\nProjectOperations()");
    }
    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        projects = Collections.emptyList();
//        projects = collectorDsl.selectFrom(PROJECT) //TODO: by instance ?
//                .orderBy(PROJECT.REG_DATETIME.desc())
//                .fetchInto(Project.class);
    }
}