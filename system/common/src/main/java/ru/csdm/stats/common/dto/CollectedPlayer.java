package ru.csdm.stats.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.utils.SomeUtils;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class CollectedPlayer {
    @Setter
    @JsonIgnore
    private String name;
    private final List<Session> sessions = new ArrayList<>();
    private final Set<String> ipAddresses = new HashSet<>();
    private final Set<String> steamIds = new HashSet<>();
    @Setter
    private LocalDateTime lastseenDatetime;
    @Setter
    private UInteger lastServerId;

    public CollectedPlayer(String name) {
        this.name = name;
    }

    public void addIpAddress(String ipAddress) {
        ipAddress = SomeUtils.extractIp(ipAddress);
        if(Objects.nonNull(ipAddress)) {
            ipAddresses.add(ipAddress);
        }
    }

    public void addSteamId(String steamId) {
        steamId = SomeUtils.extractSteamId(steamId);
        if(Objects.nonNull(steamId)) {
            steamIds.add(steamId);
        }
    }

    @JsonIgnore
    public Session getLastSession() {
        int size = sessions.size();
        if(size == 0) {
            return null;
        }

        return sessions.get(size -1);
    }

    public Session getCurrentSession(LocalDateTime startedDateTime) {
        Session session = getLastSession();

        if(Objects.isNull(session) || Objects.nonNull(session.getFinished())) {
            session = new Session(startedDateTime);
            sessions.add(session);
        }

        return session;
    }

    public void upKills(LocalDateTime dateTime) {
        getCurrentSession(dateTime).upKills();
    }

    public void upDeaths(LocalDateTime dateTime) {
        getCurrentSession(dateTime).upDeaths();
    }

    public void onDisconnected(LocalDateTime finishedDateTime) {
        Session lastSession = getLastSession();

        if(Objects.nonNull(lastSession))
            lastSession.close(finishedDateTime);
    }

    public void prepareToFlushSessions(LocalDateTime finishedDateTime) {
        for (Session session : sessions) {
            session.close(finishedDateTime);
        }
    }

    @Override
    public String toString() {
        return name;
    }

/* If necessary, which is unlikely, use the compare method.
    from org.springframework.util.LinkedCaseInsensitiveMap#convertKey
    not StringUtils.equalsIgnoreCase */
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof CollectedPlayer)) return false;
//        CollectedPlayer collectedPlayer = (CollectedPlayer) o;
//        return StringUtils.equalsIgnoreCase(name, collectedPlayer.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name);
//    }
}