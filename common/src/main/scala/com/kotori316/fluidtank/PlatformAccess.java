package com.kotori316.fluidtank;

import com.kotori316.fluidtank.fluids.PlatformFluidAccess;
import com.kotori316.fluidtank.tank.PlatformTileAccess;

public interface PlatformAccess extends PlatformFluidAccess, PlatformTileAccess {
    static void setInstance(PlatformAccess access) {
        PlatformFluidAccess.setInstance(access);
        PlatformTileAccess.setInstance(access);
    }
}
