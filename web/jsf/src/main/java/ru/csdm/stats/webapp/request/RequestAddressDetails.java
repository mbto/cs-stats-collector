package ru.csdm.stats.webapp.request;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.common.dto.CollectedPlayer;
import ru.csdm.stats.common.dto.Session;
import ru.csdm.stats.webapp.AggregatedPlayer;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static ru.csdm.stats.common.Constants.IPADDRESS_PORT_PATTERN;

@RequestScoped
@Named
@Slf4j
public class RequestAddressDetails {
    @Autowired
    private Map<String, Map<String, CollectedPlayer>> gameSessionByAddress;
    @Getter
    @Setter
    private String selectedAddress;
    @Getter
    private List<AggregatedPlayer> aggregatedPlayers = Collections.emptyList();;

    public void fetch() {
        if(log.isDebugEnabled())
            log.debug("\nfetch");

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        selectedAddress = request.getParameter("address");

        FacesContext fc = FacesContext.getCurrentInstance();

        if(Objects.isNull(selectedAddress) || !IPADDRESS_PORT_PATTERN.matcher(selectedAddress).matches()) {
            selectedAddress = null;
            fc.addMessage("fetchMsgs", new FacesMessage(SEVERITY_WARN, "Invalid address", ""));
            return;
        }

        Map<String, CollectedPlayer> gameSessions = gameSessionByAddress.get(selectedAddress);
        if(Objects.nonNull(gameSessions)) {
            aggregatedPlayers = gameSessions
                    .values()
                    .stream()
                    .map(collectedPlayer -> {
                        AggregatedPlayer aggPlayer = new AggregatedPlayer();
                        for (Session session : collectedPlayer.getSessions()) {
                            aggPlayer.setName(collectedPlayer.getName());
                            aggPlayer.setKills(session.getKills());
                            aggPlayer.setDeaths(session.getDeaths());
                            aggPlayer.setStarted(session.getStarted());
                            aggPlayer.setFinished(session.getFinished());
                        }

                        if (!collectedPlayer.getIpAddresses().isEmpty()) {
                            aggPlayer.setIpAddresses(String.join("<br/>", collectedPlayer.getIpAddresses()));
                        }

                        if (!collectedPlayer.getSteamIds().isEmpty()) {
                            aggPlayer.setSteamIds(String.join("<br/>", collectedPlayer.getSteamIds()));
                        }

                        return aggPlayer;
                    }).collect(Collectors.toList());
        }
    }
}