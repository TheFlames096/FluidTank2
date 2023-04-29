package com.kotori316.fluidtank;

import com.kotori316.fluidtank.fluids.PlatformFluidAccess;

public interface PlatformAccess extends PlatformFluidAccess {
    static void setInstance(PlatformAccess access) {
        PlatformFluidAccess.setInstance(access);
    }
}
