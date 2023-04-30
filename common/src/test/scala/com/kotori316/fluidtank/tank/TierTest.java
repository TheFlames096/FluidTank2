package com.kotori316.fluidtank.tank;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TierTest {
    @TestFactory
    Stream<DynamicTest> amount() {
        return DynamicTest.stream(Stream.of(Tier.values()), Tier::toString, t -> assertDoesNotThrow(t::getCapacity));
    }
}
