package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.model.collector.Tables.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@RequestScoped
@Named
@Slf4j
public class RequestKnownServersByInstanceId {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private DependentUtil util;

    @Getter
    private Instance selectedInstance;
    @Getter
    private List<KnownServer> currentKnownServers;
    @Getter
    private Map<UInteger, String> projectNameByProjectId;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit");

        FacesContext fc = FacesContext.getCurrentInstance();
        if(fc.isPostback()) {
            if(util.sendRedirect("showKnServsForm", "knServsTblId", "projectKnownServers", "projectId"))
                return;
        }
    }

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String instanceIdStr = request.getParameter("instanceId");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!StringUtils.isNumeric(instanceIdStr)) {
            fc.addMessage(null, new FacesMessage(SEVERITY_WARN, "Invalid instanceId", ""));
            return;
        }

        selectedInstance = collectorDsl.select(INSTANCE.ID, INSTANCE.NAME)
                .from(INSTANCE)
                .where(INSTANCE.ID.eq(UInteger.valueOf(instanceIdStr)))
                .fetchOneInto(Instance.class);

        if(Objects.isNull(selectedInstance)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage(null/*TODO:try use "msgs"*/, new FacesMessage(SEVERITY_WARN,
                    "Instance [" + instanceIdStr + "] not founded", ""));
            return;
        }

        currentKnownServers = collectorDsl.select(KNOWN_SERVER.fields())
                .from(KNOWN_SERVER)
                .join(INSTANCE).on(KNOWN_SERVER.INSTANCE_ID.eq(INSTANCE.ID))
                .where(INSTANCE.ID.eq(selectedInstance.getId()))
                .orderBy(KNOWN_SERVER.PROJECT_ID.desc(), KNOWN_SERVER.ID.asc())
                .fetchInto(KnownServer.class);

        projectNameByProjectId = collectorDsl.selectDistinct(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT)
                .join(KNOWN_SERVER).on(PROJECT.ID.eq(KNOWN_SERVER.PROJECT_ID))
                .where(KNOWN_SERVER.INSTANCE_ID.eq(selectedInstance.getId()))
                .fetchMap(PROJECT.ID, PROJECT.NAME);
    }
}