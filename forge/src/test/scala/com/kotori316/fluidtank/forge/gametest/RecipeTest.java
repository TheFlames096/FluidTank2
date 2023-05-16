package com.kotori316.fluidtank.forge.gametest;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.support.ReflectionSupport;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.forge.FluidTank;
import com.kotori316.fluidtank.forge.recipe.RecipeInventoryUtil;
import com.kotori316.fluidtank.forge.recipe.TierRecipeForge;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.testutil.GameTestUtil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unused")
@GameTestHolder(FluidTankCommon.modId)
@PrefixGameTestTemplate(value = false)
final class RecipeTest {

    @GameTestGenerator
    List<TestFunction> generator() {
        // no args
        var noArgs = Stream.of(getClass().getDeclaredMethods())
            .filter(m -> m.getReturnType() == Void.TYPE)
            .filter(m -> m.getParameterCount() == 0)
            .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.STATIC)) == 0)
            .map(m -> GameTestUtil.create(FluidTankCommon.modId, "recipe_test",
                getClass().getSimpleName() + "_" + m.getName(),
                () -> ReflectionSupport.invokeMethod(m, this)));
        var withHelper = Stream.of(getClass().getDeclaredMethods())
            .filter(m -> m.getReturnType() == Void.TYPE)
            .filter(m -> Arrays.equals(m.getParameterTypes(), new Class<?>[]{GameTestHelper.class}))
            .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.STATIC)) == 0)
            .map(m -> GameTestUtil.create(FluidTankCommon.modId, "recipe_test",
                getClass().getSimpleName() + "_" + m.getName(),
                g -> ReflectionSupport.invokeMethod(m, this, g)));
        return Stream.concat(noArgs, withHelper).toList();
    }

    @NotNull
    private static TierRecipeForge getRecipe() {
        return new TierRecipeForge(new ResourceLocation(FluidTankCommon.modId, "test1"), Tier.STONE,
            Ingredient.of(FluidTank.TANK_MAP.get(Tier.WOOD).get()), Ingredient.of(Tags.Items.STONE)
        );
    }

    void createInstance() {
        TierRecipeForge recipe = getRecipe();
        assertNotNull(recipe);
    }

    void match1() {
        var recipe = getRecipe();
        assertTrue(recipe.matches(RecipeInventoryUtil.getInv("tst", "s s", "tst", CollectionConverters.asScala(Map.of(
            't', new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get()),
            's', new ItemStack(Items.STONE)
        ))), null));
    }

    void match2() {
        var recipe = getRecipe();
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get());
        RecipeInventoryUtil.getFluidHandler(stack).fill(FluidAmountUtil.BUCKET_WATER(), true);

        assertTrue(recipe.matches(RecipeInventoryUtil.getInv("tst", "s s", "tst", CollectionConverters.asScala(Map.of(
            't', stack,
            's', new ItemStack(Items.STONE)
        ))), null));
    }

    void match3() {
        var recipe = getRecipe();
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get());
        RecipeInventoryUtil.getFluidHandler(stack).fill(FluidAmountUtil.BUCKET_WATER(), true);

        assertTrue(recipe.matches(RecipeInventoryUtil.getInv("tsk", "s s", "kst", CollectionConverters.asScala(Map.of(
            't', stack,
            'k', new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get()),
            's', new ItemStack(Items.STONE)
        ))), null));
    }

    void notMatch4() {
        var recipe = getRecipe();
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get());
        RecipeInventoryUtil.getFluidHandler(stack).fill(FluidAmountUtil.BUCKET_WATER(), true);
        var stack2 = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get());
        RecipeInventoryUtil.getFluidHandler(stack2).fill(FluidAmountUtil.BUCKET_LAVA(), true);

        assertFalse(recipe.matches(RecipeInventoryUtil.getInv("tsk", "s s", "kst", CollectionConverters.asScala(Map.of(
            't', stack,
            'k', stack2,
            's', new ItemStack(Items.STONE)
        ))), null));
    }

    void notMatch5() {
        var recipe = getRecipe();
        assertFalse(recipe.matches(RecipeInventoryUtil.getInv("tst", "s s", "ts ", CollectionConverters.asScala(Map.of(
            't', new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD).get()),
            's', new ItemStack(Items.STONE)
        ))), null));
    }

}
