package com.kotori316.fluidtank.forge;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.testutil.MCTestInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

public abstract class BeforeMC {
    @BeforeAll
    public static void initialize() {
        MCTestInitializer.setUp(FluidTankCommon.modId, BeforeMC::setup);
    }

    private static void setup() {

    }

    public static void assertEqualHelper(Object expected, Object actual) {
        Assertions.assertEquals(expected, actual, "Expected: %s, Actual: %s".formatted(expected, actual));
    }
}
