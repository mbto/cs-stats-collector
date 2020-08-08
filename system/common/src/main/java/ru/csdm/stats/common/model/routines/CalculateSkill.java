/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.routines;


import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import ru.csdm.stats.common.model.Csstats;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CalculateSkill extends AbstractRoutine<Integer> {

    private static final long serialVersionUID = -29789308;

    /**
     * The parameter <code>csstats.calculate_skill.RETURN_VALUE</code>.
     */
    public static final Parameter<Integer> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.INTEGER, false, false);

    /**
     * The parameter <code>csstats.calculate_skill.kills</code>.
     */
    public static final Parameter<Long> KILLS = Internal.createParameter("kills", org.jooq.impl.SQLDataType.BIGINT, false, false);

    /**
     * The parameter <code>csstats.calculate_skill.deaths</code>.
     */
    public static final Parameter<Long> DEATHS = Internal.createParameter("deaths", org.jooq.impl.SQLDataType.BIGINT, false, false);

    /**
     * Create a new routine call instance
     */
    public CalculateSkill() {
        super("calculate_skill", Csstats.CSSTATS, org.jooq.impl.SQLDataType.INTEGER);

        setReturnParameter(RETURN_VALUE);
        addInParameter(KILLS);
        addInParameter(DEATHS);
    }

    /**
     * Set the <code>kills</code> parameter IN value to the routine
     */
    public void setKills(Long value) {
        setValue(KILLS, value);
    }

    /**
     * Set the <code>kills</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setKills(Field<Long> field) {
        setField(KILLS, field);
    }

    /**
     * Set the <code>deaths</code> parameter IN value to the routine
     */
    public void setDeaths(Long value) {
        setValue(DEATHS, value);
    }

    /**
     * Set the <code>deaths</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setDeaths(Field<Long> field) {
        setField(DEATHS, field);
    }
}
