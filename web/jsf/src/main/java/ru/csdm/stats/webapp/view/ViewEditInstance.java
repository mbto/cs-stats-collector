package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.webapp.session.SessionInstanceHolder;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;

@ViewScoped
@Named
@Slf4j
public class ViewEditInstance {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private SessionInstanceHolder sessionInstanceHolder;

    @Getter
    private Instance selectedInstance;

    @Getter
    private int knownServersAtInstance;
    @Getter
    private int knownServersAtAllInstances;

    private int changesCount;

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

        UInteger instanceId = UInteger.valueOf(instanceIdStr);

        selectedInstance = collectorDsl.selectFrom(INSTANCE)
                .where(INSTANCE.ID.eq(instanceId))
                .fetchOneInto(Instance.class);

        if(Objects.isNull(selectedInstance)) {
            fc.getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            fc.addMessage(null, new FacesMessage(SEVERITY_WARN, "Instance [" + instanceId + "] not founded", ""));
            return;
        }

        fetchKnownServersCounts();
    }

    private void fetchKnownServersCounts() {
        Record2<Integer, Integer> knownServersCounts = collectorDsl.select(
                DSL.selectCount()
                        .from(KNOWN_SERVER)
                        .join(INSTANCE).on(KNOWN_SERVER.INSTANCE_ID.eq(INSTANCE.ID))
                        .where(KNOWN_SERVER.INSTANCE_ID.eq(selectedInstance.getId()),
                                INSTANCE.ID.eq(sessionInstanceHolder.getCurrentInstanceId()))
                        .<Integer>asField("at_instance"),
                DSL.selectCount()
                        .from(KNOWN_SERVER)
                        .where(KNOWN_SERVER.INSTANCE_ID.eq(selectedInstance.getId()))
                        .<Integer>asField("at_all_instances")
        ).fetchOne();

        knownServersAtInstance = knownServersCounts.getValue("at_instance", Integer.class);
        knownServersAtAllInstances = knownServersCounts.getValue("at_all_instances", Integer.class);
    }

    public void save() {
        FacesContext fc = FacesContext.getCurrentInstance();
        changesCount = 0;

        try {
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                changesCount += transactionalDsl.update(INSTANCE)
                        .set(INSTANCE.DESCRIPTION, StringUtils.isBlank(selectedInstance.getDescription()) ? null : selectedInstance.getDescription())
                        .where(INSTANCE.ID.eq(selectedInstance.getId()))
                        .execute();
            });

            fc.addMessage("msgs", new FacesMessage("Instance [" + selectedInstance.getId() + "] saved, " + changesCount + " changes", ""));
        } catch (Exception e) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save instance [" + selectedInstance.getId() + "]",
                    e.toString()));
        }
    }

    public String delete() {
        try {
            fetchKnownServersCounts();

            if(knownServersAtAllInstances > 0) {
                throw new IllegalStateException("You must delete " + knownServersAtAllInstances
                        + " known server" + (knownServersAtAllInstances > 1 ? "s" : "") + " from all instances for delete this instance"
                        + " [" + selectedInstance.getId() + "] " + selectedInstance.getName());
            }

            collectorDsl.deleteFrom(INSTANCE)
                    .where(INSTANCE.ID.eq(selectedInstance.getId()))
                    .execute();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed delete instance [" + selectedInstance.getId() + "] " + selectedInstance.getName(),
                    e.toString()));

            return null;
        }

        return "/instances?faces-redirect=true";
    }
}