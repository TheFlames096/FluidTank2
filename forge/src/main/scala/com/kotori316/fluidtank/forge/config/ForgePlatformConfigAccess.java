package com.kotori316.fluidtank.forge.config;

import com.kotori316.fluidtank.config.ConfigData;
import com.kotori316.fluidtank.config.PlatformConfigAccess;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgePlatformConfigAccess implements PlatformConfigAccess {
    private FluidTankConfig tankConfig;
    private ConfigData cached;

   public ForgeConfigSpec.Builder setupConfig() {
        var builder = new ForgeConfigSpec.Builder();
        this.tankConfig = new FluidTankConfig(builder);
        return builder;
    }

    @Override
    public ConfigData getConfig() {
        if (cached == null) {
            cached = this.tankConfig.createConfigData();
        }
        return cached;
    }
}
