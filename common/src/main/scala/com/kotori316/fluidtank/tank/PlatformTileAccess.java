package com.kotori316.fluidtank.tank;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface PlatformTileAccess {
    @NotNull
    static PlatformTileAccess getInstance() {
        return PlatformTileAccessHolder.access;
    }

    static void setInstance(PlatformTileAccess instance) {
        PlatformTileAccessHolder.access = instance;
    }

    BlockEntityType<? extends TileTank> getNormalType();

    BlockEntityType<? extends TileTank> getCreativeType();

    BlockEntityType<? extends TileTank> getVoidType();
}

class PlatformTileAccessHolder {
    @NotNull
    static PlatformTileAccess access = new Default();

    @ApiStatus.Internal
    private static class Default implements PlatformTileAccess {

        @Override
        public BlockEntityType<? extends TileTank> getNormalType() {
            return null;
        }

        @Override
        public BlockEntityType<? extends TileTank> getCreativeType() {
            return null;
        }

        @Override
        public BlockEntityType<? extends TileTank> getVoidType() {
            return null;
        }
    }
}
