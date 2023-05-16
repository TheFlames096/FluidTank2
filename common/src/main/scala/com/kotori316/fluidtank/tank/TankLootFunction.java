package com.kotori316.fluidtank.tank;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class TankLootFunction extends LootItemConditionalFunction {
    public static final String NAME = "content_tank";

    public TankLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var tile = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (stack.getItem() instanceof ItemBlockTank tank) {
            tank.blockTank().saveTankNBT(tile, stack);
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return PlatformTankAccess.getInstance().getTankLoot();
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return LootItemConditionalFunction.simpleBuilder(TankLootFunction::new);
    }

    public static final class TankLootSerializer extends LootItemConditionalFunction.Serializer<TankLootFunction> {
        @Override
        public TankLootFunction deserialize(JsonObject object, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new TankLootFunction(conditions);
        }
    }
}
