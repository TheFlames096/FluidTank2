package com.kotori316.fluidtank.forge.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.forge.FluidTank;

import static com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@GameTestHolder(FluidTankCommon.modId)
@PrefixGameTestTemplate(value = false)
class SideProxyTest {

    @GameTest(template = EMPTY_STRUCTURE)
    void checkProxyClass(GameTestHelper helper) throws ReflectiveOperationException {
        var clazz = Class.forName("com.kotori316.fluidtank.forge.SideProxy$ServerProxy");
        assertEquals(clazz, FluidTank.proxy.getClass());
        helper.succeed();
    }
}
