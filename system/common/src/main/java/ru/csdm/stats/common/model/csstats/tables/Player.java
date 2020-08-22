/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.csstats.tables;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.csstats.Csstats;
import ru.csdm.stats.common.model.csstats.Indexes;
import ru.csdm.stats.common.model.csstats.Keys;
import ru.csdm.stats.common.model.csstats.tables.records.PlayerRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Player extends TableImpl<PlayerRecord> {

    private static final long serialVersionUID = -1699256408;

    /**
     * The reference instance of <code>csstats.player</code>
     */
    public static final Player PLAYER = new Player();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PlayerRecord> getRecordType() {
        return PlayerRecord.class;
    }

    /**
     * The column <code>csstats.player.id</code>.
     */
    public final TableField<PlayerRecord, UInteger> ID = createField("id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>csstats.player.name</code>.
     */
    public final TableField<PlayerRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(31).nullable(false), this, "");

    /**
     * The column <code>csstats.player.kills</code>.
     */
    public final TableField<PlayerRecord, UInteger> KILLS = createField("kills", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGERUNSIGNED)), this, "");

    /**
     * The column <code>csstats.player.deaths</code>.
     */
    public final TableField<PlayerRecord, UInteger> DEATHS = createField("deaths", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGERUNSIGNED)), this, "");

    /**
     * The column <code>csstats.player.time_secs</code>.
     */
    public final TableField<PlayerRecord, UInteger> TIME_SECS = createField("time_secs", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGERUNSIGNED)), this, "");

    /**
     * The column <code>csstats.player.rank_id</code>.
     */
    public final TableField<PlayerRecord, UInteger> RANK_ID = createField("rank_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this, "");

    /**
     * The column <code>csstats.player.lastseen_datetime</code>.
     */
    public final TableField<PlayerRecord, LocalDateTime> LASTSEEN_DATETIME = createField("lastseen_datetime", org.jooq.impl.SQLDataType.LOCALDATETIME, this, "");

    /**
     * The column <code>csstats.player.last_server_name</code>.
     */
    public final TableField<PlayerRecord, String> LAST_SERVER_NAME = createField("last_server_name", org.jooq.impl.SQLDataType.VARCHAR(31), this, "");

    /**
     * Create a <code>csstats.player</code> table reference
     */
    public Player() {
        this(DSL.name("player"), null);
    }

    /**
     * Create an aliased <code>csstats.player</code> table reference
     */
    public Player(String alias) {
        this(DSL.name(alias), PLAYER);
    }

    /**
     * Create an aliased <code>csstats.player</code> table reference
     */
    public Player(Name alias) {
        this(alias, PLAYER);
    }

    private Player(Name alias, Table<PlayerRecord> aliased) {
        this(alias, aliased, null);
    }

    private Player(Name alias, Table<PlayerRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Player(Table<O> child, ForeignKey<O, PlayerRecord> key) {
        super(child, key, PLAYER);
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
        return Arrays.<Index>asList(Indexes.PLAYER_ID_UNIQUE, Indexes.PLAYER_NAME_UNIQUE, Indexes.PLAYER_PLAYER_RANK_ID_IDX, Indexes.PLAYER_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<PlayerRecord, UInteger> getIdentity() {
        return Keys.IDENTITY_PLAYER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PlayerRecord> getPrimaryKey() {
        return Keys.KEY_PLAYER_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PlayerRecord>> getKeys() {
        return Arrays.<UniqueKey<PlayerRecord>>asList(Keys.KEY_PLAYER_PRIMARY, Keys.KEY_PLAYER_ID_UNIQUE, Keys.KEY_PLAYER_NAME_UNIQUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<PlayerRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<PlayerRecord, ?>>asList(Keys.PLAYER_RANK_ID_FK);
    }

    public Rank rank() {
        return new Rank(this, Keys.PLAYER_RANK_ID_FK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player as(String alias) {
        return new Player(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player as(Name alias) {
        return new Player(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Player rename(String name) {
        return new Player(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Player rename(Name name) {
        return new Player(name, null);
    }
}