package com.kotori316.fluidtank.forge.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.fluids.FluidConnection;
import com.kotori316.fluidtank.tank.TileVoidTank;

public final class TileVoidTankForge extends TileVoidTank {
    public TileVoidTankForge(BlockPos p, BlockState s) {
        super(p, s);
    }

    private LazyOptional<IFluidHandler> fluidHandler = createHandler();

    @Override
    public void setConnection(FluidConnection c) {
        super.setConnection(c);
        this.fluidHandler.invalidate();
        this.fluidHandler = createHandler();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!remove && cap == ForgeCapabilities.FLUID_HANDLER) {
            return this.fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @NotNull
    private LazyOptional<IFluidHandler> createHandler() {
        return LazyOptional.of(() -> new ConnectionHandler(this.getConnection()));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.fluidHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        // Basically, not called for block entity
        super.reviveCaps();
        this.fluidHandler = createHandler();
    }
}
