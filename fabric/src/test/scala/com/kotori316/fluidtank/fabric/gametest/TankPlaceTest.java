package com.kotori316.fluidtank.fabric.gametest;

import com.google.common.base.CaseFormat;
import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.tank.Tier;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings({"unused"})
public final class TankPlaceTest implements FabricGameTest {
    private static final String BATCH_NAME = "tank_place_test";

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

    void checkBatchName() {
        Assertions.assertEquals(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClass().getSimpleName()),
                BATCH_NAME);
    }

    @GameTestGenerator
    public List<TestFunction> notRemovedByFluid() {
        return Stream.of(Tier.values())
                .filter(Predicate.isEqual(Tier.INVALID).negate())
                .flatMap(t -> Stream.of(Blocks.LAVA, Blocks.WATER).map(b -> Pair.of(t, b)))
                .map(p -> GameTestUtil.createWithStructure(FluidTankCommon.modId, BATCH_NAME,
                        "%s_%s_%s".formatted(BATCH_NAME, p.getKey().toString(), p.getValue().getName().getString()).toLowerCase(Locale.ROOT),
                        "check_water",
                        g -> notRemovedByFluid(g, p.getKey(), p.getValue()))
                ).toList();
    }

    private void notRemovedByFluid(GameTestHelper helper, Tier tier, Block fluid) {
        var pos = new BlockPos(4, 2, 4);
        helper.startSequence()
                .thenExecute(() -> TankTest.placeTank(helper, pos, tier))
                .thenExecuteAfter(1, () -> helper.setBlock(pos.west(), fluid))
                .thenIdle(40)
                .thenExecute(() -> helper.assertBlockPresent(fluid, pos.west().north()))
                .thenExecute(() -> helper.assertBlockPresent(TankTest.getBlock(tier), pos))
                .thenSucceed();
    }
}
