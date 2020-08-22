/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.collector;


import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.collector.tables.*;
import ru.csdm.stats.common.model.collector.tables.records.*;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>collector</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<DriverPropertyRecord, UInteger> IDENTITY_DRIVER_PROPERTY = Identities0.IDENTITY_DRIVER_PROPERTY;
    public static final Identity<InstanceRecord, UInteger> IDENTITY_INSTANCE = Identities0.IDENTITY_INSTANCE;
    public static final Identity<KnownServerRecord, UInteger> IDENTITY_KNOWN_SERVER = Identities0.IDENTITY_KNOWN_SERVER;
    public static final Identity<ManagerRecord, UInteger> IDENTITY_MANAGER = Identities0.IDENTITY_MANAGER;
    public static final Identity<ProjectRecord, UInteger> IDENTITY_PROJECT = Identities0.IDENTITY_PROJECT;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<DriverPropertyRecord> KEY_DRIVER_PROPERTY_PRIMARY = UniqueKeys0.KEY_DRIVER_PROPERTY_PRIMARY;
    public static final UniqueKey<DriverPropertyRecord> KEY_DRIVER_PROPERTY_ID_UNIQUE = UniqueKeys0.KEY_DRIVER_PROPERTY_ID_UNIQUE;
    public static final UniqueKey<InstanceRecord> KEY_INSTANCE_PRIMARY = UniqueKeys0.KEY_INSTANCE_PRIMARY;
    public static final UniqueKey<InstanceRecord> KEY_INSTANCE_ID_UNIQUE = UniqueKeys0.KEY_INSTANCE_ID_UNIQUE;
    public static final UniqueKey<InstanceRecord> KEY_INSTANCE_NAME_UNIQUE = UniqueKeys0.KEY_INSTANCE_NAME_UNIQUE;
    public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_PRIMARY = UniqueKeys0.KEY_KNOWN_SERVER_PRIMARY;
    public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_ID_UNIQUE = UniqueKeys0.KEY_KNOWN_SERVER_ID_UNIQUE;
    public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_IPPORT_UNIQUE = UniqueKeys0.KEY_KNOWN_SERVER_IPPORT_UNIQUE;
    public static final UniqueKey<ManagerRecord> KEY_MANAGER_PRIMARY = UniqueKeys0.KEY_MANAGER_PRIMARY;
    public static final UniqueKey<ManagerRecord> KEY_MANAGER_ID_UNIQUE = UniqueKeys0.KEY_MANAGER_ID_UNIQUE;
    public static final UniqueKey<ManagerRecord> KEY_MANAGER_USERNAME_UNIQUE = UniqueKeys0.KEY_MANAGER_USERNAME_UNIQUE;
    public static final UniqueKey<ProjectRecord> KEY_PROJECT_PRIMARY = UniqueKeys0.KEY_PROJECT_PRIMARY;
    public static final UniqueKey<ProjectRecord> KEY_PROJECT_ID_UNIQUE = UniqueKeys0.KEY_PROJECT_ID_UNIQUE;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<DriverPropertyRecord, ProjectRecord> DRIVER_PROPERTY_PROJECT_ID_FK = ForeignKeys0.DRIVER_PROPERTY_PROJECT_ID_FK;
    public static final ForeignKey<KnownServerRecord, InstanceRecord> KNOWN_SERVER_INSTANCE_ID_FK = ForeignKeys0.KNOWN_SERVER_INSTANCE_ID_FK;
    public static final ForeignKey<KnownServerRecord, ProjectRecord> KNOWN_SERVER_PROJECT_ID_FK = ForeignKeys0.KNOWN_SERVER_PROJECT_ID_FK;
    public static final ForeignKey<ManagerRecord, ProjectRecord> MANAGER_PROJECT_ID_FK = ForeignKeys0.MANAGER_PROJECT_ID_FK;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<DriverPropertyRecord, UInteger> IDENTITY_DRIVER_PROPERTY = Internal.createIdentity(DriverProperty.DRIVER_PROPERTY, DriverProperty.DRIVER_PROPERTY.ID);
        public static Identity<InstanceRecord, UInteger> IDENTITY_INSTANCE = Internal.createIdentity(Instance.INSTANCE, Instance.INSTANCE.ID);
        public static Identity<KnownServerRecord, UInteger> IDENTITY_KNOWN_SERVER = Internal.createIdentity(KnownServer.KNOWN_SERVER, KnownServer.KNOWN_SERVER.ID);
        public static Identity<ManagerRecord, UInteger> IDENTITY_MANAGER = Internal.createIdentity(Manager.MANAGER, Manager.MANAGER.ID);
        public static Identity<ProjectRecord, UInteger> IDENTITY_PROJECT = Internal.createIdentity(Project.PROJECT, Project.PROJECT.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<DriverPropertyRecord> KEY_DRIVER_PROPERTY_PRIMARY = Internal.createUniqueKey(DriverProperty.DRIVER_PROPERTY, "KEY_driver_property_PRIMARY", DriverProperty.DRIVER_PROPERTY.ID);
        public static final UniqueKey<DriverPropertyRecord> KEY_DRIVER_PROPERTY_ID_UNIQUE = Internal.createUniqueKey(DriverProperty.DRIVER_PROPERTY, "KEY_driver_property_id_UNIQUE", DriverProperty.DRIVER_PROPERTY.ID);
        public static final UniqueKey<InstanceRecord> KEY_INSTANCE_PRIMARY = Internal.createUniqueKey(Instance.INSTANCE, "KEY_instance_PRIMARY", Instance.INSTANCE.ID);
        public static final UniqueKey<InstanceRecord> KEY_INSTANCE_ID_UNIQUE = Internal.createUniqueKey(Instance.INSTANCE, "KEY_instance_id_UNIQUE", Instance.INSTANCE.ID);
        public static final UniqueKey<InstanceRecord> KEY_INSTANCE_NAME_UNIQUE = Internal.createUniqueKey(Instance.INSTANCE, "KEY_instance_name_UNIQUE", Instance.INSTANCE.NAME);
        public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_PRIMARY = Internal.createUniqueKey(KnownServer.KNOWN_SERVER, "KEY_known_server_PRIMARY", KnownServer.KNOWN_SERVER.ID);
        public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_ID_UNIQUE = Internal.createUniqueKey(KnownServer.KNOWN_SERVER, "KEY_known_server_id_UNIQUE", KnownServer.KNOWN_SERVER.ID);
        public static final UniqueKey<KnownServerRecord> KEY_KNOWN_SERVER_IPPORT_UNIQUE = Internal.createUniqueKey(KnownServer.KNOWN_SERVER, "KEY_known_server_ipport_UNIQUE", KnownServer.KNOWN_SERVER.IPPORT);
        public static final UniqueKey<ManagerRecord> KEY_MANAGER_PRIMARY = Internal.createUniqueKey(Manager.MANAGER, "KEY_manager_PRIMARY", Manager.MANAGER.ID);
        public static final UniqueKey<ManagerRecord> KEY_MANAGER_ID_UNIQUE = Internal.createUniqueKey(Manager.MANAGER, "KEY_manager_id_UNIQUE", Manager.MANAGER.ID);
        public static final UniqueKey<ManagerRecord> KEY_MANAGER_USERNAME_UNIQUE = Internal.createUniqueKey(Manager.MANAGER, "KEY_manager_username_UNIQUE", Manager.MANAGER.USERNAME);
        public static final UniqueKey<ProjectRecord> KEY_PROJECT_PRIMARY = Internal.createUniqueKey(Project.PROJECT, "KEY_project_PRIMARY", Project.PROJECT.ID);
        public static final UniqueKey<ProjectRecord> KEY_PROJECT_ID_UNIQUE = Internal.createUniqueKey(Project.PROJECT, "KEY_project_id_UNIQUE", Project.PROJECT.ID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<DriverPropertyRecord, ProjectRecord> DRIVER_PROPERTY_PROJECT_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.collector.Keys.KEY_PROJECT_PRIMARY, DriverProperty.DRIVER_PROPERTY, "driver_property_project_id_fk", DriverProperty.DRIVER_PROPERTY.PROJECT_ID);
        public static final ForeignKey<KnownServerRecord, InstanceRecord> KNOWN_SERVER_INSTANCE_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.collector.Keys.KEY_INSTANCE_PRIMARY, KnownServer.KNOWN_SERVER, "known_server_instance_id_fk", KnownServer.KNOWN_SERVER.INSTANCE_ID);
        public static final ForeignKey<KnownServerRecord, ProjectRecord> KNOWN_SERVER_PROJECT_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.collector.Keys.KEY_PROJECT_PRIMARY, KnownServer.KNOWN_SERVER, "known_server_project_id_fk", KnownServer.KNOWN_SERVER.PROJECT_ID);
        public static final ForeignKey<ManagerRecord, ProjectRecord> MANAGER_PROJECT_ID_FK = Internal.createForeignKey(ru.csdm.stats.common.model.collector.Keys.KEY_PROJECT_PRIMARY, Manager.MANAGER, "manager_project_id_fk", Manager.MANAGER.PROJECT_ID);
    }
}