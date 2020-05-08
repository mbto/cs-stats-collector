package ru.csdm.stats.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
}
