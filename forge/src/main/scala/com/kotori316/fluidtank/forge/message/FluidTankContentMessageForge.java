package com.kotori316.fluidtank.forge.message;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

import com.kotori316.fluidtank.contents.Tank;
import com.kotori316.fluidtank.message.FluidTankContentMessage;
import com.kotori316.fluidtank.tank.TileTank;

public final class FluidTankContentMessageForge extends FluidTankContentMessage {
    public FluidTankContentMessageForge(BlockPos pos, ResourceKey<Level> dim, Tank<Fluid> tank) {
        super(pos, dim, tank);
    }

    public FluidTankContentMessageForge(TileTank tileTank) {
        this(
            tileTank.getBlockPos(),
            Objects.requireNonNull(tileTank.getLevel()).dimension(),
            tileTank.getTank()
        );
    }

    FluidTankContentMessageForge(FriendlyByteBuf buf) {
        super(buf);
    }

    void onReceive(Supplier<NetworkEvent.Context> supplier) {
        // Should be client side
        supplier.get().enqueueWork(() ->
            this.onReceive(LogicalSidedProvider.CLIENTWORLD.get(supplier.get().getDirection().getReceptionSide()).orElse(null))
        );
    }
}
