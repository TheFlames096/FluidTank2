package com.kotori316.fluidtank;

import com.kotori316.fluidtank.fluids.PlatformFluidAccess;
import com.kotori316.fluidtank.item.PlatformItemAccess;
import com.kotori316.fluidtank.tank.PlatformTankAccess;

public interface PlatformAccess extends PlatformFluidAccess, PlatformTankAccess, PlatformItemAccess {
    static void setInstance(PlatformAccess access) {
        PlatformFluidAccess.setInstance(access);
        PlatformTankAccess.setInstance(access);
        PlatformItemAccess.setInstance(access);
    }
}
