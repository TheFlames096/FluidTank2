package com.kotori316.fluidtank.neoforge.config;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.config.ConfigData;
import com.kotori316.fluidtank.config.PlatformConfigAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoForgePlatformConfigAccess implements PlatformConfigAccess {
    private FluidTankConfig tankConfig;
    private ConfigData cached;

    public NeoForgePlatformConfigAccess() {
    }

    public ModConfigSpec.Builder setupConfig() {
        var builder = new ModConfigSpec.Builder();
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
    }
}
