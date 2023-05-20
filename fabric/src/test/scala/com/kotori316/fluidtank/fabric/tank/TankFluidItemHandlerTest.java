package com.kotori316.fluidtank.fabric.tank;

import java.util.Objects;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.fluidtank.contents.GenericUnit;
import com.kotori316.fluidtank.contents.Tank;
import com.kotori316.fluidtank.contents.TankUtil;
import com.kotori316.fluidtank.fabric.BeforeMC;
import com.kotori316.fluidtank.fabric.FluidTank;
import com.kotori316.fluidtank.fabric.recipe.RecipeInventoryUtil;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.fluidtank.tank.TileTank;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("UnstableApiUsage")
final class TankFluidItemHandlerTest extends BeforeMC {

    @Test
    void createInstance() {
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD));
        var handler = new TankFluidItemHandler(Tier.WOOD, stack);
        assertAll(
            () -> assertEquals(0, handler.getAmount()),
            () -> assertEquals(FluidVariant.blank(), handler.getResource()),
            () -> assertEquals(4 * FluidConstants.BUCKET, handler.getCapacity())
        );
    }

    @Test
    void initialState1() {
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD));
        var tag = stack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG);
        tag.putString(TileTank.KEY_TIER(), Tier.WOOD.name());
        var tank = Tank.apply(FluidAmountUtil.BUCKET_WATER(), Tier.WOOD.getCapacity());
        tag.put(TileTank.KEY_TANK(), TankUtil.save(tank, FluidAmountUtil.access()));

        var handler = new TankFluidItemHandler(Tier.WOOD, stack);
        assertAll(
            () -> assertEquals(FluidConstants.BUCKET, handler.getAmount()),
            () -> assertEquals(FluidVariant.of(Fluids.WATER), handler.getResource()),
            () -> assertEquals(4 * FluidConstants.BUCKET, handler.getCapacity())
        );
    }

    @Test
    void initialState2() {
        var tier = Tier.STONE;
        var stack = new ItemStack(FluidTank.TANK_MAP.get(tier));
        var tag = stack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG);
        tag.putString(TileTank.KEY_TIER(), tier.name());
        var tank = Tank.apply(FluidAmountUtil.BUCKET_LAVA().setAmount(GenericUnit.fromForge(3000)), tier.getCapacity());
        tag.put(TileTank.KEY_TANK(), TankUtil.save(tank, FluidAmountUtil.access()));

        var handler = new TankFluidItemHandler(tier, stack);
        assertAll(
            () -> assertEquals(FluidConstants.BUCKET * 3, handler.getAmount()),
            () -> assertEquals(FluidVariant.of(Fluids.LAVA), handler.getResource()),
            () -> assertEquals(16 * FluidConstants.BUCKET, handler.getCapacity())
        );
    }

    @Nested
    class UtilTest {
        @ParameterizedTest
        @EnumSource(value = Tier.class, names = {"WOOD", "STONE", "IRON", "GOLD"})
        void getFilled(Tier tier) {
            var stack = RecipeInventoryUtil.getFilledTankStack(tier, FluidAmountUtil.BUCKET_LAVA());
            var tag = BlockItem.getBlockEntityData(stack);
            assertNotNull(tag);
            var expected = Tank.apply(FluidAmountUtil.BUCKET_LAVA(), tier.getCapacity());
            var actual = TankUtil.load(tag.getCompound(TileTank.KEY_TANK()), FluidAmountUtil.access());
            assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(tier.name(), tag.getString(TileTank.KEY_TIER()))
            );
        }
    }

    @Nested
    class FillTest {
        @Test
        void fillExecute() {
            var tier = Tier.WOOD;
            var stack = new ItemStack(FluidTank.TANK_MAP.get(tier));
            var handler = new TankFluidItemHandler(tier, stack);
            assertNull(BlockItem.getBlockEntityData(stack));

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidVariant.blank(), handler.getResource());
                handler.insert(FluidVariant.of(Fluids.WATER), 3 * FluidConstants.BUCKET, transaction);
                assertEquals(FluidVariant.of(Fluids.WATER), handler.getResource());
                assertEquals(3 * FluidConstants.BUCKET, handler.getAmount());

                var tag = BlockItem.getBlockEntityData(stack);
                assertNotNull(tag);
                transaction.commit();
            }
            assertEquals(FluidVariant.of(Fluids.WATER), handler.getResource());
            assertEquals(3 * FluidConstants.BUCKET, handler.getAmount());

            var tag = BlockItem.getBlockEntityData(stack);
            assertNotNull(tag);
            var expected = Tank.apply(FluidAmountUtil.BUCKET_WATER().setAmount(GenericUnit.fromForge(3000)), tier.getCapacity());
            var actual = TankUtil.load(tag.getCompound(TileTank.KEY_TANK()), FluidAmountUtil.access());
            assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(tier.name(), tag.getString(TileTank.KEY_TIER()))
            );
        }

        @Test
        void fillExecute2() {
            var tier = Tier.WOOD;
            var stack = RecipeInventoryUtil.getFilledTankStack(tier, FluidAmountUtil.BUCKET_WATER());
            var before = Objects.requireNonNull(BlockItem.getBlockEntityData(stack)).copy();
            var handler = new TankFluidItemHandler(tier, stack);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidVariant.of(Fluids.WATER), handler.getResource());
                assertEquals(FluidConstants.BUCKET, handler.getAmount());

                var inserted = handler.insert(FluidVariant.of(Fluids.WATER), 3 * FluidConstants.BUCKET, transaction);
                assertEquals(4 * FluidConstants.BUCKET, handler.getAmount());
                assertEquals(3 * FluidConstants.BUCKET, inserted);

                transaction.commit();
            }
            var tag = BlockItem.getBlockEntityData(stack);
            assertNotEquals(before, tag);
            assertNotNull(tag);
            var expected = Tank.apply(FluidAmountUtil.BUCKET_WATER().setAmount(GenericUnit.fromForge(4000)), tier.getCapacity());
            var actual = TankUtil.load(tag.getCompound(TileTank.KEY_TANK()), FluidAmountUtil.access());
            assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(tier.name(), tag.getString(TileTank.KEY_TIER()))
            );
        }

        @Test
        void fillSimulate() {
            var tier = Tier.WOOD;
            var stack = new ItemStack(FluidTank.TANK_MAP.get(tier));
            var handler = new TankFluidItemHandler(tier, stack);
            assertNull(BlockItem.getBlockEntityData(stack));

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidVariant.blank(), handler.getResource());
                var inserted = handler.insert(FluidVariant.of(Fluids.WATER), 3 * FluidConstants.BUCKET, transaction);
                assertEquals(FluidVariant.of(Fluids.WATER), handler.getResource());
                assertEquals(3 * FluidConstants.BUCKET, handler.getAmount());
                assertEquals(3 * FluidConstants.BUCKET, inserted);

                var tag = BlockItem.getBlockEntityData(stack);
                assertNotNull(tag);
                transaction.abort();
            }
            assertEquals(FluidVariant.blank(), handler.getResource());
            assertEquals(0, handler.getAmount());

            var tag = BlockItem.getBlockEntityData(stack);
            assertNull(tag);
        }

        @Test
        void fillSimulate2() {
            var tier = Tier.WOOD;
            var stack = RecipeInventoryUtil.getFilledTankStack(tier, FluidAmountUtil.BUCKET_WATER());
            var before = Objects.requireNonNull(BlockItem.getBlockEntityData(stack)).copy();
            var handler = new TankFluidItemHandler(tier, stack);

            try (Transaction transaction = Transaction.openOuter()) {
                assertEquals(FluidVariant.of(Fluids.WATER), handler.getResource());
                assertEquals(FluidConstants.BUCKET, handler.getAmount());

                var inserted = handler.insert(FluidVariant.of(Fluids.WATER), 3 * FluidConstants.BUCKET, transaction);
                assertEquals(4 * FluidConstants.BUCKET, handler.getAmount());
                assertEquals(3 * FluidConstants.BUCKET, inserted);

                transaction.abort();
            }
            assertEquals(before, BlockItem.getBlockEntityData(stack));
        }
    }
}
