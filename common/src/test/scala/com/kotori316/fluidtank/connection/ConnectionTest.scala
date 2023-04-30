package com.kotori316.fluidtank.connection

import cats.data.Chain
import com.kotori316.fluidtank.contents
import com.kotori316.fluidtank.contents.{GenericAmount, GenericUnit, Tank}
import net.minecraft.core.BlockPos
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.{DynamicNode, DynamicTest, Nested, Test, TestFactory}

class ConnectionTest {

  def getConnection(initialTank: Chain[Tank[String]]): StringConnection = {
    val tanks = initialTank.zipWithIndex.map { case (tank, index) => StringTile(BlockPos.ZERO.atY(index), tank, None) }.toList
    Connection.createAndInit(tanks)

    tanks.head.connection.get
  }

  @Test
  def create(): Unit = {
    val initialTank = contents.createTanks(("", 0, 1000), ("", 0, 700), ("", 0, 600))
    val tanks = initialTank.zipWithIndex.map { case (tank, index) => StringTile(BlockPos.ZERO.atY(index), tank, None) }.toList
    Connection.createAndInit(tanks)

    val connectionOption = tanks.head.connection
    assertTrue(connectionOption.isDefined)
    val connection = connectionOption.get
    assertTrue(connection.isInstanceOf[StringConnection])
  }

  @Test
  def updateInternal(): Unit = {
    val initialTank = contents.createTanks(("", 0, 1000), ("", 0, 700), ("", 0, 600))
    val tanks = initialTank.zipWithIndex.map { case (tank, index) => StringTile(BlockPos.ZERO.atY(index), tank, None) }.toList
    Connection.createAndInit(tanks)

    val connection: StringConnection = tanks.head.connection.get
    val newTanks = contents.createTanks(("a", 500, 1000), ("a", 100, 700), ("", 0, 600))
    connection.getHandler.updateTanks(newTanks)
    assertEquals(newTanks, connection.getHandler.getTank)
    assertEquals(newTanks.toList, tanks.map(_.tank))
  }

  @Test
  def initialModify1(): Unit = {
    val initialTank = contents.createTanks(("", 0, 1000), ("a", 200, 700), ("", 0, 600))
    val c = getConnection(initialTank)
    assertEquals(contents.createTanks(("a", 200, 1000), ("a", 0, 700), ("", 0, 600)), c.getHandler.getTank)
  }

  @Test
  def initialModify2(): Unit = {
    val initialTank = contents.createTanks(("", 0, 1000), ("gas", 200, 700), ("", 0, 600))
    val c = getConnection(initialTank)
    assertEquals(contents.createTanks(("", 0, 1000), ("gas", 0, 700), ("gas", 200, 600)), c.getHandler.getTank)
  }

  @Test
  def setSameConnection(): Unit = {
    val initialTank = contents.createTanks(("", 0, 1000), ("", 0, 700), ("", 0, 600))
    val c = getConnection(initialTank)

    assertTrue(c.sortedTanks.forall(tile => tile.connection == Option(c)))
  }

  @Nested
  class FillTest {
    @TestFactory
    def fillSimulate(): Array[DynamicNode] = {
      val initialTank = contents.createTanks(("", 0, 1000), ("", 0, 700), ("", 0, 800))
      val tests: Seq[DynamicNode] = for {
        fill <- Seq(
          GenericAmount("a", GenericUnit.fromFabric(100), None),
          GenericAmount("a", GenericUnit.fromFabric(1100), None),
          GenericAmount("a", GenericUnit.fromFabric(2500), None),
        )
        name = s"$fill"
      } yield DynamicTest.dynamicTest(name, () => {
        val connection = getConnection(initialTank)
        val filled = connection.getHandler.fill(fill, execute = false)
        assertEquals(fill, filled)
        assertEquals(initialTank, connection.getHandler.getTank)
      })

      tests.toArray
    }

    @TestFactory
    def fillExecute(): Array[DynamicNode] = {
      val initialTank = contents.createTanks(("", 0, 1000), ("", 0, 700), ("", 0, 800))
      val tests: Seq[(String, Executable)] = Seq(
        ("fill100", () => {
          val connection = getConnection(initialTank)
          val toFill = GenericAmount("a", GenericUnit.fromFabric(100), None)
          val filled = connection.getHandler.fill(toFill, execute = true)
          assertEquals(toFill, filled)
          assertEquals(contents.createTanks(("a", 100, 1000), ("", 0, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("fill1000", () => {
          val connection = getConnection(initialTank)
          val toFill = GenericAmount("a", GenericUnit.fromFabric(1000), None)
          val filled = connection.getHandler.fill(toFill, execute = true)
          assertEquals(toFill, filled)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("", 0, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("fill1100", () => {
          val connection = getConnection(initialTank)
          val toFill = GenericAmount("a", GenericUnit.fromFabric(1100), None)
          val filled = connection.getHandler.fill(toFill, execute = true)
          assertEquals(toFill, filled)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("a", 100, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("fill1700", () => {
          val connection = getConnection(initialTank)
          val toFill = GenericAmount("a", GenericUnit.fromFabric(1700), None)
          val filled = connection.getHandler.fill(toFill, execute = true)
          assertEquals(toFill, filled)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("a", 700, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("fill1800", () => {
          val connection = getConnection(initialTank)
          val toFill = GenericAmount("a", GenericUnit.fromFabric(1800), None)
          val filled = connection.getHandler.fill(toFill, execute = true)
          assertEquals(toFill, filled)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("a", 700, 700), ("a", 100, 800)), connection.getHandler.getTank)
        }),
        ("fill2500", () => {
          val connection = getConnection(initialTank)
          val toFill = GenericAmount("a", GenericUnit.fromFabric(2500), None)
          val filled = connection.getHandler.fill(toFill, execute = true)
          assertEquals(toFill, filled)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("a", 700, 700), ("a", 800, 800)), connection.getHandler.getTank)
        }),
        ("fill3000", () => {
          val connection = getConnection(initialTank)
          val toFill = GenericAmount("a", GenericUnit.fromFabric(3000), None)
          val filled = connection.getHandler.fill(toFill, execute = true)
          assertEquals(GenericAmount("a", GenericUnit.fromFabric(2500), None), filled)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("a", 700, 700), ("a", 800, 800)), connection.getHandler.getTank)
        }),
      )

      tests.map { case (str, executable) => DynamicTest.dynamicTest(str, executable) }
        .toArray
    }
  }

  @Nested
  class DrainTest {
    @TestFactory
    def drainSimulate(): Array[DynamicNode] = {
      val initialTank = contents.createTanks(("a", 1000, 1000), ("a", 500, 700), ("", 0, 800))
      val tests: Seq[DynamicNode] = for {
        drain <- Seq(
          GenericAmount("a", GenericUnit.fromFabric(100), None),
          GenericAmount("a", GenericUnit.fromFabric(1100), None),
          GenericAmount("a", GenericUnit.fromFabric(1500), None),
        )
        name = s"$drain"
      } yield DynamicTest.dynamicTest(name, () => {
        val connection = getConnection(initialTank)
        val drained = connection.getHandler.drain(drain, execute = false)
        assertEquals(drain, drained)
        assertEquals(initialTank, connection.getHandler.getTank)
      })
      tests.toArray
    }

    @TestFactory
    def drainExecute(): Array[DynamicNode] = {
      val initialTank = contents.createTanks(("a", 1000, 1000), ("a", 500, 700), ("", 0, 800))
      val tests: Seq[(String, Executable)] = Seq(
        ("drain100", () => {
          val connection = getConnection(initialTank)
          val toDrain = GenericAmount("a", GenericUnit.fromFabric(100), None)
          val drained = connection.getHandler.drain(toDrain, execute = true)
          assertEquals(toDrain, drained)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("a", 400, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("drain500", () => {
          val connection = getConnection(initialTank)
          val toDrain = GenericAmount("a", GenericUnit.fromFabric(500), None)
          val drained = connection.getHandler.drain(toDrain, execute = true)
          assertEquals(toDrain, drained)
          assertEquals(contents.createTanks(("a", 1000, 1000), ("a", 0, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("drain1000", () => {
          val connection = getConnection(initialTank)
          val toDrain = GenericAmount("a", GenericUnit.fromFabric(1000), None)
          val drained = connection.getHandler.drain(toDrain, execute = true)
          assertEquals(toDrain, drained)
          assertEquals(contents.createTanks(("a", 500, 1000), ("a", 0, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("drain1500", () => {
          val connection = getConnection(initialTank)
          val toDrain = GenericAmount("a", GenericUnit.fromFabric(1500), None)
          val drained = connection.getHandler.drain(toDrain, execute = true)
          assertEquals(toDrain, drained)
          assertEquals(contents.createTanks(("a", 0, 1000), ("a", 0, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
        ("drain1501", () => {
          val connection = getConnection(initialTank)
          val toDrain = GenericAmount("a", GenericUnit.fromFabric(1501), None)
          val drained = connection.getHandler.drain(toDrain, execute = true)
          assertEquals(GenericAmount("a", GenericUnit.fromFabric(1500), None), drained)
          assertEquals(contents.createTanks(("a", 0, 1000), ("a", 0, 700), ("", 0, 800)), connection.getHandler.getTank)
        }),
      )

      tests.map { case (name, executable) => DynamicTest.dynamicTest(name, executable) }
        .toArray
    }
  }
}
