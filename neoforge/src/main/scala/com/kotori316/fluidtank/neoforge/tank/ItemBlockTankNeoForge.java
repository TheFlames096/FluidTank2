package com.kotori316.fluidtank.neoforge.tank;

import com.kotori316.fluidtank.neoforge.render.RenderItemTank;
import com.kotori316.fluidtank.tank.BlockTank;
import com.kotori316.fluidtank.tank.ItemBlockTank;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.function.Consumer;

public final class ItemBlockTankNeoForge extends ItemBlockTank {
    public ItemBlockTankNeoForge(BlockTank b) {
        super(b);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RenderItemTank.INSTANCE();
            }
        });
    }

    public static IFluidHandlerItem initCapabilities(ItemStack stack, Void ignored) {
        return new TankFluidItemHandler(((ItemBlockTankNeoForge) stack.getItem()).blockTank().tier(), stack);
    }
}
