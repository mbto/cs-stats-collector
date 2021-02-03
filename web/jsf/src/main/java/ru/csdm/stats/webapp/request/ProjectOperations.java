package ru.csdm.stats.webapp.request;

import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.util.List;

import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@Named
@RequestScoped
public class ProjectOperations {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private DependentUtil util;

    @Value("${collector.instance.name}")
    private String collectorInstanceName;

    @Getter
    private List<Project> projects;

    @PostConstruct
    public void init() {
        projects = collectorDsl.selectFrom(PROJECT)
                .orderBy(PROJECT.REG_DATETIME.desc())
                .fetchInto(Project.class);
    }

    public void onRowSelect(SelectEvent event) {
        UInteger projectId = ((Project) event.getObject()).getId();

        util.sendRedirect(util.getAbsoluteContextPath(true) + "/editProject?projectId=" + projectId);
    }



//    public String literalOfProjectDatabaseServerTimezone(ProjectDatabaseServerTimezone timezone) {
//        return timezone.getLiteral();
//    }
}