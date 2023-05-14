package com.kotori316.fluidtank.fabric.render;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.contents.GenericAmount;
import com.kotori316.fluidtank.fabric.fluid.FabricConverter;

@SuppressWarnings("UnstableApiUsage")
final class RenderResourceHelper {
    static TextureAtlasSprite getSprite(GenericAmount<Fluid> fluid) {
        return FluidVariantRendering.getSprite(FabricConverter.toVariant(fluid));
    }

    static int getColor(GenericAmount<Fluid> fluid) {
        return FluidVariantRendering.getColor(FabricConverter.toVariant(fluid));
    }

    static int getColorWithPos(GenericAmount<Fluid> fluid, @Nullable BlockAndTintGetter view, BlockPos pos) {
        return FluidVariantRendering.getColor(FabricConverter.toVariant(fluid), view, pos);
    }

    static int getLuminance(GenericAmount<Fluid> fluid) {
        return FluidVariantAttributes.getLuminance(FabricConverter.toVariant(fluid));
    }
}
