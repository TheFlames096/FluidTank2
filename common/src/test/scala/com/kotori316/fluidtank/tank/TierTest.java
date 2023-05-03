package com.kotori316.fluidtank.tank;

import java.util.EnumMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import scala.math.BigInt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TierTest {
    @TestFactory
    Stream<DynamicTest> testGetCapacity() {
        return DynamicTest.stream(Stream.of(Tier.values()), Tier::toString, t -> assertDoesNotThrow(t::getCapacity));
    }

    @Test
    void checkWoodCapacity() {
        assertEquals(BigInt.apply(81000 * 4), Tier.WOOD.getCapacity());
    }

    @Test
    void checkStoneCapacity() {
        assertEquals(BigInt.apply(81000 * 16), Tier.STONE.getCapacity());
    }

    @Test
    void setBadMap() {
        assertThrows(IllegalArgumentException.class, () -> Tier.setCapacityMap(new EnumMap<>(Tier.class)));
    }
}
