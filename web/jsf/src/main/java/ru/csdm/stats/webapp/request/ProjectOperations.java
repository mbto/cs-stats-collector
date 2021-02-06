package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.DependentUtil;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.util.List;

import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@RequestScoped
@Named
@Slf4j
public class ProjectOperations {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private DependentUtil util;

    @Getter
    private List<Project> projects;

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        projects = collectorDsl.selectFrom(PROJECT)
                .orderBy(PROJECT.REG_DATETIME.desc())
                .fetchInto(Project.class);
    }

    public void onRowSelect() {
        String projectId = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("showProjForm:projTblId_instantSelectedRowKey");
        util.sendRedirect(util.getAbsoluteContextPath(true) + "/editProject?projectId=" + projectId);
    }
}