/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.tables.records;


import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import ru.csdm.stats.common.model.tables.Player;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PlayerRecord extends UpdatableRecordImpl<PlayerRecord> implements Record8<UInteger, String, UInteger, UInteger, UInteger, UInteger, LocalDateTime, UInteger> {

    private static final long serialVersionUID = 1663184062;

    /**
     * Setter for <code>csstats.player.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>csstats.player.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>csstats.player.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>csstats.player.name</code>.
     */
    @NotNull
    @Size(max = 31)
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>csstats.player.kills</code>.
     */
    public void setKills(UInteger value) {
        set(2, value);
    }

    /**
     * Getter for <code>csstats.player.kills</code>.
     */
    public UInteger getKills() {
        return (UInteger) get(2);
    }

    /**
     * Setter for <code>csstats.player.deaths</code>.
     */
    public void setDeaths(UInteger value) {
        set(3, value);
    }

    /**
     * Getter for <code>csstats.player.deaths</code>.
     */
    public UInteger getDeaths() {
        return (UInteger) get(3);
    }

    /**
     * Setter for <code>csstats.player.time_secs</code>.
     */
    public void setTimeSecs(UInteger value) {
        set(4, value);
    }

    /**
     * Getter for <code>csstats.player.time_secs</code>.
     */
    public UInteger getTimeSecs() {
        return (UInteger) get(4);
    }

    /**
     * Setter for <code>csstats.player.rank_id</code>.
     */
    public void setRankId(UInteger value) {
        set(5, value);
    }

    /**
     * Getter for <code>csstats.player.rank_id</code>.
     */
    public UInteger getRankId() {
        return (UInteger) get(5);
    }

    /**
     * Setter for <code>csstats.player.lastseen_datetime</code>.
     */
    public void setLastseenDatetime(LocalDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>csstats.player.lastseen_datetime</code>.
     */
    public LocalDateTime getLastseenDatetime() {
        return (LocalDateTime) get(6);
    }

    /**
     * Setter for <code>csstats.player.last_server_id</code>.
     */
    public void setLastServerId(UInteger value) {
        set(7, value);
    }

    /**
     * Getter for <code>csstats.player.last_server_id</code>.
     */
    public UInteger getLastServerId() {
        return (UInteger) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<UInteger, String, UInteger, UInteger, UInteger, UInteger, LocalDateTime, UInteger> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<UInteger, String, UInteger, UInteger, UInteger, UInteger, LocalDateTime, UInteger> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return Player.PLAYER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Player.PLAYER.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field3() {
        return Player.PLAYER.KILLS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field4() {
        return Player.PLAYER.DEATHS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field5() {
        return Player.PLAYER.TIME_SECS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field6() {
        return Player.PLAYER.RANK_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field7() {
        return Player.PLAYER.LASTSEEN_DATETIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field8() {
        return Player.PLAYER.LAST_SERVER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component3() {
        return getKills();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component4() {
        return getDeaths();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component5() {
        return getTimeSecs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component6() {
        return getRankId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component7() {
        return getLastseenDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component8() {
        return getLastServerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value3() {
        return getKills();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value4() {
        return getDeaths();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value5() {
        return getTimeSecs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value6() {
        return getRankId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value7() {
        return getLastseenDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value8() {
        return getLastServerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value3(UInteger value) {
        setKills(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value4(UInteger value) {
        setDeaths(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value5(UInteger value) {
        setTimeSecs(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value6(UInteger value) {
        setRankId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value7(LocalDateTime value) {
        setLastseenDatetime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord value8(UInteger value) {
        setLastServerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerRecord values(UInteger value1, String value2, UInteger value3, UInteger value4, UInteger value5, UInteger value6, LocalDateTime value7, UInteger value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PlayerRecord
     */
    public PlayerRecord() {
        super(Player.PLAYER);
    }

    /**
     * Create a detached, initialised PlayerRecord
     */
    public PlayerRecord(UInteger id, String name, UInteger kills, UInteger deaths, UInteger timeSecs, UInteger rankId, LocalDateTime lastseenDatetime, UInteger lastServerId) {
        super(Player.PLAYER);

        set(0, id);
        set(1, name);
        set(2, kills);
        set(3, deaths);
        set(4, timeSecs);
        set(5, rankId);
        set(6, lastseenDatetime);
        set(7, lastServerId);
    }
}
