package com.kotori316.fluidtank;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;

public class BeforeMC {

    @BeforeAll
    public static void initMC() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
