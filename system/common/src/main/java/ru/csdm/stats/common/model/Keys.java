/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model;


import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;
import org.jooq.types.UInteger;

import ru.csdm.stats.common.model.tables.History;
import ru.csdm.stats.common.model.tables.KnownServer;
import ru.csdm.stats.common.model.tables.Manager;
import ru.csdm.stats.common.model.tables.Player;
import ru.csdm.stats.common.model.tables.PlayerIp;
import ru.csdm.stats.common.model.tables.PlayerSteamid;
import ru.csdm.stats.common.model.tables.Rank;
import ru.csdm.stats.common.model.tables.records.HistoryRecord;
import ru.csdm.stats.common.model.tables.records.KnownServerRecord;
import ru.csdm.stats.common.model.tables.records.ManagerRecord;
import ru.csdm.stats.common.model.tables.records.PlayerIpRecord;
import ru.csdm.stats.common.model.tables.records.PlayerRecord;
import ru.csdm.stats.common.model.tables.records.PlayerSteamidRecord;
import ru.csdm.stats.common.model.tables.records.RankRecord;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>csstats</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<HistoryRecord, UInteger> IDENTITY_HISTORY = Identities0.IDENTITY_HISTORY;
    public static final Identity<KnownServerRecord, UInteger> IDENTITY_KNOWN_SERVER = Identities0.IDENTITY_KNOWN_SERVER;
    public static final Identity<ManagerRecord, UInteger> IDENTITY_MANAGER = Identities0.IDENTITY_MANAGER;
    public static final Identity<PlayerRecord, UInteger> IDENTITY_PLAYER = Identities0.IDENTITY_PLAYER;
    public static final Identity<PlayerIpRecord, UInteger> IDENTITY_PLAYER_IP = Identities0.IDENTITY_PLAYER_IP;
    public static final Identity<PlayerSteamidRecord, UInteger> IDENTITY_PLAYER_STEAMID = Identities0.IDENTITY_PLAYER_STEAMID;
    public static final Identity<RankRecord, UInteger> IDENTITY_RANK = Identities0.IDENTITY_RANK;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<HistoryRecord> KEY_HISTORY_PRIMARY = UniqueKeys0.KEY_HISTORY_PRIMARY;
    public static final UniqueKey<HistoryRecord> KEY_HISTORY_ID_UNIQUE = UniqueKeys0.KEY_HISTORY_ID_UNIQUE;
    public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_PRIMARY = UniqueKeys0.KEY_KNOWN_SERVER_PRIMARY;
    public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_ID_UNIQUE = UniqueKeys0.KEY_KNOWN_SERVER_ID_UNIQUE;
    public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_IPPORT_UNIQUE = UniqueKeys0.KEY_KNOWN_SERVER_IPPORT_UNIQUE;
    public static final UniqueKey<ManagerRecord> KEY_MANAGER_PRIMARY = UniqueKeys0.KEY_MANAGER_PRIMARY;
    public static final UniqueKey<ManagerRecord> KEY_MANAGER_ID_UNIQUE = UniqueKeys0.KEY_MANAGER_ID_UNIQUE;
    public static final UniqueKey<ManagerRecord> KEY_MANAGER_USERNAME_UNIQUE = UniqueKeys0.KEY_MANAGER_USERNAME_UNIQUE;
    public static final UniqueKey<PlayerRecord> KEY_PLAYER_PRIMARY = UniqueKeys0.KEY_PLAYER_PRIMARY;
    public static final UniqueKey<PlayerRecord> KEY_PLAYER_ID_UNIQUE = UniqueKeys0.KEY_PLAYER_ID_UNIQUE;
    public static final UniqueKey<PlayerRecord> KEY_PLAYER_NAME_UNIQUE = UniqueKeys0.KEY_PLAYER_NAME_UNIQUE;
    public static final UniqueKey<PlayerIpRecord> KEY_PLAYER_IP_PRIMARY = UniqueKeys0.KEY_PLAYER_IP_PRIMARY;
    public static final UniqueKey<PlayerIpRecord> KEY_PLAYER_IP_ID_UNIQUE = UniqueKeys0.KEY_PLAYER_IP_ID_UNIQUE;
    public static final UniqueKey<PlayerSteamidRecord> KEY_PLAYER_STEAMID_PRIMARY = UniqueKeys0.KEY_PLAYER_STEAMID_PRIMARY;
    public static final UniqueKey<PlayerSteamidRecord> KEY_PLAYER_STEAMID_ID_UNIQUE = UniqueKeys0.KEY_PLAYER_STEAMID_ID_UNIQUE;
    public static final UniqueKey<RankRecord> KEY_RANK_PRIMARY = UniqueKeys0.KEY_RANK_PRIMARY;
    public static final UniqueKey<RankRecord> KEY_RANK_ID_UNIQUE = UniqueKeys0.KEY_RANK_ID_UNIQUE;
    public static final UniqueKey<RankRecord> KEY_RANK_LEVEL_UNIQUE = UniqueKeys0.KEY_RANK_LEVEL_UNIQUE;
    public static final UniqueKey<RankRecord> KEY_RANK_NAME_UNIQUE = UniqueKeys0.KEY_RANK_NAME_UNIQUE;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<HistoryRecord, PlayerRecord> HISTORY_PLAYER_ID_FK = ForeignKeys0.HISTORY_PLAYER_ID_FK;
    public static final ForeignKey<HistoryRecord, RankRecord> HISTORY_OLD_RANK_ID_FK = ForeignKeys0.HISTORY_OLD_RANK_ID_FK;
    public static final ForeignKey<HistoryRecord, RankRecord> HISTORY_NEW_RANK_ID_FK = ForeignKeys0.HISTORY_NEW_RANK_ID_FK;
    public static final ForeignKey<PlayerRecord, RankRecord> PLAYER_RANK_ID_FK = ForeignKeys0.PLAYER_RANK_ID_FK;
    public static final ForeignKey<PlayerRecord, KnownServerRecord> PLAYER_LAST_SERVER_ID_FK = ForeignKeys0.PLAYER_LAST_SERVER_ID_FK;
    public static final ForeignKey<PlayerIpRecord, PlayerRecord> PLAYER_IP_PLAYER_ID_FK = ForeignKeys0.PLAYER_IP_PLAYER_ID_FK;
    public static final ForeignKey<PlayerSteamidRecord, PlayerRecord> PLAYER_STEAMID_PLAYER_ID_FK = ForeignKeys0.PLAYER_STEAMID_PLAYER_ID_FK;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<HistoryRecord, UInteger> IDENTITY_HISTORY = Internal.createIdentity(History.HISTORY, History.HISTORY.ID);
        public static Identity<KnownServerRecord, UInteger> IDENTITY_KNOWN_SERVER = Internal.createIdentity(KnownServer.KNOWN_SERVER, KnownServer.KNOWN_SERVER.ID);
        public static Identity<ManagerRecord, UInteger> IDENTITY_MANAGER = Internal.createIdentity(Manager.MANAGER, Manager.MANAGER.ID);
        public static Identity<PlayerRecord, UInteger> IDENTITY_PLAYER = Internal.createIdentity(Player.PLAYER, Player.PLAYER.ID);
        public static Identity<PlayerIpRecord, UInteger> IDENTITY_PLAYER_IP = Internal.createIdentity(PlayerIp.PLAYER_IP, PlayerIp.PLAYER_IP.ID);
        public static Identity<PlayerSteamidRecord, UInteger> IDENTITY_PLAYER_STEAMID = Internal.createIdentity(PlayerSteamid.PLAYER_STEAMID, PlayerSteamid.PLAYER_STEAMID.ID);
        public static Identity<RankRecord, UInteger> IDENTITY_RANK = Internal.createIdentity(Rank.RANK, Rank.RANK.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<HistoryRecord> KEY_HISTORY_PRIMARY = Internal.createUniqueKey(History.HISTORY, "KEY_history_PRIMARY", History.HISTORY.ID);
        public static final UniqueKey<HistoryRecord> KEY_HISTORY_ID_UNIQUE = Internal.createUniqueKey(History.HISTORY, "KEY_history_id_UNIQUE", History.HISTORY.ID);
        public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_PRIMARY = Internal.createUniqueKey(KnownServer.KNOWN_SERVER, "KEY_known_server_PRIMARY", KnownServer.KNOWN_SERVER.ID);
        public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_ID_UNIQUE = Internal.createUniqueKey(KnownServer.KNOWN_SERVER, "KEY_known_server_id_UNIQUE", KnownServer.KNOWN_SERVER.ID);
        public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_IPPORT_UNIQUE = Internal.createUniqueKey(KnownServer.KNOWN_SERVER, "KEY_known_server_ipport_UNIQUE", KnownServer.KNOWN_SERVER.IPPORT);
        public static final UniqueKey<ManagerRecord> KEY_MANAGER_PRIMARY = Internal.createUniqueKey(Manager.MANAGER, "KEY_manager_PRIMARY", Manager.MANAGER.ID);
        public static final UniqueKey<ManagerRecord> KEY_MANAGER_ID_UNIQUE = Internal.createUniqueKey(Manager.MANAGER, "KEY_manager_id_UNIQUE", Manager.MANAGER.ID);
        public static final UniqueKey<ManagerRecord> KEY_MANAGER_USERNAME_UNIQUE = Internal.createUniqueKey(Manager.MANAGER, "KEY_manager_username_UNIQUE", Manager.MANAGER.USERNAME);
        public static final UniqueKey<PlayerRecord> KEY_PLAYER_PRIMARY = Internal.createUniqueKey(Player.PLAYER, "KEY_player_PRIMARY", Player.PLAYER.ID);
        public static final UniqueKey<PlayerRecord> KEY_PLAYER_ID_UNIQUE = Internal.createUniqueKey(Player.PLAYER, "KEY_player_id_UNIQUE", Player.PLAYER.ID);
        public static final UniqueKey<PlayerRecord> KEY_PLAYER_NAME_UNIQUE = Internal.createUniqueKey(Player.PLAYER, "KEY_player_name_UNIQUE", Player.PLAYER.NAME);
        public static final UniqueKey<PlayerIpRecord> KEY_PLAYER_IP_PRIMARY = Internal.createUniqueKey(PlayerIp.PLAYER_IP, "KEY_player_ip_PRIMARY", PlayerIp.PLAYER_IP.ID);
        public static final UniqueKey<PlayerIpRecord> KEY_PLAYER_IP_ID_UNIQUE = Internal.createUniqueKey(PlayerIp.PLAYER_IP, "KEY_player_ip_id_UNIQUE", PlayerIp.PLAYER_IP.ID);
        public static final UniqueKey<PlayerSteamidRecord> KEY_PLAYER_STEAMID_PRIMARY = Internal.createUniqueKey(PlayerSteamid.PLAYER_STEAMID, "KEY_player_steamid_PRIMARY", PlayerSteamid.PLAYER_STEAMID.ID);
        public static final UniqueKey<PlayerSteamidRecord> KEY_PLAYER_STEAMID_ID_UNIQUE = Internal.createUniqueKey(PlayerSteamid.PLAYER_STEAMID, "KEY_player_steamid_id_UNIQUE", PlayerSteamid.PLAYER_STEAMID.ID);
        public static final UniqueKey<RankRecord> KEY_RANK_PRIMARY = Internal.createUniqueKey(Rank.RANK, "KEY_rank_PRIMARY", Rank.RANK.ID);
        public static final UniqueKey<RankRecord> KEY_RANK_ID_UNIQUE = Internal.createUniqueKey(Rank.RANK, "KEY_rank_id_UNIQUE", Rank.RANK.ID);
        public static final UniqueKey<RankRecord> KEY_RANK_LEVEL_UNIQUE = Internal.createUniqueKey(Rank.RANK, "KEY_rank_level_UNIQUE", Rank.RANK.LEVEL);
        public static final UniqueKey<RankRecord> KEY_RANK_NAME_UNIQUE = Internal.createUniqueKey(Rank.RANK, "KEY_rank_name_UNIQUE", Rank.RANK.NAME);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<HistoryRecord, PlayerRecord> HISTORY_PLAYER_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.Keys.KEY_PLAYER_PRIMARY, History.HISTORY, "history_player_id_fk", History.HISTORY.PLAYER_ID);
        public static final ForeignKey<HistoryRecord, RankRecord> HISTORY_OLD_RANK_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.Keys.KEY_RANK_PRIMARY, History.HISTORY, "history_old_rank_id_fk", History.HISTORY.OLD_RANK_ID);
        public static final ForeignKey<HistoryRecord, RankRecord> HISTORY_NEW_RANK_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.Keys.KEY_RANK_PRIMARY, History.HISTORY, "history_new_rank_id_fk", History.HISTORY.NEW_RANK_ID);
        public static final ForeignKey<PlayerRecord, RankRecord> PLAYER_RANK_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.Keys.KEY_RANK_PRIMARY, Player.PLAYER, "player_rank_id_fk", Player.PLAYER.RANK_ID);
        public static final ForeignKey<PlayerRecord, KnownServerRecord> PLAYER_LAST_SERVER_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.Keys.KEY_KNOWN_SERVER_PRIMARY, Player.PLAYER, "player_last_server_id_fk", Player.PLAYER.LAST_SERVER_ID);
        public static final ForeignKey<PlayerIpRecord, PlayerRecord> PLAYER_IP_PLAYER_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.Keys.KEY_PLAYER_PRIMARY, PlayerIp.PLAYER_IP, "player_ip_player_id_fk", PlayerIp.PLAYER_IP.PLAYER_ID);
        public static final ForeignKey<PlayerSteamidRecord, PlayerRecord> PLAYER_STEAMID_PLAYER_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.Keys.KEY_PLAYER_PRIMARY, PlayerSteamid.PLAYER_STEAMID, "player_steamid_player_id_fk", PlayerSteamid.PLAYER_STEAMID.PLAYER_ID);
    }
}
