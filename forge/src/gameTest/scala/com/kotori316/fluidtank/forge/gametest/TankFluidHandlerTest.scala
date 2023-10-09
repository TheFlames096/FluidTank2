package com.kotori316.fluidtank.forge.gametest

import cats.implicits.catsSyntaxSemigroup
import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.contents.GenericUnit
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, PotionType}
import com.kotori316.fluidtank.forge.fluid.ForgeConverter.*
import com.kotori316.fluidtank.forge.gametest.GetGameTestMethods.assertEqualHelper
import com.kotori316.fluidtank.forge.tank.TileTankForge
import com.kotori316.fluidtank.tank.Tier
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.{GameTestGenerator, GameTestHelper, TestFunction}
import net.minecraft.world.item.alchemy.Potions
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.gametest.GameTestHolder
import org.junit.jupiter.api.Assertions.{assertDoesNotThrow, assertEquals, assertNotNull, assertTrue}

@GameTestHolder(FluidTankCommon.modId)
class TankFluidHandlerTest {
  private final val BATCH = "defaultBatch"

  @GameTestGenerator
  def generator(): java.util.List[TestFunction] = {
    GetGameTestMethods.getTests(getClass, this, BATCH)
  }

  def testGetCapability(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]

    val cap = assertDoesNotThrow(() => tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null))
    assertNotNull(cap)
    helper.succeed()
  }

  def capacity(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]

    val cap = tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElseThrow(() => new AssertionError())
    assertEquals(4000, cap.getTankCapacity(0))
    helper.succeed()
  }

  def amount1(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]
    tile.getConnection.getHandler.fill(FluidAmountUtil.BUCKET_WATER, execute = true)

    val cap = tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElseThrow(() => new AssertionError())
    assertEquals(FluidAmountUtil.BUCKET_WATER.toStack, cap.getFluidInTank(0))
    helper.succeed()
  }

  def fillSimulate1(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]
    val handler = tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElseThrow(() => new AssertionError())

    val filled = handler.fill(FluidAmountUtil.BUCKET_WATER.toStack, FluidAction.SIMULATE)
    assertEquals(1000, filled)
    assertEquals(GenericUnit.ZERO, tile.getConnection.amount)
    helper.succeed()
  }

  def fillExecute1(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]
    TankTest.placeTank(helper, basePos.above(), Tier.STONE)
    val handler = tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElseThrow(() => new AssertionError())

    val toFill = FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(20000))
    val filled = handler.fill(toFill.toStack, FluidAction.EXECUTE)
    assertEquals(20000, filled)
    assertEqualHelper(Option(toFill), tile.getConnection.getContent)
    helper.succeed()
  }

  def potionTank(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]
    TankTest.placeTank(helper, basePos.above(), Tier.STONE)
    tile.getConnection.getHandler.fill(FluidAmountUtil.from(PotionType.SPLASH, Potions.NIGHT_VISION, GenericUnit.ONE_BUCKET), execute = true)

    val handler = tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElseThrow(() => new AssertionError())
    assertEquals(20000, handler.getTankCapacity(0))
    assertTrue(handler.getFluidInTank(0).isEmpty)
    helper.succeed()
  }

  def potionTank2(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]
    TankTest.placeTank(helper, basePos.above(), Tier.STONE)
    val content = FluidAmountUtil.from(PotionType.SPLASH, Potions.NIGHT_VISION, GenericUnit.ONE_BUCKET.combineN(3))
    tile.getConnection.getHandler.fill(content, execute = true)

    val handler = tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElseThrow(() => new AssertionError())
    val filled = handler.fill(FluidAmountUtil.BUCKET_WATER.toStack, FluidAction.SIMULATE)
    assertEquals(0, filled)
    assertEqualHelper(Option(content), tile.getConnection.getContent)
    helper.succeed()
  }

  def potionTank3(helper: GameTestHelper): Unit = {
    val basePos = BlockPos.ZERO.above()
    val tile = TankTest.placeTank(helper, basePos, Tier.WOOD).asInstanceOf[TileTankForge]
    TankTest.placeTank(helper, basePos.above(), Tier.STONE)
    val content = FluidAmountUtil.from(PotionType.SPLASH, Potions.NIGHT_VISION, GenericUnit.ONE_BUCKET.combineN(3))
    tile.getConnection.getHandler.fill(content, execute = true)

    val handler = tile.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElseThrow(() => new AssertionError())
    val drained = handler.drain(FluidAmountUtil.BUCKET_WATER.toStack, FluidAction.SIMULATE)
    assertTrue(drained.isEmpty)
    assertEqualHelper(Option(content), tile.getConnection.getContent)
    helper.succeed()
  }
}
