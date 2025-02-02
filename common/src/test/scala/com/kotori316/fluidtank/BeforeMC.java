package com.kotori316.fluidtank;

import com.google.common.collect.Iterables;
import com.kotori316.fluidtank.config.ConfigData;
import com.kotori316.fluidtank.config.PlatformConfigAccess;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;

import java.util.IdentityHashMap;
import java.util.List;

public class BeforeMC {

    @BeforeAll
    public static void initMC() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        unfreezeRegistry();
        PlatformConfigAccess.setInstance(ConfigData::DEFAULT);
    }

    private static void unfreezeRegistry() {
        try {
            final var frozenField = MappedRegistry.class.getDeclaredField("frozen");
            final var mapField = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            frozenField.setAccessible(true);
            mapField.setAccessible(true);

            var registries = Iterables.concat(List.of(BuiltInRegistries.REGISTRY), BuiltInRegistries.REGISTRY);
            for (Registry<?> registry : registries) {
                if (registry instanceof MappedRegistry<?>) {
                    frozenField.setBoolean(registry, false);
                    mapField.set(registry, new IdentityHashMap<>());
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
