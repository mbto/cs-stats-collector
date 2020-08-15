/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.tables;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.Csstats;
import ru.csdm.stats.common.model.Indexes;
import ru.csdm.stats.common.model.Keys;
import ru.csdm.stats.common.model.tables.records.KnownServerRecord;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class KnownServer extends TableImpl<KnownServerRecord> {

    private static final long serialVersionUID = -590713618;

    /**
     * The reference instance of <code>csstats.known_server</code>
     */
    public static final KnownServer KNOWN_SERVER = new KnownServer();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<KnownServerRecord> getRecordType() {
        return KnownServerRecord.class;
    }

    /**
     * The column <code>csstats.known_server.id</code>.
     */
    public final TableField<KnownServerRecord, UInteger> ID = createField("id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>csstats.known_server.ipport</code>. ip:port of the server from which the logs will be expected
     */
    public final TableField<KnownServerRecord, String> IPPORT = createField("ipport", org.jooq.impl.SQLDataType.VARCHAR(21).nullable(false), this, "ip:port of the server from which the logs will be expected");

    /**
     * The column <code>csstats.known_server.name</code>.
     */
    public final TableField<KnownServerRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(31).nullable(false), this, "");

    /**
     * The column <code>csstats.known_server.active</code>. Are ip:port allowed?: 1-allowed; 0-not allowed (logs/stats from this ip:port will be ignored)
     */
    public final TableField<KnownServerRecord, Boolean> ACTIVE = createField("active", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.BOOLEAN)), this, "Are ip:port allowed?: 1-allowed; 0-not allowed (logs/stats from this ip:port will be ignored)");

    /**
     * The column <code>csstats.known_server.ffa</code>. game server is FREE-FOR-ALL mode (Example: CS-DeathMatch): 1-true; 0-false
     */
    public final TableField<KnownServerRecord, Boolean> FFA = createField("ffa", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.BOOLEAN)), this, "game server is FREE-FOR-ALL mode (Example: CS-DeathMatch): 1-true; 0-false");

    /**
     * The column <code>csstats.known_server.ignore_bots</code>. 1-ignore statistics, when killer or victim is BOT; 0-don't ignore (include all player's)
     */
    public final TableField<KnownServerRecord, Boolean> IGNORE_BOTS = createField("ignore_bots", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.BOOLEAN)), this, "1-ignore statistics, when killer or victim is BOT; 0-don't ignore (include all player's)");

    /**
     * The column <code>csstats.known_server.start_session_on_action</code>. 1-start player's session on event "... killed ... with ..." (not for kreedz servers); 0-start player's session on event "... connected, address ..." or "... entered the game"
     */
    public final TableField<KnownServerRecord, Boolean> START_SESSION_ON_ACTION = createField("start_session_on_action", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.BOOLEAN)), this, "1-start player's session on event \"... killed ... with ...\" (not for kreedz servers); 0-start player's session on event \"... connected, address ...\" or \"... entered the game\"");

    /**
     * Create a <code>csstats.known_server</code> table reference
     */
    public KnownServer() {
        this(DSL.name("known_server"), null);
    }

    /**
     * Create an aliased <code>csstats.known_server</code> table reference
     */
    public KnownServer(String alias) {
        this(DSL.name(alias), KNOWN_SERVER);
    }

    /**
     * Create an aliased <code>csstats.known_server</code> table reference
     */
    public KnownServer(Name alias) {
        this(alias, KNOWN_SERVER);
    }

    private KnownServer(Name alias, Table<KnownServerRecord> aliased) {
        this(alias, aliased, null);
    }

    private KnownServer(Name alias, Table<KnownServerRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> KnownServer(Table<O> child, ForeignKey<O, KnownServerRecord> key) {
        super(child, key, KNOWN_SERVER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Csstats.CSSTATS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.KNOWN_SERVER_ID_UNIQUE, Indexes.KNOWN_SERVER_IPPORT_UNIQUE, Indexes.KNOWN_SERVER_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<KnownServerRecord, UInteger> getIdentity() {
        return Keys.IDENTITY_KNOWN_SERVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<KnownServerRecord> getPrimaryKey() {
        return Keys.KEY_KNOWN_SERVER_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<KnownServerRecord>> getKeys() {
        return Arrays.<UniqueKey<KnownServerRecord>>asList(Keys.KEY_KNOWN_SERVER_PRIMARY, Keys.KEY_KNOWN_SERVER_ID_UNIQUE, Keys.KEY_KNOWN_SERVER_IPPORT_UNIQUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServer as(String alias) {
        return new KnownServer(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServer as(Name alias) {
        return new KnownServer(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public KnownServer rename(String name) {
        return new KnownServer(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public KnownServer rename(Name name) {
        return new KnownServer(name, null);
    }
}
