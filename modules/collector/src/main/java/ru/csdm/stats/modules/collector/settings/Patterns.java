package ru.csdm.stats.modules.collector.settings;

import java.util.regex.Pattern;

/**
 * HL Log Standard Examples
 * https://developer.valvesoftware.com/wiki/HL_Log_Standard_Examples#Example_1:_Perl:_Log_parsing_routines.
 */
public enum Patterns {
    // L 01/01/2020 - 13:15:00: "Name1<STEAM_ID_LAN><5><>" connected, address "12.12.12.12:27005"
    LOG("L (?<date>\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}:\\d{2}): (?<msg>.*)"),

    //    # matches events 057,058,059,066
    //057. Kills
    //"Name<uid><wonid><team>" killed "Name<uid><wonid><team>" with "weapon"
    //058. Injuring
    //This event allows for recording of partial kills and teammate friendly-fire injuries. The suggested damage property3 could be used to show how much health the victim lost. If the injury results in a kill, a Kill message (057) should be logged instead/also.
    //"Name<uid><wonid><team>" attacked "Name<uid><wonid><team>" with "weapon" (damage "damage")
    //059. Player-Player Actions
    //This event allows for logging of a wide range of events where one player performs an action on another player. For example, in TFC this event may cover medic healings and infections, sentry gun destruction, spy uncovering, etc. More detail about the action can be given by appending more properties to the end of the event3.
    //"Name<uid><wonid><team>" triggered "action" against "Name<uid><wonid><team>"
    //066. Private Chat
    //"Name<uid><wonid><team>" tell "Name<uid><wonid><team>" message "message"
    //    $team = "";
    //    $player = $1; # parse out name, uid and team later
    //    $event1 = $2; # event type - "killed", "attacked", etc.
    //    $noun1 = $3; # victim name/objective code, etc.
    //    $event2 = $4; # "with", etc.
    //    $noun2 = $5; # weapon/victim name, etc.
    //    $properties = $6; # parse out keys and values later
    TWO("\"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\"(.*)"),

    //    # matches events 050,053,054,055,056,060,063a,063b,068,069
    //050. Connection
    //"Name<uid><wonid><>" connected, address "ip:port"
    //053. Suicides
    //"Name<uid><wonid><team>" committed suicide with "weapon"
    //054. Team Selection
    //"Name<uid><wonid><team>" joined team "team"
    //055. Role Selection
    //This event covers classes in games like TFC, FLF and DOD.
    //"Name<uid><wonid><team>" changed role to "role"
    //056. Change Name
    //"Name<uid><wonid><team>" changed name to "Name"
    //060. Player Objectives/Actions
    //"Name<uid><wonid><team>" triggered "action"
    //063. Chat
    //"Name<uid><wonid><team>" say "message"
    //"Name<uid><wonid><team>" say_team "message"
    //068. Weapon Selection
    //Use this event to show what weapon a player currently has selected.
    //"Name<uid><wonid><team>" selected weapon "weapon"
    //069. Weapon Pickup
    //"Name<uid><wonid><team>" acquired weapon "weapon"
    //    $team = "";
    //    $player = $1;
    //    $event1 = $2;
    //    $noun1 = $3; # weapon/team code/objective code, etc.
    //    $event2 = "";
    //    $noun2 = "";
    //    $properties = $4;
    THREE("\"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\"(.*)"),

    //    # matches events 050b,051,052
    //050b. Validation
    //"Name<uid><wonid><>" STEAM USERID validated
    //051. Enter Game
    //"Name<uid><wonid><>" entered the game
    //052. Disconnection
    //"Name<uid><wonid><team>" disconnected
    //    $team = "";
    //    $player = $1;
    //    $event1 = $2;
    //    $noun1 = "";
    //    $event2 = "";
    //    $noun2 = "";
    //    $properties = $3;
    FOUR("\"([^\"]+)\" ([^\\(]+)(.*)"),

    //    # matches events 061,064
    //061. Team Objectives/Actions
    //Team "team" triggered "action"
    //064. Team Alliances
    //Team "team" formed alliance with team "team"
    //    $team = $1; # Team code
    //    $player = 0;
    //    $event1 = $2;
    //    $noun1 = $3;
    //    $event2 = "";
    //    $noun2 = "";
    //    $properties = $4;
    FIVE("Team \"([^\"]+)\" ([^\"\\(]+) \"([^\"]+)\"(.*)"),

    //    # matches events 062,003a,003b,005,006
    //062. World Objectives/Actions
    //This event allows logging of anything which does not happen in response to the actions of a player or team. For example a gate opening at the start of a round.
    //World triggered "action"
    //003. Change Map
    //This event replaces the current "Spawning server" message.
    //Loading map "map"
    //This event replaces the current "Map CRC" message. The message should appear AFTER all PackFile messages, to indicate when the game actually commences.
    //Started map "map" (CRC "crc")
    //005. Server Name
    //Server name is "hostname"
    //006. Server Say
    //Server say "message"
    //    $team = "";
    //    $player = 0;
    //    $event1 = $1;
    //    $noun1 = $2;
    //    $event2 = "";
    //    $noun2 = "";
    //    $properties = $3;
    SIX("([^\"\\(]+) \"([^\"]+)\"(.*)"),

    //"time-the-kill.freeeemaN.<15064><STEAM_ID_LAN><TERRORIST>"
    //"time-the-kill.freeeemaN.<-1><STEAM_ID_LAN><TERRORIST>"
    PLAYER("(?<name>.+)<(?<id>.*)><(?<auth>.*)><(?<team>.*)>");

    public final Pattern pattern;

    Patterns(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }
}
