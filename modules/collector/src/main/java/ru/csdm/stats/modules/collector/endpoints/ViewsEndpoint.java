package ru.csdm.stats.modules.collector.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.csdm.stats.ViewsDao;
import ru.csdm.stats.common.dto.view.PlayerSummary;

@RestController
@RequestMapping("/stats")
@Slf4j
public class ViewsEndpoint {
    @Autowired
    private ViewsDao viewsDao;

    /**
     * http://127.0.0.1:8890/stats/player?name=%3A%3A%5B%40A%40%5D%3A%3A***%3A%3AMr.%5BH%5DoLm%5BS%5D%3A%3A
{
    "id": 5409,
    "name": "::[@A@]::***::Mr.[H]oLm[S]::",
    "kills": 324000,
    "deaths": 583201,
    "gaming_time": "5мес",
    "rank_name": "Ст.охранник",
    "stars": "★★★☆☆☆"
} */
    @GetMapping(value = "/player", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('viewer')")
    public PlayerSummary playerSummary(@RequestParam String name) {
        return viewsDao.playerSummary(name);
    }
}
