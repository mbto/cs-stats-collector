package ru.csdm.stats;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.csdm.stats.common.dto.PlayerStat;
import ru.csdm.stats.model.Csstats;
import ru.csdm.stats.model.CsstatsServers;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DependsOn(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
@Slf4j
public class LogsTests {
    @Autowired
    private SettingsService settingsService;
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
    public void server4_27015_27015() throws Exception {
        addServer(27015, 27015, true, true, false, true);
        sendLogs("server4.log", 27015, 27015);

        List<PlayerStat> playerStats = fetchPlayersStats();

        String[][] expectedStats = {
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
                {"castzOr", "14", "15", "395"}
        };

        List<PlayerStat> expectedPlayers = Stream.of(expectedStats)
                .map(this::makePlayerStat)
                .collect(Collectors.toList());

        for (PlayerStat playerStat : playerStats) {
            System.out.println(playerStat);
        }

        assertEquals(playerStats, expectedPlayers);
    }

    @Test
    public void server4_27015_27025() throws Exception {
        addServer(27015, 27025, true, true, false, true);
        sendLogs("server4.log", 27015, 27025);

        List<PlayerStat> playerStats = fetchPlayersStats();

        String[][] expectedStats = {
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
        };

        List<PlayerStat> expectedPlayers = Stream.of(expectedStats)
                .map(this::makePlayerStat)
                .collect(Collectors.toList());

        for (PlayerStat playerStat : playerStats) {
            System.out.println(playerStat);
        }

        assertEquals(playerStats, expectedPlayers);
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

    private List<PlayerStat> fetchPlayersStats() {
        return adminDsl.select(Csstats.name_field.as("name"),
                Csstats.kills_field.as("totalKills"),
                Csstats.deaths_field.as("totalDeaths"),
                Csstats.time_secs_field.as("totalTimeInSecs")
        ).from(Csstats.csstats_table)
                .orderBy(Csstats.time_secs_field.desc())
                .fetchInto(PlayerStat.class);
    }

    private void addServer(int portStart,
                           int portEnd,
                           boolean active,
                           boolean ffa,
                           boolean ignore_bots,
                           boolean start_session_on_action) {
        adminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);

            List<InsertSetMoreStep<Record>> steps = new ArrayList<>();

            for (int port = portStart; port <= portEnd; port++) {
                log.info(port + " adding port " + port);

                InsertSetMoreStep<Record> step = DSL.insertInto(CsstatsServers.csstats_servers_table)
                        .set(CsstatsServers.ipport_field, "127.0.0.1:" + port)
                        .set(CsstatsServers.active_field, active)
                        .set(CsstatsServers.ffa_field, ffa)
                        .set(CsstatsServers.ignore_bots_field, ignore_bots)
                        .set(CsstatsServers.start_session_on_action, start_session_on_action);

                steps.add(step);
            }

            transactionalDsl.batch(steps).execute();
        });

        settingsService.updateSettings(false);
    }

    private void truncateTables() {
        adminDsl.transaction(config -> {
            DSLContext transactionalDsl = DSL.using(config);
            transactionalDsl.truncate(Csstats.csstats_table).execute();
            transactionalDsl.truncate(CsstatsServers.csstats_servers_table).execute();
        });
    }

    private PlayerStat makePlayerStat(String[] sourceRaw) {
        PlayerStat stat = new PlayerStat();
        stat.setName(sourceRaw[0]);
        stat.setTotalKills(Long.parseLong(sourceRaw[1]));
        stat.setTotalDeaths(Long.parseLong(sourceRaw[2]));
        stat.setTotalTimeInSecs(Long.parseLong(sourceRaw[3]));
        return stat;
    }
}