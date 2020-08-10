/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.tables.PlayerSteamid;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PlayerSteamidRecord extends UpdatableRecordImpl<PlayerSteamidRecord> implements Record4<UInteger, UInteger, String, LocalDateTime> {

    private static final long serialVersionUID = 665613351;

    /**
     * Setter for <code>csstats.player_steamid.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>csstats.player_steamid.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>csstats.player_steamid.player_id</code>.
     */
    public void setPlayerId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>csstats.player_steamid.player_id</code>.
     */
    @NotNull
    public UInteger getPlayerId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>csstats.player_steamid.steamid</code>.
     */
    public void setSteamid(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>csstats.player_steamid.steamid</code>.
     */
    @NotNull
    @Size(max = 22)
    public String getSteamid() {
        return (String) get(2);
    }

    /**
     * Setter for <code>csstats.player_steamid.reg_datetime</code>.
     */
    public void setRegDatetime(LocalDateTime value) {
        set(3, value);
    }

    /**
     * Getter for <code>csstats.player_steamid.reg_datetime</code>.
     */
    public LocalDateTime getRegDatetime() {
        return (LocalDateTime) get(3);
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
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<UInteger, UInteger, String, LocalDateTime> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<UInteger, UInteger, String, LocalDateTime> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return PlayerSteamid.PLAYER_STEAMID.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return PlayerSteamid.PLAYER_STEAMID.PLAYER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return PlayerSteamid.PLAYER_STEAMID.STEAMID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field4() {
        return PlayerSteamid.PLAYER_STEAMID.REG_DATETIME;
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
    public UInteger component2() {
        return getPlayerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getSteamid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component4() {
        return getRegDatetime();
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
    public UInteger value2() {
        return getPlayerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getSteamid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value4() {
        return getRegDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerSteamidRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerSteamidRecord value2(UInteger value) {
        setPlayerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerSteamidRecord value3(String value) {
        setSteamid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerSteamidRecord value4(LocalDateTime value) {
        setRegDatetime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerSteamidRecord values(UInteger value1, UInteger value2, String value3, LocalDateTime value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PlayerSteamidRecord
     */
    public PlayerSteamidRecord() {
        super(PlayerSteamid.PLAYER_STEAMID);
    }

    /**
     * Create a detached, initialised PlayerSteamidRecord
     */
    public PlayerSteamidRecord(UInteger id, UInteger playerId, String steamid, LocalDateTime regDatetime) {
        super(PlayerSteamid.PLAYER_STEAMID);

        set(0, id);
        set(1, playerId);
        set(2, steamid);
        set(3, regDatetime);
    }
}
