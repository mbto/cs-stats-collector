package ru.csdm.stats.dao;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.csdm.stats.common.model.routines.*;

import java.util.Optional;

@Repository
public class ViewsDao {
    @Autowired
    private DSLContext statsDsl;

    public String summary(Optional<Long> id, String name, String ip, String steamId, long page, long perPage) {
        Playersummaryjsonagg routine = new Playersummaryjsonagg();
        routine.setId(id.map(UInteger::valueOf).orElse(null));
        routine.setName_(name);
        routine.setIp(ip);
        routine.setSteamid(steamId);
        routine.setPage(UInteger.valueOf(page));
        routine.setPerPage(UInteger.valueOf(perPage));
        routine.execute(statsDsl.configuration());

        return (String) routine.getResults().resultsOrRows().get(0).result().getValue(0, 0);
    }

    public String detail(Optional<Long> id, String name, String ip, String steamId, long page, long perPage) {
        Playerdetailjsonagg routine = new Playerdetailjsonagg();
        routine.setId(id.map(UInteger::valueOf).orElse(null));
        routine.setName_(name);
        routine.setIp(ip);
        routine.setSteamid(steamId);
        routine.setPage(UInteger.valueOf(page));
        routine.setPerPage(UInteger.valueOf(perPage));
        routine.execute(statsDsl.configuration());

        return (String) routine.getResults().resultsOrRows().get(0).result().getValue(0, 0);
    }

    public String full(Optional<Long> id, String name, String ip, String steamId, long page, long perPage) {
        Playerfulljsonagg routine = new Playerfulljsonagg();
        routine.setId(id.map(UInteger::valueOf).orElse(null));
        routine.setName_(name);
        routine.setIp(ip);
        routine.setSteamid(steamId);
        routine.setPage(UInteger.valueOf(page));
        routine.setPerPage(UInteger.valueOf(perPage));
        routine.execute(statsDsl.configuration());

        return (String) routine.getResults().resultsOrRows().get(0).result().getValue(0, 0);
    }

    public String full2(Optional<Long> id, String name, String ip, String steamId, long page, long perPage) {
        Playerfull2jsonagg routine = new Playerfull2jsonagg();
        routine.setId(id.map(UInteger::valueOf).orElse(null));
        routine.setName_(name);
        routine.setIp(ip);
        routine.setSteamid(steamId);
        routine.setPage(UInteger.valueOf(page));
        routine.setPerPage(UInteger.valueOf(perPage));
        routine.execute(statsDsl.configuration());

        return (String) routine.getResults().resultsOrRows().get(0).result().getValue(0, 0);
    }

    public String history(Optional<Long> id, String name, String ip, String steamId,
                          long page, long perPage, long historyLimit) {
        Playerhistoryjsonagg routine = new Playerhistoryjsonagg();
        routine.setId(id.map(UInteger::valueOf).orElse(null));
        routine.setName_(name);
        routine.setIp(ip);
        routine.setSteamid(steamId);
        routine.setPage(UInteger.valueOf(page));
        routine.setPerPage(UInteger.valueOf(perPage));
        routine.setHistoryLimit(UInteger.valueOf(historyLimit));
        routine.execute(statsDsl.configuration());

        Result<Record> result = routine.getResults().resultsOrRows().get(0).result();

        // MySQL procedure PlayerHistoryJsonAgg not return a default value, like {"results": [], "count_total": 0}
        // as in other procedures, no time to deal with this. MySQL works bad with JSON, unlike PostgreSQL.
        if(result.isEmpty())
            return "{\"results\": [], \"count_total\": 0}";

        return (String) result.getValue(0, 0);
    }

    // routine.getResults().resultsOrRows().get(0).result().getValue(0, 0 /*"results"*/)
    // routine.getResults().resultsOrRows().get(0).result().intoArray(0, String.class);
}