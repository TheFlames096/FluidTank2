package com.kotori316.fluidtank.content

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{DynamicNode, DynamicTest, Nested, Test, TestFactory}

import scala.jdk.javaapi.CollectionConverters

class TankOperationsTest {
  def createTank(contentString: String, amount: Long, capacity: Long): Tank[String] = {
    Tank(GenericAmount(contentString, GenericUnit(amount), None), GenericUnit(capacity))
  }

  @Nested
  class FillTest {

    @Test
    def fillToEmpty(): Unit = {
      val tank = createTank("", 0, 1000)
      val op: Operations.TankOperation[String] = Operations.fillOp(tank)

      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(500), None))
      assertEquals(createTank("a", 500, 1000), result)
      assertTrue(rest.isEmpty)
    }

    @Test
    def fillOriginalUnchanged(): Unit = {
      val tank = createTank("", 0, 1000)
      assertTrue(tank.isEmpty)

      val op: Operations.TankOperation[String] = Operations.fillOp(tank)
      op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(500), None))

      assertTrue(tank.isEmpty)
    }

    @Test
    def fillToFilledSucceed(): Unit = {
      val tank = createTank("a", 200, 1000)
      val op = Operations.fillOp(tank)

      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(500), None))
      assertEquals(createTank("a", 700, 1000), result)
      assertTrue(rest.isEmpty)
    }

    @Test
    def fillToFilledSucceed2(): Unit = {
      val tank = createTank("a", 200, 1000)
      val op = Operations.fillOp(tank)

      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(1000), None))
      assertEquals(createTank("a", 1000, 1000), result)
      assertEquals(GenericAmount("a", GenericUnit(200), None), rest)
    }

    @Test
    def fillToFilledFail(): Unit = {
      val tank = createTank("a", 200, 1000)
      val op = Operations.fillOp(tank)

      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("b", GenericUnit(500), None))
      assertEquals(createTank("a", 200, 1000), result)
      assertTrue(rest.nonEmpty)
      assertEquals(GenericAmount("b", GenericUnit(500), None), rest)
    }

    @Test
    def fillToFilledFail2(): Unit = {
      val tank = createTank("a", 1000, 1000)
      val op = Operations.fillOp(tank)

      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(500), None))
      assertEquals(createTank("a", 1000, 1000), result)
      assertTrue(rest.nonEmpty)
      assertEquals(GenericAmount("a", GenericUnit(500), None), rest)
    }

    @Test
    def fillEmpty(): Unit = {
      val tank = createTank("a", 200, 1000)
      val op = Operations.fillOp(tank)

      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("", GenericUnit(500), None))
      assertEquals(createTank("a", 200, 1000), result)
      assertEquals(GenericUnit(500), rest.amount)
    }

    @Test
    def fillToEmpty2(): Unit = {
      val tank = createTank("", 200, 1000)
      val op = Operations.fillOp(tank)
      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(500), None))
      assertEquals(createTank("a", 700, 1000), result)
      assertTrue(rest.isEmpty)
    }

    @Test
    def fillToEmpty3(): Unit = {
      val tank = createTank("b", 0, 1000)
      val op = Operations.fillOp(tank)
      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(500), None))
      assertEquals(createTank("a", 500, 1000), result)
      assertTrue(rest.isEmpty)
    }
  }

  @Nested
  class DrainTest {
    @TestFactory
    def drainFromEmptyTank1(): java.util.List[DynamicNode] = {
      val tankAmount = Seq(0, 1000)
      val fluids = Seq(
        GenericAmount("a", GenericUnit(500), None),
        GenericAmount("b", GenericUnit(500), None),
        GenericAmount("", GenericUnit(500), None),
        GenericAmount("a", GenericUnit(0), None),
        GenericAmount("", GenericUnit(0), None),
      )
      val tests: Seq[DynamicNode] = for {
        amount <- tankAmount
        a <- fluids
      } yield {
        DynamicTest.dynamicTest(s"$amount $a", () => {
          val tank = createTank("", amount, 1000)
          val op = Operations.drainOp(tank)
          val (_, rest, result) = op.run(DefaultTransferEnv, a)
          assertEquals(a, rest)
          assertEquals(tank, result)
        })
      }

      CollectionConverters.asJava(tests)
    }

    @TestFactory
    def drainFromEmptyTank2(): java.util.List[DynamicNode] = {
      val fluids = Seq(
        GenericAmount("a", GenericUnit(500), None),
        GenericAmount("b", GenericUnit(500), None),
        GenericAmount("", GenericUnit(500), None),
        GenericAmount("a", GenericUnit(0), None),
        GenericAmount("", GenericUnit(0), None),
      )
      val tankContent = Seq("a", "b")
      val tests: Seq[DynamicNode] = for {
        content <- tankContent
        a <- fluids
      } yield {
        DynamicTest.dynamicTest(s"$content $a", () => {
          val tank = createTank(content, 0, 1000)
          val op = Operations.drainOp(tank)
          val (_, rest, result) = op.run(DefaultTransferEnv, a)
          assertEquals(a, rest)
          assertEquals(tank, result)
        })
      }
      CollectionConverters.asJava(tests)
    }

    @Test
    def drainFromEmptyTank3(): Unit = {
      val tank = createTank("", 500, 1000)
      val op = Operations.drainOp(tank)
      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("content", GenericUnit(200), None))
      assertEquals(GenericAmount("content", GenericUnit(200), None), rest)
      assertEquals(tank, result)
    }

    @Test
    def drain1(): Unit = {
      val tank = createTank("content", 500, 1000)
      val op = Operations.drainOp(tank)
      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("content", GenericUnit(200), None))
      assertTrue(rest.isEmpty)
      assertEquals(createTank("content", 300, 1000), result, "Expect 300 tank")
    }

    @Test
    def drain2(): Unit = {
      val tank = createTank("content", 500, 1000)
      val op = Operations.drainOp(tank)
      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("content", GenericUnit(500), None))
      assertTrue(rest.isEmpty)
      assertTrue(result.isEmpty)
      assertEquals(createTank("content", 0, 1000), result, "Expect empty tank")
    }

    @Test
    def drain3(): Unit = {
      val tank = createTank("content", 500, 1000)
      val op = Operations.drainOp(tank)
      val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("content", GenericUnit(800), None))
      assertAll(
        () => assertTrue(rest.nonEmpty),
        () => assertEquals(GenericAmount("content", GenericUnit(300), None), rest),
        () => assertTrue(result.isEmpty),
        () => assertEquals(createTank("content", 0, 1000), result, "Expect empty tank"),
      )
    }

    @TestFactory
    def drainFail(): java.util.List[DynamicNode] = {
      val tank = createTank("content", 500, 1000)
      val op = Operations.drainOp(tank)
      val fluids = Seq(
        GenericAmount("b", GenericUnit(500), None),
        GenericAmount("", GenericUnit(500), None),
      )
      CollectionConverters.asJava(fluids.map(a => DynamicTest.dynamicTest(s"$a", () => {
        val (_, rest, result) = op.run(DefaultTransferEnv, a)
        assertEquals(a, rest)
        assertEquals(tank, result)
      })))
    }
  }

}
