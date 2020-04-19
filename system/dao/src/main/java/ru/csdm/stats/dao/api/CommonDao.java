package ru.csdm.stats.dao.api;

import org.jooq.DSLContext;

public interface CommonDao {
    DSLContext getContext();
}
