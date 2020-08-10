/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.routines;


import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.types.UInteger;
import ru.csdm.stats.common.model.Csstats;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PlayerDetailJson extends AbstractRoutine<java.lang.Void> {

    private static final long serialVersionUID = -1132195682;

    /**
     * The parameter <code>csstats.player_detail_json.id</code>.
     */
    public static final Parameter<UInteger> ID = Internal.createParameter("id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, false, false);

    /**
     * The parameter <code>csstats.player_detail_json.name</code>.
     */
    public static final Parameter<String> NAME = Internal.createParameter("name", org.jooq.impl.SQLDataType.VARCHAR(31), false, false);

    /**
     * The parameter <code>csstats.player_detail_json.ip</code>.
     */
    public static final Parameter<String> IP = Internal.createParameter("ip", org.jooq.impl.SQLDataType.VARCHAR(15), false, false);

    /**
     * The parameter <code>csstats.player_detail_json.steamid</code>.
     */
    public static final Parameter<String> STEAMID = Internal.createParameter("steamid", org.jooq.impl.SQLDataType.VARCHAR(22), false, false);

    /**
     * Create a new routine call instance
     */
    public PlayerDetailJson() {
        super("player_detail_json", Csstats.CSSTATS);

        addInParameter(ID);
        addInParameter(NAME);
        addInParameter(IP);
        addInParameter(STEAMID);
    }

    /**
     * Set the <code>id</code> parameter IN value to the routine
     */
    public void setId(UInteger value) {
        setValue(ID, value);
    }

    /**
     * Set the <code>name</code> parameter IN value to the routine
     */
    public void setName_(String value) {
        setValue(NAME, value);
    }

    /**
     * Set the <code>ip</code> parameter IN value to the routine
     */
    public void setIp(String value) {
        setValue(IP, value);
    }

    /**
     * Set the <code>steamid</code> parameter IN value to the routine
     */
    public void setSteamid(String value) {
        setValue(STEAMID, value);
    }
}
