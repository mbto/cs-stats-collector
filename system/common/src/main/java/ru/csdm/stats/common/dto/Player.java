package ru.csdm.stats.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Player {
    @JsonIgnore
    private String name;
    private List<Session> sessions = new ArrayList<>();

    public Player(String name) {
        this.name = name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return StringUtils.equalsIgnoreCase(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}