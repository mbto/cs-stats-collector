package ru.csdm.stats.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerSetting {
    @JsonIgnore
    private String ipport;
    /**
     * true - считать статистику, когда команда у killer и victim разная или одинаковая
     * false - считать статистику, когда команда у killer и victim разная
     */
    private Boolean ffa;
    /**
     * true - не считать статистику если killer или victim боты
     * false - считать статистику если killer или victim боты
     */
    private Boolean ignore_bots;
    /**
     * true - start player's session on event "... killed ... with ..." (not for kreedz servers)
     * false - start player's session on event "... entered the game"
     */
    private Boolean start_session_on_action;

    public void applyNewValues(ServerSetting sourceServerSetting) {
        this.ffa = sourceServerSetting.ffa;
        this.ignore_bots = sourceServerSetting.ignore_bots;
        this.start_session_on_action = sourceServerSetting.start_session_on_action;
    }

    @Override
    public String toString() {
        return ipport + ": ffa=" + ffa + ", ignore_bots=" + ignore_bots + ", start_session_on_action=" + start_session_on_action;
    }
}