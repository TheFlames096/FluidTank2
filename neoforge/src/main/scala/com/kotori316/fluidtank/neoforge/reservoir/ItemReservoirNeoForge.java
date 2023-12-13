package com.kotori316.fluidtank.neoforge.reservoir;

import com.kotori316.fluidtank.neoforge.render.RenderReservoirItemForge;
import com.kotori316.fluidtank.reservoir.ItemReservoir;
import com.kotori316.fluidtank.tank.Tier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.function.Consumer;

public final class ItemReservoirNeoForge extends ItemReservoir {
    public ItemReservoirNeoForge(Tier tier) {
        super(tier);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RenderReservoirItemForge.INSTANCE;
            }
        });
    }

    public static IFluidHandlerItem initCapabilities(ItemStack stack, Void ignored) {
        return new ReservoirFluidHandler((ItemReservoir) stack.getItem(), stack);
    }
}
