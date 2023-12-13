package com.kotori316.fluidtank.neoforge.tank;

import com.kotori316.fluidtank.fluids.FluidConnection;
import com.kotori316.fluidtank.tank.TileVoidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TileVoidTankNeoForge extends TileVoidTank {
    public TileVoidTankNeoForge(BlockPos p, BlockState s) {
        super(p, s);
    }

    @NotNull
    private IFluidHandler fluidHandler = createHandler();

    @Override
    public void setConnection(FluidConnection c) {
        super.setConnection(c);
        this.invalidateCapabilities();
        this.fluidHandler = createHandler();
    }

    @NotNull
    public IFluidHandler getCapability(@Nullable Direction ignored) {
        return this.fluidHandler;
    }

    @NotNull
    private IFluidHandler createHandler() {
        return new ConnectionHandler(this.getConnection());
    }

}
