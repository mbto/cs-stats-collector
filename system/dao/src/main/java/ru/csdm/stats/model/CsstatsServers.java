package ru.csdm.stats.model;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public interface CsstatsServers {
    Table<Record> csstats_servers_table = DSL.table("csstats_servers");

    Field<String> ipport_field = DSL.field("ipport", String.class);
    Field<Boolean> active_field = DSL.field("active", Boolean.class);
    Field<Boolean> ffa_field = DSL.field("ffa", Boolean.class);
    Field<Boolean> ignore_bots_field = DSL.field("ignore_bots", Boolean.class);
    Field<Boolean> start_session_on_action = DSL.field("start_session_on_action", Boolean.class);
}
