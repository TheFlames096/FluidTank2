package com.kotori316.fluidtank.fabric;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.DSL;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.PlatformAccess;
import com.kotori316.fluidtank.fabric.message.PacketHandler;
import com.kotori316.fluidtank.fabric.tank.BlockCreativeTankFabric;
import com.kotori316.fluidtank.fabric.tank.BlockTankFabric;
import com.kotori316.fluidtank.fabric.tank.TileCreativeTankFabric;
import com.kotori316.fluidtank.fabric.tank.TileTankFabric;
import com.kotori316.fluidtank.tank.BlockTank;
import com.kotori316.fluidtank.tank.BlockVoidTank;
import com.kotori316.fluidtank.tank.ItemBlockTank;
import com.kotori316.fluidtank.tank.TankLootFunction;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.fluidtank.tank.TileCreativeTank;
import com.kotori316.fluidtank.tank.TileTank;
import com.kotori316.fluidtank.tank.TileVoidTank;

public final class FluidTank implements ModInitializer {
    @Override
    public void onInitialize() {
        FluidTankCommon.LOGGER.info("Initialize {}", FluidTankCommon.modId);
        PacketHandler.Server.initServer();
        PlatformAccess.setInstance(new FabricPlatformAccess());
        registerObjects();
        FluidTankCommon.LOGGER.info("Initialize finished {}", FluidTankCommon.modId);
    }

    public static final Map<Tier, BlockTankFabric> TANK_MAP = Stream.of(Tier.values())
        .filter(Tier::isNormalTankTier)
        .collect(Collectors.toMap(Function.identity(), BlockTankFabric::new));
    public static final BlockCreativeTankFabric BLOCK_CREATIVE_TANK = new BlockCreativeTankFabric();
    public static final BlockVoidTank BLOCK_VOID_TANK = new BlockVoidTank();
    public static final Map<Tier, ItemBlockTank> TANK_ITEM_MAP =
        Stream.concat(TANK_MAP.entrySet().stream(), Stream.of(Map.entry(Tier.CREATIVE, BLOCK_CREATIVE_TANK), Map.entry(Tier.VOID, BLOCK_VOID_TANK)))
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().itemBlock()));
    public static final BlockEntityType<TileTankFabric> TILE_TANK_TYPE =
        BlockEntityType.Builder.of(TileTankFabric::new, TANK_MAP.values().toArray(BlockTank[]::new)).build(DSL.emptyPartType());
    public static final BlockEntityType<TileCreativeTankFabric> TILE_CREATIVE_TANK_TYPE =
        BlockEntityType.Builder.of(TileCreativeTankFabric::new, BLOCK_CREATIVE_TANK).build(DSL.emptyPartType());
    public static final BlockEntityType<TileVoidTank> TILE_VOID_TANK_TYPE =
        BlockEntityType.Builder.of(TileVoidTank::new, BLOCK_VOID_TANK).build(DSL.emptyPartType());
    public static final LootItemFunctionType TANK_LOOT_FUNCTION = new LootItemFunctionType(new TankLootFunction.TankLootSerializer());

    private static void registerObjects() {
        Stream.concat(TANK_MAP.entrySet().stream(), Stream.of(Map.entry(Tier.CREATIVE, BLOCK_CREATIVE_TANK), Map.entry(Tier.VOID, BLOCK_VOID_TANK)))
            .forEach(e -> Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(FluidTankCommon.modId, e.getKey().getBlockName()), e.getValue()));
        TANK_ITEM_MAP.forEach((tier, itemBlockTank) -> Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(FluidTankCommon.modId, tier.getBlockName()), itemBlockTank));
        Map.of(TileTank.class, TILE_TANK_TYPE, TileCreativeTank.class, TILE_CREATIVE_TANK_TYPE, TileVoidTank.class, TILE_VOID_TANK_TYPE)
            .forEach((c, t) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(FluidTankCommon.modId, c.getSimpleName().toLowerCase(Locale.ROOT)), t));
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, new ResourceLocation(FluidTankCommon.modId, TankLootFunction.NAME), TANK_LOOT_FUNCTION);
        var builder = FabricItemGroup.builder(new ResourceLocation(FluidTankCommon.modId, FluidTankCommon.modId));
        createTab(builder);
        builder.build();
    }

    private static void createTab(CreativeModeTab.Builder builder) {
        builder.icon(() -> new ItemStack(TANK_MAP.get(Tier.WOOD)));
        builder.title(Component.translatable("itemGroup.fluidtank"));
        builder.displayItems((parameters, output) -> {
            // Tanks
            TANK_ITEM_MAP.values().stream().sorted(Comparator.comparing(i -> i.blockTank().tier()))
                .forEach(output::accept);
        });
    }
}
