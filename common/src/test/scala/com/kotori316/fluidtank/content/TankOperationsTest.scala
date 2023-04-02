package com.kotori316.fluidtank.content

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class TankOperationsTest {
  def createTank(contentString: String, amount: Long, capacity: Long): Tank[String] = {
    Tank(GenericAmount(contentString, GenericUnit(amount), None), GenericUnit(capacity))
  }

  @Test
  def fillToEmpty(): Unit = {
    val tank = createTank("", 0, 1000)
    val op: Operations.TankOperation[String] = Operations.fillOp(tank)

    val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("a", GenericUnit(500), None))
    assertEquals(createTank("a", 500, 1000), result)
    assertTrue(rest.isEmpty)
  }

  @Test
  def originalUnchanged(): Unit = {
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
  def fillToFilledFail(): Unit = {
    val tank = createTank("a", 200, 1000)
    val op = Operations.fillOp(tank)

    val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("b", GenericUnit(500), None))
    assertEquals(createTank("a", 200, 1000), result)
    assertTrue(rest.nonEmpty)
    assertEquals(GenericAmount("b", GenericUnit(500), None), rest)
  }

  @Test
  def fillEmpty(): Unit = {
    val tank = createTank("a", 200, 1000)
    val op = Operations.fillOp(tank)

    val (_, rest, result) = op.run(DefaultTransferEnv, GenericAmount("", GenericUnit(500), None))
    assertEquals(createTank("a", 200, 1000), result)
    assertEquals(GenericUnit(500), rest.amount)
  }
}
