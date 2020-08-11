package ru.csdm.stats.modules.collector.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.csdm.stats.dao.ViewsDao;

import java.util.Optional;

@RestController
@RequestMapping("/stats")
@Slf4j
public class ViewsEndpoint {
    @Autowired
    private ViewsDao viewsDao;

    /**
     * http://127.0.0.1:8890/stats/player/summary?name=Chris Dakota
     * http://127.0.0.1:8890/stats/player/summary?name=%3A%3A%5B%40A%40%5D%3A%3A***%3A%3AMr.%5BH%5DoLm%5BS%5D%3A%3A
     * http://127.0.0.1:8890/stats/player/summary?ip=127.0.0.1&page=1&per_page=40
     * http://127.0.0.1:8890/stats/player/summary?steamid=STEAM_0%3A0%3A123456
     * http://127.0.0.1:8890/stats/player/summary?id=4567
     * http://127.0.0.1:8890/stats/player/summary
     * http://127.0.0.1:8890/stats/player/summary?page=1&per_page=40
     */
    @GetMapping(value = "/player/summary", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('viewer')")
    public String summary(
            @RequestParam(required = false) Optional<Long> id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false, value = "steamid") String steamId,
            @RequestParam(required = false, defaultValue = "1") long page,
            @RequestParam(required = false, value = "per_page", defaultValue = "40") long perPage
    ) {
        return viewsDao.summary(id, name, ip, steamId, page, perPage);
    }

    /**
     * http://127.0.0.1:8890/stats/player/detail?name=Chris Dakota
     * http://127.0.0.1:8890/stats/player/detail?name=%3A%3A%5B%40A%40%5D%3A%3A***%3A%3AMr.%5BH%5DoLm%5BS%5D%3A%3A
     * http://127.0.0.1:8890/stats/player/detail?ip=127.0.0.1&page=1&per_page=40
     * http://127.0.0.1:8890/stats/player/detail?steamid=STEAM_0%3A0%3A123456
     * http://127.0.0.1:8890/stats/player/detail?id=4567
     * http://127.0.0.1:8890/stats/player/detail
     * http://127.0.0.1:8890/stats/player/detail?page=1&per_page=40
     */
    @GetMapping(value = "/player/detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('viewer')")
    public String detail(
            @RequestParam(required = false) Optional<Long> id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false, value = "steamid") String steamId,
            @RequestParam(required = false, defaultValue = "1") long page,
            @RequestParam(required = false, value = "per_page", defaultValue = "40") long perPage
    ) {
        return viewsDao.detail(id, name, ip, steamId, page, perPage);
    }

    /**
     * http://127.0.0.1:8890/stats/player/full?name=Chris Dakota
     * http://127.0.0.1:8890/stats/player/full?name=%3A%3A%5B%40A%40%5D%3A%3A***%3A%3AMr.%5BH%5DoLm%5BS%5D%3A%3A
     * http://127.0.0.1:8890/stats/player/full?ip=127.0.0.1&page=1&per_page=40
     * http://127.0.0.1:8890/stats/player/full?steamid=STEAM_0%3A0%3A123456
     * http://127.0.0.1:8890/stats/player/full?id=4567
     * http://127.0.0.1:8890/stats/player/full
     * http://127.0.0.1:8890/stats/player/full?page=1&per_page=40
     */
    @GetMapping(value = "/player/full", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('viewer')")
    public String full(
            @RequestParam(required = false) Optional<Long> id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false, value = "steamid") String steamId,
            @RequestParam(required = false, defaultValue = "1") long page,
            @RequestParam(required = false, value = "per_page", defaultValue = "40") long perPage
    ) {
        return viewsDao.full(id, name, ip, steamId, page, perPage);
    }

    /**
     * http://127.0.0.1:8890/stats/player/full2?name=Chris Dakota
     * http://127.0.0.1:8890/stats/player/full2?name=%3A%3A%5B%40A%40%5D%3A%3A***%3A%3AMr.%5BH%5DoLm%5BS%5D%3A%3A
     * http://127.0.0.1:8890/stats/player/full2?ip=127.0.0.1&page=1&per_page=40
     * http://127.0.0.1:8890/stats/player/full2?steamid=STEAM_0%3A0%3A123456
     * http://127.0.0.1:8890/stats/player/full2?id=4567
     * http://127.0.0.1:8890/stats/player/full2
     * http://127.0.0.1:8890/stats/player/full2?page=1&per_page=40
     */
    @GetMapping(value = "/player/full2", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('viewer')")
    public String full2(
            @RequestParam(required = false) Optional<Long> id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false, value = "steamid") String steamId,
            @RequestParam(required = false, defaultValue = "1") long page,
            @RequestParam(required = false, value = "per_page", defaultValue = "40") long perPage
    ) {
        return viewsDao.full2(id, name, ip, steamId, page, perPage);
    }
}

































