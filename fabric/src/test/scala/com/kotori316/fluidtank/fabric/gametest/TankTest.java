package com.kotori316.fluidtank.fabric.gametest;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.contents.GenericUnit;
import com.kotori316.fluidtank.fabric.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.tank.BlockTank;
import com.kotori316.fluidtank.tank.TankPos;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.fluidtank.tank.TileTank;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public final class TankTest implements FabricGameTest {
    @GameTestGenerator
    public List<TestFunction> fillTest() {
        // no args
        final var batch = "defaultBatch";
        var noArgs = Stream.of(getClass().getDeclaredMethods())
                .filter(m -> m.getReturnType() == Void.TYPE)
                .filter(m -> !m.isAnnotationPresent(GameTest.class))
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.STATIC)) == 0)
                .map(m -> GameTestUtil.create(FluidTankCommon.modId, batch,
                        getClass().getSimpleName() + "_" + m.getName(),
                        () -> ReflectionSupport.invokeMethod(m, this)));
        var withHelper = Stream.of(getClass().getDeclaredMethods())
                .filter(m -> m.getReturnType() == Void.TYPE)
                .filter(m -> !m.isAnnotationPresent(GameTest.class))
                .filter(m -> Arrays.equals(m.getParameterTypes(), new Class<?>[]{GameTestHelper.class}))
                .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.STATIC)) == 0)
                .map(m -> GameTestUtil.create(FluidTankCommon.modId, batch,
                        getClass().getSimpleName() + "_" + m.getName(),
                        g -> ReflectionSupport.invokeMethod(m, this, g)));
        return Stream.concat(noArgs, withHelper).toList();
    }

    static BlockTank getBlock(Tier tier) {
        return switch (tier) {
            case CREATIVE -> FluidTank.BLOCK_CREATIVE_TANK;
            case VOID -> FluidTank.BLOCK_VOID_TANK;
            default -> FluidTank.TANK_MAP.get(tier);
        };
    }

    static TileTank placeTank(GameTestHelper helper, BlockPos pos, Tier tier) {
        var block = getBlock(tier);
        helper.setBlock(pos, block);
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

    void capacityWithCreative(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);
        placeTank(helper, basePos.above(1), Tier.CREATIVE);

        assertEquals(GenericUnit.CREATIVE_TANK(), tile.getConnection().capacity());
        helper.succeed();
    }

    void amountWithCreative1(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);
        placeTank(helper, basePos.above(1), Tier.CREATIVE);
        tile.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        assertEquals(GenericUnit.CREATIVE_TANK(), tile.getConnection().amount());
        helper.succeed();
    }

    void amountWithCreative2(GameTestHelper helper) {
        var basePos = BlockPos.ZERO.above();
        var tile = placeTank(helper, basePos, Tier.WOOD);
        placeTank(helper, basePos.above(1), Tier.CREATIVE);
        placeTank(helper, basePos.above(2), Tier.CREATIVE);
        tile.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        assertEquals(GenericUnit.CREATIVE_TANK(), tile.getConnection().amount());
        helper.succeed();
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        method.setAccessible(true);
        FabricGameTest.super.invokeTestMethod(context, method);
    }
}
