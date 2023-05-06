package com.kotori316.fluidtank.forge;

import org.junit.jupiter.api.BeforeAll;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.testutil.MCTestInitializer;

public abstract class BeforeMC {
    @BeforeAll
    public static void initialize() {
        MCTestInitializer.setUp(FluidTankCommon.modId, BeforeMC::setup);
    }

    private static void setup() {

    }
}
