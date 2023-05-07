package com.kotori316.fluidtank.forge.tank;

import java.util.function.Consumer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import com.kotori316.fluidtank.forge.render.RenderItemTank;
import com.kotori316.fluidtank.tank.BlockTank;
import com.kotori316.fluidtank.tank.ItemBlockCreativeTank;

public final class ItemBlockCreativeTankForge extends ItemBlockCreativeTank {
    public ItemBlockCreativeTankForge(BlockTank b) {
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
}
