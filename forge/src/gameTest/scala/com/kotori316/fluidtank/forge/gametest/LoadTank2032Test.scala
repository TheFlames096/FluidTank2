package com.kotori316.fluidtank.forge.gametest

import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.contents.GenericUnit
import com.kotori316.fluidtank.fluids.FluidAmountUtil
import com.kotori316.fluidtank.forge.FluidTank
import com.kotori316.fluidtank.forge.tank.TileTankForge
import com.kotori316.fluidtank.tank.Tier
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.{GameTestGenerator, GameTestHelper, TestFunction}
import net.minecraftforge.gametest.{GameTestHolder, PrefixGameTestTemplate}
import org.junit.jupiter.api.Assertions.*

@GameTestHolder(FluidTankCommon.modId)
@PrefixGameTestTemplate(value = false)
class LoadTank2032Test {
  private final val BATCH = "defaultBatch"

  @GameTestGenerator
  def generator(): java.util.List[TestFunction] = {
    GetGameTestMethods.getTests(getClass, this, BATCH, "load_20_3_tanks")
  }

  def assumptionWood(helper: GameTestHelper): Unit = {
    val pos = new BlockPos(0, 2, 0)
    helper.assertBlockPresent(FluidTank.TANK_MAP.get(Tier.WOOD).get(), pos)
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(pos))
    assertEquals(Tier.WOOD, tile.tier)
    assertFalse(tile.getConnection.isDummy)
    helper.succeed()
  }

  def woodTypeContents(helper: GameTestHelper): Unit = {
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(new BlockPos(0, 2, 0)))
    val content = tile.getConnection.getContent.getOrElse(fail("Content is empty!"))
    assertFalse(content.isEmpty)
    assertEquals(FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(3000)), content)
    helper.succeed()
  }

  def assumptionStone(helper: GameTestHelper): Unit = {
    val pos = new BlockPos(0, 2, 1)
    helper.assertBlockPresent(FluidTank.TANK_MAP.get(Tier.STONE).get(), pos)
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(pos))
    assertEquals(Tier.STONE, tile.tier)
    assertFalse(tile.getConnection.isDummy)
    helper.succeed()
  }

  def stoneTypeContents(helper: GameTestHelper): Unit = {
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(new BlockPos(0, 2, 1)))
    val content = tile.getConnection.getContent.getOrElse(fail("Content is empty!"))
    assertFalse(content.isEmpty)
    assertEquals(FluidAmountUtil.BUCKET_LAVA.setAmount(GenericUnit.fromForge(24000)), content)

    val tanks = tile.getConnection.getHandler.getTank
    assertEquals(2, tanks.size)
    helper.succeed()
  }

  def assumptionCopper(helper: GameTestHelper): Unit = {
    val pos = new BlockPos(1, 2, 2)
    helper.assertBlockPresent(FluidTank.TANK_MAP.get(Tier.COPPER).get(), pos)
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(pos))
    assertEquals(Tier.COPPER, tile.tier)
    assertFalse(tile.getConnection.isDummy)
    assertFalse(tile.getConnection.hasCreative)
    helper.succeed()
  }

  def copperTypeContents(helper: GameTestHelper): Unit = {
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(new BlockPos(1, 2, 2)))
    val content = tile.getConnection.getContent.getOrElse(fail("Content is empty!"))
    assertFalse(content.isEmpty)
    assertEquals(FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(30000)), content)

    val tanks = tile.getConnection.getHandler.getTank
    assertEquals(2, tanks.size)
    helper.succeed()
  }

  def assumptionStar(helper: GameTestHelper): Unit = {
    val pos = new BlockPos(1, 2, 1)
    helper.assertBlockPresent(FluidTank.TANK_MAP.get(Tier.STAR).get(), pos)
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(pos))
    assertEquals(Tier.STAR, tile.tier)
    assertFalse(tile.getConnection.isDummy)
    assertTrue(tile.getConnection.hasCreative)
    assertFalse(tile.getConnection.hasVoid)
    helper.succeed()
  }

  def starTypeContents(helper: GameTestHelper): Unit = {
    val tile = assertInstanceOf(classOf[TileTankForge], helper.getBlockEntity(new BlockPos(1, 2, 1)))
    val content = tile.getConnection.getContent.getOrElse(fail("Content is empty!"))
    assertFalse(content.isEmpty)
    assertTrue(content.contentEqual(FluidAmountUtil.BUCKET_LAVA))

    val tanks = tile.getConnection.getHandler.getTank
    assertEquals(3, tanks.size)
    helper.succeed()
  }
}
