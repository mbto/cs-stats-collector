package ru.csdm.stats.webapp.view;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.webapp.PojoStatus;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.webapp.PojoStatus.*;

@ViewScoped
@Named
public class ViewKnownServers {
    @Autowired
    private DSLContext collectorDsl;
    @Value("${collector.instance.name}")
    private String collectorInstanceName;

    @Getter
    private List<MutableTriple<KnownServer, UUID, PojoStatus>> knownServerWithStatus = new ArrayList<>();

    @Getter
    private SelectItem[] availableInstances;

    @Getter
    private SelectItem[] availableProjects;

    @Getter
    private Integer tablesCount;

    public void fetch() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String projectId = request.getParameter("projectId");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (!StringUtils.isNumeric(projectId)) {
            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN, "Invalid projectId", ""));
            return;
        }

        collectorDsl.select(KNOWN_SERVER.fields())
                .from(KNOWN_SERVER)
                .join(INSTANCE).on(KNOWN_SERVER.INSTANCE_ID.eq(INSTANCE.ID))
                .where(INSTANCE.NAME.eq(collectorInstanceName),
                        KNOWN_SERVER.PROJECT_ID.eq(UInteger.valueOf(projectId)))
                .fetchInto(KnownServer.class)
                .forEach(knownServer -> {
                    knownServerWithStatus.add(MutableTriple.of(knownServer, UUID.randomUUID(), EXISTED));
                });

        availableInstances = collectorDsl.selectFrom(INSTANCE)
                .orderBy(INSTANCE.ID.asc())
                .fetchInto(Instance.class)
                .stream()
                .map(instance -> new SelectItem(instance.getId(), instance.getName()))
                .toArray(SelectItem[]::new);
    }

    public void save() {

    }

    public void onAddProperty() {
        knownServerWithStatus.add(MutableTriple.of(new KnownServer(), UUID.randomUUID(), NEW));
    }

    public void onRestoreProperty(UUID driverPropertyGid) {
        knownServerWithStatus
                .stream()
                .filter(triple -> triple.getMiddle().equals(driverPropertyGid))
                .forEach(triple -> {
                    triple.setRight(Objects.isNull(triple.getLeft().getId()) ? NEW : EXISTED);
                });
    }

    public void onRemoveProperty(UUID driverPropertyGid) {
        knownServerWithStatus
                .stream()
                .filter(triple -> triple.getMiddle().equals(driverPropertyGid))
                .forEach(triple -> triple.setRight(TO_REMOVE));
    }
}