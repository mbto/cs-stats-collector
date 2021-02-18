package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.common.utils.SomeUtils;
import ru.csdm.stats.service.CollectorService;
import ru.csdm.stats.service.InstanceHolder;
import ru.csdm.stats.webapp.application.ChangesCounter;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_FRONTEND;
import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;

@ViewScoped
@Named
@Slf4j
public class ViewEditInstance {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CollectorService collectorService;

    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private InstanceHolder instanceHolder;
    @Autowired
    private ChangesCounter changesCounter;

    @Getter
    private Instance selectedInstance;

    @Getter
    private int knownServersAtAllInstances;

    private int localChangesCounter;

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

        UInteger instanceId = UInteger.valueOf(instanceIdStr);

        selectedInstance = instanceHolder.getAvailableInstances(true)
                .get(instanceId);

        if(Objects.isNull(selectedInstance)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Instance [" + instanceId + "] not founded", ""));
            return;
        }

        fetchKnownServersCounts();
    }

    private void fetchKnownServersCounts() {
        knownServersAtAllInstances = collectorDsl.selectCount()
                .from(KNOWN_SERVER)
                .where(KNOWN_SERVER.INSTANCE_ID.eq(selectedInstance.getId()))
                .fetchOne(DSL.count());
    }

    public void save() {
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;

        try {
            final String newDescription = StringUtils.isBlank(selectedInstance.getDescription()) ? null : selectedInstance.getDescription();

            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                localChangesCounter += SomeUtils.pointwiseUpdateQuery(INSTANCE.ID,
                        Arrays.asList(Pair.of(INSTANCE.DESCRIPTION, newDescription)),
                        selectedInstance.getId(),
                        transactionalDsl);
            });

            changesCounter.increment(localChangesCounter);

            instanceHolder.getAvailableInstances(true);

            fc.addMessage("msgs", new FacesMessage("Instance [" + selectedInstance.getId() + "] "
                    + selectedInstance.getName() + " saved", localChangesCounter + " changes"));
        } catch (Exception e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save instance [" + selectedInstance.getId() + "] " + selectedInstance.getName(),
                    e.toString()));
        }
    }

    public String delete() {
        localChangesCounter = 0;

        try {
            fetchKnownServersCounts();

            if(knownServersAtAllInstances > 0) {
                throw new IllegalStateException("You must delete " + knownServersAtAllInstances
                        + " known server" + (knownServersAtAllInstances > 1 ? "s" : "") + " from all instances for delete this instance"
                        + " [" + selectedInstance.getId() + "] " + selectedInstance.getName());
            }

            localChangesCounter += collectorDsl.deleteFrom(INSTANCE)
                    .where(INSTANCE.ID.eq(selectedInstance.getId()))
                    .execute();

            changesCounter.increment(localChangesCounter);

            instanceHolder.getAvailableInstances(true);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed delete instance [" + selectedInstance.getId() + "] " + selectedInstance.getName(),
                    e.toString()));

            return null;
        }

        return "/instances?faces-redirect=true";
    }

    public int getSessionsCount() {
        return gameSessionByAddress.values()
                .stream()
                .mapToInt(gameSessions -> gameSessions
                        .values()
                        .stream()
                        .mapToInt(cp -> cp.getSessions().size())
                        .sum())
                .sum();
    }

    public void flushAllAddresses() {
        log.info("Flush all sessions received from frontend");

        List<String> msgs = new ArrayList<>();

        for (String address : gameSessionByAddress.keySet()) {
            try {
                collectorService.flush(address, FLUSH_FROM_FRONTEND, false);
            } catch (Exception e) {
                log.warn(address + " Flush not registered, " + e.getMessage());

                msgs.add("Flush " + address + " not registered, " + e.getMessage());
                continue;
            }

            log.info(address + " Flush registered");

            msgs.add("Flush " + address + " registered");
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        fc.addMessage("msgs", new FacesMessage(String.join("<br/>", msgs), ""));

        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ignored) {}
    }

    public void shutdownInstance() {
        log.info("Shutdown received from frontend");

        int code = SpringApplication.exit(applicationContext, () -> 1);
        System.exit(code);
    }
}