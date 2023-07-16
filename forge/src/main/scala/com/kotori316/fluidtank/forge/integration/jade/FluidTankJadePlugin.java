package com.kotori316.fluidtank.forge.integration.jade;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.tank.BlockTank;
import com.kotori316.fluidtank.tank.TileTank;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(FluidTankCommon.modId)
public class FluidTankJadePlugin implements IWailaPlugin {
    public FluidTankJadePlugin() {
        FluidTankCommon.LOGGER.info("Registering {}", getClass().getSimpleName());
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        IWailaPlugin.super.register(registration);
        registration.registerBlockDataProvider(new FluidTankJadeProvider(), TileTank.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        IWailaPlugin.super.registerClient(registration);
        registration.registerBlockComponent(new FluidTankJadeProvider(), BlockTank.class);
    }
}
