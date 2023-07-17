package com.kotori316.fluidtank.fabric.gametest;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.contents.GenericUnit;
import com.kotori316.fluidtank.fabric.FluidTank;
import com.kotori316.fluidtank.fabric.tank.ConnectionStorage;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.tank.Tier;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.material.Fluids;
import org.junit.platform.commons.support.ReflectionSupport;
import scala.Option;
import scala.math.BigInt;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public final class ConnectionStorageTest implements FabricGameTest {

    @GameTestGenerator
    public List<TestFunction> generator() {
        // no args
        var batch = "connection_storage_test";
        var noArgs = Stream.of(getClass().getDeclaredMethods())
                .filter(m -> m.getReturnType() == Void.TYPE)
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.STATIC)) == 0)
                .map(m -> GameTestUtil.create(FluidTankCommon.modId, batch,
                        getClass().getSimpleName() + "_" + m.getName(),
                        () -> ReflectionSupport.invokeMethod(m, this)));
        var withHelper = Stream.of(getClass().getDeclaredMethods())
                .filter(m -> m.getReturnType() == Void.TYPE)
                .filter(m -> Arrays.equals(m.getParameterTypes(), new Class<?>[]{GameTestHelper.class}))
                .filter(m -> (m.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.STATIC)) == 0)
                .map(m -> GameTestUtil.create(FluidTankCommon.modId, batch,
                        getClass().getSimpleName() + "_" + m.getName(),
                        g -> ReflectionSupport.invokeMethod(m, this, g)));
        return Stream.concat(noArgs, withHelper).toList();
    }

    void getInstance(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        helper.setBlock(pos, FluidTank.TANK_MAP.get(Tier.WOOD));
        assertNotNull(getStorage(helper, pos));

        helper.succeed();
    }

    private static ConnectionStorage getStorage(GameTestHelper helper, BlockPos pos) {
        var storage = FluidStorage.SIDED.find(helper.getLevel(), helper.absolutePos(pos), null);
        if (storage == null) {
            GameTestUtil.throwExceptionAt(helper, pos, "Storage must be presented");
        }
        return assertInstanceOf(ConnectionStorage.class, storage);
    }

    void getCapacity1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        TankTest.placeTank(helper, pos, Tier.WOOD);
        var storage = getStorage(helper, pos);

        assertEquals(4 * FluidConstants.BUCKET, storage.getCapacity());
        assertEquals(0, storage.getAmount());
        assertTrue(storage.isResourceBlank());
        assertEquals(FluidVariant.blank(), storage.getResource());

        helper.succeed();
    }

    void getCapacity2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var storage = getStorage(helper, pos);

        assertEquals(20 * FluidConstants.BUCKET, storage.getCapacity());
        assertEquals(0, storage.getAmount());
        assertTrue(storage.isResourceBlank());
        assertEquals(FluidVariant.blank(), storage.getResource());

        helper.succeed();
    }

    void getCapacity3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.CREATIVE);
        var storage = getStorage(helper, pos);

        assertEquals(GenericUnit.CREATIVE_TANK(), BigInt.apply(storage.getCapacity()),
                "Connection capacity must not be over GenericUnit.CREATIVE_TANK");
        helper.succeed();
    }

    void getAmountWithCreative1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tank = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.CREATIVE);

        tank.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var storage = getStorage(helper, pos);
        assertEquals(GenericUnit.CREATIVE_TANK(), BigInt.apply(storage.getAmount()),
                "Connection capacity must not be over GenericUnit.CREATIVE_TANK");
        helper.succeed();
    }

    void getAmountWithCreative2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tank = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(1), Tier.CREATIVE);
        TankTest.placeTank(helper, pos.above(2), Tier.CREATIVE);

        tank.getConnection().getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var storage = getStorage(helper, pos);
        assertEquals(GenericUnit.CREATIVE_TANK(), BigInt.apply(storage.getAmount()),
                "Connection capacity must not be over GenericUnit.CREATIVE_TANK");
        helper.succeed();
    }

    void fillToEmpty(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var storage = getStorage(helper, pos);

        long filled;
        try (Transaction transaction = Transaction.openOuter()) {
            filled = storage.insert(FluidVariant.of(Fluids.WATER), 6 * FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        assertEquals(6 * FluidConstants.BUCKET, filled);

        var connection = tile.getConnection();
        assertEquals(GenericUnit.fromForge(20000), connection.capacity());
        assertEquals(GenericUnit.fromForge(6000), connection.amount());
        assertEquals(FluidAmountUtil.BUCKET_WATER().setAmount(GenericUnit.fromForge(6000)), connection.getContent().get());
        helper.succeed();
    }

    void fillToFilled(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var connection = tile.getConnection();
        connection.getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var storage = getStorage(helper, pos);

        long filled;
        try (Transaction transaction = Transaction.openOuter()) {
            filled = storage.insert(FluidVariant.of(Fluids.WATER), 6 * FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        assertEquals(6 * FluidConstants.BUCKET, filled);

        assertEquals(GenericUnit.fromForge(20000), connection.capacity());
        assertEquals(GenericUnit.fromForge(7000), connection.amount());
        assertEquals(FluidAmountUtil.BUCKET_WATER().setAmount(GenericUnit.fromForge(7000)), connection.getContent().get());
        helper.succeed();
    }

    void fillToFilled2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var connection = tile.getConnection();
        connection.getHandler().fill(FluidAmountUtil.BUCKET_LAVA(), true);

        var storage = getStorage(helper, pos);

        long filled;
        try (Transaction transaction = Transaction.openOuter()) {
            filled = storage.insert(FluidVariant.of(Fluids.WATER), 6 * FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        assertEquals(0, filled);

        assertEquals(GenericUnit.fromForge(20000), connection.capacity());
        assertEquals(FluidAmountUtil.BUCKET_LAVA().setAmount(GenericUnit.fromForge(1000)), connection.getContent().get());
        helper.succeed();
    }

    void fillToFilled3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var connection = tile.getConnection();
        connection.getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var storage = getStorage(helper, pos);

        long filled;
        try (Transaction transaction = Transaction.openOuter()) {
            filled = storage.insert(FluidVariant.of(Fluids.WATER), 40 * FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        assertEquals(19 * FluidConstants.BUCKET, filled);

        assertEquals(GenericUnit.fromForge(20000), connection.capacity());
        assertEquals(GenericUnit.fromForge(20000), connection.amount());
        assertEquals(FluidAmountUtil.BUCKET_WATER().setAmount(GenericUnit.fromForge(20000)), connection.getContent().get());
        helper.succeed();
    }

    void abort1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var storage = getStorage(helper, pos);

        long filled;
        try (Transaction transaction = Transaction.openOuter()) {
            filled = storage.insert(FluidVariant.of(Fluids.WATER), 6 * FluidConstants.BUCKET, transaction);
            transaction.abort();
        }
        assertEquals(6 * FluidConstants.BUCKET, filled);

        var connection = tile.getConnection();
        assertEquals(GenericUnit.fromForge(20000), connection.capacity());
        assertEquals(GenericUnit.fromForge(0), connection.amount());
        assertTrue(connection.getContent().isEmpty());
        helper.succeed();
    }

    void abort2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var connection = tile.getConnection();
        connection.getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);

        var storage = getStorage(helper, pos);

        long filled;
        try (Transaction transaction = Transaction.openOuter()) {
            filled = storage.insert(FluidVariant.of(Fluids.WATER), 6 * FluidConstants.BUCKET, transaction);
            transaction.abort();
        }
        assertEquals(6 * FluidConstants.BUCKET, filled);

        assertEquals(GenericUnit.fromForge(20000), connection.capacity());
        assertEquals(GenericUnit.fromForge(1000), connection.amount());
        assertEquals(FluidAmountUtil.BUCKET_WATER().setAmount(GenericUnit.fromForge(1000)), connection.getContent().get());
        helper.succeed();
    }

    void drainFromEmpty(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var storage = getStorage(helper, pos);

        long drained;
        try (Transaction transaction = Transaction.openOuter()) {
            drained = storage.extract(FluidVariant.of(Fluids.WATER), 6 * FluidConstants.BUCKET, transaction);
            transaction.abort();
        }
        assertEquals(0, drained);
        helper.succeed();
    }

    void drainFromFilled1(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var connection = tile.getConnection();
        connection.getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);
        var storage = getStorage(helper, pos);

        long drained;
        try (Transaction transaction = Transaction.openOuter()) {
            drained = storage.extract(FluidVariant.of(Fluids.WATER), 6 * FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        assertEquals(FluidConstants.BUCKET, drained);
        assertTrue(connection.getContent().isEmpty());
        helper.succeed();
    }

    void drainFromFilled2(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var connection = tile.getConnection();
        connection.getHandler().fill(FluidAmountUtil.BUCKET_WATER().setAmount(GenericUnit.fromForge(20000)), true);
        var storage = getStorage(helper, pos);

        long drained;
        try (Transaction transaction = Transaction.openOuter()) {
            drained = storage.extract(FluidVariant.of(Fluids.WATER), 19 * FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        assertEquals(FluidConstants.BUCKET * 19, drained);
        assertEquals(Option.apply(FluidAmountUtil.BUCKET_WATER()), connection.getContent());
        helper.succeed();
    }

    void drainFromFilled3(GameTestHelper helper) {
        var pos = BlockPos.ZERO.above();
        var tile = TankTest.placeTank(helper, pos, Tier.WOOD);
        TankTest.placeTank(helper, pos.above(), Tier.STONE);
        var connection = tile.getConnection();
        connection.getHandler().fill(FluidAmountUtil.BUCKET_WATER(), true);
        var storage = getStorage(helper, pos);

        long drained;
        try (Transaction transaction = Transaction.openOuter()) {
            drained = storage.extract(FluidVariant.of(Fluids.LAVA), 6 * FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        assertEquals(0, drained);
        assertEquals(Option.apply(FluidAmountUtil.BUCKET_WATER()), connection.getContent());
        helper.succeed();
    }
}
