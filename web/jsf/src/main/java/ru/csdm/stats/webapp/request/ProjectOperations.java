package ru.csdm.stats.webapp.request;

import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.csdm.stats.common.model.collector.enums.ProjectDatabaseServerTimezone;
import ru.csdm.stats.common.model.collector.tables.Instance;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import java.io.IOException;
import java.util.List;

import static ru.csdm.stats.common.model.collector.tables.Instance.*;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@Named
@RequestScoped
public class ProjectOperations {

    @Autowired
    private DSLContext collectorDsl;

    @Value("${collector.instance.name}")
    private String collectorInstanceName;

    @Autowired
    private DependentUtil util;

    @Getter
    private List<Project> projects;

    @PostConstruct
    public void init() {
        projects = collectorDsl.selectFrom(PROJECT)
                .orderBy(PROJECT.REG_DATETIME.desc())
                .fetchInto(Project.class);
    }

    public void onRowSelect(SelectEvent event) {
        UInteger id = ((Project) event.getObject()).getId();

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext exCtx = fc.getExternalContext();
        try {
            exCtx.redirect(util.getAbsoluteContextPath(true) + "/editProject?id=" + id);
        } catch (IOException ignored) {
        } finally {
            fc.responseComplete();
        }
    }

    public String literalOfProjectDatabaseServerTimezone(ProjectDatabaseServerTimezone timezone) {
        return timezone.getLiteral();
    }
}