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
public class IsValidSteamid extends AbstractRoutine<Boolean> {

    private static final long serialVersionUID = -1655363936;

    /**
     * The parameter <code>csstats.is_valid_steamid.RETURN_VALUE</code>.
     */
    public static final Parameter<Boolean> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.BOOLEAN, false, false);

    /**
     * The parameter <code>csstats.is_valid_steamid.steamid</code>.
     */
    public static final Parameter<String> STEAMID = Internal.createParameter("steamid", org.jooq.impl.SQLDataType.VARCHAR(22), false, false);

    /**
     * The parameter <code>csstats.is_valid_steamid.nullable</code>.
     */
    public static final Parameter<Byte> NULLABLE = Internal.createParameter("nullable", org.jooq.impl.SQLDataType.TINYINT, false, false);

    /**
     * The parameter <code>csstats.is_valid_steamid.only_legal</code>.
     */
    public static final Parameter<Byte> ONLY_LEGAL = Internal.createParameter("only_legal", org.jooq.impl.SQLDataType.TINYINT, false, false);

    /**
     * Create a new routine call instance
     */
    public IsValidSteamid() {
        super("is_valid_steamid", Csstats.CSSTATS, org.jooq.impl.SQLDataType.BOOLEAN);

        setReturnParameter(RETURN_VALUE);
        addInParameter(STEAMID);
        addInParameter(NULLABLE);
        addInParameter(ONLY_LEGAL);
    }

    /**
     * Set the <code>steamid</code> parameter IN value to the routine
     */
    public void setSteamid(String value) {
        setValue(STEAMID, value);
    }

    /**
     * Set the <code>steamid</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setSteamid(Field<String> field) {
        setField(STEAMID, field);
    }

    /**
     * Set the <code>nullable</code> parameter IN value to the routine
     */
    public void setNullable(Byte value) {
        setValue(NULLABLE, value);
    }

    /**
     * Set the <code>nullable</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setNullable(Field<Byte> field) {
        setField(NULLABLE, field);
    }

    /**
     * Set the <code>only_legal</code> parameter IN value to the routine
     */
    public void setOnlyLegal(Byte value) {
        setValue(ONLY_LEGAL, value);
    }

    /**
     * Set the <code>only_legal</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public void setOnlyLegal(Field<Byte> field) {
        setField(ONLY_LEGAL, field);
    }
}
