package net.turtton.packetexporter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class PacketExporter implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger("packetexporter");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var gson = new GsonBuilder().setPrettyPrinting().create();
            for (NetworkState state : NetworkState.values()) {
                Multimap<NetworkSide, String> dataMap = ArrayListMultimap.create();
                state.packetHandlers.forEach(((side, packetHandler) -> {
                    Map<Integer, String> packetMap = Maps.newTreeMap();
                    packetHandler.packetIds.forEach((packet, id) -> packetMap.put(id, packet.getSimpleName()));
                    dataMap.putAll(side, packetMap.entrySet().stream().map(entry -> String.format("0x%02X", entry.getKey()) + " " + entry.getValue()).toList());
                }

                ));
                var encoded = gson.toJson(dataMap.asMap());
                LOGGER.info(state.name() + encoded);
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("Packet." + state.name() + ".json"));
                    bufferedWriter.write(encoded);
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
