package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.common.model.collector.tables.records.InstanceRecord;
import ru.csdm.stats.service.InstanceHolder;
import ru.csdm.stats.webapp.application.ChangesCounter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.time.LocalDateTime;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;

@ViewScoped
@Named
@Slf4j
public class ViewNewInstance {
    @Autowired
    private DSLContext collectorDsl;
    @Autowired
    private InstanceHolder instanceHolder;
    @Autowired
    private ChangesCounter changesCounter;

    @Getter
    private Instance selectedInstance;

    private int localChangesCounter;

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        selectedInstance = new Instance();
        selectedInstance.setRegDatetime(LocalDateTime.now());
    }

    public String save() {
        FacesContext fc = FacesContext.getCurrentInstance();
        localChangesCounter = 0;

        try {
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                InstanceRecord instanceRecord = transactionalDsl.insertInto(INSTANCE)
                        .set(INSTANCE.NAME, selectedInstance.getName())
                        .set(INSTANCE.DESCRIPTION, StringUtils.isBlank(selectedInstance.getDescription()) ? null : selectedInstance.getDescription())
                        .returning(INSTANCE.asterisk())
                        .fetchOne();

                ++localChangesCounter;

                selectedInstance = instanceRecord.into(Instance.class);
            });

            changesCounter.increment(localChangesCounter);

            instanceHolder.getAvailableInstances(true);

            return "/editInstance?faces-redirect=true&instanceId=" + selectedInstance.getId();
        } catch (Throwable e) {
            selectedInstance.setId(null);
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Failed save new instance", e.toString()));

            return null;
        }
    }

    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if(instanceHolder.getAvailableInstances()
                .values()
                .stream()
                .anyMatch(instance -> value.equalsIgnoreCase(instance.getName())))
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Instance name '" + value + "' already existed", ""));
    }
}