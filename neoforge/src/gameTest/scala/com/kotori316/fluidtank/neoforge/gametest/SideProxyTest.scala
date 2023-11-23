package com.kotori316.fluidtank.neoforge.gametest

import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.neoforge.FluidTank
import com.kotori316.testutil.GameTestUtil
import net.minecraft.gametest.framework.{GameTestGenerator, GameTestHelper, TestFunction}
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.gametest.GameTestHolder
import org.junit.jupiter.api.Assertions.assertEquals

@GameTestHolder(FluidTankCommon.modId)
final class SideProxyTest {
  @GameTestGenerator
  def generator(): java.util.List[TestFunction] = {
    GetGameTestMethods.getTests(getClass, this, GetGameTestMethods.DEFAULT_BATCH, GameTestUtil.NO_PLACE_STRUCTURE)
  }

  def checkProxyClass(helper: GameTestHelper): Unit = {
    val clazz = FMLEnvironment.dist match {
      case Dist.DEDICATED_SERVER => Class.forName("com.kotori316.fluidtank.neoforge.SideProxy$ServerProxy")
      case Dist.CLIENT => Class.forName("com.kotori316.fluidtank.neoforge.SideProxy$ClientProxy")
    }
    assertEquals(clazz, FluidTank.proxy.getClass)
    helper.succeed()
  }
}
