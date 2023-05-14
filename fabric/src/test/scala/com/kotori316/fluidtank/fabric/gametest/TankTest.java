package com.kotori316.fluidtank.fabric.gametest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.fabric.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.tank.TankPos;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.fluidtank.tank.TileTank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TankTest implements FabricGameTest {
    @GameTestGenerator
    public List<TestFunction> fillTest() {
        final var prefix = getClass().getSimpleName().toLowerCase(Locale.ROOT) + "_";
        final var batch = "defaultBatch";
        List<TestFunction> testFunctions = List.of(
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "fill1", this::fill1),
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "fill2", this::fill2),
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "drain1", this::drain1),
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "drain2", this::drain2),
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "drain3", this::drain3),
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "fillFail1", this::fillFail1),
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "place", this::place),
            GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "place2", this::place2)
        );
        var expectCount = Stream.of(getClass().getDeclaredMethods())
            .filter(m -> m.getReturnType() == Void.TYPE)
            .filter(m -> m.getParameterCount() > 0)
            .filter(m -> m.getParameterTypes()[0] == GameTestHelper.class)
            .filter(m -> !m.getName().equals("invokeTestMethod")) // fabric override method
            .count();
        if (expectCount != testFunctions.size()) {
            // Not all test registered.
            var copy = new ArrayList<>(testFunctions);
            copy.add(GameTestUtil.create(FluidTankCommon.modId, batch, prefix + "assumption_fail", g ->
                g.fail("Not all test registered in TankTest, expect: %d, actual: %d".formatted(expectCount, testFunctions.size()))));
            return copy;
        }
        return testFunctions;
    }

    TileTank placeTank(GameTestHelper helper, BlockPos pos, Tier tier) {
        helper.setBlock(pos, FluidTank.TANK_MAP.get(tier));
        var tile = helper.getBlockEntity(pos);
        if (tile instanceof TileTank tileTank) {
            tileTank.onBlockPlacedBy();
            return tileTank;
        } else {
            throw new GameTestAssertPosException("Expect tank tile", helper.absolutePos(pos), pos, helper.getTick());
        }
    }

    void place(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);

        assertFalse(tile.getConnection().isDummy());
        helper.assertBlockPresent(FluidTank.TANK_MAP.get(Tier.WOOD), basePos);
        helper.assertBlockProperty(basePos, TankPos.TANK_POS_PROPERTY, TankPos.SINGLE);
        helper.succeed();
    }

    void place2(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile1 = placeTank(helper, basePos, Tier.WOOD);
        var tile2 = placeTank(helper, basePos.above(), Tier.STONE);

        var c1 = tile1.getConnection();
        var c2 = tile2.getConnection();
        assertFalse(c1.isDummy());
        assertSame(c1, c2);
        assertEquals(2, c1.getTiles().size());
        helper.succeed();
    }

    void fill1(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);

        var player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        helper.useBlock(basePos, player);

        assertEquals(FluidAmountUtil.BUCKET_WATER(), tile.getTank().content());
        assertEquals(Items.WATER_BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem());
        helper.succeed();
    }

    void fill2(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.STONE);

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        helper.useBlock(basePos, player);
        assertEquals(FluidAmountUtil.BUCKET_WATER(), tile.getTank().content());
        assertEquals(Items.BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(),
            "Inventory item must be consumed and replaced.");
        helper.succeed();
    }

    void drain1(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);
        tile.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var player = helper.makeMockPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        helper.useBlock(basePos, player);

        assertTrue(tile.getTank().isEmpty());
        assertEquals(Items.BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "In creative, the item must not change.");
        helper.succeed();
    }

    void drain2(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);
        tile.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        helper.useBlock(basePos, player);

        assertTrue(tile.getTank().isEmpty());
        assertEquals(Items.WATER_BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "In survival, the item must change.");
        helper.succeed();
    }

    void drain3(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);
        tile.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET, 10));
        assertEquals(0, player.getInventory().countItem(Items.WATER_BUCKET), "Test assumption");
        helper.useBlock(basePos, player);

        assertTrue(tile.getTank().isEmpty());
        assertEquals(Items.BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "In survival, the item must change.");
        assertEquals(9, player.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "In survival, the item must change.");
        assertEquals(1, player.getInventory().countItem(Items.WATER_BUCKET));
        helper.succeed();
    }

    void fillFail1(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);
        tile.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
        helper.useBlock(basePos, player);

        assertEquals(FluidAmountUtil.BUCKET_WATER(), tile.getTank().content());
        assertEquals(Items.LAVA_BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem());
        helper.succeed();
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        method.setAccessible(true);
        FabricGameTest.super.invokeTestMethod(context, method);
    }
}
