package ru.csdm.stats.webapp.view;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.service.CollectorService;
import ru.csdm.stats.webapp.AggregatedPlayer;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.Constants.IPADDRESS_PORT_PATTERN;
import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_FRONTEND;

@ViewScoped
@Named
@Slf4j
public class ViewAddressDetails {
    @Autowired
    private CollectorService collectorService;
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;
    @Getter
    @Setter
    private String selectedAddress;
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

        fetchAggregatedPlayers();
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

    public void flush() {
        log.info(selectedAddress + " Flush received from frontend");

        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            collectorService.flush(selectedAddress, FLUSH_FROM_FRONTEND, false);
        } catch (Exception e) {
            log.warn(selectedAddress + " Flush not registered, " + e.getMessage());

            fc.addMessage("msgs", new FacesMessage(SEVERITY_WARN,
                    "Flush " + selectedAddress + " not registered", e.getMessage()));

            return;
        }

        log.info(selectedAddress + " Flush registered");

        fc.addMessage("msgs", new FacesMessage(
                "Flush " + selectedAddress + " registered", ""));

        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ignored) {}

        fetchAggregatedPlayers();
    }
}