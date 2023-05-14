package com.kotori316.fluidtank.fabric.message;

import java.util.Objects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import com.kotori316.fluidtank.contents.Tank;
import com.kotori316.fluidtank.message.FluidTankContentMessage;
import com.kotori316.fluidtank.message.IMessage;
import com.kotori316.fluidtank.tank.TileTank;

/**
 * To client only.
 */
public final class FluidTankContentMessageFabric extends FluidTankContentMessage {
    static final ResourceLocation NAME = IMessage.createIdentifier(FluidTankContentMessageFabric.class);

    public FluidTankContentMessageFabric(BlockPos pos, ResourceKey<Level> dim, Tank<Fluid> tank) {
        super(pos, dim, tank);
    }

    public FluidTankContentMessageFabric(TileTank tileTank) {
        this(
            tileTank.getBlockPos(),
            Objects.requireNonNull(tileTank.getLevel()).dimension(),
            tileTank.getTank()
        );
    }

    FluidTankContentMessageFabric(FriendlyByteBuf buf) {
        super(buf);
    }

    @Environment(EnvType.CLIENT)
    static class HandlerHolder {
        static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler, buf, responseSender) -> {
            var message = new FluidTankContentMessageFabric(buf);
            var level = client.level;
            client.execute(() -> message.onReceive(level));
        };
    }
}
