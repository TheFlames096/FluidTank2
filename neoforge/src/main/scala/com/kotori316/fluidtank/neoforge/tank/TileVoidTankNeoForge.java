package com.kotori316.fluidtank.neoforge.tank;

import com.kotori316.fluidtank.fluids.FluidConnection;
import com.kotori316.fluidtank.tank.TileVoidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TileVoidTankNeoForge extends TileVoidTank {
    public TileVoidTankNeoForge(BlockPos p, BlockState s) {
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
        if (!remove && cap == Capabilities.FLUID_HANDLER) {
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
