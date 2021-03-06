/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model;


import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;
import ru.csdm.stats.common.model.tables.*;


/**
 * A class modelling indexes of tables of the <code>csstats</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index API_USER_ID_UNIQUE = Indexes0.API_USER_ID_UNIQUE;
    public static final Index API_USER_PRIMARY = Indexes0.API_USER_PRIMARY;
    public static final Index API_USER_USERNAME_UNIQUE = Indexes0.API_USER_USERNAME_UNIQUE;
    public static final Index HISTORY_HISTORY_NEW_RANK_ID_IDX = Indexes0.HISTORY_HISTORY_NEW_RANK_ID_IDX;
    public static final Index HISTORY_HISTORY_OLD_RANK_ID_IDX = Indexes0.HISTORY_HISTORY_OLD_RANK_ID_IDX;
    public static final Index HISTORY_HISTORY_PLAYER_ID_IDX = Indexes0.HISTORY_HISTORY_PLAYER_ID_IDX;
    public static final Index HISTORY_ID_UNIQUE = Indexes0.HISTORY_ID_UNIQUE;
    public static final Index HISTORY_PRIMARY = Indexes0.HISTORY_PRIMARY;
    public static final Index KNOWN_SERVER_ID_UNIQUE = Indexes0.KNOWN_SERVER_ID_UNIQUE;
    public static final Index KNOWN_SERVER_IPPORT_UNIQUE = Indexes0.KNOWN_SERVER_IPPORT_UNIQUE;
    public static final Index KNOWN_SERVER_PRIMARY = Indexes0.KNOWN_SERVER_PRIMARY;
    public static final Index PLAYER_ID_UNIQUE = Indexes0.PLAYER_ID_UNIQUE;
    public static final Index PLAYER_NAME_UNIQUE = Indexes0.PLAYER_NAME_UNIQUE;
    public static final Index PLAYER_PLAYER_LAST_SERVER_ID_IDX = Indexes0.PLAYER_PLAYER_LAST_SERVER_ID_IDX;
    public static final Index PLAYER_PLAYER_RANK_ID_IDX = Indexes0.PLAYER_PLAYER_RANK_ID_IDX;
    public static final Index PLAYER_PRIMARY = Indexes0.PLAYER_PRIMARY;
    public static final Index PLAYER_IP_ID_UNIQUE = Indexes0.PLAYER_IP_ID_UNIQUE;
    public static final Index PLAYER_IP_PLAYER_IP_PLAYER_ID_IDX = Indexes0.PLAYER_IP_PLAYER_IP_PLAYER_ID_IDX;
    public static final Index PLAYER_IP_PRIMARY = Indexes0.PLAYER_IP_PRIMARY;
    public static final Index PLAYER_STEAMID_ID_UNIQUE = Indexes0.PLAYER_STEAMID_ID_UNIQUE;
    public static final Index PLAYER_STEAMID_PLAYER_STEAMID_PLAYER_ID_IDX = Indexes0.PLAYER_STEAMID_PLAYER_STEAMID_PLAYER_ID_IDX;
    public static final Index PLAYER_STEAMID_PRIMARY = Indexes0.PLAYER_STEAMID_PRIMARY;
    public static final Index RANK_ID_UNIQUE = Indexes0.RANK_ID_UNIQUE;
    public static final Index RANK_LEVEL_UNIQUE = Indexes0.RANK_LEVEL_UNIQUE;
    public static final Index RANK_NAME_UNIQUE = Indexes0.RANK_NAME_UNIQUE;
    public static final Index RANK_PRIMARY = Indexes0.RANK_PRIMARY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index API_USER_ID_UNIQUE = Internal.createIndex("id_UNIQUE", ApiUser.API_USER, new OrderField[] { ApiUser.API_USER.ID }, true);
        public static Index API_USER_PRIMARY = Internal.createIndex("PRIMARY", ApiUser.API_USER, new OrderField[] { ApiUser.API_USER.ID }, true);
        public static Index API_USER_USERNAME_UNIQUE = Internal.createIndex("username_UNIQUE", ApiUser.API_USER, new OrderField[] { ApiUser.API_USER.USERNAME }, true);
        public static Index HISTORY_HISTORY_NEW_RANK_ID_IDX = Internal.createIndex("history_new_rank_id_idx", History.HISTORY, new OrderField[] { History.HISTORY.NEW_RANK_ID }, false);
        public static Index HISTORY_HISTORY_OLD_RANK_ID_IDX = Internal.createIndex("history_old_rank_id_idx", History.HISTORY, new OrderField[] { History.HISTORY.OLD_RANK_ID }, false);
        public static Index HISTORY_HISTORY_PLAYER_ID_IDX = Internal.createIndex("history_player_id_idx", History.HISTORY, new OrderField[] { History.HISTORY.PLAYER_ID }, false);
        public static Index HISTORY_ID_UNIQUE = Internal.createIndex("id_UNIQUE", History.HISTORY, new OrderField[] { History.HISTORY.ID }, true);
        public static Index HISTORY_PRIMARY = Internal.createIndex("PRIMARY", History.HISTORY, new OrderField[] { History.HISTORY.ID }, true);
        public static Index KNOWN_SERVER_ID_UNIQUE = Internal.createIndex("id_UNIQUE", KnownServer.KNOWN_SERVER, new OrderField[] { KnownServer.KNOWN_SERVER.ID }, true);
        public static Index KNOWN_SERVER_IPPORT_UNIQUE = Internal.createIndex("ipport_UNIQUE", KnownServer.KNOWN_SERVER, new OrderField[] { KnownServer.KNOWN_SERVER.IPPORT }, true);
        public static Index KNOWN_SERVER_PRIMARY = Internal.createIndex("PRIMARY", KnownServer.KNOWN_SERVER, new OrderField[] { KnownServer.KNOWN_SERVER.ID }, true);
        public static Index PLAYER_ID_UNIQUE = Internal.createIndex("id_UNIQUE", Player.PLAYER, new OrderField[] { Player.PLAYER.ID }, true);
        public static Index PLAYER_NAME_UNIQUE = Internal.createIndex("name_UNIQUE", Player.PLAYER, new OrderField[] { Player.PLAYER.NAME }, true);
        public static Index PLAYER_PLAYER_LAST_SERVER_ID_IDX = Internal.createIndex("player_last_server_id_idx", Player.PLAYER, new OrderField[] { Player.PLAYER.LAST_SERVER_ID }, false);
        public static Index PLAYER_PLAYER_RANK_ID_IDX = Internal.createIndex("player_rank_id_idx", Player.PLAYER, new OrderField[] { Player.PLAYER.RANK_ID }, false);
        public static Index PLAYER_PRIMARY = Internal.createIndex("PRIMARY", Player.PLAYER, new OrderField[] { Player.PLAYER.ID }, true);
        public static Index PLAYER_IP_ID_UNIQUE = Internal.createIndex("id_UNIQUE", PlayerIp.PLAYER_IP, new OrderField[] { PlayerIp.PLAYER_IP.ID }, true);
        public static Index PLAYER_IP_PLAYER_IP_PLAYER_ID_IDX = Internal.createIndex("player_ip_player_id_idx", PlayerIp.PLAYER_IP, new OrderField[] { PlayerIp.PLAYER_IP.PLAYER_ID }, false);
        public static Index PLAYER_IP_PRIMARY = Internal.createIndex("PRIMARY", PlayerIp.PLAYER_IP, new OrderField[] { PlayerIp.PLAYER_IP.ID }, true);
        public static Index PLAYER_STEAMID_ID_UNIQUE = Internal.createIndex("id_UNIQUE", PlayerSteamid.PLAYER_STEAMID, new OrderField[] { PlayerSteamid.PLAYER_STEAMID.ID }, true);
        public static Index PLAYER_STEAMID_PLAYER_STEAMID_PLAYER_ID_IDX = Internal.createIndex("player_steamid_player_id_idx", PlayerSteamid.PLAYER_STEAMID, new OrderField[] { PlayerSteamid.PLAYER_STEAMID.PLAYER_ID }, false);
        public static Index PLAYER_STEAMID_PRIMARY = Internal.createIndex("PRIMARY", PlayerSteamid.PLAYER_STEAMID, new OrderField[] { PlayerSteamid.PLAYER_STEAMID.ID }, true);
        public static Index RANK_ID_UNIQUE = Internal.createIndex("id_UNIQUE", Rank.RANK, new OrderField[] { Rank.RANK.ID }, true);
        public static Index RANK_LEVEL_UNIQUE = Internal.createIndex("level_UNIQUE", Rank.RANK, new OrderField[] { Rank.RANK.LEVEL }, true);
        public static Index RANK_NAME_UNIQUE = Internal.createIndex("name_UNIQUE", Rank.RANK, new OrderField[] { Rank.RANK.NAME }, true);
        public static Index RANK_PRIMARY = Internal.createIndex("PRIMARY", Rank.RANK, new OrderField[] { Rank.RANK.ID }, true);
    }
}
