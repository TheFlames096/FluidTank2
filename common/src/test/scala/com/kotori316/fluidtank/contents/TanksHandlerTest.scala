package com.kotori316.fluidtank.contents

import cats.data.Chain
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{DynamicNode, DynamicTest, Nested, Test, TestFactory}

class TanksHandlerTest {
  class ImplForTest(limit: Boolean) extends TanksHandler[String, Chain](limit) {
    override def updateTanks(newTanks: Chain[Tank[String]]): Unit = this.tanks = newTanks

    def getTank: Chain[Tank[String]] = this.tanks
  }

  class ImplNoLimit extends ImplForTest(false)

  class ImplLimit extends ImplForTest(true)

  def testBothMany(executables: Seq[(String, ImplForTest => Unit)]): Array[DynamicNode] = {
    val emptyTanks = Seq(("No Limit", new ImplNoLimit), ("With Limit", new ImplLimit))
    emptyTanks.flatMap[DynamicNode] {
      case (title1, value) => executables.map {
        case (title2, f) => DynamicTest.dynamicTest(s"$title2 $title1", () => f(value))
      }
    }.toArray
  }

  def testBoth(executable: ImplForTest => Unit): Array[DynamicNode] = {
    val emptyTanks = Seq(("No Limit", new ImplNoLimit), ("With Limit", new ImplLimit))
    emptyTanks.map[DynamicNode] {
      case (str, value) => DynamicTest.dynamicTest(str, () => executable(value))
    }.toArray
  }

  @TestFactory
  def emptyCapacity(): Array[DynamicNode] = {
    testBoth(t => assertEquals(GenericUnit.ZERO, t.getSumOfCapacity))
  }

  @TestFactory
  def fillToNoTank(): Array[DynamicNode] = {
    testBothMany(Seq(true, false).map(e =>
      s"execute = $e" -> (t => {
        val filled = t.fill(GenericAmount("a", GenericUnit(100), None), execute = e)
        assertTrue(filled.isEmpty)
      })))
  }

  @TestFactory
  def drainFromNoTank(): Array[DynamicNode] = {
    testBothMany(Seq(true, false).map(e =>
      s"execute = $e" -> (t => {
        val drained = t.drain(GenericAmount("a", GenericUnit(100), None), execute = e)
        assertTrue(drained.isEmpty)
      })))
  }

  @TestFactory
  def fillToEmpty1Simulate(): Array[DynamicNode] = {
    testBoth(tanks => {
      tanks.updateTanks(createTanks(("", 0, 1000)))

      val filled = tanks.fill(GenericAmount("a", GenericUnit(100), None), execute = false)
      assertEquals(GenericAmount("a", GenericUnit(100), None), filled)
      assertEquals(createTanks(("", 0, 1000)), tanks.getTank)
    })
  }

  @TestFactory
  def fillToEmpty1Execute(): Array[DynamicNode] = {
    testBoth(tanks => {
      tanks.updateTanks(createTanks(("", 0, 1000)))

      val filled = tanks.fill(GenericAmount("a", GenericUnit(100), None), execute = true)
      assertEquals(GenericAmount("a", GenericUnit(100), None), filled)
      assertEquals(createTanks(("a", 100, 1000)), tanks.getTank)
    })
  }

  @TestFactory
  def fillToEmpty2Simulate(): Array[DynamicNode] = {
    testBoth(tanks => {
      tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))

      val filled = tanks.fill(GenericAmount("a", GenericUnit(1500), None), execute = false)
      assertEquals(GenericAmount("a", GenericUnit(1500), None), filled)
      assertEquals(createTanks(("", 0, 1000), ("", 0, 1000)), tanks.getTank)
    })
  }

  @TestFactory
  def fillToEmpty2Execute(): Array[DynamicNode] = {
    testBoth(tanks => {
      tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))

      val filled = tanks.fill(GenericAmount("a", GenericUnit(1500), None), execute = true)
      assertEquals(GenericAmount("a", GenericUnit(1500), None), filled)
      assertEquals(createTanks(("a", 1000, 1000), ("a", 500, 1000)), tanks.getTank)
    })
  }

  @TestFactory
  def fillToFilled1Simulate(): Array[DynamicNode] = {
    testBoth { tanks =>
      tanks.updateTanks(createTanks(("a", 500, 1000), ("", 0, 1000)))
      val filled = tanks.fill(GenericAmount("a", GenericUnit(1000), None), execute = false)
      assertEquals(GenericAmount("a", GenericUnit(1000), None), filled)
      assertEquals(createTanks(("a", 500, 1000), ("", 0, 1000)), tanks.getTank)
    }
  }

  @TestFactory
  def fillToFilled1Execute(): Array[DynamicNode] = {
    testBoth { tanks =>
      tanks.updateTanks(createTanks(("a", 500, 1000), ("", 0, 1000)))
      val filled = tanks.fill(GenericAmount("a", GenericUnit(1000), None), execute = true)
      assertEquals(GenericAmount("a", GenericUnit(1000), None), filled)
      assertEquals(createTanks(("a", 1000, 1000), ("a", 500, 1000)), tanks.getTank)
    }
  }

  @Nested
  class NoLimitTest {
    @Test
    def fillToFilled1Simulate(): Unit = {
      val tanks = new ImplNoLimit
      val initial = createTanks(("a", 100, 1000), ("", 0, 1000))
      tanks.updateTanks(initial)

      val filled = tanks.fill(GenericAmount("b", GenericUnit(100), None), execute = false)
      assertEquals(GenericAmount("b", GenericUnit(100), None), filled)
      assertEquals(initial, tanks.getTank, "In simulation")
    }

    @Test
    def fillToFilled1Execute(): Unit = {
      val tanks = new ImplNoLimit
      val initial = createTanks(("a", 100, 1000), ("", 0, 1000))
      tanks.updateTanks(initial)

      val filled = tanks.fill(GenericAmount("b", GenericUnit(100), None), execute = true)
      assertEquals(GenericAmount("b", GenericUnit(100), None), filled)
      assertEquals(createTanks(("a", 100, 1000), ("b", 100, 1000)), tanks.getTank, "In execution")
    }
  }

  @Nested
  class WithLimitTest {
    @TestFactory
    def fillToFilled1(): Array[DynamicNode] = {
      Seq(true, false).map(execution => DynamicTest.dynamicTest(s"execute=$execution", () => {
        val tanks = new ImplLimit
        val initial = createTanks(("a", 100, 1000), ("", 0, 1000))
        tanks.updateTanks(initial)

        val filled = tanks.fill(GenericAmount("b", GenericUnit(100), None), execute = execution)
        assertTrue(filled.isEmpty)
        assertEquals(initial, tanks.getTank, "no change")
      })).toArray
    }

    @Test
    def fillToEmpty1(): Unit = {
      val tanks = new ImplLimit
      val initial = createTanks(("a", 0, 1000), ("", 0, 1000))
      tanks.updateTanks(initial)

      val filled = tanks.fill(GenericAmount("b", GenericUnit(100), None), execute = true)
      assertEquals(GenericAmount("b", GenericUnit(100), None), filled)
      assertEquals(createTanks(("b", 100, 1000), ("", 0, 1000)), tanks.getTank)
    }
  }
}
