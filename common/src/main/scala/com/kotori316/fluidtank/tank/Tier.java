package com.kotori316.fluidtank.tank;

import com.google.common.base.CaseFormat;
import com.kotori316.fluidtank.config.PlatformConfigAccess;
import com.kotori316.fluidtank.contents.GenericUnit;
import org.jetbrains.annotations.NotNull;
import scala.math.BigInt;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public enum Tier {
    INVALID(0, ""),
    WOOD(1),
    IRON(3),
    GOLD(4),
    DIAMOND(5),
    EMERALD(6, "aluminum"),
    STAR(7),
    CREATIVE(10, "creative"),
    VOID(0),
    COPPER(2),
    TIN(2,"stainless_steel"),
    STONE(2,"steel"),
    BRONZE(3,"titanium"),
    LEAD(3,"tungstensteel"),
    SILVER(3),
    ;

    private final int rank;
    private final String blockName;
    private final String name;

    Tier(int rank, String blockName) {
        this.rank = rank;
        this.blockName = blockName;
        this.name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
    }

    Tier(int rank) {
        this.rank = rank;
        this.blockName = "tank_" + name().toLowerCase(Locale.ROOT);
        this.name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
    }

    public BigInt getCapacity() {
        var capacityMap = PlatformConfigAccess.getInstance().getConfig().capacityMap();
        return capacityMap.get(this).getOrElse(() -> {
            throw new IllegalStateException("No capacity for %s".formatted(this));
        });
    }

    public int getRank() {
        return rank;
    }

    public String getBlockName() {
        return blockName;
    }

    public boolean isNormalTankTier() {
        return getRank() != 0 && getRank() != 10;
    }

    @Override
    public String toString() {
        return this.name;
    }

    static BigInt fromForge(long forgeAmount) {
        return GenericUnit.asBigIntFromForge(forgeAmount);
    }

    @NotNull
    public static EnumMap<Tier, BigInt> getDefaultCapacityMap() {
        return new EnumMap<>(Map.ofEntries(
            Map.entry(INVALID, fromForge(0)),
            Map.entry(WOOD, fromForge(16_000)),
            Map.entry(STONE, fromForge(48_000)),
            Map.entry(IRON, fromForge(32_000)),
            Map.entry(GOLD, fromForge(48_000)),
            Map.entry(DIAMOND, fromForge(64_000)),
            Map.entry(EMERALD, fromForge(96_000)),
            Map.entry(STAR, fromForge(64_000_000)),
            Map.entry(CREATIVE, GenericUnit.CREATIVE_TANK()),
            Map.entry(VOID, fromForge(0)),
            Map.entry(COPPER, fromForge(24_000)),
            Map.entry(TIN, fromForge(128_000)),
            Map.entry(BRONZE, fromForge(256_000)),
            Map.entry(LEAD, fromForge(512_000)),
            Map.entry(SILVER, fromForge(40_000))
        ));
    }
}
