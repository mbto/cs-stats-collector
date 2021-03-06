/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.tables.ApiUser;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


/**
 * Who to share API access to endpoints /stats/ *
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ApiUserRecord extends UpdatableRecordImpl<ApiUserRecord> implements Record7<UInteger, Boolean, String, String, Boolean, Boolean, LocalDateTime> {

    private static final long serialVersionUID = -593830349;

    /**
     * Setter for <code>csstats.api_user.id</code>.
     */
    public void setId(UInteger value) {
        set(0, value);
    }

    /**
     * Getter for <code>csstats.api_user.id</code>.
     */
    public UInteger getId() {
        return (UInteger) get(0);
    }

    /**
     * Setter for <code>csstats.api_user.active</code>.
     */
    public void setActive(Boolean value) {
        set(1, value);
    }

    /**
     * Getter for <code>csstats.api_user.active</code>.
     */
    public Boolean getActive() {
        return (Boolean) get(1);
    }

    /**
     * Setter for <code>csstats.api_user.username</code>.
     */
    public void setUsername(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>csstats.api_user.username</code>.
     */
    @NotNull
    @Size(max = 31)
    public String getUsername() {
        return (String) get(2);
    }

    /**
     * Setter for <code>csstats.api_user.password</code>. https://www.browserling.com/tools/bcrypt
     */
    public void setPassword(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>csstats.api_user.password</code>. https://www.browserling.com/tools/bcrypt
     */
    @NotNull
    @Size(max = 60)
    public String getPassword() {
        return (String) get(3);
    }

    /**
     * Setter for <code>csstats.api_user.manage</code>. 1-can invoke 'managers' endpoints (/stats/updateSettings, /stats/flush, /stats/, etc...);0-can't invoke 'managers' endpoints
     */
    public void setManage(Boolean value) {
        set(4, value);
    }

    /**
     * Getter for <code>csstats.api_user.manage</code>. 1-can invoke 'managers' endpoints (/stats/updateSettings, /stats/flush, /stats/, etc...);0-can't invoke 'managers' endpoints
     */
    public Boolean getManage() {
        return (Boolean) get(4);
    }

    /**
     * Setter for <code>csstats.api_user.view</code>. 1-can invoke 'views' endpoints (/stats/player, etc...);0-can't invoke 'views' endpoints
     */
    public void setView(Boolean value) {
        set(5, value);
    }

    /**
     * Getter for <code>csstats.api_user.view</code>. 1-can invoke 'views' endpoints (/stats/player, etc...);0-can't invoke 'views' endpoints
     */
    public Boolean getView() {
        return (Boolean) get(5);
    }

    /**
     * Setter for <code>csstats.api_user.reg_datetime</code>.
     */
    public void setRegDatetime(LocalDateTime value) {
        set(6, value);
    }

    /**
     * Getter for <code>csstats.api_user.reg_datetime</code>.
     */
    public LocalDateTime getRegDatetime() {
        return (LocalDateTime) get(6);
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
    // Record7 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<UInteger, Boolean, String, String, Boolean, Boolean, LocalDateTime> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<UInteger, Boolean, String, String, Boolean, Boolean, LocalDateTime> valuesRow() {
        return (Row7) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UInteger> field1() {
        return ApiUser.API_USER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field2() {
        return ApiUser.API_USER.ACTIVE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return ApiUser.API_USER.USERNAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return ApiUser.API_USER.PASSWORD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field5() {
        return ApiUser.API_USER.MANAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field6() {
        return ApiUser.API_USER.VIEW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field7() {
        return ApiUser.API_USER.REG_DATETIME;
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
    public Boolean component2() {
        return getActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component5() {
        return getManage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component6() {
        return getView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime component7() {
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
    public Boolean value2() {
        return getActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value5() {
        return getManage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value6() {
        return getView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value7() {
        return getRegDatetime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord value1(UInteger value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord value2(Boolean value) {
        setActive(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord value3(String value) {
        setUsername(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord value4(String value) {
        setPassword(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord value5(Boolean value) {
        setManage(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord value6(Boolean value) {
        setView(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord value7(LocalDateTime value) {
        setRegDatetime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiUserRecord values(UInteger value1, Boolean value2, String value3, String value4, Boolean value5, Boolean value6, LocalDateTime value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ApiUserRecord
     */
    public ApiUserRecord() {
        super(ApiUser.API_USER);
    }

    /**
     * Create a detached, initialised ApiUserRecord
     */
    public ApiUserRecord(UInteger id, Boolean active, String username, String password, Boolean manage, Boolean view, LocalDateTime regDatetime) {
        super(ApiUser.API_USER);

        set(0, id);
        set(1, active);
        set(2, username);
        set(3, password);
        set(4, manage);
        set(5, view);
        set(6, regDatetime);
    }
}
