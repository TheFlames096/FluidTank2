package com.kotori316.fluidtank.forge.message;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.message.IMessage;

public final class PacketHandler {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL =
        NetworkRegistry.ChannelBuilder.named(new ResourceLocation(FluidTankCommon.modId, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(Predicate.isEqual(PROTOCOL))
            .serverAcceptedVersions(Predicate.isEqual(PROTOCOL))
            .simpleChannel();

    public static void init() {
        AtomicInteger count = new AtomicInteger(1);
        CHANNEL.registerMessage(count.getAndIncrement(), FluidTankContentMessageForge.class, IMessage::write,
            FluidTankContentMessageForge::new, setHandled(FluidTankContentMessageForge::onReceive));
    }

    static <MSG> BiConsumer<MSG, Supplier<NetworkEvent.Context>> setHandled(BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        return (msg, supplier) -> {
            messageConsumer.accept(msg, supplier);
            supplier.get().setPacketHandled(true);
        };
    }

    public static void sendToClient(IMessage<?> message, Level level) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }
}
