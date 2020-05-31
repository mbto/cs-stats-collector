package ru.csdm.stats.model;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public interface Csstats {
    Table<Record> csstats_table = DSL.table("csstats");

    Field<Long> id_field = DSL.field("id", Long.class);
    Field<String> name_field = DSL.field("name", String.class);
    Field<Long> kills_field = DSL.field("kills", Long.class);
    Field<Long> deaths_field = DSL.field("deaths", Long.class);
    Field<Long> time_secs_field = DSL.field("time_secs", Long.class);
}
