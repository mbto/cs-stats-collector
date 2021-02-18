package ru.csdm.stats.modules.collector.settings;

import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
public class PacketUtils {
    private static final Map<String, BiFunction<String, DatagramPacket, Boolean>> validatorByGameName = new HashMap<>(1, 1f);
    private static final Map<String, BiFunction<String, DatagramPacket, String>> converterByGameName = new HashMap<>(1, 1f);

    static {
        validatorByGameName.put("CS16", (address, packet) -> {
            byte[] data = packet.getData(); // [-1, -1, -1, -1, 108, 111, 103, 32, 76, ...]
            if(data.length < 9 || data[4] != 'l' || data[5] != 'o' || data[6] != 'g' || data[7] != ' ' || data[8] != 'L') {
                if(log.isDebugEnabled()) {
                    log.debug(address + " Invalid data: " +
                            "'" + convert("CS16", address, packet) + "'"
                            + ", raw: " + Arrays.toString(Arrays.copyOf(data, packet.getLength())));
                }

                return false;
            }

            return true;
        });
        converterByGameName.put("CS16", (address, packet) ->
                new String(packet.getData(), 8, packet.getLength() -8, StandardCharsets.UTF_8).trim());

        /* For developers: You can add yours game packet data converter/validator into here */
    }

    public static boolean validate(String gameName, String address, DatagramPacket packet) {
        BiFunction<String, DatagramPacket, Boolean> validator = validatorByGameName.get(gameName);
        return validator != null ? validator.apply(address, packet) : false;
    }

    public static String convert(String gameName, String address, DatagramPacket packet) {
        BiFunction<String, DatagramPacket, String> converter = converterByGameName.get(gameName);
        return converter != null ? converter.apply(address, packet) : null;
    }
}