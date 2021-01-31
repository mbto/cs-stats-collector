package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.webapp.Row;
import ru.csdm.stats.webapp.session.SessionInstanceHolder;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;
import static ru.csdm.stats.webapp.PojoStatus.*;

@ViewScoped
@Named
@Slf4j
public class ViewKnownServers {
    @Autowired
    private DSLContext collectorDsl;

    @Autowired
    private SessionInstanceHolder sessionInstanceHolder;

    @Getter
    private Project selectedProject;

    @Getter
    private final List<Row<KnownServer>> currentInstanceRows = new ArrayList<>();
    @Getter
    private final Map<UInteger, List<Row<KnownServer>>> otherInstanceRows = new LinkedHashMap<>();

    @Getter
    private boolean addServerBtnDisabled;

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String projectIdStr = request.getParameter("projectId");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!StringUtils.isNumeric(projectIdStr)) {
            fc.addMessage(null, new FacesMessage(SEVERITY_WARN, "Invalid projectId", ""));
            return;
        }

        UInteger projectId = UInteger.valueOf(projectIdStr);

        selectedProject = collectorDsl.select(PROJECT.ID, PROJECT.NAME)
                .from(PROJECT)
                .where(PROJECT.ID.eq(projectId))
                .fetchOneInto(Project.class);

        if(Objects.isNull(selectedProject)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage(null, new FacesMessage(SEVERITY_WARN, "Project [" + projectId + "] not founded", ""));
            return;
        }

        List<Row<KnownServer>> knownServers = collectorDsl.selectFrom(KNOWN_SERVER)
                .where(KNOWN_SERVER.PROJECT_ID.eq(projectId))
                .orderBy(KNOWN_SERVER.ID.asc())
                .fetchInto(KnownServer.class)
                .stream()
                .map(knownServer -> new Row<>(knownServer, EXISTED))
                .collect(Collectors.toList());

        for (Row<KnownServer> knownServerRow : knownServers) {
            KnownServer knownServer = knownServerRow.getPojo();
            if(knownServer.getInstanceId().equals(sessionInstanceHolder.getCurrentInstanceId())) {
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
    }

    public void save() {

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

        KnownServer knownServer = new KnownServer();
        knownServer.setInstanceId(sessionInstanceHolder.getCurrentInstanceId());
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