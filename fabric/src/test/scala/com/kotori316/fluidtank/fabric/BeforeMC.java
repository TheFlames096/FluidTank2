package com.kotori316.fluidtank.fabric;

import com.kotori316.fluidtank.config.ConfigData;
import com.kotori316.fluidtank.config.PlatformConfigAccess;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

public abstract class BeforeMC {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        PlatformConfigAccess.setInstance(ConfigData::DEFAULT);
    }

    public static void assertEqualHelper(Object expected, Object actual) {
        Assertions.assertEquals(expected, actual, "Expected: %s, Actual: %s".formatted(expected, actual));
    }
}
