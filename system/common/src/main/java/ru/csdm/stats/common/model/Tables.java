/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model;


import ru.csdm.stats.common.model.tables.History;
import ru.csdm.stats.common.model.tables.KnownServer;
import ru.csdm.stats.common.model.tables.Player;
import ru.csdm.stats.common.model.tables.PlayerIp;
import ru.csdm.stats.common.model.tables.PlayerSteamid;
import ru.csdm.stats.common.model.tables.Rank;


/**
 * Convenience access to all tables in csstats
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>csstats.history</code>.
     */
    public static final History HISTORY = ru.csdm.stats.common.model.tables.History.HISTORY;

    /**
     * The table <code>csstats.known_server</code>.
     */
    public static final KnownServer KNOWN_SERVER = ru.csdm.stats.common.model.tables.KnownServer.KNOWN_SERVER;

    /**
     * The table <code>csstats.player</code>.
     */
    public static final Player PLAYER = ru.csdm.stats.common.model.tables.Player.PLAYER;

    /**
     * The table <code>csstats.player_ip</code>.
     */
    public static final PlayerIp PLAYER_IP = ru.csdm.stats.common.model.tables.PlayerIp.PLAYER_IP;

    /**
     * The table <code>csstats.player_steamid</code>.
     */
    public static final PlayerSteamid PLAYER_STEAMID = ru.csdm.stats.common.model.tables.PlayerSteamid.PLAYER_STEAMID;

    /**
     * The table <code>csstats.rank</code>.
     */
    public static final Rank RANK = ru.csdm.stats.common.model.tables.Rank.RANK;
}