package com.kotori316.fluidtank.config;

import org.jetbrains.annotations.NotNull;

public interface PlatformConfigAccess {
    @NotNull
    static PlatformConfigAccess getInstance() {
        return PlatformConfigAccessHolder.instance;
    }

    static void setInstance(@NotNull PlatformConfigAccess access) {
        PlatformConfigAccessHolder.instance = access;
    }

    @NotNull
    ConfigData getConfig();
}

class PlatformConfigAccessHolder {
    static PlatformConfigAccess instance = null;
}
