package com.kotori316.fluidtank.tank

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.{DynamicTest, Test, TestFactory}

class TierTest {
  @TestFactory
  def testGetCapacity: Array[DynamicTest] = {
    Tier.values
      .map(t => DynamicTest.dynamicTest(t.toString, () => assertDoesNotThrow(() => t.getCapacity)))
  }

  @Test
  def checkWoodCapacity(): Unit = {
    assertEquals(BigInt.apply(81000 * 4), Tier.WOOD.getCapacity)
  }

  @Test
  def checkStoneCapacity(): Unit = {
    assertEquals(BigInt.apply(81000 * 16), Tier.STONE.getCapacity)
  }

  @Test
  def setBadMap(): Unit = {
    assertThrows(classOf[IllegalArgumentException], () => Tier.setCapacityMap(new java.util.EnumMap[Tier, BigInt](classOf[Tier])))
  }
}