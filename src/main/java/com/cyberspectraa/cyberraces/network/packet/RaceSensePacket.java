package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.client.ClientRaceSenseState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Short, server-authoritative and per-player sensing information. */
public record RaceSensePacket(int mode, List<BlockPos> ores,
                              List<BlockPos> containers, List<Integer> creatures) {
    public static final int TREASURE = 1;
    public static final int CREATURES = 2;
    private static final int MAX_ENTRIES = 24;

    public static RaceSensePacket treasure(List<BlockPos> ores, List<BlockPos> containers) {
        return new RaceSensePacket(TREASURE, List.copyOf(ores), List.copyOf(containers), List.of());
    }

    public static RaceSensePacket creatures(List<Integer> ids) {
        return new RaceSensePacket(CREATURES, List.of(), List.of(), List.copyOf(ids));
    }

    public static void encode(RaceSensePacket packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.mode);
        writePositions(buf, packet.ores);
        writePositions(buf, packet.containers);
        buf.writeVarInt(Math.min(MAX_ENTRIES, packet.creatures.size()));
        for (int i = 0; i < Math.min(MAX_ENTRIES, packet.creatures.size()); i++)
            buf.writeVarInt(packet.creatures.get(i));
    }

    private static void writePositions(FriendlyByteBuf buf, List<BlockPos> positions) {
        int count = Math.min(MAX_ENTRIES, positions.size());
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) buf.writeBlockPos(positions.get(i));
    }

    public static RaceSensePacket decode(FriendlyByteBuf buf) {
        int mode = buf.readUnsignedByte();
        List<BlockPos> ore = readPositions(buf);
        List<BlockPos> chests = readPositions(buf);
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENTRIES)
            throw new IllegalArgumentException("Invalid CyberRaces creature sense size");
        List<Integer> mobs = new ArrayList<>();
        for (int i = 0; i < count; i++) mobs.add(buf.readVarInt());
        return new RaceSensePacket(mode, ore, chests, mobs);
    }

    private static List<BlockPos> readPositions(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENTRIES)
            throw new IllegalArgumentException("Invalid CyberRaces block sense size");
        List<BlockPos> result = new ArrayList<>();
        for (int i = 0; i < count; i++) result.add(buf.readBlockPos());
        return result;
    }

    public static void handle(RaceSensePacket packet,
                              Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
        if (packet.mode == TREASURE || packet.mode == CREATURES)
            ClientRaceSenseState.accept(packet);
        context.setPacketHandled(true);
    }
}
