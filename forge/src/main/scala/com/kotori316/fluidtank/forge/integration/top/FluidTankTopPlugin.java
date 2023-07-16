package com.kotori316.fluidtank.forge.integration.top;

import com.kotori316.fluidtank.FluidTankCommon;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.InterModComms;

import java.util.function.Function;

public class FluidTankTopPlugin {
    private static final String TOP_ID = "theoneprobe";

    public static void sendIMC() {
        boolean registered = InterModComms.sendTo(FluidTankCommon.modId, TOP_ID, "getTheOneProbe", Sender::new);
        if (registered) {
            FluidTankCommon.LOGGER.info("Registered TheOneProbe Plugin");
        }
    }
}

class Sender implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe register) {
        register.registerProvider(FluidTankTopProvider$.MODULE$);
        return null;
    }
}
