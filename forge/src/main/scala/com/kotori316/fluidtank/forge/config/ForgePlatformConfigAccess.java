package com.kotori316.fluidtank.forge.config;

import com.google.gson.GsonBuilder;
import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.config.ConfigData;
import com.kotori316.fluidtank.config.PlatformConfigAccess;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class ForgePlatformConfigAccess implements PlatformConfigAccess {
    private FluidTankConfig tankConfig;
    private ConfigData cached;

    public ForgePlatformConfigAccess() {
    }

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

    @SubscribeEvent
    public void onReload(ModConfigEvent.Reloading event) {
        if (cached != null) {
            // ignore changes in initial setup
            FluidTankCommon.LOGGER.debug("Reload FluidTank config {}",
                event.getConfig().getFileName());
        }
        cached = tankConfig.createConfigData();
        var configString = new GsonBuilder().disableHtmlEscaping().create().toJson(cached.createJson());
        FluidTankCommon.LOGGER.debug("Config: {}", configString);
    }
}
