package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.common.utils.SomeUtils;
import ru.csdm.stats.service.EventService;
import ru.csdm.stats.service.InstanceHolder;
import ru.csdm.stats.webapp.PojoStatus;
import ru.csdm.stats.webapp.Row;
import ru.csdm.stats.webapp.application.ChangesCounter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.BrokerEvent.FLUSH_FROM_FRONTEND;
import static ru.csdm.stats.common.Constants.IPADDRESS_PORT_PATTERN;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;
import static ru.csdm.stats.webapp.PojoStatus.*;

@ViewScoped
@Named
@Slf4j
public class ViewKnownServersByProjectId {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private InstanceHolder instanceHolder;
    @Autowired
    private ChangesCounter changesCounter;
    @Autowired
    private EventService eventService;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;

    @Getter
    private Project selectedProject;

    @Getter
    private final List<Row<KnownServer>> currentInstanceRows = new ArrayList<>();
    @Getter
    private final Map<UInteger, List<Row<KnownServer>>> otherInstanceRows = new LinkedHashMap<>();
    @Getter
    private String existedIpports;
    @Getter
    private final Map<String, Integer> projectSessionsCount = new HashMap<>();

    @Getter
    private boolean addServerBtnDisabled;

    private int localChangesCounter;

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String projectIdStr = request.getParameter("projectId");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!StringUtils.isNumeric(projectIdStr)) {
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Invalid projectId", ""));
            return;
        }

        selectedProject = collectorDsl.select(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT)
                .where(PROJECT.ID.eq(UInteger.valueOf(projectIdStr)))
                .fetchOneInto(Project.class);

        if(Objects.isNull(selectedProject)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Project [" + projectIdStr + "] not founded", ""));
            return;
        }

        fetchKnownServers();
        updateProjectSessionsCount();
    }

    private void fetchKnownServers() {
        List<Row<KnownServer>> knownServerRows = collectorDsl.selectFrom(KNOWN_SERVER)
                .where(KNOWN_SERVER.PROJECT_ID.eq(selectedProject.getId()))
                .orderBy(KNOWN_SERVER.INSTANCE_ID.desc(), KNOWN_SERVER.ID.asc())
                .fetchInto(KnownServer.class)
                .stream()
                .map(knownServer -> new Row<>(knownServer, EXISTED))
                .collect(Collectors.toList());

        currentInstanceRows.clear();
        otherInstanceRows.clear();

        for (Row<KnownServer> knownServerRow : knownServerRows) {
            KnownServer knownServer = knownServerRow.getPojo();
            if(knownServer.getInstanceId().equals(instanceHolder.getCurrentInstanceId())) {
                currentInstanceRows.add(knownServerRow);
            } else {
                List<Row<KnownServer>> rows = otherInstanceRows.get(knownServer.getInstanceId());
                if(Objects.isNull(rows)) {
                    rows = new ArrayList<>();
                    otherInstanceRows.put(knownServer.getInstanceId(), rows);
                }

                rows.add(knownServerRow);
            }
        }

        fetchExistedPorts();
    }

    private void updateProjectSessionsCount() {
        projectSessionsCount.clear();

        for (Row<KnownServer> currentInstanceRow : currentInstanceRows) {
            String port = currentInstanceRow.getPojo().getIpport();

            if(Objects.isNull(port))
                continue;

            Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(port);

            if(Objects.isNull(gameSessions))
                continue;

            int sum = gameSessions.values()
                    .stream()
                    .mapToInt(cp -> cp.getSessions().size())
                    .sum();

            projectSessionsCount.put(port, sum);
        }
    }

    private void fetchExistedPorts() {
        existedIpports = collectorDsl.select(
                DSL.groupConcat(KNOWN_SERVER.IPPORT)
                        .orderBy(KNOWN_SERVER.IPPORT.asc()).separator("<br/>")
        ).from(KNOWN_SERVER)
                .where(KNOWN_SERVER.PROJECT_ID.notEqual(selectedProject.getId()),
                        KNOWN_SERVER.INSTANCE_ID.eq(instanceHolder.getCurrentInstanceId())
                ).fetchOneInto(String.class);
    }

    public void flushProjectSessions() {
        log.info("Flush project sessions [" + selectedProject.getId() + "] "
                + selectedProject.getName() + "  received from frontend");

        List<String> infoMsgs = new ArrayList<>();
        List<String> warnMsgs = new ArrayList<>();

        for (String port : gameSessionByAddress.keySet()) {
            ServerData serverData = serverDataByAddress.get(port);

            if(Objects.nonNull(serverData) && !serverData.getProject().getId().equals(selectedProject.getId()))
                continue;
// чтобы всем этим гавном не крутить на фронте (проверки,условия итд) - их нужно убрать в Broker.consumeFlush
// тогда будет уверенность в отсутствии гонок, если например находимся здесь >< в момент refresh()
// т.к. у этих кнопок другие потоки. + если у проекта 20 серверов - ненадо отправлять 20 Message в брокер
            Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(port);

            if(Objects.isNull(gameSessions) || gameSessions.isEmpty()) {
                String logMsg = "Skip flush sessions, due empty gameSessions registry or not exists";
                log.info(port + " " + logMsg);
                serverData.addMessage(logMsg);
                continue;
            }

            try {
                eventService.flushSessions(port, FLUSH_FROM_FRONTEND, false);
            } catch (Throwable e) {
                log.info(port + " Flush not registered, " + e.getMessage()); // info, not warn

                warnMsgs.add("Flush " + port + " not registered, " + e.getMessage());
                continue;
            }

            log.info(port + " Flush registered");

            infoMsgs.add("Flush " + port + " registered");
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        if(!infoMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, String.join("<br/>", infoMsgs), ""));

        if(!warnMsgs.isEmpty())
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, String.join("<br/>", warnMsgs), ""));

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException ignored) {}

        updateProjectSessionsCount();
    }

    public void refreshProjectSettings() {
        log.info("Refresh project settings [" + selectedProject.getId() + "] "
                + selectedProject.getName() + " received from frontend");

        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            eventService.refreshSettings(selectedProject.getId());
        } catch (Throwable e) {
            String msg = "Refresh not registered, " + e.getMessage();
            log.info(msg); // info, not warn

            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, msg, ""));
            return;
        }

        fc.addMessage("msgs", new FacesMessage(SEVERITY_INFO, "Refresh registered", ""));

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException ignored) {}

        updateProjectSessionsCount();
    }

    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if(Objects.isNull(value) || !IPADDRESS_PORT_PATTERN.matcher(value).matches())
            throw makeValidatorException(value, "");

        int rowIndexVar = (int) component.getAttributes().get("rowIndexVar");

        if(log.isDebugEnabled())
            log.debug("\nrowIndexVar=" + rowIndexVar + ", currentInstanceRows.size=" + currentInstanceRows.size());

        for (int i = 0; i < currentInstanceRows.size(); i++) {
            if(i == rowIndexVar)
                continue;

            Row<KnownServer> currentInstanceRow = currentInstanceRows.get(i);
            KnownServer knownServer = currentInstanceRow.getPojo();

            if(!value.equals(knownServer.getIpport()))
                continue;

            throw makeValidatorException(value, "Already exists at known server "
                    + knownServer.getIpport()
                    + " (" + knownServer.getName() + ")");
        }

        Project projectWithSameIpport = collectorDsl.select(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT)
                .join(KNOWN_SERVER).on(PROJECT.ID.eq(KNOWN_SERVER.PROJECT_ID))
                .where(KNOWN_SERVER.IPPORT.eq(value),
                       KNOWN_SERVER.PROJECT_ID.notEqual(selectedProject.getId()),
                       KNOWN_SERVER.INSTANCE_ID.eq(instanceHolder.getCurrentInstanceId())
                ).groupBy(PROJECT.ID)
                .fetchOneInto(Project.class);

        if(Objects.nonNull(projectWithSameIpport)) {
            throw makeValidatorException(value, "This ip:port belongs to another project [" + projectWithSameIpport.getId() + "] "
                    + projectWithSameIpport.getName() + " at same instance");
        }
    }

    private ValidatorException makeValidatorException(String ipport, String details) {
        return new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Failed validation ip:port '" + ipport + "'",
                details));
    }

    public void save() {
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;

        try {
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                List<UInteger> toRemoveKnownServerIds = new ArrayList<>(currentInstanceRows.size());
                for (Iterator<Row<KnownServer>> iterator = currentInstanceRows.iterator(); iterator.hasNext(); ) {
                    Row<KnownServer> row = iterator.next();

                    KnownServer knownServer = row.getPojo();
                    if(row.getStatus() == TO_REMOVE) {
                        if(Objects.nonNull(knownServer.getId())) {
                            toRemoveKnownServerIds.add(knownServer.getId());
                        }

                        iterator.remove();
                    } else if(row.getStatus() == NEW) {
                        if(StringUtils.isBlank(knownServer.getIpport()) || StringUtils.isBlank(knownServer.getName())) {
                            iterator.remove();
                        }
                    }
                }

                if(!toRemoveKnownServerIds.isEmpty()) {
                    localChangesCounter += transactionalDsl.deleteFrom(KNOWN_SERVER)
                            .where(KNOWN_SERVER.ID.in(toRemoveKnownServerIds))
                            .execute();
                }

                for (Row<KnownServer> row : currentInstanceRows) {
                    KnownServer knownServer = row.getPojo();
                    PojoStatus pojoStatus = row.getStatus();

                    if(pojoStatus == CHANGED) {
                        localChangesCounter += SomeUtils.pointwiseUpdateQuery(KNOWN_SERVER.ID,
                                Arrays.asList(
                                    Pair.of(KNOWN_SERVER.IPPORT, knownServer.getIpport()),
                                    Pair.of(KNOWN_SERVER.NAME, knownServer.getName()),
                                    Pair.of(KNOWN_SERVER.ACTIVE, knownServer.getActive()),
                                    Pair.of(KNOWN_SERVER.FFA, knownServer.getFfa()),
                                    Pair.of(KNOWN_SERVER.IGNORE_BOTS, knownServer.getIgnoreBots()),
                                    Pair.of(KNOWN_SERVER.START_SESSION_ON_ACTION, knownServer.getStartSessionOnAction())),
                                knownServer.getId(),
                                transactionalDsl);
                    } else if(pojoStatus == NEW) {
                        localChangesCounter += transactionalDsl.insertInto(KNOWN_SERVER)
                                .set(KNOWN_SERVER.INSTANCE_ID, knownServer.getInstanceId())
                                .set(KNOWN_SERVER.PROJECT_ID, knownServer.getProjectId())
                                .set(KNOWN_SERVER.IPPORT, knownServer.getIpport())
                                .set(KNOWN_SERVER.NAME, knownServer.getName())
                                .set(KNOWN_SERVER.ACTIVE, knownServer.getActive())
                                .set(KNOWN_SERVER.FFA, knownServer.getFfa())
                                .set(KNOWN_SERVER.IGNORE_BOTS, knownServer.getIgnoreBots())
                                .set(KNOWN_SERVER.START_SESSION_ON_ACTION, knownServer.getStartSessionOnAction())
                                .execute();
                    }
                }
            });

            changesCounter.increment(localChangesCounter);

            fetchKnownServers();

            fc.addMessage("msgs", new FacesMessage("Project [" + selectedProject.getId() + "] "
                    + selectedProject.getName() + " saved", localChangesCounter + " changes"));
        } catch (Throwable e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save project [" + selectedProject.getId() + "] " + selectedProject.getName(),
                    e.toString()));
        } finally {
            addServerBtnDisabled = false;
        }
    }

    public void onRowEdit(RowEditEvent event) {
        Row<KnownServer> row = (Row<KnownServer>) event.getObject();

        if(Objects.nonNull(row.getPojo().getId())) {
            row.setStatus(CHANGED);
            row.setPreviousStatus(null);
        } else if(currentInstanceRows.get(currentInstanceRows.size() -1).equals(row)) {
            addServerBtnDisabled = false;
        }

        if(log.isDebugEnabled())
            log.debug("\nonRowEdit " + row);
    }

    public void onAddKnownServer() {
        if(log.isDebugEnabled())
            log.debug("\nonAddKnownServer");

        fetchExistedPorts();

        KnownServer knownServer = new KnownServer();
        knownServer.setInstanceId(instanceHolder.getCurrentInstanceId());
        knownServer.setProjectId(selectedProject.getId());
        currentInstanceRows.add(new Row<>(knownServer, NEW));
        addServerBtnDisabled = true;
    }

    public void onRestoreRow(Row<KnownServer> row) {
        row.setStatus(row.getPreviousStatus());
        row.setPreviousStatus(null);

        if(log.isDebugEnabled())
            log.debug("\nonRestoreRow " + row);
    }

    public void onRemoveRow(Row<KnownServer> row) {
        row.setPreviousStatus(row.getStatus());
        row.setStatus(TO_REMOVE);

        if(log.isDebugEnabled())
            log.debug("\nonRemoveRow " + row);
    }
}