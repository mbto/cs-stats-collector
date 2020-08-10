package ru.csdm.stats;

import com.sun.security.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.csdm.stats.common.model.tables.pojos.Player;
import ru.csdm.stats.common.model.tables.pojos.PlayerIp;
import ru.csdm.stats.common.model.tables.pojos.PlayerSteamid;
import ru.csdm.stats.common.model.tables.records.KnownServerRecord;
import ru.csdm.stats.modules.collector.endpoints.StatsEndpoint;
import ru.csdm.stats.modules.collector.service.SettingsService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;
import static ru.csdm.stats.common.Constants.YYYYMMDD_HHMMSS_PATTERN;
import static ru.csdm.stats.common.model.tables.History.HISTORY;
import static ru.csdm.stats.common.model.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.tables.Player.PLAYER;
import static ru.csdm.stats.common.model.tables.PlayerIp.PLAYER_IP;
import static ru.csdm.stats.common.model.tables.PlayerSteamid.PLAYER_STEAMID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DependsOn(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
@Slf4j
public class LogsTests {
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private StatsEndpoint statsEndpoint;
    @Autowired
    private DSLContext adminDsl;
    @Value("${stats.listener.port:8888}")
    private int listenerPort;

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
    }

    @Test
    public void server1_27015_27015() throws Exception {
        addKnownServer(27015, 27015, true, true, false, true);
        sendLogs("server1.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"2", "Name2", "0", "11", "66", "1", "2020-01-01 13:16:08", "1"}, // 1m 6s
                {"1", "Name1", "10", "1", "10", "1", "2020-01-01 13:16:07", "1"} // 10s
        });
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server1_27015_27015_with_changing_names() throws Exception {
        addKnownServer(27015, 27015, true, true, false, false);
        sendLogs("server1_changing_names.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);

        assertPlayers(actualDB, new String[][] {
                {"2", "Name2", "0", "12", "69", "1", "2020-01-01 13:16:11", "1"}, // 1m 9s
                {"1", "Name1", "5", "0", "10", "1", "2020-01-01 13:16:10", "1"}, // 10s
                {"4", "Name9", "4", "0", "6", "1", "2020-01-01 13:16:08", "1"}, // 6s
                {"3", "Name5", "2", "1", "4", "1", "2020-01-01 13:15:08", "1"} // 4s
        });
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "24.24.24.24", "2020-01-01 13:16:10"},
                {"2", "1", "12.12.12.12", "2020-01-01 13:16:10"},
                {"3", "3", "12.12.12.12", "2020-01-01 13:15:08"},
                {"4", "4", "24.24.24.24", "2020-01-01 13:16:08"},
                {"5", "4", "12.12.12.12", "2020-01-01 13:16:08"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:123123123123", "2020-01-01 13:16:10"},
                {"2", "1", "STEAM_0:0:999999999999", "2020-01-01 13:16:10"},
                {"3", "2", "STEAM_0:1:987654", "2020-01-01 13:16:11"},
                {"4", "3", "STEAM_0:0:123123123123", "2020-01-01 13:15:08"},
                {"5", "4", "STEAM_0:0:123123123123", "2020-01-01 13:16:08"},
                {"6", "4", "STEAM_0:0:999999999999", "2020-01-01 13:16:08"}
        });
    }

    @Test
    public void server4_27016_27016() throws Exception {
        addKnownServer(27016, 27016, true, true, false, true);
        sendLogs("server4.log", 27016, 27016);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "17", "11", "449", "1", "2020-01-01 21:25:07", "1"}, // 7m 29s
                {"4", "yeppi", "11", "21", "443", "1", "2020-01-01 21:24:58", "1"}, // 7m 23s
                {"5", "sonic", "10", "16", "443", "1", "2020-01-01 21:24:58", "1"}, // 7m 23s
                {"8", "wRa1 wRa1", "13", "17", "440", "1", "2020-01-01 21:24:58", "1"}, // 7m 20s
                {"9", "showw", "21", "14", "438", "1", "2020-01-01 21:24:58", "1"}, // 7m 18s
                {"2", "pravwOw~", "8", "22", "438", "1", "2020-01-01 21:24:58", "1"}, // 7m 18s
                {"15", "BoBka’)", "8", "11", "438", "1", "2020-01-01 21:24:58", "1"}, // 7m 18s
                {"10", "haaimbat", "14", "19", "435", "1", "2020-01-01 21:24:58", "1"}, // 7m 15s
                {"7", "BatalOOl", "12", "10", "435", "1", "2020-01-01 21:24:58", "1"}, // 7m 15s
                {"14", "KaRJlSoH", "20", "10", "434", "1", "2020-01-01 21:24:58", "1"}, // 7m 14s
                {"16", "nameasd", "18", "10", "434", "1", "2020-01-01 21:24:58", "1"}, // 7m 14s
                {"13", "[52 xemaike2h blanil", "14", "17", "422", "1", "2020-01-01 21:24:58", "1"}, // 7m 2s
                {"11", "Currv", "20", "16", "417", "1", "2020-01-01 21:24:58", "1"}, // 6m 57s
                {"3", "aromaken1", "14", "16", "415", "1", "2020-01-01 21:24:58", "1"}, // 6m 55s
                {"12", "~kewAw0w~~", "13", "17", "400", "1", "2020-01-01 21:24:58", "1"}, // 6m 40s
                {"6", "castzOr", "14", "16", "395", "1", "2020-01-01 21:24:58", "1"} // 6m 35s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server1_27015_27016_start_session_on_action() throws Exception {
        addKnownServer(27015, 27016, true, true, false, true);
        sendLogs("server1.log", 27015, 27016);

        Map<Table<?>, List<Field<?>>> excludeColumns = new HashMap<>();
        /* logs sends in parallel, so PLAYER.LAST_SERVER_ID is undefined */
        excludeColumns.put(PLAYER, Arrays.asList(PLAYER.LAST_SERVER_ID));
        ActualDB actualDB = new ActualDB(adminDsl, excludeColumns);
        
        assertPlayers(actualDB, new String[][] {
                {"2", "Name2", "0", "22", "132", "1", "2020-01-01 13:16:08", null}, // 2m 12s
                {"1", "Name1", "20", "2", "20", "1", "2020-01-01 13:16:07", null} // 20s
        });
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server1_27015_27016_dont_start_session_on_action() throws Exception {
        addKnownServer(27015, 27016, true, true, false, false);
        sendLogs("server1.log", 27015, 27016);

        Map<Table<?>, List<Field<?>>> excludeColumns = new HashMap<>();
        /* logs sends in parallel, so PLAYER.LAST_SERVER_ID is undefined */
        excludeColumns.put(PLAYER, Arrays.asList(PLAYER.LAST_SERVER_ID));
        ActualDB actualDB = new ActualDB(adminDsl, excludeColumns);

        assertPlayers(actualDB, new String[][] {
                {"2", "Name2", "0", "22", "132", "1", "2020-01-01 13:16:08", null}, // 2m 12s
                {"1", "Name1", "20", "2", "28", "1", "2020-01-01 13:16:07", null} // 28s
        });
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "12.12.12.12", "2020-01-01 13:16:07"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server2_27015_27015_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, true);
        sendLogs("server2.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "7", "5", "103", "1", "2020-01-01 20:58:38", "1"}, // 1m 43s
                {"4", "cusoma", "0", "8", "89", "1", "2020-01-01 20:52:10", "1"}, // 1m 29s
                {"3", "timoxatw", "5", "1", "76", "1", "2020-01-01 20:52:10", "1"}, // 1m 16s
                {"2", "no kill", "3", "2", "51", "1", "2020-01-01 20:52:10", "1"} // 51s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server2_27015_27015_dont_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, false);
        sendLogs("server2.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "7", "5", "221", "1", "2020-01-01 20:58:38", "1"}, // 3m 41s
                {"2", "no kill", "3", "2", "113", "1", "2020-01-01 20:52:10", "1"}, // 1m 53s
                {"3", "timoxatw", "5", "1", "110", "1", "2020-01-01 20:52:10", "1"}, // 1m 50s
                {"4", "cusoma", "0", "8", "104", "1", "2020-01-01 20:52:10", "1"} // 1m 44s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server3_27015_27015_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, true);
        sendLogs("server3.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"2", "cusoma", "0", "1", "95", "1", "2020-01-01 20:50:41", "1"}, // 1m 35s
                {"1", "Admin", "1", "0", "94", "1", "2020-01-01 20:52:15", "1"} // 1m 34s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server3_27015_27015_dont_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, false);
        sendLogs("server3.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "1", "0", "127", "1", "2020-01-01 20:52:15", "1"}, // 2m 7s
                {"2", "no kill", "0", "0", "119", "1", "2020-01-01 20:50:17", "1"}, // 1m 59s
                {"3", "timoxatw", "0", "0", "116", "1", "2020-01-01 20:50:20", "1"}, // 1m 56s
                {"4", "cusoma", "0", "1", "110", "1", "2020-01-01 20:50:41", "1"} // 1m 50s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server4_27015_27015() throws Exception {
        addKnownServer(27015, 27015, true, true, false, true);
        sendLogs("server4.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "17", "11", "449", "1", "2020-01-01 21:25:07", "1"}, // 7m 29s
                {"4", "yeppi", "11", "21", "443", "1", "2020-01-01 21:24:58", "1"}, // 7m 23s
                {"5", "sonic", "10", "16", "443", "1", "2020-01-01 21:24:58", "1"}, // 7m 23s
                {"8", "wRa1 wRa1", "13", "17", "440", "1", "2020-01-01 21:24:58", "1"}, // 7m 20s
                {"9", "showw", "21", "14", "438", "1", "2020-01-01 21:24:58", "1"}, // 7m 18s
                {"2", "pravwOw~", "8", "22", "438", "1", "2020-01-01 21:24:58", "1"}, // 7m 18s
                {"15", "BoBka’)", "8", "11", "438", "1", "2020-01-01 21:24:58", "1"}, // 7m 18s
                {"10", "haaimbat", "14", "19", "435", "1", "2020-01-01 21:24:58", "1"}, // 7m 15s
                {"7", "BatalOOl", "12", "10", "435", "1", "2020-01-01 21:24:58", "1"}, // 7m 15s
                {"14", "KaRJlSoH", "20", "10", "434", "1", "2020-01-01 21:24:58", "1"}, // 7m 14s
                {"16", "nameasd", "18", "10", "434", "1", "2020-01-01 21:24:58", "1"}, // 7m 14s
                {"13", "[52 xemaike2h blanil", "14", "17", "422", "1", "2020-01-01 21:24:58", "1"}, // 7m 2s
                {"11", "Currv", "20", "16", "417", "1", "2020-01-01 21:24:58", "1"}, // 6m 57s
                {"3", "aromaken1", "14", "16", "415", "1", "2020-01-01 21:24:58", "1"}, // 6m 55s
                {"12", "~kewAw0w~~", "13", "17", "400", "1", "2020-01-01 21:24:58", "1"}, // 6m 40s
                {"6", "castzOr", "14", "16", "395", "1", "2020-01-01 21:24:58", "1"} // 6m 35s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server4_27015_27025() throws Exception {
        addKnownServer(27015, 27025, true, true, false, true);
        sendLogs("server4.log", 27015, 27025);

        Map<Table<?>, List<Field<?>>> excludeColumns = new HashMap<>();
        /* logs sends in parallel, so PLAYER.LAST_SERVER_ID is undefined */
        excludeColumns.put(PLAYER, Arrays.asList(PLAYER.LAST_SERVER_ID));
        ActualDB actualDB = new ActualDB(adminDsl, excludeColumns);
        
        assertPlayers(actualDB, new String[][] {
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
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, true);
        sendLogs("ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "2", "0", "20", "1", "2020-01-01 23:42:21", "1"}, // 20s
                {"2", "CeHb^Oaa", "0", "2", "20", "1", "2020-01-01 23:42:21", "1"} // 20s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:42:21"}
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, false);
        sendLogs("ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "2", "0", "76", "1", "2020-01-01 23:42:21", "1"}, // 1m 16s
                {"4", "CeHb^Oaa", "0", "2", "51", "1", "2020-01-01 23:42:21", "1"}, // 51s
                {"2", "FENIX2H", "0", "0", "8", "1", "2020-01-01 23:41:22", "1"}, // 8s
                {"3", "relish -w 800", "0", "0", "3", "1", "2020-01-01 23:41:27", "1"} // 3s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:42:21"}
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action_no_ffa() throws Exception {
        addKnownServer(27015, 27015, true, false, false, true);
        sendLogs("ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action_no_ffa() throws Exception {
        addKnownServer(27015, 27015, true, false, false, false);
        sendLogs("ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "0", "0", "76", "1", "2020-01-01 23:42:21", "1"}, // 1m 16s
                {"4", "CeHb^Oaa", "0", "0", "51", "1", "2020-01-01 23:42:21", "1"}, // 51s
                {"2", "FENIX2H", "0", "0", "8", "1", "2020-01-01 23:41:22", "1"}, // 8s
                {"3", "relish -w 800", "0", "0", "3", "1", "2020-01-01 23:41:27", "1"} // 3s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:42:21"}
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, true);
        sendLogs("no_ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "4", "0", "46", "1", "2020-01-01 23:45:56", "1"}, // 46s
                {"2", "desch", "0", "4", "46", "1", "2020-01-01 23:45:56", "1"} // 46s
        });
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "255.0.0.142", "2020-01-01 23:45:56"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:45:56"}
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action() throws Exception {
        addKnownServer(27015, 27015, true, true, false, false);
        sendLogs("no_ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "4", "0", "101", "1", "2020-01-01 23:45:56", "1"}, // 1m 41s
                {"2", "desch", "0", "4", "88", "1", "2020-01-01 23:45:56", "1"} // 1m 28s
        });
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "255.0.0.142", "2020-01-01 23:45:56"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:45:56"}
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action_no_ffa() throws Exception {
        addKnownServer(27015, 27015, true, false, false, true);
        sendLogs("no_ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action_no_ffa() throws Exception {
        addKnownServer(27015, 27015, true, false, false, false);
        sendLogs("no_ffa.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "0", "0", "101", "1", "2020-01-01 23:45:56", "1"}, // 1m 41s
                {"2", "desch", "0", "0", "88", "1", "2020-01-01 23:45:56", "1"} // 1m 28s
        });
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "255.0.0.142", "2020-01-01 23:45:56"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:123456", "2020-01-01 23:45:56"}
        });
    }

    @Test
    public void server4_27015_27017_start_session_on_action_ignore_bots() throws Exception {
        addKnownServer(27015, 27025, true, true, true, true);
        sendLogs("server4.log", 27015, 27017);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server4_27015_27015_dont_start_session_on_action_ignore_bots() throws Exception {
        addKnownServer(27015, 27015, true, true, true, false);
        sendLogs("server4.log", 27015, 27015);
        ActualDB actualDB = new ActualDB(adminDsl);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "0", "0", "598", "1", "2020-01-01 21:25:07", "1"} // 9m 58s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server4_27015_27017_dont_start_session_on_action_ignore_bots() throws Exception {
        addKnownServer(27015, 27025, true, true, true, false);
        sendLogs("server4.log", 27015, 27017);

        Map<Table<?>, List<Field<?>>> excludeColumns = new HashMap<>();
        /* logs sends in parallel, so PLAYER.LAST_SERVER_ID is undefined */
        excludeColumns.put(PLAYER, Arrays.asList(PLAYER.LAST_SERVER_ID));
        ActualDB actualDB = new ActualDB(adminDsl, excludeColumns);
        
        assertPlayers(actualDB, new String[][] {
                {"1", "Admin", "0", "0", "1794", "1", "2020-01-01 21:25:07", null} // 29m 54s
        });
        assertPlayersIps(actualDB, new String[][] {
        });
        assertPlayersSteamIds(actualDB, new String[][] {
        });
    }

    @Test
    public void server4_manual_flush_27014_27018_dont_start_session_on_action() throws Exception {
        addKnownServer(27015, 27017, true, true, false, false);
        sendLogs("server4_only_load.log", 27014, 27018);

        Map<String, String> results = statsEndpoint.flush(new UserPrincipal("tester"));
        log.info("statsEndpoint results: " + results.toString());

        Thread.sleep(1000);

        Map<Table<?>, List<Field<?>>> excludeColumns = new HashMap<>();
        /* logs sends in parallel, so PLAYER.LAST_SERVER_ID is undefined */
        excludeColumns.put(PLAYER, Arrays.asList(PLAYER.LAST_SERVER_ID));
        ActualDB actualDB = new ActualDB(adminDsl, excludeColumns);
        
        assertPlayers(actualDB, new String[][] {
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
        assertPlayersIps(actualDB, new String[][] {
                {"1", "1", "127.0.1.1", "2020-01-01 21:23:52"}
        });
        assertPlayersSteamIds(actualDB, new String[][] {
                {"1", "1", "STEAM_0:0:555000", "2020-01-01 21:23:52"}
        });
    }

    private void assertPlayers(ActualDB actualDB,
                               String[][] expectedRaw) {
        List<Player> actualData = actualDB.getPlayers();

        List<Player> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayerFromRaw)
                .collect(Collectors.toList());

        assertEquals(actualData, expectedData);
    }

    private void assertPlayersIps(ActualDB actualDB, String[][] expectedRaw) {
        List<PlayerIp> actualData = actualDB.getPlayersIps();

        List<PlayerIp> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayersIpsFromRaw)
                .collect(Collectors.toList());

        assertEquals(actualData, expectedData);
    }

    private void assertPlayersSteamIds(ActualDB actualDB, String[][] expectedRaw) {
        List<PlayerSteamid> actualData = actualDB.getPlayerSteamIds();

        List<PlayerSteamid> expectedData = Stream.of(expectedRaw)
                .map(this::makePlayersSteamIdsFromRaw)
                .collect(Collectors.toList());

        assertEquals(actualData, expectedData);
    }

    private void sendLogs(String fileName, int portStart, int portEnd) throws Exception {
        List<String> logs;

        try (InputStream resourceAsStream = LogsTests.class.getResourceAsStream("/collector/" + fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            logs = br.lines()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }

        InetSocketAddress serv1 = new InetSocketAddress("127.0.0.1", listenerPort);

        CompletableFuture<Void>[] tasks = IntStream.rangeClosed(portStart, portEnd)
                .boxed()
                .map(port -> CompletableFuture.runAsync(() -> {
                    boolean debugEnabled = log.isDebugEnabled();

                    try (DatagramSocket socket = new DatagramSocket(port)) {
                        for (String payload : logs) {
                            if(debugEnabled)
                                log.debug(port + " sending payload=" + payload);

                            /* L 01/01/2020 - 13:15:00: "Name1<5><STEAM_ID_LAN><>" connected, address "12.12.12.12:27005" */
                            byte[] rawSource = payload.getBytes();
                            byte[] rawPayload = new byte[rawSource.length + 8]; // "-1 -1 -1 -1 l o g  "
                            Arrays.fill(rawPayload, 0, 5, (byte) -1);
                            rawPayload[4] = 'l';
                            rawPayload[5] = 'o';
                            rawPayload[6] = 'g';
                            rawPayload[7] = ' ';
                            System.arraycopy(rawSource, 0, rawPayload, 8, rawPayload.length - 8);

                            DatagramPacket datagramPacket = new DatagramPacket(rawPayload, 0, rawPayload.length, serv1);
                            datagramPacket.setSocketAddress(serv1);
                            socket.send(datagramPacket);

                            Thread.sleep(ThreadLocalRandom.current().nextInt(1, 15));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })).<CompletableFuture<Void>>toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(tasks).join();
        Thread.sleep(1000);
    }

    private void addKnownServer(int portStart,
                                int portEnd,
                                boolean active,
                                boolean ffa,
                                boolean ignore_bots,
                                boolean start_session_on_action) {

        List<InsertSetMoreStep<KnownServerRecord>> steps = new ArrayList<>(portEnd - portStart + 1);

        for (int port = portStart; port <= portEnd; port++) {
            log.info(port + " adding port " + port);

            InsertSetMoreStep<KnownServerRecord> step = DSL.insertInto(KNOWN_SERVER)
                    .set(KNOWN_SERVER.IPPORT, "127.0.0.1:" + port)
                    .set(KNOWN_SERVER.NAME, "Test Server #1")
                    .set(KNOWN_SERVER.ACTIVE, active)
                    .set(KNOWN_SERVER.FFA, ffa)
                    .set(KNOWN_SERVER.IGNORE_BOTS, ignore_bots)
                    .set(KNOWN_SERVER.START_SESSION_ON_ACTION, start_session_on_action);

            steps.add(step);
        }

        adminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            transactionalDsl.batch(steps).execute();
        });

        settingsService.updateSettings(false);
    }

    public void truncateTables() {
        adminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            try {
                transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 0;");
                transactionalDsl.truncate(HISTORY).execute();
                transactionalDsl.truncate(PLAYER_IP).execute();
                transactionalDsl.truncate(PLAYER_STEAMID).execute();
                transactionalDsl.truncate(PLAYER).execute();
                transactionalDsl.truncate(KNOWN_SERVER).execute();
            } finally {
                transactionalDsl.execute("SET FOREIGN_KEY_CHECKS = 1;");
            }
        });
    }

    /**
     * {"2", "Name2", "0", "11", "66", "1", "2020-01-01 13:16:08", "1"}, // 1m 6s
     * {"1", "Name1", "10", "1", "10", "1", "2020-01-01 13:16:07", "1"} // 10s
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
            player.setLastServerId(UInteger.valueOf(sourceRaw[7]));

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