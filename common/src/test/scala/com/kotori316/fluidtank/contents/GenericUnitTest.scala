package com.kotori316.fluidtank.contents

import cats.implicits.{catsSyntaxGroup, catsSyntaxSemigroup}
import org.junit.jupiter.api.{Assertions, Nested, Test}

class GenericUnitTest {
  @Nested
  class ForgeTest {
    @Test
    def asForge(): Unit = {
      Assertions.assertEquals(1000, GenericUnit.ONE_BUCKET.asForge)
    }

    @Test
    def oneBucketFromForge(): Unit = {
      Assertions.assertEquals(GenericUnit.ONE_BUCKET, GenericUnit.fromForge(1000))
    }

    @Test
    def max(): Unit = {
      val maxUnit = GenericUnit.fromForge(Int.MaxValue)
      Assertions.assertEquals(Int.MaxValue, maxUnit.asForge)
    }

    @Test
    def maxPlus1(): Unit = {
      val overMax = GenericUnit.fromForge(1L + Int.MaxValue)
      Assertions.assertEquals(Int.MaxValue, overMax.asForge)
    }

    @Test
    def maxMinus1(): Unit = {
      val underMax = GenericUnit.fromForge(-1L + Int.MaxValue)
      Assertions.assertEquals(Int.MaxValue - 1, underMax.asForge)
    }
  }

  @Nested
  class FabricTest {
    @Test
    def asFabric(): Unit = {
      Assertions.assertEquals(81000L, GenericUnit.ONE_BUCKET.asFabric)
    }

    @Test
    def oneBucketFromFabric(): Unit = {
      Assertions.assertEquals(GenericUnit.ONE_BUCKET, GenericUnit.fromFabric(81000))
    }

    val MAX_BUCKET = 113_868_790_578_454L

    @Test
    def max(): Unit = {
      val maxUnit = GenericUnit.fromFabric(Long.MaxValue)
      Assertions.assertEquals(Long.MaxValue, maxUnit.asFabric)
    }

    @Test
    def maxPlus1(): Unit = {
      val overMax = GenericUnit(BigInt(Long.MaxValue) + BigInt(1))
      Assertions.assertEquals(Long.MaxValue, overMax.asFabric)
    }

    @Test
    def maxMinus1(): Unit = {
      val underMax = GenericUnit.fromFabric(-1L + Long.MaxValue)
      Assertions.assertEquals(Long.MaxValue - 1, underMax.asFabric)
    }
  }

  @Nested
  class GroupTest {
    @Test
    def add(): Unit = {
      val a = GenericUnit(BigInt(200))
      val b = GenericUnit(BigInt(30))
      Assertions.assertEquals(GenericUnit(BigInt(230)), a |+| b)
    }

    @Test
    def minus(): Unit = {
      val a = GenericUnit(BigInt(200))
      val b = GenericUnit(BigInt(30))
      Assertions.assertEquals(GenericUnit(BigInt(170)), a |-| b)
    }
  }
}
