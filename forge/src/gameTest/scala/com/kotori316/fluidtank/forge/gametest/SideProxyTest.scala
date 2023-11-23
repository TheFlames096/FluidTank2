package com.kotori316.fluidtank.forge.gametest

import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.forge.FluidTank
import com.kotori316.testutil.GameTestUtil
import net.minecraft.gametest.framework.{GameTestGenerator, GameTestHelper, TestFunction}
import net.minecraftforge.api.distmarker.Dist.*
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.gametest.GameTestHolder
import org.junit.jupiter.api.Assertions.assertEquals

@GameTestHolder(FluidTankCommon.modId)
final class SideProxyTest {
  @GameTestGenerator
  def generator(): java.util.List[TestFunction] = {
    GetGameTestMethods.getTests(getClass, this, GetGameTestMethods.DEFAULT_BATCH, GameTestUtil.NO_PLACE_STRUCTURE)
  }

  def checkProxyClass(helper: GameTestHelper): Unit = {
    val clazz = FMLEnvironment.dist match {
      case DEDICATED_SERVER => Class.forName("com.kotori316.fluidtank.forge.SideProxy$ServerProxy")
      case CLIENT => Class.forName("com.kotori316.fluidtank.forge.SideProxy$ClientProxy")
    }
    assertEquals(clazz, FluidTank.proxy.getClass)
    helper.succeed()
  }
}
