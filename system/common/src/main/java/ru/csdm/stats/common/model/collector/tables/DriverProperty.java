/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.collector.tables;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.collector.Collector;
import ru.csdm.stats.common.model.collector.Indexes;
import ru.csdm.stats.common.model.collector.Keys;
import ru.csdm.stats.common.model.collector.tables.records.DriverPropertyRecord;

import java.util.Arrays;
import java.util.List;


/**
 * Additional JDBC driver connection properties https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DriverProperty extends TableImpl<DriverPropertyRecord> {

    private static final long serialVersionUID = 1600871838;

    /**
     * The reference instance of <code>collector.driver_property</code>
     */
    public static final DriverProperty DRIVER_PROPERTY = new DriverProperty();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DriverPropertyRecord> getRecordType() {
        return DriverPropertyRecord.class;
    }

    /**
     * The column <code>collector.driver_property.id</code>.
     */
    public final TableField<DriverPropertyRecord, UInteger> ID = createField("id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).identity(true), this, "");

    /**
     * The column <code>collector.driver_property.project_id</code>.
     */
    public final TableField<DriverPropertyRecord, UInteger> PROJECT_ID = createField("project_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

    /**
     * The column <code>collector.driver_property.key</code>.
     */
    public final TableField<DriverPropertyRecord, String> KEY = createField("key", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>collector.driver_property.value</code>.
     */
    public final TableField<DriverPropertyRecord, String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * Create a <code>collector.driver_property</code> table reference
     */
    public DriverProperty() {
        this(DSL.name("driver_property"), null);
    }

    /**
     * Create an aliased <code>collector.driver_property</code> table reference
     */
    public DriverProperty(String alias) {
        this(DSL.name(alias), DRIVER_PROPERTY);
    }

    /**
     * Create an aliased <code>collector.driver_property</code> table reference
     */
    public DriverProperty(Name alias) {
        this(alias, DRIVER_PROPERTY);
    }

    private DriverProperty(Name alias, Table<DriverPropertyRecord> aliased) {
        this(alias, aliased, null);
    }

    private DriverProperty(Name alias, Table<DriverPropertyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("Additional JDBC driver connection properties https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html"));
    }

    public <O extends Record> DriverProperty(Table<O> child, ForeignKey<O, DriverPropertyRecord> key) {
        super(child, key, DRIVER_PROPERTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Collector.COLLECTOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.DRIVER_PROPERTY_DRIVER_PROPERTY_PROJECT_ID_IDX, Indexes.DRIVER_PROPERTY_ID_UNIQUE, Indexes.DRIVER_PROPERTY_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<DriverPropertyRecord, UInteger> getIdentity() {
        return Keys.IDENTITY_DRIVER_PROPERTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<DriverPropertyRecord> getPrimaryKey() {
        return Keys.KEY_DRIVER_PROPERTY_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<DriverPropertyRecord>> getKeys() {
        return Arrays.<UniqueKey<DriverPropertyRecord>>asList(Keys.KEY_DRIVER_PROPERTY_PRIMARY, Keys.KEY_DRIVER_PROPERTY_ID_UNIQUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<DriverPropertyRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<DriverPropertyRecord, ?>>asList(Keys.DRIVER_PROPERTY_PROJECT_ID_FK);
    }

    public Project project() {
        return new Project(this, Keys.DRIVER_PROPERTY_PROJECT_ID_FK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverProperty as(String alias) {
        return new DriverProperty(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverProperty as(Name alias) {
        return new DriverProperty(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public DriverProperty rename(String name) {
        return new DriverProperty(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public DriverProperty rename(Name name) {
        return new DriverProperty(name, null);
    }
}
