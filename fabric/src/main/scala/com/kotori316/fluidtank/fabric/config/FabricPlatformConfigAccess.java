package com.kotori316.fluidtank.fabric.config;

import com.kotori316.fluidtank.config.ConfigData;
import com.kotori316.fluidtank.config.PlatformConfigAccess;
import org.jetbrains.annotations.NotNull;

public class FabricPlatformConfigAccess implements PlatformConfigAccess {

    @Override
    public @NotNull ConfigData getConfig() {
        return ConfigData.DEFAULT();
    }
}
