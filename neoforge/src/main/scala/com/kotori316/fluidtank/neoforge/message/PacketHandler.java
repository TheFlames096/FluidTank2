package com.kotori316.fluidtank.neoforge.message;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.message.FluidTankContentMessage;
import com.kotori316.fluidtank.message.IMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.MessageFunctions;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;

public final class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(FluidTankCommon.modId, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void init() {
        INSTANCE.registerMessage(1, FluidTankContentMessageNeoForge.class, FluidTankContentMessage::write, FluidTankContentMessageNeoForge::new, setHandled(FluidTankContentMessageNeoForge::onReceiveMessage));
    }

    static <MSG> MessageFunctions.MessageConsumer<MSG> setHandled(BiConsumer<MSG, NetworkEvent.Context> messageConsumer) {
        return (msg, context) -> {
            messageConsumer.accept(msg, context);
            context.setPacketHandled(true);
        };
    }

    public static void sendToClient(IMessage<?> message, Level level) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }
}
