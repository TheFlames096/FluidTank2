package com.kotori316.fluidtank.forge;

import com.kotori316.fluidtank.PlatformAccess;
import com.kotori316.fluidtank.contents.GenericUnit;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.fluids.FluidLike;
import com.kotori316.fluidtank.fluids.PotionType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import scala.Option;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForgePlatformAccessTest extends BeforeMC {
    public static final PlatformAccess ACCESS = new ForgePlatformAccess();

    @Nested
    class FluidContentTest {
        @Test
        void getWaterBucket() {
            var fluid = ACCESS.getFluidContained(new ItemStack(Items.WATER_BUCKET));
            assertEquals(FluidAmountUtil.BUCKET_WATER(), fluid);
        }

        @Test
        void getLavaBucket() {
            var fluid = ACCESS.getFluidContained(new ItemStack(Items.LAVA_BUCKET));
            assertEquals(FluidAmountUtil.BUCKET_LAVA(), fluid);
        }

        @Test
        void getEmptyBucket() {
            var fluid = ACCESS.getFluidContained(new ItemStack(Items.BUCKET));
            assertEquals(FluidAmountUtil.EMPTY(), fluid);
        }

        @Test
        void getEmptyBottle() {
            var fluid = ACCESS.getFluidContained(new ItemStack(Items.GLASS_BOTTLE));
            assertEquals(FluidAmountUtil.EMPTY(), fluid);
        }

        @TestFactory
        List<DynamicTest> getPotion() {
            return Stream.of(Potions.WATER, Potions.EMPTY, Potions.NIGHT_VISION)
                    .flatMap(p -> Stream.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION).map(i ->
                            DynamicTest.dynamicTest(i + " " + p.getName(""), () -> {
                                var potion = PotionUtils.setPotion(new ItemStack(i), p);
                                var fluid = ACCESS.getFluidContained(potion);
                                var expected = FluidAmountUtil.from(FluidLike.of(PotionType.fromItemUnsafe(i)),
                                        GenericUnit.ONE_BOTTLE(), Option.apply(potion.getTag()));

                                assertEquals(expected, fluid);
                            }))).toList();
        }
    }

    @Nested
    class IsContainerTest {
        @TestFactory
        List<DynamicTest> containers() {
            return Stream.concat(
                    Stream.of(Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.GLASS_BOTTLE).map(ItemStack::new),
                    Stream.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION).flatMap(
                            i -> ForgeRegistries.POTIONS.getValues().stream().map(p -> PotionUtils.setPotion(new ItemStack(i), p)))
            ).map(s -> DynamicTest.dynamicTest(s.toString() + " " + s.getTag(),
                    () -> assertTrue(ACCESS.isFluidContainer(s)))).toList();
        }
    }
}
