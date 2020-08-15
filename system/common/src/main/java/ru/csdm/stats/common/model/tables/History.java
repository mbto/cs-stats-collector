/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import ru.csdm.stats.common.model.Csstats;
import ru.csdm.stats.common.model.Indexes;
import ru.csdm.stats.common.model.Keys;
import ru.csdm.stats.common.model.tables.records.HistoryRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class History extends TableImpl<HistoryRecord> {

    private static final long serialVersionUID = -1481933391;

    /**
     * The reference instance of <code>csstats.history</code>
     */
    public static final History HISTORY = new History();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<HistoryRecord> getRecordType() {
        return HistoryRecord.class;
    }

    /**
     * The column <code>csstats.history.id</code>.
     */
    public final TableField<HistoryRecord, UInteger> ID = createField("id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>csstats.history.player_id</code>.
     */
    public final TableField<HistoryRecord, UInteger> PLAYER_ID = createField("player_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>csstats.history.old_rank_id</code>.
     */
    public final TableField<HistoryRecord, UInteger> OLD_RANK_ID = createField("old_rank_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this, "");

    /**
     * The column <code>csstats.history.new_rank_id</code>.
     */
    public final TableField<HistoryRecord, UInteger> NEW_RANK_ID = createField("new_rank_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this, "");

    /**
     * The column <code>csstats.history.reg_datetime</code>.
     */
    public final TableField<HistoryRecord, LocalDateTime> REG_DATETIME = createField("reg_datetime", org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false).defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * Create a <code>csstats.history</code> table reference
     */
    public History() {
        this(DSL.name("history"), null);
    }

    /**
     * Create an aliased <code>csstats.history</code> table reference
     */
    public History(String alias) {
        this(DSL.name(alias), HISTORY);
    }

    /**
     * Create an aliased <code>csstats.history</code> table reference
     */
    public History(Name alias) {
        this(alias, HISTORY);
    }

    private History(Name alias, Table<HistoryRecord> aliased) {
        this(alias, aliased, null);
    }

    private History(Name alias, Table<HistoryRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> History(Table<O> child, ForeignKey<O, HistoryRecord> key) {
        super(child, key, HISTORY);
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
        return Arrays.<Index>asList(Indexes.HISTORY_HISTORY_NEW_RANK_ID_IDX, Indexes.HISTORY_HISTORY_OLD_RANK_ID_IDX, Indexes.HISTORY_HISTORY_PLAYER_ID_IDX, Indexes.HISTORY_ID_UNIQUE, Indexes.HISTORY_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<HistoryRecord, UInteger> getIdentity() {
        return Keys.IDENTITY_HISTORY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<HistoryRecord> getPrimaryKey() {
        return Keys.KEY_HISTORY_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<HistoryRecord>> getKeys() {
        return Arrays.<UniqueKey<HistoryRecord>>asList(Keys.KEY_HISTORY_PRIMARY, Keys.KEY_HISTORY_ID_UNIQUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<HistoryRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<HistoryRecord, ?>>asList(Keys.HISTORY_PLAYER_ID_FK, Keys.HISTORY_OLD_RANK_ID_FK, Keys.HISTORY_NEW_RANK_ID_FK);
    }

    public Player player() {
        return new Player(this, Keys.HISTORY_PLAYER_ID_FK);
    }

    public Rank historyOldRankIdFk() {
        return new Rank(this, Keys.HISTORY_OLD_RANK_ID_FK);
    }

    public Rank historyNewRankIdFk() {
        return new Rank(this, Keys.HISTORY_NEW_RANK_ID_FK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public History as(String alias) {
        return new History(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public History as(Name alias) {
        return new History(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public History rename(String name) {
        return new History(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public History rename(Name name) {
        return new History(name, null);
    }
}
