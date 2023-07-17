package com.kotori316.fluidtank.forge.gametest

import cats.implicits.catsSyntaxSemigroup
import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.contents.GenericUnit
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, PotionType}
import com.kotori316.fluidtank.forge.BeforeMC.assertEqualHelper
import com.kotori316.fluidtank.forge.fluid.ForgeConverter.*
import com.kotori316.fluidtank.forge.tank.TileTankForge
import com.kotori316.fluidtank.tank.Tier
import com.kotori316.testutil.GameTestUtil
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.{GameTest, GameTestGenerator, GameTestHelper, TestFunction}
import net.minecraft.world.item.alchemy.Potions
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.gametest.{GameTestHolder, PrefixGameTestTemplate}
import org.junit.jupiter.api.Assertions.{assertDoesNotThrow, assertEquals, assertNotNull, assertTrue}
import org.junit.platform.commons.support.ReflectionSupport

import scala.jdk.javaapi.CollectionConverters

@GameTestHolder(FluidTankCommon.modId)
@PrefixGameTestTemplate(value = false)
class TankFluidHandlerTest {
  private final val BATCH = "defaultBatch"

  @GameTestGenerator
  def generator(): java.util.List[TestFunction] = {
    val noArgs = getClass.getDeclaredMethods.toSeq
      .filter(m => m.getReturnType == Void.TYPE)
      .filter(m => !m.isAnnotationPresent(classOf[GameTest]))
      .filter(m => m.getParameterCount == 0)
      .map { m =>
        val test: Runnable = () => ReflectionSupport.invokeMethod(m, this)
        GameTestUtil.create(FluidTankCommon.modId, BATCH, getClass.getSimpleName + "_" + m.getName,
          test
        )
      }

    val withHelper = getClass.getDeclaredMethods.toSeq
      .filter(m => m.getReturnType == Void.TYPE)
      .filter(m => !m.isAnnotationPresent(classOf[GameTest]))
      .filter(m => m.getParameterTypes.toSeq == Seq(classOf[GameTestHelper]))
      .map { m =>
        val test: java.util.function.Consumer[GameTestHelper] = g => ReflectionSupport.invokeMethod(m, this, g)
        GameTestUtil.create(FluidTankCommon.modId, BATCH, getClass.getSimpleName + "_" + m.getName,
          test
        )
      }

    CollectionConverters.asJava(noArgs ++ withHelper)
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
