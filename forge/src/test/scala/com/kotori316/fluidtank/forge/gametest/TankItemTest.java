package com.kotori316.fluidtank.forge.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.forge.FluidTank;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.testutil.GameTestUtil;

@GameTestHolder(FluidTankCommon.modId)
@PrefixGameTestTemplate(value = false)
final class TankItemTest {

    @GameTest(template = GameTestUtil.NO_PLACE_STRUCTURE)
    void tankItemHasCap(GameTestHelper helper) {
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get());
        var handler = FluidUtil.getFluidHandler(stack);
        Assertions.assertTrue(handler.isPresent());

        helper.succeed();
    }

    @GameTest(template = GameTestUtil.NO_PLACE_STRUCTURE)
    void tankItemCheckNBT(GameTestHelper helper) {
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get());
        var handler = FluidUtil.getFluidHandler(stack);
        handler.ifPresent(h -> h.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE));

        Assertions.assertNotNull(BlockItem.getBlockEntityData(stack));

        helper.succeed();
    }
}
