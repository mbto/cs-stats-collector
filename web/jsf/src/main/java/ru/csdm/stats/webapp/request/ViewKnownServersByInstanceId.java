package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.service.EventService;
import ru.csdm.stats.webapp.DependentUtil;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_FRONTEND;
import static ru.csdm.stats.common.model.collector.Tables.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;

@ViewScoped
@Named
@Slf4j
public class ViewKnownServersByInstanceId {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private DependentUtil util;

    @Autowired
    private EventService eventService;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Getter
    private Instance selectedInstance;
    @Getter
    private List<KnownServer> currentKnownServers;
    @Getter
    private Map<UInteger, String> projectNameByProjectId;
    @Getter
    private Integer sessionsCount;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled())
            log.debug("\ninit");
    }

    public void onRowSelect(SelectEvent event) {
        Object object = event.getObject();

        if(log.isDebugEnabled())
            log.debug("\nonRowSelect " + object);

        UInteger projectId = ((KnownServer) object).getProjectId();

        util.sendRedirect("/knownServersByProject?projectId=" + projectId);
    }

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String instanceIdStr = request.getParameter("instanceId");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!StringUtils.isNumeric(instanceIdStr)) {
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Invalid instanceId", ""));
            return;
        }

        selectedInstance = collectorDsl.select(INSTANCE.ID, INSTANCE.NAME)
                .from(INSTANCE)
                .where(INSTANCE.ID.eq(UInteger.valueOf(instanceIdStr)))
                .fetchOneInto(Instance.class);

        if(Objects.isNull(selectedInstance)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN,
                    "Instance [" + instanceIdStr + "] not founded", ""));
            return;
        }

        currentKnownServers = collectorDsl.select(KNOWN_SERVER.asterisk())
                .from(KNOWN_SERVER)
                .join(INSTANCE).on(KNOWN_SERVER.INSTANCE_ID.eq(INSTANCE.ID))
                .where(INSTANCE.ID.eq(selectedInstance.getId()))
                .orderBy(KNOWN_SERVER.PROJECT_ID.desc(), KNOWN_SERVER.ID.asc())
                .fetchInto(KnownServer.class);

        projectNameByProjectId = collectorDsl.select(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT)
                .join(KNOWN_SERVER).on(PROJECT.ID.eq(KNOWN_SERVER.PROJECT_ID))
                .where(KNOWN_SERVER.INSTANCE_ID.eq(selectedInstance.getId()))
                .groupBy(PROJECT.ID)
                .fetchMap(PROJECT.ID, PROJECT.NAME);

        fetchAllSessionCount();
    }

    private void fetchAllSessionCount() {
        sessionsCount = gameSessionByAddress.values()
                .stream()
                .mapToInt(gameSessions -> gameSessions
                        .values()
                        .stream()
                        .mapToInt(cp -> cp.getSessions().size())
                        .sum())
                .sum();
    }

    public Integer getSessionCount(String address) {
        Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(address);
        if(Objects.isNull(gameSessions))
            return null;

        return gameSessions.values()
                .stream()
                .mapToInt(cp -> cp.getSessions().size())
                .sum();
    }

    public void flushAllAddresses() {
        log.info("Flush all sessions received from frontend");

        List<String> infoMsgs = new ArrayList<>();
        List<String> warnMsgs = new ArrayList<>();

        for (String address : gameSessionByAddress.keySet()) {
            try {
                eventService.flush(address, FLUSH_FROM_FRONTEND, false);
            } catch (Throwable e) {
                log.warn(address + " Flush not registered, " + e.getMessage());

                warnMsgs.add("Flush " + address + " not registered, " + e.getMessage());
                continue;
            }

            log.info(address + " Flush registered");

            infoMsgs.add("Flush " + address + " registered");
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        if(!infoMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, String.join("<br/>", infoMsgs), ""));

        if(!warnMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, String.join("<br/>", warnMsgs), ""));

        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException ignored) {}

        fetchAllSessionCount();
    }

    public void refreshAllAddresses() {
        log.info("Refresh all settings received from frontend");

        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            eventService.refresh(null);
        } catch (Throwable e) {
            String msg = "Refresh not registered, " + e.getMessage();
            log.warn(msg);

            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, msg, ""));
            return;
        }

        fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, "Refresh registered", ""));

        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException ignored) {}

        fetchAllSessionCount();
    }
}