package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.collector.tables.pojos.DriverProperty;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;

import java.util.List;
import java.util.Map;

/**
 * Using in ru.csdm.stats.dao.CollectorDao to bypass the lambda in the transaction
 */
@Getter
@Setter
public class CollectorData {
    private List<KnownServer> knownServers;
    private Map<UInteger, Project> projectByProjectId;
    private Map<UInteger, List<DriverProperty>> driverPropertiesByProjectId;
}