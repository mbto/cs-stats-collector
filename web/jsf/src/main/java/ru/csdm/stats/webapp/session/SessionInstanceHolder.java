package ru.csdm.stats.webapp.session;

import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.csdm.stats.common.model.collector.tables.pojos.Instance;
import ru.csdm.stats.dao.InstanceDao;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Map;

@SessionScoped
@Named
public class SessionInstanceHolder implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private InstanceDao instanceDao;

    @Value("${collector.instance.name}")
    private String collectorInstanceName;

    @Getter
    private Map<UInteger, Instance> availableInstances;
    @Getter
    @Setter
    private UInteger currentInstanceId;

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        availableInstances = instanceDao.findAll();
        availableInstances.values()
                .stream().filter(instance -> instance.getName().equals(collectorInstanceName))
                .findFirst().ifPresent(instance -> currentInstanceId = instance.getId());
    }

//    public void instanceChanged(AjaxBehaviorEvent event) {
//
//    }
}