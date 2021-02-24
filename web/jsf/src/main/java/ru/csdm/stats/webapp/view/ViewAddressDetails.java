package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record2;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.dao.CollectorDao;
import ru.csdm.stats.service.EventService;
import ru.csdm.stats.service.InstanceHolder;
import ru.csdm.stats.webapp.AggregatedPlayer;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.Constants.IPADDRESS_PORT_PATTERN;
import static ru.csdm.stats.common.BrokerEvent.FLUSH_FROM_FRONTEND;

@ViewScoped
@Named
@Slf4j
public class ViewAddressDetails {
    @Autowired
    private InstanceHolder instanceHolder;
    @Autowired
    private CollectorDao collectorDao;
    @Autowired
    private EventService eventService;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;
    @Getter
    @Setter
    private String selectedAddress;
    @Getter
    @Setter
    private ServerData selectedServerData;
    @Getter
    private int knownServersAtInstance;
    @Getter
    private int knownServersAtAllInstances;
    @Getter
    private List<AggregatedPlayer> aggregatedPlayers = Collections.emptyList();
    @Getter
    private final Map<String, String> aggregatedIps = new HashMap<>();
    @Getter
    private final Map<String, String> aggregatedSteamIds = new HashMap<>();

    public void fetch() {
        if (log.isDebugEnabled())
            log.debug("\nfetch");

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        selectedAddress = request.getParameter("address");

        FacesContext fc = FacesContext.getCurrentInstance();

        if (Objects.isNull(selectedAddress) || !IPADDRESS_PORT_PATTERN.matcher(selectedAddress).matches()) {
            selectedAddress = null;
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Invalid address", ""));
            return;
        }

        selectedServerData = serverDataByAddress.get(selectedAddress);

        fetchAggregatedPlayers();
        fetchKnownServersCounts();
    }

    public void fetchAggregatedPlayers() {
        Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(selectedAddress);
        if(Objects.isNull(gameSessions)) {
            aggregatedPlayers = Collections.emptyList();
            return;
        }

        aggregatedPlayers = gameSessions
                .values()
                .stream()
                .map(collectedPlayer -> {
                    if (!collectedPlayer.getIpAddresses().isEmpty()) {
                        aggregatedIps.put(collectedPlayer.getName(), String.join("<br/>", collectedPlayer.getIpAddresses()));
                    }

                    if (!collectedPlayer.getSteamIds().isEmpty()) {
                        aggregatedSteamIds.put(collectedPlayer.getName(), String.join("<br/>", collectedPlayer.getSteamIds()));
                    }

                    return collectedPlayer.getSessions()
                            .stream()
                            .map(session -> {
                                AggregatedPlayer aggPlayer = new AggregatedPlayer();
                                aggPlayer.setName(collectedPlayer.getName());
                                aggPlayer.setKills(session.getKills());
                                aggPlayer.setDeaths(session.getDeaths());
                                aggPlayer.setStarted(session.getStarted());
                                aggPlayer.setFinished(session.getFinished());
                                return aggPlayer;
                            }).collect(Collectors.toList());
                })
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(AggregatedPlayer::getName))
                .collect(Collectors.toList());
    }

    private void fetchKnownServersCounts() {
        if(Objects.isNull(selectedServerData)) {
            return;
        }

        Record2<Integer, Integer> knownServersCounts = collectorDao
                .fetchKnownServersCounts(selectedServerData.getProject().getId(),
                        instanceHolder.getCurrentInstanceId());

        knownServersAtInstance = knownServersCounts.getValue("at_instance", Integer.class);
        knownServersAtAllInstances = knownServersCounts.getValue("at_all_instances", Integer.class);
    }

    public void flushOneAddress() {
        log.info(selectedAddress + " Flush received from frontend");

        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            eventService.flushSessions(selectedAddress, FLUSH_FROM_FRONTEND, false);
        } catch (Throwable e) {
            log.info(selectedAddress + " Flush not registered, " + e.getMessage()); // info, not warn

            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Flush " + selectedAddress + " not registered", e.getMessage()));

            return;
        }

        log.info(selectedAddress + " Flush registered");

        fc.addMessage("msgs", new FacesMessage(
                "Flush " + selectedAddress + " registered", ""));

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException ignored) {}

        fetchAggregatedPlayers();
    }
}