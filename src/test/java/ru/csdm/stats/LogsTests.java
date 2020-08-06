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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static ru.csdm.stats.common.model.tables.History.HISTORY;
import static ru.csdm.stats.common.model.tables.KnownServer.KNOWN_SERVER;
import static ru.csdm.stats.common.model.tables.Player.PLAYER;

import ru.csdm.stats.common.model.tables.History;
import ru.csdm.stats.common.model.tables.pojos.Player;
import ru.csdm.stats.common.model.tables.records.KnownServerRecord;
import ru.csdm.stats.common.utils.SomeUtils;
import ru.csdm.stats.modules.collector.endpoints.StatsEndpoint;
import ru.csdm.stats.modules.collector.service.SettingsService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;
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
    public void server1_27015_27015() throws Exception {
        addServer(27015, 27015, true, true, false, true);
        sendLogs("server1.log", 27015, 27015);
    }

    @Test
    public void server4_27016_27016() throws Exception {
        addServer(27016, 27016, true, true, false, true);
        sendLogs("server4.log", 27016, 27016);
    }

    @Test
    public void server1_27015_27016_start_session_on_action() throws Exception {
        addServer(27015, 27016, true, true, false, true);
        sendLogs("server1.log", 27015, 27016);

        assertStats(new String[][] {
                {"Name2", "0", "20", "132"},
                {"Name1", "20", "0", "20"},
        });
    }

    @Test
    public void server1_27015_27016_dont_start_session_on_action() throws Exception {
        addServer(27015, 27016, true, true, false, false);
        sendLogs("server1.log", 27015, 27016);

        assertStats(new String[][] {
                {"Name2", "0", "20", "132"},
                {"Name1", "20", "0", "24"},
        });
    }

    @Test
    public void server2_27015_27015_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, true);
        sendLogs("server2.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "7", "4", "94"},
                {"cusoma", "0", "8", "89"},
                {"timoxatw", "5", "1", "76"},
                {"no kill", "3", "2", "51"},
        });
    }

    @Test
    public void server2_27015_27015_dont_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, false);
        sendLogs("server2.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "7", "4", "220"},
                {"no kill", "3", "2", "113"},
                {"timoxatw", "5", "1", "110"},
                {"cusoma", "0", "8", "104"},
        });
    }

    @Test
    public void server3_27015_27015_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, true);
        sendLogs("server3.log", 27015, 27015);

        assertStats(new String[][] {
                {"cusoma", "0", "1", "95"},
                {"Admin", "1", "0", "94"},
        });
    }

    @Test
    public void server3_27015_27015_dont_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, false);
        sendLogs("server3.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "1", "0", "126"},
                {"no kill", "0", "0", "119"},
                {"timoxatw", "0", "0", "116"},
                {"cusoma", "0", "1", "110"},
        });
    }

    @Test
    public void server4_27015_27015() throws Exception {
        addServer(27015, 27015, true, true, false, true);
        sendLogs("server4.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "17", "11", "449"},
                {"yeppi", "11", "20", "443"},
                {"sonic", "10", "15", "443"},
                {"wRa1 wRa1", "13", "16", "440"},
                {"BoBka’)", "8", "10", "438"},
                {"showw", "21", "13", "438"},
                {"pravwOw~", "8", "21", "438"},
                {"haaimbat", "14", "18", "435"},
                {"BatalOOl", "12", "9", "435"},
                {"KaRJlSoH", "20", "9", "434"},
                {"nameasd", "18", "9", "434"},
                {"[52 xemaike2h blanil", "14", "16", "422"},
                {"Currv", "20", "14", "417"},
                {"aromaken1", "14", "15", "415"},
                {"~kewAw0w~~", "13", "16", "400"},
                {"castzOr", "14", "15", "395"},
        });
    }

    @Test
    public void server4_27015_27025() throws Exception {
        addServer(27015, 27025, true, true, false, true);
        sendLogs("server4.log", 27015, 27025);

        assertStats(new String[][] {
                {"Admin", "187", "121", "4939"},
                {"yeppi", "121", "220", "4873"},
                {"sonic", "110", "165", "4873"},
                {"wRa1 wRa1", "143", "176", "4840"},
                {"BoBka’)", "88", "110", "4818"},
                {"showw", "231", "143", "4818"},
                {"pravwOw~", "88", "231", "4818"},
                {"haaimbat", "154", "198", "4785"},
                {"BatalOOl", "132", "99", "4785"},
                {"KaRJlSoH", "220", "99", "4774"},
                {"nameasd", "198", "99", "4774"},
                {"[52 xemaike2h blanil", "154", "176", "4642"},
                {"Currv", "220", "154", "4587"},
                {"aromaken1", "154", "165", "4565"},
                {"~kewAw0w~~", "143", "176", "4400"},
                {"castzOr", "154", "165", "4345"},
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, true);
        sendLogs("ffa.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "2", "0", "20"},
                {"CeHb^Oaa", "0", "2", "20"},
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, false);
        sendLogs("ffa.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "2", "0", "76"},
                {"CeHb^Oaa", "0", "2", "51"},
                {"FENIX2H", "0", "0", "8"},
                {"relish -w 800", "0", "0", "3"},
        });
    }

    @Test
    public void ffa_27015_27015_start_session_on_action_no_ffa() throws Exception {
        addServer(27015, 27015, true, false, false, true);
        sendLogs("ffa.log", 27015, 27015);

        assertStats(new String[][] {
        });
    }

    @Test
    public void ffa_27015_27015_dont_start_session_on_action_no_ffa() throws Exception {
        addServer(27015, 27015, true, false, false, false);
        sendLogs("ffa.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "0", "0", "76"},
                {"CeHb^Oaa", "0", "0", "51"},
                {"FENIX2H", "0", "0", "8"},
                {"relish -w 800", "0", "0", "3"},
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, true);
        sendLogs("no_ffa.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "4", "0", "46"},
                {"desch", "0", "4", "46"},
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action() throws Exception {
        addServer(27015, 27015, true, true, false, false);
        sendLogs("no_ffa.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "4", "0", "100"},
                {"desch", "0", "4", "88"},
        });
    }

    @Test
    public void no_ffa_27015_27015_start_session_on_action_no_ffa() throws Exception {
        addServer(27015, 27015, true, false, false, true);
        sendLogs("no_ffa.log", 27015, 27015);

        assertStats(new String[][] {
        });
    }

    @Test
    public void no_ffa_27015_27015_dont_start_session_on_action_no_ffa() throws Exception {
        addServer(27015, 27015, true, false, false, false);
        sendLogs("no_ffa.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "0", "0", "100"},
                {"desch", "0", "0", "88"},
        });
    }

    @Test
    public void server4_27015_27017_start_session_on_action_ignore_bots() throws Exception {
        addServer(27015, 27025, true, true, true, true);
        sendLogs("server4.log", 27015, 27017);

        assertStats(new String[][] {
        });
    }

    @Test
    public void server4_27015_27015_dont_start_session_on_action_ignore_bots() throws Exception {
        addServer(27015, 27015, true, true, true, false);
        sendLogs("server4.log", 27015, 27015);

        assertStats(new String[][] {
                {"Admin", "0", "0", "597"},
        });
    }

    @Test
    public void server4_27015_27017_dont_start_session_on_action_ignore_bots() throws Exception {
        addServer(27015, 27025, true, true, true, false);
        sendLogs("server4.log", 27015, 27017);

        assertStats(new String[][] {
                {"Admin", "0", "0", "1791"}
        });
    }

    @Test
    public void server4_manual_flush_27014_27018_dont_start_session_on_action() throws Exception {
        addServer(27015, 27017, true, true, false, false);
        sendLogs("server4_only_load.log", 27014, 27018);

        Map<String, String> results = statsEndpoint.flush();
        log.info("statsEndpoint results: " + results.toString());

        Thread.sleep(1000);

        assertStats(new String[][] {
                {"Admin", "51", "33", "1764"},
                {"pravwOw~", "24", "63", "1350"},
                {"aromaken1", "42", "45", "1347"},
                {"yeppi", "33", "60", "1347"},
                {"sonic", "30", "45", "1344"},
                {"castzOr", "42", "45", "1344"},
                {"BatalOOl", "36", "27", "1341"},
                {"wRa1 wRa1", "39", "48", "1341"},
                {"showw", "63", "39", "1338"},
                {"haaimbat", "42", "54", "1338"},
                {"Currv", "60", "42", "1335"},
                {"~kewAw0w~~", "39", "48", "1335"},
                {"[52 xemaike2h blanil", "42", "48", "1332"},
                {"KaRJlSoH", "60", "27", "1332"},
                {"BoBka’)", "24", "30", "1329"},
                {"nameasd", "54", "27", "1329"},
        });
    }

    private void assertStats(String[][] expectedStats) {
        List<Player> players = fetchPlayers();

        List<Player> expectedPlayers = Stream.of(expectedStats)
                .map(this::makePlayerFromRawStats)
                .collect(Collectors.toList());

        for (Player player : players) {
            log.info(SomeUtils.playerRecordToString(player));
        }

        assertEquals(players, expectedPlayers);
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

    private List<Player> fetchPlayers() {
        return adminDsl.select(
                PLAYER.NAME,
                PLAYER.KILLS,
                PLAYER.DEATHS,
                PLAYER.TIME_SECS
        ).from(PLAYER)
                .orderBy(PLAYER.TIME_SECS.desc())
                .fetchInto(Player.class); //todo: check functionality
    }

    private void addServer(int portStart,
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

    private void truncateTables() {
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

    private Player makePlayerFromRawStats(String[] sourceRaw) {
        Player stat = new Player();
        stat.setName(sourceRaw[0]);
        stat.setKills(UInteger.valueOf(sourceRaw[1]));
        stat.setDeaths(UInteger.valueOf(sourceRaw[2]));
        stat.setTimeSecs(UInteger.valueOf(sourceRaw[3]));
        return stat;
    }
}