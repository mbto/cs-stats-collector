package ru.csdm.stats.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
public class Session {
    private long kills;
    private long deaths;
    private LocalDateTime started;
    private LocalDateTime finished;

    public Session(LocalDateTime startedDateTime) {
        this.started = startedDateTime;
    }

    public void upKills() {
        ++kills;
    }

    public void upDeaths() {
        ++deaths;
    }

    public void close(LocalDateTime finishedDateTime) {
        if(Objects.isNull(finished))
            finished = finishedDateTime;
    }

/* I guess it won't be necessary at all */
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Session)) return false;
//        Session session = (Session) o;
//        return started.equals(session.started);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(started);
//    }
}
