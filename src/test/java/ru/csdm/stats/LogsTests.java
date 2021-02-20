package ru.csdm.stats;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.csdm.stats.common.dto.ServerData;
import ru.csdm.stats.common.model.collector.tables.pojos.Project;
import ru.csdm.stats.common.model.collector.tables.records.KnownServerRecord;
import ru.csdm.stats.common.model.csstats.tables.pojos.Player;
import ru.csdm.stats.common.model.csstats.tables.pojos.PlayerIp;
import ru.csdm.stats.common.model.csstats.tables.pojos.PlayerSteamid;
import ru.csdm.stats.service.EventService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static ru.csdm.stats.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static ru.csdm.stats.common.SystemEvent.FLUSH_FROM_FRONTEND;
import static ru.csdm.stats.common.model.collector.tables.DriverProperty.DRIVER_PROPERTY;
import static ru.csdm.stats.common.model.collector.tables.Instance.INSTANCE;
import static ru.csdm.stats.common.model.collector.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.collector.tables.Project.PROJECT;
import static ru.csdm.stats.common.model.csstats.Tables.PLAYER;
import static ru.csdm.stats.common.utils.SomeUtils.timezoneEnumByLiteral;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DependsOn("brokerTE")
@Slf4j
public class LogsTests {
    @Autowired
    private EventService eventService;
    @Autowired
    private DSLContext collectorAdminDsl;
    @Autowired
    private LogsSender logsSender;
    @Autowired
    private ProjectMaker projectMaker;
    @Autowired
    private Map<String, ServerData> serverDataByAddress;

    @BeforeClass
    public static void beforeClass() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void beforeTest() {
        truncateTables();
    }

    @After
    public void afterTest() {
//        truncateTables();
    }

    @Test
    public void truncateOnly() {
        truncateTables();
    }

    public Project buildDefaultProject(String projectName, String projectSchema) {
        Project project = new Project();
        project.setName(projectName);
        project.setDatabaseHostport("127.0.0.1:3306");
        project.setDatabaseSchema(projectSchema);
        project.setDatabaseUsername("stats_tester"); /* grants same as `stats`, but with TRUNCATE */
        project.setDatabasePassword("stats_tester");
        project.setDatabaseServerTimezone(timezoneEnumByLiteral.get("Europe/Moscow"));
        return project;
    }

    @Test
    public void server1_27015_27015() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27015);
        });
        
        assertPlayers(projectMaker, new String[][] {
                {"2", "Name2", "0", "11", "66", "1", "2020-01-01 13:16:08", "Test server 127.0.0.1:27015"}, // 1m 6s
                {"1", "Name1", "10", "1", "10", "1", "2020-01-01 13:16:07", "Test server 127.0.0.1:27015"} // 10s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void another_project_server1_27015_27015() {
        Project project = buildDefaultProject("Another CS project", "csstats_another_project");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"2", "Name2", "0", "11", "66", "1", "2020-01-01 13:16:08", "Test server 127.0.0.1:27015"}, // 1m 6s
                {"1", "Name1", "10", "1", "10", "1", "2020-01-01 13:16:07", "Test server 127.0.0.1:27015"} // 10s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server1_27015_27015_with_changing_names() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1_changing_names.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"2", "Name2", "0", "12", "69", "1", "2020-01-01 13:16:11", "Test server 127.0.0.1:27015"}, // 1m 9s
                {"1", "Name1", "5", "0", "10", "1", "2020-01-01 13:16:10", "Test server 127.0.0.1:27015"}, // 10s
                {"4", "Name9", "4", "0", "6", "1", "2020-01-01 13:16:08", "Test server 127.0.0.1:27015"}, // 6s
                {"3", "Name5", "2", "1", "4", "1", "2020-01-01 13:15:08", "Test server 127.0.0.1:27015"} // 4s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "24.24.24.24", "2020-01-01 13:16:10"},
                {"2", "1", "12.12.12.12", "2020-01-01 13:16:10"},
                {"3", "3", "12.12.12.12", "2020-01-01 13:15:08"},
                {"4", "4", "24.24.24.24", "2020-01-01 13:16:08"},
                {"5", "4", "12.12.12.12", "2020-01-01 13:16:08"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:123123123123", "2020-01-01 13:16:10"},
                {"2", "1", "STEAM_0:0:999999999999", "2020-01-01 13:16:10"},
                {"3", "2", "STEAM_0:1:987654", "2020-01-01 13:16:11"},
                {"4", "3", "STEAM_0:0:123123123123", "2020-01-01 13:15:08"},
                {"5", "4", "STEAM_0:0:123123123123", "2020-01-01 13:16:08"},
                {"6", "4", "STEAM_0:0:999999999999", "2020-01-01 13:16:08"}
        });
    }

    @Test
    public void server1_27015_27015_max_ips_steamids() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1_max_ips_steamids.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Name1", "0", "0", "34", "1", "2020-01-01 13:15:50", "Test server 127.0.0.1:27015"} // 34s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"3", "1", "127.0.0.10", "2020-01-01 13:15:50"},
                {"4", "1", "127.0.0.5", "2020-01-01 13:15:50"},
                {"5", "1", "127.0.0.4", "2020-01-01 13:15:50"},
                {"6", "1", "127.0.0.13", "2020-01-01 13:15:50"},
                {"7", "1", "127.0.0.14", "2020-01-01 13:15:50"},
                {"8", "1", "127.0.0.9", "2020-01-01 13:15:50"},
                {"9", "1", "127.0.0.11", "2020-01-01 13:15:50"},
                {"10", "1", "127.0.0.8", "2020-01-01 13:15:50"},
                {"11", "1", "127.0.0.12", "2020-01-01 13:15:50"},
                {"12", "1", "127.0.0.17", "2020-01-01 13:15:50"},
                {"13", "1", "127.0.0.15", "2020-01-01 13:15:50"},
                {"14", "1", "127.0.0.16", "2020-01-01 13:15:50"},
                {"15", "1", "127.0.0.3", "2020-01-01 13:15:50"},
                {"16", "1", "127.0.0.2", "2020-01-01 13:15:50"},
                {"17", "1", "127.0.0.1", "2020-01-01 13:15:50"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"3", "1", "STEAM_0:0:000008", "2020-01-01 13:15:50"},
                {"4", "1", "STEAM_0:0:000005", "2020-01-01 13:15:50"},
                {"5", "1", "STEAM_0:0:000016", "2020-01-01 13:15:50"},
                {"6", "1", "STEAM_0:0:000006", "2020-01-01 13:15:50"},
                {"7", "1", "STEAM_0:0:000017", "2020-01-01 13:15:50"},
                {"8", "1", "STEAM_0:0:000003", "2020-01-01 13:15:50"},
                {"9", "1", "STEAM_0:0:000014", "2020-01-01 13:15:50"},
                {"10", "1", "STEAM_0:0:000004", "2020-01-01 13:15:50"},
                {"11", "1", "STEAM_0:0:000015", "2020-01-01 13:15:50"},
                {"12", "1", "STEAM_0:0:000001", "2020-01-01 13:15:50"},
                {"13", "1", "STEAM_0:0:000012", "2020-01-01 13:15:50"},
                {"14", "1", "STEAM_0:0:000002", "2020-01-01 13:15:50"},
                {"15", "1", "STEAM_0:0:000013", "2020-01-01 13:15:50"},
                {"16", "1", "STEAM_0:0:000010", "2020-01-01 13:15:50"},
                {"17", "1", "STEAM_0:0:000011", "2020-01-01 13:15:50"}
        });
    }

    @Test
    public void server4_27016_27016() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27016, 27016, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27016, 27016);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "17", "11", "449", "1", "2020-01-01 21:25:07", "Test server 127.0.0.1:27016"}, // 7m 29s
                {"4", "yeppi", "11", "21", "443", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 23s
                {"5", "sonic", "10", "16", "443", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 23s
                {"8", "wRa1 wRa1", "13", "17", "440", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 20s
                {"9", "showw", "21", "14", "438", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 18s
                {"2", "pravwOw~", "8", "22", "438", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 18s
                {"15", "BoBka’)", "8", "11", "438", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 18s
                {"10", "haaimbat", "14", "19", "435", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 15s
                {"7", "BatalOOl", "12", "10", "435", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 15s
                {"14", "KaRJlSoH", "20", "10", "434", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 14s
                {"16", "nameasd", "18", "10", "434", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 14s
                {"13", "[52 xemaike2h blanil", "14", "17", "422", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 7m 2s
                {"11", "Currv", "20", "16", "417", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 6m 57s
                {"3", "aromaken1", "14", "16", "415", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 6m 55s
                {"12", "~kewAw0w~~", "13", "17", "400", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"}, // 6m 40s
                {"6", "castzOr", "14", "16", "395", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27016"} // 6m 35s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server1_27015_27016_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27016, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27016);
        }, Arrays.asList(PLAYER.LAST_SERVER_NAME));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */

        assertPlayers(projectMaker, new String[][] {
                {"2", "Name2", "0", "22", "132", "1", "2020-01-01 13:16:08", null}, // 2m 12s
                {"1", "Name1", "20", "2", "20", "1", "2020-01-01 13:16:07", null} // 20s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server1_27015_27016_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27016, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server1.log", 27015, 27016);
        }, Arrays.asList(PLAYER.LAST_SERVER_NAME));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */

        assertPlayers(projectMaker, new String[][] {
                {"2", "Name2", "0", "22", "132", "1", "2020-01-01 13:16:08", null}, // 2m 12s
                {"1", "Name1", "20", "2", "28", "1", "2020-01-01 13:16:07", null} // 28s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server2_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server2.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "7", "5", "103", "1", "2020-01-01 20:58:38", "Test server 127.0.0.1:27015"}, // 1m 43s
                {"4", "cusoma", "0", "8", "89", "1", "2020-01-01 20:52:10", "Test server 127.0.0.1:27015"}, // 1m 29s
                {"3", "timoxatw", "5", "1", "76", "1", "2020-01-01 20:52:10", "Test server 127.0.0.1:27015"}, // 1m 16s
                {"2", "no kill", "3", "2", "51", "1", "2020-01-01 20:52:10", "Test server 127.0.0.1:27015"} // 51s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server2_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server2.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "7", "5", "221", "1", "2020-01-01 20:58:38", "Test server 127.0.0.1:27015"}, // 3m 41s
                {"2", "no kill", "3", "2", "113", "1", "2020-01-01 20:52:10", "Test server 127.0.0.1:27015"}, // 1m 53s
                {"3", "timoxatw", "5", "1", "110", "1", "2020-01-01 20:52:10", "Test server 127.0.0.1:27015"}, // 1m 50s
                {"4", "cusoma", "0", "8", "104", "1", "2020-01-01 20:52:10", "Test server 127.0.0.1:27015"} // 1m 44s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server3_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server3.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"2", "cusoma", "0", "1", "95", "1", "2020-01-01 20:50:41", "Test server 127.0.0.1:27015"}, // 1m 35s
                {"1", "Admin", "1", "0", "94", "1", "2020-01-01 20:52:15", "Test server 127.0.0.1:27015"} // 1m 34s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server3_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server3.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "1", "0", "127", "1", "2020-01-01 20:52:15", "Test server 127.0.0.1:27015"}, // 2m 7s
                {"2", "no kill", "0", "0", "119", "1", "2020-01-01 20:50:17", "Test server 127.0.0.1:27015"}, // 1m 59s
                {"3", "timoxatw", "0", "0", "116", "1", "2020-01-01 20:50:20", "Test server 127.0.0.1:27015"}, // 1m 56s
                {"4", "cusoma", "0", "1", "110", "1", "2020-01-01 20:50:41", "Test server 127.0.0.1:27015"} // 1m 50s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server4_27015_27015() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "17", "11", "449", "1", "2020-01-01 21:25:07", "Test server 127.0.0.1:27015"}, // 7m 29s
                {"4", "yeppi", "11", "21", "443", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 23s
                {"5", "sonic", "10", "16", "443", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 23s
                {"8", "wRa1 wRa1", "13", "17", "440", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 20s
                {"9", "showw", "21", "14", "438", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 18s
                {"2", "pravwOw~", "8", "22", "438", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 18s
                {"15", "BoBka’)", "8", "11", "438", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 18s
                {"10", "haaimbat", "14", "19", "435", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 15s
                {"7", "BatalOOl", "12", "10", "435", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 15s
                {"14", "KaRJlSoH", "20", "10", "434", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 14s
                {"16", "nameasd", "18", "10", "434", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 14s
                {"13", "[52 xemaike2h blanil", "14", "17", "422", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 7m 2s
                {"11", "Currv", "20", "16", "417", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 6m 57s
                {"3", "aromaken1", "14", "16", "415", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 6m 55s
                {"12", "~kewAw0w~~", "13", "17", "400", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"}, // 6m 40s
                {"6", "castzOr", "14", "16", "395", "1", "2020-01-01 21:24:58", "Test server 127.0.0.1:27015"} // 6m 35s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server4_27015_27025() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27025, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27025);
        }, Arrays.asList(PLAYER.LAST_SERVER_NAME));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "187", "121", "4939", "3", "2020-01-01 21:25:07", null}, // 1h 22m 19s
                {"4", "yeppi", "121", "231", "4873", "1", "2020-01-01 21:24:58", null}, // 1h 21m 13s
                {"5", "sonic", "110", "176", "4873", "1", "2020-01-01 21:24:58", null}, // 1h 21m 13s
                {"8", "wRa1 wRa1", "143", "187", "4840", "1", "2020-01-01 21:24:58", null}, // 1h 20m 40s
                {"9", "showw", "231", "154", "4818", "4", "2020-01-01 21:24:58", null}, // 1h 20m 18s
                {"2", "pravwOw~", "88", "242", "4818", "1", "2020-01-01 21:24:58", null}, // 1h 20m 18s
                {"15", "BoBka’)", "88", "121", "4818", "1", "2020-01-01 21:24:58", null}, // 1h 20m 18s
                {"10", "haaimbat", "154", "209", "4785", "1", "2020-01-01 21:24:58", null}, // 1h 19m 45s
                {"7", "BatalOOl", "132", "110", "4785", "1", "2020-01-01 21:24:58", null}, // 1h 19m 45s
                {"14", "KaRJlSoH", "220", "110", "4774", "5", "2020-01-01 21:24:58", null}, // 1h 19m 34s
                {"16", "nameasd", "198", "110", "4774", "4", "2020-01-01 21:24:58", null}, // 1h 19m 34s
                {"13", "[52 xemaike2h blanil", "154", "187", "4642", "1", "2020-01-01 21:24:58", null}, // 1h 17m 22s
                {"11", "Currv", "220", "176", "4587", "2", "2020-01-01 21:24:58", null}, // 1h 16m 27s
                {"3", "aromaken1", "154", "176", "4565", "1", "2020-01-01 21:24:58", null}, // 1h 16m 5s
                {"12", "~kewAw0w~~", "143", "187", "4400", "1", "2020-01-01 21:24:58", null}, // 1h 13m 20s
                {"6", "castzOr", "154", "176", "4345", "1", "2020-01-01 21:24:58", null} // 1h 12m 25s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "2", "0", "20", "1", "2020-01-01 23:42:21", "Test server 127.0.0.1:27015"}, // 20s
                {"2", "CeHb^Oaa", "0", "2", "20", "1", "2020-01-01 23:42:21", "Test server 127.0.0.1:27015"} // 20s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:42:21"}
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "2", "0", "76", "1", "2020-01-01 23:42:21", "Test server 127.0.0.1:27015"}, // 1m 16s
                {"4", "CeHb^Oaa", "0", "2", "51", "1", "2020-01-01 23:42:21", "Test server 127.0.0.1:27015"}, // 51s
                {"2", "FENIX2H", "0", "0", "8", "1", "2020-01-01 23:41:22", "Test server 127.0.0.1:27015"}, // 8s
                {"3", "relish -w 800", "0", "0", "3", "1", "2020-01-01 23:41:27", "Test server 127.0.0.1:27015"} // 3s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:42:21"}
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, false, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, false, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "0", "0", "76", "1", "2020-01-01 23:42:21", "Test server 127.0.0.1:27015"}, // 1m 16s
                {"4", "CeHb^Oaa", "0", "0", "51", "1", "2020-01-01 23:42:21", "Test server 127.0.0.1:27015"}, // 51s
                {"2", "FENIX2H", "0", "0", "8", "1", "2020-01-01 23:41:22", "Test server 127.0.0.1:27015"}, // 8s
                {"3", "relish -w 800", "0", "0", "3", "1", "2020-01-01 23:41:27", "Test server 127.0.0.1:27015"} // 3s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:42:21"}
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "4", "0", "46", "1", "2020-01-01 23:45:56", "Test server 127.0.0.1:27015"}, // 46s
                {"2", "desch", "0", "4", "46", "1", "2020-01-01 23:45:56", "Test server 127.0.0.1:27015"} // 46s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "255.0.0.142", "2020-01-01 23:45:56"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:45:56"}
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "4", "0", "101", "1", "2020-01-01 23:45:56", "Test server 127.0.0.1:27015"}, // 1m 41s
                {"2", "desch", "0", "4", "88", "1", "2020-01-01 23:45:56", "Test server 127.0.0.1:27015"} // 1m 28s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "255.0.0.142", "2020-01-01 23:45:56"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:45:56"}
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, false, false, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action_no_ffa() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, false, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("no_ffa.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "0", "0", "101", "1", "2020-01-01 23:45:56", "Test server 127.0.0.1:27015"}, // 1m 41s
                {"2", "desch", "0", "0", "88", "1", "2020-01-01 23:45:56", "Test server 127.0.0.1:27015"} // 1m 28s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "255.0.0.142", "2020-01-01 23:45:56"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:45:56"}
        });
    }

    @Test
    public void server4_27015_27017_start_session_on_action_ignore_bots() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27025, true, true, true, true);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27017);
        });

        assertPlayers(projectMaker, new String[][] {
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server4_27015_27015_dont_start_session_on_action_ignore_bots() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27015, true, true, true, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27015);
        });

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "0", "0", "598", "1", "2020-01-01 21:25:07", "Test server 127.0.0.1:27015"} // 9m 58s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server4_27015_27017_dont_start_session_on_action_ignore_bots() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27025, true, true, true, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4.log", 27015, 27017);
        }, Arrays.asList(PLAYER.LAST_SERVER_NAME));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "0", "0", "1794", "1", "2020-01-01 21:25:07", null} // 29m 54s
        });
        assertPlayersIps(projectMaker, new String[][] {
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
        });
    }

    @Test
    public void server4_manual_flush_27014_27018_dont_start_session_on_action() {
        Project project = buildDefaultProject("Default CS project", "csstats");

        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            UInteger instanceId = addInstance(transactionalDsl, "instance_1");
            addProject(transactionalDsl, project);
            addKnownServer(transactionalDsl, instanceId, project.getId(), 27015, 27017, true, true, false, false);
        });

        eventService.refresh(project.getId());

        projectMaker.process(project, () -> {
            logsSender.sendLogs("server4_only_load.log", 27014, 27018);

            for (String address : serverDataByAddress.keySet()) {
                try {
                    eventService.flush(address, FLUSH_FROM_FRONTEND, false);

                    log.info("Flush " + address + " registered");
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }, Arrays.asList(PLAYER.LAST_SERVER_NAME));
        /* logs sends in parallel, so PLAYER.LAST_SERVER_NAME is undefined */

        assertPlayers(projectMaker, new String[][] {
                {"1", "Admin", "51", "33", "1767", "1", "2020-01-01 21:23:52", null}, // 29m 27s
                {"2", "pravwOw~", "24", "66", "1350", "1", "2020-01-01 21:24:57", null}, // 22m 30s
                {"3", "aromaken1", "42", "48", "1347", "1", "2020-01-01 21:24:47", null}, // 22m 27s
                {"4", "yeppi", "33", "63", "1347", "1", "2020-01-01 21:24:47", null}, // 22m 27s
                {"6", "castzOr", "42", "48", "1344", "1", "2020-01-01 21:24:47", null}, // 22m 24s
                {"5", "sonic", "30", "48", "1344", "1", "2020-01-01 21:24:47", null}, // 22m 24s
                {"8", "wRa1 wRa1", "39", "51", "1341", "1", "2020-01-01 21:24:47", null}, // 22m 21s
                {"7", "BatalOOl", "36", "30", "1341", "1", "2020-01-01 21:24:47", null}, // 22m 21s
                {"9", "showw", "63", "42", "1338", "1", "2020-01-01 21:24:57", null}, // 22m 18s
                {"10", "haaimbat", "42", "57", "1338", "1", "2020-01-01 21:24:58", null}, // 22m 18s
                {"11", "Currv", "60", "48", "1335", "1", "2020-01-01 21:24:47", null}, // 22m 15s
                {"12", "~kewAw0w~~", "39", "51", "1335", "1", "2020-01-01 21:24:47", null}, // 22m 15s
                {"14", "KaRJlSoH", "60", "30", "1332", "2", "2020-01-01 21:24:57", null}, // 22m 12s
                {"13", "[52 xemaike2h blanil", "42", "51", "1332", "1", "2020-01-01 21:24:47", null}, // 22m 12s
                {"16", "nameasd", "54", "30", "1329", "1", "2020-01-01 21:24:58", null}, // 22m 9s
                {"15", "BoBka’)", "24", "33", "1329", "1", "2020-01-01 21:24:57", null} // 22m 9s
        });
        assertPlayersIps(projectMaker, new String[][] {
                {"1", "1", "127.0.1.1", "2020-01-01 21:23:52"}
        });
        assertPlayersSteamIds(projectMaker, new String[][] {
                {"1", "1", "STEAM_0:0:555000", "2020-01-01 21:23:52"}
        });
    }

    private void assertPlayers(ProjectMaker projectMaker,
                               String[][] expectedRaw) {
        List<Player> actualData = projectMaker.getPlayers();

        List<Player> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayerFromRaw)
                .collect(Collectors.toList());

        assertEquals(actualData, expectedData);
    }

    private void assertPlayersIps(ProjectMaker projectMaker, String[][] expectedRaw) {
        List<PlayerIp> actualData = projectMaker.getPlayersIps();

        List<PlayerIp> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayersIpsFromRaw)
                .collect(Collectors.toList());

        assertEquals(actualData, expectedData);
    }

    private void assertPlayersSteamIds(ProjectMaker projectMaker, String[][] expectedRaw) {
        List<PlayerSteamid> actualData = projectMaker.getPlayerSteamIds();

        List<PlayerSteamid> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayersSteamIdsFromRaw)
                .collect(Collectors.toList());

        assertEquals(actualData, expectedData);
    }

    private UInteger addInstance(DSLContext transactionalDsl, String instanceName) {
        log.info("Add instance name: " + instanceName);
        return transactionalDsl.insertInto(INSTANCE)
                .set(INSTANCE.NAME, instanceName)
                .returning(INSTANCE.ID).fetchOne().getId();
    }

    private void addProject(DSLContext transactionalDsl, Project project) {
        log.info("Add project name: " + project.getName() + ", hostport: " + project.getDatabaseHostport() + ", schema: " + project.getDatabaseSchema());
        transactionalDsl.insertInto(PROJECT)
                .set(PROJECT.NAME, project.getName())
                .set(PROJECT.DATABASE_HOSTPORT, project.getDatabaseHostport())
                .set(PROJECT.DATABASE_SCHEMA, project.getDatabaseSchema())
                .set(PROJECT.DATABASE_USERNAME, project.getDatabaseUsername())
                .set(PROJECT.DATABASE_PASSWORD, project.getDatabasePassword())
                .set(PROJECT.DATABASE_SERVER_TIMEZONE, project.getDatabaseServerTimezone())
                .returning().fetchOne().into(project);
    }

    private void addKnownServer(DSLContext transactionalDsl,
                                UInteger instanceId,
                                UInteger projectId,
                                int portStart,
                                int portEnd,
                                boolean active,
                                boolean ffa,
                                boolean ignore_bots,
                                boolean start_session_on_action) {

        List<InsertSetMoreStep<KnownServerRecord>> insertSteps = new ArrayList<>(portEnd - portStart + 1);

        for (int port = portStart; port <= portEnd; port++) {
            String ipport = "127.0.0.1:" + port;
            log.info("Add address " + ipport);

            InsertSetMoreStep<KnownServerRecord> insertStep = DSL.insertInto(KNOWN_SERVER)
                    .set(KNOWN_SERVER.INSTANCE_ID, instanceId)
                    .set(KNOWN_SERVER.PROJECT_ID, projectId)
                    .set(KNOWN_SERVER.IPPORT, ipport)
                    .set(KNOWN_SERVER.NAME, "Test server " + ipport)
                    .set(KNOWN_SERVER.ACTIVE, active)
                    .set(KNOWN_SERVER.FFA, ffa)
                    .set(KNOWN_SERVER.IGNORE_BOTS, ignore_bots)
                    .set(KNOWN_SERVER.START_SESSION_ON_ACTION, start_session_on_action);

            insertSteps.add(insertStep);
        }

        transactionalDsl.batch(insertSteps).execute();
    }

    public void truncateTables() {
        collectorAdminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            try {
                transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 0;");

                transactionalDsl.truncate(INSTANCE).execute();
                transactionalDsl.truncate(PROJECT).execute();
                transactionalDsl.truncate(KNOWN_SERVER).execute();
                transactionalDsl.truncate(DRIVER_PROPERTY).execute();
            } finally {
                transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 1;");
            }
        });
    }

    /**
     * {"2", "Name2", "0", "11", "66", "1", "2020-01-01 13:16:08", "Test server 127.0.0.1:27015"}, // 1m 6s
     * {"1", "Name1", "10", "1", "10", "1", "2020-01-01 13:16:07", "Test server 127.0.0.1:27015"} // 10s
     */
    private Player makePlayerFromRaw(String[] sourceRaw) {
        Player player = new Player();
        player.setId(UInteger.valueOf(sourceRaw[0]));
        player.setName(sourceRaw[1]);
        player.setKills(UInteger.valueOf(sourceRaw[2]));
        player.setDeaths(UInteger.valueOf(sourceRaw[3]));
        player.setTimeSecs(UInteger.valueOf(sourceRaw[4]));

        if(StringUtils.isNoneBlank(sourceRaw[5]))
            player.setRankId(UInteger.valueOf(sourceRaw[5]));

        if(StringUtils.isNoneBlank(sourceRaw[6]))
            player.setLastseenDatetime(LocalDateTime.parse(sourceRaw[6], YYYYMMDD_HHMMSS_PATTERN));

        if(StringUtils.isNoneBlank(sourceRaw[7]))
            player.setLastServerName(sourceRaw[7]);

        return player;
    }

    /**
     * {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"},
     * {"2", "1", "23.23.23.23", "2020-01-01 13:20:10"}
     */
    private PlayerIp makePlayersIpsFromRaw(String[] sourceRaw) {
        PlayerIp playerIp = new PlayerIp();
        playerIp.setId(UInteger.valueOf(sourceRaw[0]));
        playerIp.setPlayerId(UInteger.valueOf(sourceRaw[1]));
        playerIp.setIp(sourceRaw[2]);
        playerIp.setRegDatetime(LocalDateTime.parse(sourceRaw[3], YYYYMMDD_HHMMSS_PATTERN));

        return playerIp;
    }

    /**
     * {"1", "1", "STEAM_0:0:123123123", "2020-01-01 13:16:07"},
     * {"2", "1", "STEAM_0:0:456456456", "2020-01-01 13:20:10"}
     */
    private PlayerSteamid makePlayersSteamIdsFromRaw(String[] sourceRaw) {
        PlayerSteamid playerSteamid = new PlayerSteamid();
        playerSteamid.setId(UInteger.valueOf(sourceRaw[0]));
        playerSteamid.setPlayerId(UInteger.valueOf(sourceRaw[1]));
        playerSteamid.setSteamid(sourceRaw[2]);
        playerSteamid.setRegDatetime(LocalDateTime.parse(sourceRaw[3], YYYYMMDD_HHMMSS_PATTERN));

        return playerSteamid;
    }
}