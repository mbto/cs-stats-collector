/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.collector.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.collector.tables.KnownServer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class KnownServerRecord extends UpdatableRecordImpl<KnownServerRecord> implements Record9<UInteger, UInteger, UInteger, String, String, Boolean, Boolean, Boolean, Boolean> {

    private static final long serialVersionUID = 1788141626;

    /**
     * Setter for <code>collector.known_server.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>collector.known_server.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>collector.known_server.instance_id</code>.
     */
    public void setInstanceId(UInteger value) {
        set(1, value);
    }

    /**
     * Getter for <code>collector.known_server.instance_id</code>.
     */
    @NotNull
    public UInteger getInstanceId() {
        return (UInteger) get(1);
    }

    /**
     * Setter for <code>collector.known_server.project_id</code>.
     */
    public void setProjectId(UInteger value) {
        set(2, value);
    }

    /**
     * Getter for <code>collector.known_server.project_id</code>.
     */
    @NotNull
    public UInteger getProjectId() {
        return (UInteger) get(2);
    }

    /**
     * Setter for <code>collector.known_server.ipport</code>. ip:port of the server from which the logs will be expected
     */
    public void setIpport(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>collector.known_server.ipport</code>. ip:port of the server from which the logs will be expected
     */
    @NotNull
    @Size(max = 21)
    public String getIpport() {
        return (String) get(3);
    }

    /**
     * Setter for <code>collector.known_server.name</code>.
     */
    public void setName(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>collector.known_server.name</code>.
     */
    @NotNull
    @Size(max = 31)
    public String getName() {
        return (String) get(4);
    }

    /**
     * Setter for <code>collector.known_server.active</code>. Are ip:port allowed?: 1-allowed; 0-not allowed (logs/stats from this ip:port will be ignored)
     */
    public void setActive(Boolean value) {
        set(5, value);
    }

    /**
     * Getter for <code>collector.known_server.active</code>. Are ip:port allowed?: 1-allowed; 0-not allowed (logs/stats from this ip:port will be ignored)
     */
    public Boolean getActive() {
        return (Boolean) get(5);
    }

    /**
     * Setter for <code>collector.known_server.ffa</code>. game server is FREE-FOR-ALL mode (Example: CS-DeathMatch): 1-true; 0-false
     */
    public void setFfa(Boolean value) {
        set(6, value);
    }

    /**
     * Getter for <code>collector.known_server.ffa</code>. game server is FREE-FOR-ALL mode (Example: CS-DeathMatch): 1-true; 0-false
     */
    public Boolean getFfa() {
        return (Boolean) get(6);
    }

    /**
     * Setter for <code>collector.known_server.ignore_bots</code>. 1-ignore statistics, when killer or victim is BOT; 0-don't ignore (include all player's)
     */
    public void setIgnoreBots(Boolean value) {
        set(7, value);
    }

    /**
     * Getter for <code>collector.known_server.ignore_bots</code>. 1-ignore statistics, when killer or victim is BOT; 0-don't ignore (include all player's)
     */
    public Boolean getIgnoreBots() {
        return (Boolean) get(7);
    }

    /**
     * Setter for <code>collector.known_server.start_session_on_action</code>. 1-start player's session on event "... killed ... with ..." (not for kreedz servers); 0-start player's session on event "... connected, address ..." or "... entered the game"
     */
    public void setStartSessionOnAction(Boolean value) {
        set(8, value);
    }

    /**
     * Getter for <code>collector.known_server.start_session_on_action</code>. 1-start player's session on event "... killed ... with ..." (not for kreedz servers); 0-start player's session on event "... connected, address ..." or "... entered the game"
     */
    public Boolean getStartSessionOnAction() {
        return (Boolean) get(8);
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
    // Record9 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<UInteger, UInteger, UInteger, String, String, Boolean, Boolean, Boolean, Boolean> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row9<UInteger, UInteger, UInteger, String, String, Boolean, Boolean, Boolean, Boolean> valuesRow() {
        return (Row9) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return KnownServer.KNOWN_SERVER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field2() {
        return KnownServer.KNOWN_SERVER.INSTANCE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field3() {
        return KnownServer.KNOWN_SERVER.PROJECT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return KnownServer.KNOWN_SERVER.IPPORT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return KnownServer.KNOWN_SERVER.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field6() {
        return KnownServer.KNOWN_SERVER.ACTIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field7() {
        return KnownServer.KNOWN_SERVER.FFA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field8() {
        return KnownServer.KNOWN_SERVER.IGNORE_BOTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field9() {
        return KnownServer.KNOWN_SERVER.START_SESSION_ON_ACTION;
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
        return getInstanceId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger component3() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getIpport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component5() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component6() {
        return getActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component7() {
        return getFfa();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component8() {
        return getIgnoreBots();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component9() {
        return getStartSessionOnAction();
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
        return getInstanceId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UInteger value3() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getIpport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value6() {
        return getActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value7() {
        return getFfa();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value8() {
        return getIgnoreBots();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value9() {
        return getStartSessionOnAction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value2(UInteger value) {
        setInstanceId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value3(UInteger value) {
        setProjectId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value4(String value) {
        setIpport(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value5(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value6(Boolean value) {
        setActive(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value7(Boolean value) {
        setFfa(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value8(Boolean value) {
        setIgnoreBots(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord value9(Boolean value) {
        setStartSessionOnAction(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnownServerRecord values(UInteger value1, UInteger value2, UInteger value3, String value4, String value5, Boolean value6, Boolean value7, Boolean value8, Boolean value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached KnownServerRecord
     */
    public KnownServerRecord() {
        super(KnownServer.KNOWN_SERVER);
    }

    /**
     * Create a detached, initialised KnownServerRecord
     */
    public KnownServerRecord(UInteger id, UInteger instanceId, UInteger projectId, String ipport, String name, Boolean active, Boolean ffa, Boolean ignoreBots, Boolean startSessionOnAction) {
        super(KnownServer.KNOWN_SERVER);

        set(0, id);
        set(1, instanceId);
        set(2, projectId);
        set(3, ipport);
        set(4, name);
        set(5, active);
        set(6, ffa);
        set(7, ignoreBots);
        set(8, startSessionOnAction);
    }
}
