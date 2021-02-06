package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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

    @Getter
    private Instance selectedInstance;

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        selectedInstance = new Instance();
        selectedInstance.setRegDatetime(LocalDateTime.now());
    }

    public String save() {
        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            collectorDsl.transaction(config -> {
                DSLContext transactionalDsl = DSL.using(config);

                UInteger instanceId = transactionalDsl.insertInto(INSTANCE)
                        .set(INSTANCE.NAME, selectedInstance.getName())
                        .set(INSTANCE.DESCRIPTION, StringUtils.isBlank(selectedInstance.getDescription()) ? null : selectedInstance.getDescription())
                        .returning(INSTANCE.ID)
                        .fetchOne().getId();

                selectedInstance.setId(instanceId);
            });

            return "/editInstance?faces-redirect=true&instanceId=" + selectedInstance.getId();
        } catch (Exception e) {
            selectedInstance.setId(null);
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, "Failed save new instance", e.toString()));

            return null;
        }
    }
}