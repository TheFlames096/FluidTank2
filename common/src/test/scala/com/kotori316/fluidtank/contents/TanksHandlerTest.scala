package com.kotori316.fluidtank.contents

import cats.data.Chain
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{DynamicNode, DynamicTest, Nested, Test, TestFactory}

class TanksHandlerTest {

  import TanksHandlerTest._

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

  def testBothAndExecution(executable: (ImplForTest, Boolean) => Unit): Array[DynamicNode] = {
    testBothMany(Seq(true, false).map(execution =>
      (s"Execution=$execution", t => executable(t, execution))
    ))
  }

  @TestFactory
  def emptyCapacity(): Array[DynamicNode] = {
    testBoth(t => assertEquals(GenericUnit.ZERO, t.getSumOfCapacity))
  }

  @Nested
  class FillTest {
    @TestFactory
    def fillToNoTank(): Array[DynamicNode] = {
      testBothAndExecution { case (t, e) =>
        val filled = t.fill(GenericAmount("a", GenericUnit(100), None), execute = e)
        assertTrue(filled.isEmpty)
      }
    }

    @TestFactory
    def fillToEmpty1Simulate(): Array[DynamicNode] = {
      testBoth { tanks =>
        tanks.updateTanks(createTanks(("", 0, 1000)))

        val filled = tanks.fill(GenericAmount("a", GenericUnit(100), None), execute = false)
        assertEquals(GenericAmount("a", GenericUnit(100), None), filled)
        assertEquals(createTanks(("", 0, 1000)), tanks.getTank)
      }
    }

    @TestFactory
    def fillToEmpty1Execute(): Array[DynamicNode] = {
      testBoth { tanks =>
        tanks.updateTanks(createTanks(("", 0, 1000)))

        val filled = tanks.fill(GenericAmount("a", GenericUnit(100), None), execute = true)
        assertEquals(GenericAmount("a", GenericUnit(100), None), filled)
        assertEquals(createTanks(("a", 100, 1000)), tanks.getTank)
      }
    }

    @TestFactory
    def fillToEmpty2Simulate(): Array[DynamicNode] = {
      testBoth { tanks =>
        tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))

        val filled = tanks.fill(GenericAmount("a", GenericUnit(1500), None), execute = false)
        assertEquals(GenericAmount("a", GenericUnit(1500), None), filled)
        assertEquals(createTanks(("", 0, 1000), ("", 0, 1000)), tanks.getTank)
      }
    }

    @TestFactory
    def fillToEmpty2Execute(): Array[DynamicNode] = {
      testBoth { tanks =>
        tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))

        val filled = tanks.fill(GenericAmount("a", GenericUnit(1500), None), execute = true)
        assertEquals(GenericAmount("a", GenericUnit(1500), None), filled)
        assertEquals(createTanks(("a", 1000, 1000), ("a", 500, 1000)), tanks.getTank)
      }
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

    @TestFactory
    def fillGasSimulate(): Array[DynamicNode] = {
      testBothMany(Seq(
        ("fill100", tanks => {
          tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))
          val before = tanks.getTank
          val filled = tanks.fill(GenericAmount("gas", GenericUnit(100), None), execute = false)
          assertEquals(GenericAmount("gas", GenericUnit(100), None), filled)
          assertEquals(before, tanks.getTank)
        }),
        ("fill1100", tanks => {
          tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))
          val before = tanks.getTank
          val filled = tanks.fill(GenericAmount("gas", GenericUnit(1100), None), execute = false)
          assertEquals(GenericAmount("gas", GenericUnit(1100), None), filled)
          assertEquals(before, tanks.getTank)
        }),
      ))
    }

    @TestFactory
    def fillGasExecute(): Array[DynamicNode] = {
      testBothMany(Seq(
        ("fill100", tanks => {
          tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))
          val filled = tanks.fill(GenericAmount("gas", GenericUnit(100), None), execute = true)
          assertEquals(GenericAmount("gas", GenericUnit(100), None), filled)
          assertEquals(createTanks(("", 0, 1000), ("gas", 100, 1000)), tanks.getTank)
        }),
        ("fill1100", tanks => {
          tanks.updateTanks(createTanks(("", 0, 1000), ("", 0, 1000)))
          val filled = tanks.fill(GenericAmount("gas", GenericUnit(1100), None), execute = true)
          assertEquals(GenericAmount("gas", GenericUnit(1100), None), filled)
          assertEquals(createTanks(("gas", 100, 1000), ("gas", 1000, 1000)), tanks.getTank)
        }),
      ))
    }
  }

  @Nested
  class DrainTest {
    @TestFactory
    def drainFromNoTank(): Array[DynamicNode] = {
      testBothAndExecution { case (t, e) =>
        val drained = t.drain(GenericAmount("a", GenericUnit(100), None), execute = e)
        assertTrue(drained.isEmpty)
      }
    }

    @TestFactory
    def drainSimulate1(): Array[DynamicNode] = {
      val initial = createTanks(("a", 500, 1000), ("", 0, 1000))
      val execution = false
      testBothMany(Seq(
        ("drain100", tanks => {
          tanks.updateTanks(initial)
          val drained = tanks.drain(GenericAmount("a", GenericUnit(100), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(100), None), drained)
          assertEquals(initial, tanks.getTank)
        }),
        ("drain500", tanks => {
          tanks.updateTanks(initial)
          val drained = tanks.drain(GenericAmount("a", GenericUnit(500), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(500), None), drained)
          assertEquals(initial, tanks.getTank)
        }),
        ("drain1000", tanks => {
          tanks.updateTanks(initial)
          val drained = tanks.drain(GenericAmount("a", GenericUnit(1000), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(500), None), drained)
          assertEquals(initial, tanks.getTank)
        }),
        ("drain1100", tanks => {
          tanks.updateTanks(createTanks(("a", 1000, 1000), ("a", 500, 1000)))
          val drained = tanks.drain(GenericAmount("a", GenericUnit(1100), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(1100), None), drained)
          assertEquals(createTanks(("a", 1000, 1000), ("a", 500, 1000)), tanks.getTank)
        }),
        ("drainGas100", tanks => {
          tanks.updateTanks(createTanks(("gas", 500, 1000), ("gas", 1000, 1000)))
          val drained = tanks.drain(GenericAmount("gas", GenericUnit(1100), None), execute = execution)
          assertEquals(GenericAmount("gas", GenericUnit(1100), None), drained)
          assertEquals(createTanks(("gas", 500, 1000), ("gas", 1000, 1000)), tanks.getTank)
        }),
      ))
    }

    @TestFactory
    def drainExecute1(): Array[DynamicNode] = {
      val initial = createTanks(("a", 500, 1000), ("", 0, 1000))
      val execution = true
      testBothMany(Seq(
        ("drain100", tanks => {
          tanks.updateTanks(initial)
          val drained = tanks.drain(GenericAmount("a", GenericUnit(100), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(100), None), drained)
          assertEquals(createTanks(("a", 400, 1000), ("", 0, 1000)), tanks.getTank)
        }),
        ("drain500", tanks => {
          tanks.updateTanks(initial)
          val drained = tanks.drain(GenericAmount("a", GenericUnit(500), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(500), None), drained)
          assertEquals(createTanks(("a", 0, 1000), ("", 0, 1000)), tanks.getTank)
        }),
        ("drain1000", tanks => {
          tanks.updateTanks(initial)
          val drained = tanks.drain(GenericAmount("a", GenericUnit(1000), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(500), None), drained)
          assertEquals(createTanks(("a", 0, 1000), ("", 0, 1000)), tanks.getTank)
        }),
        ("drain1100", tanks => {
          tanks.updateTanks(createTanks(("a", 1000, 1000), ("a", 500, 1000)))
          val drained = tanks.drain(GenericAmount("a", GenericUnit(1100), None), execute = execution)
          assertEquals(GenericAmount("a", GenericUnit(1100), None), drained)
          assertEquals(createTanks(("a", 400, 1000), ("a", 0, 1000)), tanks.getTank)
        }),
        ("drainGas100", tanks => {
          tanks.updateTanks(createTanks(("gas", 500, 1000), ("gas", 1000, 1000)))
          val drained = tanks.drain(GenericAmount("gas", GenericUnit(1100), None), execute = execution)
          assertEquals(GenericAmount("gas", GenericUnit(1100), None), drained)
          assertEquals(createTanks(("gas", 0, 1000), ("gas", 400, 1000)), tanks.getTank)
        }),
      ))
    }

    @TestFactory
    def drainFail(): Array[DynamicNode] = {
      val toDrain = Seq(
        GenericAmount("", GenericUnit(0), None),
        GenericAmount("", GenericUnit(500), None),
        GenericAmount("a", GenericUnit(0), None),
        GenericAmount("b", GenericUnit(500), None),
      )
      testBothMany(
        for {
          execution <- Seq(true, false)
          f <- toDrain
        } yield {
          (s"$f execute=$execution", (tanks: ImplForTest) => {
            val initial = createTanks(("a", 500, 1000), ("", 0, 1000))
            tanks.updateTanks(initial)
            val drained = tanks.drain(f, execution)
            assertTrue(drained.isEmpty)
            assertEquals(initial, tanks.getTank)
          })
        }
      )
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

    @Test
    def withVoid1(): Unit = {
      val tanks = new ImplLimit
      val initial = createTanks(("a", 100, 1000)) :+ VoidTank[String]
      tanks.updateTanks(initial)

      val filled = tanks.fill(GenericAmount("a", GenericUnit(100), None), execute = true)
      assertEquals(GenericAmount("a", GenericUnit(100), None), filled)
      assertEquals(createTanks(("a", 200, 1000)) :+ VoidTank[String], tanks.getTank)
    }

    @Test
    def withVoid2(): Unit = {
      val tanks = new ImplLimit
      val initial = createTanks(("a", 100, 1000)) :+ VoidTank[String]
      tanks.updateTanks(initial)

      val filled = tanks.fill(GenericAmount("a", GenericUnit(10000), None), execute = true)
      assertEquals(GenericAmount("a", GenericUnit(10000), None), filled)
      assertEquals(createTanks(("a", 1000, 1000)) :+ VoidTank[String], tanks.getTank)
    }

    @Test
    def withVoid3(): Unit = {
      val tanks = new ImplLimit
      val initial = (createTanks(("a", 100, 1000)) :+ VoidTank[String]) ++ createTanks(("a", 100, 1000))
      tanks.updateTanks(initial)

      val filled = tanks.fill(GenericAmount("a", GenericUnit(10000), None), execute = true)
      assertEquals(GenericAmount("a", GenericUnit(10000), None), filled)
      assertEquals(
        (createTanks(("a", 1000, 1000)) :+ VoidTank[String]) ++ createTanks(("a", 100, 1000)),
        tanks.getTank
      )
    }
  }

  @Nested
  class VoidTest {
    @TestFactory
    def fillVoid(): Array[DynamicNode] = {
      val tanks = Chain(VoidTank[String])
      val toFill = GenericAmount("a", GenericUnit(10), None)
      testBothAndExecution { (handler, e) =>
        handler.updateTanks(tanks)
        val filled1 = handler.fill(toFill, execute = e)
        assertEquals(toFill, filled1)
      }
    }

    @TestFactory
    def drainVoid(): Array[DynamicNode] = {
      val tanks = Chain(VoidTank[String])
      val toDrain = GenericAmount("a", GenericUnit(10), None)
      testBothAndExecution { (handler, e) =>
        handler.updateTanks(tanks)
        val drained = handler.drain(toDrain, execute = e)
        assertTrue(drained.isEmpty)
      }
    }

    @Test
    def fill1(): Unit = {
      val tanks = createTanks(("", 0, 1000)) :+ VoidTank[String]
      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toFill = GenericAmount("a", GenericUnit(10), None)
      val filled1 = handler.fill(toFill, execute = false)
      assertEquals(toFill, filled1)

      val filled2 = handler.fill(toFill, execute = true)
      assertEquals(toFill, filled2)
      assertEquals(createTanks(("a", 10, 1000)) :+ VoidTank[String], handler.getTank)
    }

    @TestFactory
    def fill2(): Array[DynamicNode] = {
      val tanks = createTanks(("", 0, 1000)) :+ VoidTank[String]
      val toFillList = Seq(1000, 1001, 10000, Int.MaxValue)
        .map(i => GenericAmount("a", GenericUnit(i), None))

      toFillList.map(toFill => DynamicTest.dynamicTest(s"$toFill", () => {
        val handler = new ImplLimit
        handler.updateTanks(tanks)

        val filled2 = handler.fill(toFill, execute = true)
        assertEquals(toFill, filled2)
        assertEquals(createTanks(("a", 1000, 1000)) :+ VoidTank[String], handler.getTank)
      })).toArray
    }

    @TestFactory
    def fill3(): Array[DynamicNode] = {
      val tanks = (createTanks(("", 0, 1000)) :+ VoidTank[String]) ++ createTanks(("", 0, 1000))
      val toFillList = Seq(1000, 1001, 10000, Int.MaxValue)
        .map(i => GenericAmount("a", GenericUnit(i), None))

      toFillList.map(toFill => DynamicTest.dynamicTest(s"$toFill", () => {
        val handler = new ImplLimit
        handler.updateTanks(tanks)

        val filled2 = handler.fill(toFill, execute = true)
        assertEquals(toFill, filled2)
        assertEquals((createTanks(("a", 1000, 1000)) :+ VoidTank[String]) ++ createTanks(("", 0, 1000)), handler.getTank)
      })).toArray
    }
  }

  @Nested
  class CreativeTest {
    @Test
    def fill1(): Unit = {
      val tanks = createTanks(("", 0, 1000)) :+ new CreativeTank(GenericAmount("", GenericUnit.ZERO, None), GenericUnit(1000))
      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toFill = GenericAmount("a", GenericUnit(10), None)
      val filled1 = handler.fill(toFill, execute = false)
      assertEquals(toFill, filled1)

      val filled2 = handler.fill(toFill, execute = true)
      assertEquals(toFill, filled2)
      assertEquals(createTanks(("a", 1000, 1000)) :+ new CreativeTank(GenericAmount("a", GenericUnit(1000), None), GenericUnit(1000)), handler.getTank)
    }

    @Test
    def fill2(): Unit = {
      val tanks = createTanks(("a", 10, 1000)) :+ new CreativeTank(GenericAmount("", GenericUnit.ZERO, None), GenericUnit(1000))

      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toFill = GenericAmount("a", GenericUnit(10), None)
      val filled1 = handler.fill(toFill, execute = false)
      assertEquals(toFill, filled1)

      val filled2 = handler.fill(toFill, execute = true)
      assertEquals(toFill, filled2)
      assertEquals(createTanks(("a", 1000, 1000)) :+ new CreativeTank(GenericAmount("a", GenericUnit(1000), None), GenericUnit(1000)), handler.getTank)
    }

    @Test
    def fill3(): Unit = {
      val tanks = createTanks(("b", 10, 1000)) :+ new CreativeTank(GenericAmount("b", GenericUnit.ZERO, None), GenericUnit(1000))

      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toFill = GenericAmount("a", GenericUnit(10), None)
      val filled1 = handler.fill(toFill, execute = false)
      assertTrue(filled1.isEmpty)

      val filled2 = handler.fill(toFill, execute = true)
      assertTrue(filled2.isEmpty)
      assertEquals(tanks, handler.getTank)
    }

    @Test
    def drain1(): Unit = {
      val tanks = createTanks(("a", 10, 1000)) :+ new CreativeTank(GenericAmount("", GenericUnit.ZERO, None), GenericUnit(1000))

      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toDrain = GenericAmount("a", GenericUnit(1000), None)
      val drained1 = handler.drain(toDrain, execute = false)
      assertEquals(toDrain, drained1)
      assertEquals(tanks, handler.getTank)

      val drained2 = handler.drain(toDrain, execute = true)
      assertEquals(toDrain, drained2)
      assertEquals(tanks, handler.getTank)
    }

    @Test
    def drain2(): Unit = {
      val tanks = createTanks(("a", 10, 1000)) :+ new CreativeTank(GenericAmount("", GenericUnit.ZERO, None), GenericUnit(1000))

      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toDrain = GenericAmount("b", GenericUnit(1000), None)
      val drained1 = handler.drain(toDrain, execute = false)
      assertTrue(drained1.isEmpty)
      assertEquals(tanks, handler.getTank)

      val drained2 = handler.drain(toDrain, execute = true)
      assertTrue(drained2.isEmpty)
      assertEquals(tanks, handler.getTank)
    }

    @Test
    def drain3(): Unit = {
      val tanks = createTanks(("", 0, 1000)) :+ new CreativeTank(GenericAmount("", GenericUnit.ZERO, None), GenericUnit(1000))

      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toDrain = GenericAmount("a", GenericUnit(1000), None)
      val drained1 = handler.drain(toDrain, execute = false)
      assertTrue(drained1.isEmpty)
      assertEquals(tanks, handler.getTank)

      val drained2 = handler.drain(toDrain, execute = true)
      assertTrue(drained2.isEmpty)
      assertEquals(tanks, handler.getTank)
    }

    @Test
    def drain4(): Unit = {
      val tanks = createTanks(("", 0, 1000)) :+ new CreativeTank(GenericAmount("a", GenericUnit(10), None), GenericUnit(1000))

      val handler = new ImplLimit
      handler.updateTanks(tanks)

      val toDrain = GenericAmount("a", GenericUnit(1000), None)
      val drained1 = handler.drain(toDrain, execute = false)
      assertEquals(toDrain, drained1)
      assertEquals(tanks, handler.getTank)

      val drained2 = handler.drain(toDrain, execute = true)
      assertEquals(toDrain, drained2)
      assertEquals(tanks, handler.getTank)
    }

  }
}

object TanksHandlerTest {
  class ImplForTest(limit: Boolean) extends ChainTanksHandler[String](limit) {
    override def updateTanks(newTanks: Chain[Tank[String]]): Unit = super.updateTanks(newTanks)

    def getTank: Chain[Tank[String]] = this.tanks
  }

  class ImplNoLimit extends ImplForTest(false)

  class ImplLimit extends ImplForTest(true)

}
