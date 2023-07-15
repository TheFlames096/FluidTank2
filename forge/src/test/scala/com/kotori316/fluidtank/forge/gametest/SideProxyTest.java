package com.kotori316.fluidtank.forge.gametest;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.forge.FluidTank;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.kotori316.testutil.GameTestUtil.NO_PLACE_STRUCTURE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@GameTestHolder(FluidTankCommon.modId)
@PrefixGameTestTemplate(value = false)
final class SideProxyTest {

    @GameTest(template = NO_PLACE_STRUCTURE)
    void checkProxyClass(GameTestHelper helper) throws ReflectiveOperationException {
        var clazz = Class.forName("com.kotori316.fluidtank.forge.SideProxy$ServerProxy");
        assertEquals(clazz, FluidTank.proxy.getClass());
        helper.succeed();
    }
}
