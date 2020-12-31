package ru.csdm.stats.webapp.request;

import lombok.Getter;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.csdm.stats.common.model.collector.enums.ProjectDatabaseServerTimezone;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.DependentUtil;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class KnownServersOperations {

    @Autowired
    private DSLContext collectorDsl;

    @Value("${collector.instance.name}")
    private String collectorInstanceName;

    @Autowired
    private DependentUtil util;

    @Getter
    private List<Project> projects;

//    public void onRowSelect(SelectEvent event) {
//        UInteger id = ((KnownServer) event.getObject()).getId();
//
//        FacesContext fc = FacesContext.getCurrentInstance();
//        ExternalContext exCtx = fc.getExternalContext();
//        try {
//            exCtx.redirect(util.getAbsoluteContextPath(true) + "/editKnownServer?id=" + id);
//        } catch (IOException ignored) {
//        } finally {
//            fc.responseComplete();
//        }
//    }

    public String literalOfProjectDatabaseServerTimezone(ProjectDatabaseServerTimezone timezone) {
        return timezone.getLiteral();
    }
}