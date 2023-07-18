package com.kotori316.fluidtank.fabric.gametest;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.PlatformAccess;
import com.kotori316.fluidtank.contents.GenericAmount;
import com.kotori316.fluidtank.contents.GenericUnit;
import com.kotori316.fluidtank.fabric.FabricPlatformAccessTest;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.fluids.FluidLike;
import com.kotori316.fluidtank.fluids.PotionType;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.platform.commons.support.ReflectionSupport;
import scala.Option;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public class PlatformAccessTest implements FabricGameTest {
    private static final PlatformAccess ACCESS = FabricPlatformAccessTest.ACCESS;
    private static final String BATCH_NAME = "platform_test";

    @GameTestGenerator
    public List<TestFunction> generator() {
        // no args
        var batch = BATCH_NAME;
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

    void fillBucketWater(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var stack = new ItemStack(Items.BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var transferred = ACCESS.fillItem(FluidAmountUtil.BUCKET_WATER(), stack, player, InteractionHand.MAIN_HAND, true);
        assertEquals(FluidAmountUtil.BUCKET_WATER(), transferred.moved());
        assertEquals(Items.WATER_BUCKET, transferred.toReplace().getItem(), "Transfer result");
        assertEquals(Items.WATER_BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "Player item");

        helper.succeed();
    }

    void fillBucketLava(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var stack = new ItemStack(Items.BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var transferred = ACCESS.fillItem(FluidAmountUtil.BUCKET_LAVA(), stack, player, InteractionHand.MAIN_HAND, true);
        assertFalse(transferred.shouldMove(), "Fabric module already moved items");
        assertEquals(FluidAmountUtil.BUCKET_LAVA(), transferred.moved());
        assertEquals(Items.LAVA_BUCKET, transferred.toReplace().getItem(), "Transfer result");
        assertEquals(Items.LAVA_BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "Player item");

        helper.succeed();
    }

    void drainWaterBucket(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var stack = new ItemStack(Items.WATER_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var transferred = ACCESS.drainItem(FluidAmountUtil.BUCKET_WATER(), stack, player, InteractionHand.MAIN_HAND, true);
        assertFalse(transferred.shouldMove(), "Fabric module already moved items");
        assertEquals(FluidAmountUtil.BUCKET_WATER(), transferred.moved());
        assertEquals(Items.BUCKET, transferred.toReplace().getItem(), "Transfer result");
        assertEquals(Items.BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "Player item");

        helper.succeed();
    }

    void drainLavaBucket(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var stack = new ItemStack(Items.LAVA_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var transferred = ACCESS.drainItem(FluidAmountUtil.BUCKET_LAVA(), stack, player, InteractionHand.MAIN_HAND, true);
        assertFalse(transferred.shouldMove(), "Fabric module already moved items");
        assertEquals(FluidAmountUtil.BUCKET_LAVA(), transferred.moved());
        assertEquals(Items.BUCKET, transferred.toReplace().getItem(), "Transfer result");
        assertEquals(Items.BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "Player item");

        helper.succeed();
    }

    static GenericAmount<FluidLike> potionFluid(PotionType potionType, Potion potion) {
        var dummy = PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
        return FluidAmountUtil.from(FluidLike.of(potionType), GenericUnit.ONE_BOTTLE(),
                Option.apply(dummy.getTag()));
    }

    void fillFail1(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var stack = new ItemStack(Items.BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var transferred = ACCESS.drainItem(potionFluid(PotionType.NORMAL, Potions.WATER),
                stack, player, InteractionHand.MAIN_HAND, true);

        assertTrue(transferred.moved().isEmpty(), "Nothing moved");
        assertEquals(Items.BUCKET, transferred.toReplace().getItem(), "Transfer result");
        assertEquals(Items.BUCKET, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "Player item");

        helper.succeed();
    }

    void fillFail2(GameTestHelper helper) {
        var player = helper.makeMockSurvivalPlayer();
        var stack = new ItemStack(Items.GLASS_BOTTLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        var transferred = ACCESS.drainItem(FluidAmountUtil.BUCKET_WATER(),
                stack, player, InteractionHand.MAIN_HAND, true);

        assertTrue(transferred.moved().isEmpty(), "Nothing moved");
        assertEquals(Items.GLASS_BOTTLE, transferred.toReplace().getItem(), "Transfer result");
        assertEquals(Items.GLASS_BOTTLE, player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), "Player item");

        helper.succeed();
    }

    static Stream<Pair<PotionType, Potion>> potions() {
        return Stream.of(PotionType.values()).flatMap(t -> Stream.of(
                Potions.WATER, Potions.EMPTY, Potions.NIGHT_VISION, Potions.LONG_NIGHT_VISION
        ).map(p ->
                Pair.of(t, p)
        ));
    }

    @GameTestGenerator
    public List<TestFunction> fillPotion() {
        return potions().map(pp -> GameTestUtil.createWithStructure(FluidTankCommon.modId, BATCH_NAME,
                "fill_potion_%s_%s".formatted(pp.getKey().name(), pp.getValue().getName("")).toLowerCase(Locale.ROOT),
                GameTestUtil.NO_PLACE_STRUCTURE,
                g -> FillDrainTest.fillPotion(g, pp.getKey(), pp.getValue()))).toList();
    }

    @GameTestGenerator
    public List<TestFunction> fillFailPotionWithAmount() {
        return potions().map(pp -> GameTestUtil.createWithStructure(FluidTankCommon.modId, BATCH_NAME,
                "fill_fail1_potion_%s_%s".formatted(pp.getKey().name(), pp.getValue().getName("")).toLowerCase(Locale.ROOT),
                GameTestUtil.NO_PLACE_STRUCTURE,
                g -> FillDrainTest.fillFailPotionWithAmount(g, pp.getKey(), pp.getValue()))).toList();
    }

    @GameTestGenerator
    public List<TestFunction> drainPotion() {
        return potions().map(pp -> GameTestUtil.createWithStructure(FluidTankCommon.modId, BATCH_NAME,
                "drain_potion_%s_%s".formatted(pp.getKey().name(), pp.getValue().getName("")).toLowerCase(Locale.ROOT),
                GameTestUtil.NO_PLACE_STRUCTURE,
                g -> FillDrainTest.drainPotion(g, pp.getKey(), pp.getValue()))).toList();
    }

    @GameTestGenerator
    public List<TestFunction> drainFailPotionWithAmount() {
        return potions().map(pp -> GameTestUtil.createWithStructure(FluidTankCommon.modId, BATCH_NAME,
                "drain_fail1_potion_%s_%s".formatted(pp.getKey().name(), pp.getValue().getName("")).toLowerCase(Locale.ROOT),
                GameTestUtil.NO_PLACE_STRUCTURE,
                g -> FillDrainTest.drainPotion(g, pp.getKey(), pp.getValue()))).toList();
    }

    static class FillDrainTest {
        static void fillPotion(GameTestHelper helper, PotionType potionType, Potion potion) {
            var player = helper.makeMockSurvivalPlayer();
            var stack = new ItemStack(Items.GLASS_BOTTLE);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);

            var toFill = potionFluid(potionType, potion);
            var transferred = ACCESS.fillItem(toFill,
                    stack, player, InteractionHand.MAIN_HAND, true);
            assertFalse(transferred.shouldMove(), "Fabric module already moved items");
            assertEquals(toFill, transferred.moved());
            var expected = PotionUtils.setPotion(new ItemStack(potionType.getItem()), potion);
            assertTrue(ItemStack.isSameItemSameTags(expected, transferred.toReplace()),
                    "transferred, Ex: %s, Ac: %s".formatted(expected.getTag(), transferred.toReplace().getTag()));
            assertTrue(ItemStack.isSameItemSameTags(expected, player.getMainHandItem()),
                    "getMainHandItem, Ex: %s, Ac: %s".formatted(expected.getTag(), player.getMainHandItem().getTag()));

            helper.succeed();
        }

        static void fillFailPotionWithAmount(GameTestHelper helper, PotionType potionType, Potion potion) {
            var player = helper.makeMockSurvivalPlayer();
            var stack = new ItemStack(Items.GLASS_BOTTLE);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);

            var toFill = potionFluid(potionType, potion).setAmount(GenericUnit.fromFabric(26999));
            var transferred = ACCESS.fillItem(toFill,
                    stack, player, InteractionHand.MAIN_HAND, true);
            assertFalse(transferred.shouldMove(), "Fabric module already moved items");
            assertTrue(transferred.moved().isEmpty());
            var expected = new ItemStack(Items.GLASS_BOTTLE);
            assertTrue(ItemStack.isSameItemSameTags(expected, transferred.toReplace()),
                    "transferred, Ex: %s, Ac: %s".formatted(expected.getTag(), transferred.toReplace().getTag()));
            assertTrue(ItemStack.isSameItemSameTags(expected, player.getMainHandItem()),
                    "getMainHandItem, Ex: %s, Ac: %s".formatted(expected.getTag(), player.getMainHandItem().getTag()));

            helper.succeed();
        }

        static void drainPotion(GameTestHelper helper, PotionType potionType, Potion potion) {
            var player = helper.makeMockSurvivalPlayer();
            var stack = PotionUtils.setPotion(new ItemStack(potionType.getItem()), potion);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);

            var toDrain = potionFluid(potionType, potion);
            var transferred = ACCESS.drainItem(toDrain, stack, player, InteractionHand.MAIN_HAND, true);

            assertFalse(transferred.shouldMove(), "Fabric module already moved items");
            assertEquals(toDrain, transferred.moved());

            var expected = Items.GLASS_BOTTLE.getDefaultInstance();
            assertTrue(ItemStack.isSameItemSameTags(expected, transferred.toReplace()),
                    "transferred, Ex: %s, Ac: %s".formatted(expected.getTag(), transferred.toReplace().getTag()));
            assertTrue(ItemStack.isSameItemSameTags(expected, player.getMainHandItem()),
                    "getMainHandItem, Ex: %s, Ac: %s".formatted(expected.getTag(), player.getMainHandItem().getTag()));

            helper.succeed();
        }

        static void drainFailPotionWithAmount(GameTestHelper helper, PotionType potionType, Potion potion) {
            var player = helper.makeMockSurvivalPlayer();
            var stack = PotionUtils.setPotion(new ItemStack(potionType.getItem()), potion);
            var expected = stack.copy();
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);

            var toDrain = potionFluid(potionType, potion).setAmount(GenericUnit.fromFabric(26999));
            var transferred = ACCESS.drainItem(toDrain, stack, player, InteractionHand.MAIN_HAND, true);

            assertFalse(transferred.shouldMove(), "Fabric module already moved items");
            assertTrue(transferred.moved().isEmpty());

            assertTrue(ItemStack.isSameItemSameTags(expected, transferred.toReplace()),
                    "transferred, Ex: %s, Ac: %s".formatted(expected.getTag(), transferred.toReplace().getTag()));
            assertTrue(ItemStack.isSameItemSameTags(expected, player.getMainHandItem()),
                    "getMainHandItem, Ex: %s, Ac: %s".formatted(expected.getTag(), player.getMainHandItem().getTag()));

            helper.succeed();
        }
    }
}
