package com.kotori316.fluidtank.neoforge.render;

import com.kotori316.fluidtank.contents.Tank;
import com.kotori316.fluidtank.fluids.FluidLike;
import com.kotori316.fluidtank.fluids.VanillaFluid;
import com.kotori316.fluidtank.fluids.VanillaPotion;
import com.kotori316.fluidtank.neoforge.fluid.NeoForgeConverter;
import com.kotori316.fluidtank.render.RenderReservoirItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import scala.jdk.javaapi.OptionConverters;

import java.util.List;

public final class RenderReservoirItemForge extends RenderReservoirItem {
    public static final RenderReservoirItemForge INSTANCE = new RenderReservoirItemForge();

    @Override
    public TextureAtlasSprite getFluidTexture(Tank<FluidLike> tank) {
        var fluid = FluidLike.asFluid(tank.content().content(), Fluids.WATER);
        var attributes = IClientFluidTypeExtensions.of(fluid);
        var location = attributes.getStillTexture(fluid.defaultFluidState(),
            Minecraft.getInstance().level, Minecraft.getInstance().player.getOnPos());
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }

    @Override
    public int getFluidColor(Tank<FluidLike> tank) {
        var content = tank.content();
        if (content.content() instanceof VanillaFluid vanillaFluid) {
            var attributes = IClientFluidTypeExtensions.of(vanillaFluid.fluid());
            var normal = attributes.getTintColor();
            if (attributes == IClientFluidTypeExtensions.DEFAULT) {
                return normal;
            }
            var stackColor = attributes.getTintColor(NeoForgeConverter.toStack(content));
            if (normal == stackColor) {
                return attributes.getTintColor(vanillaFluid.fluid().defaultFluidState(),
                    Minecraft.getInstance().level, Minecraft.getInstance().player.getOnPos());
            } else {
                return stackColor;
            }
        } else if (content.content() instanceof VanillaPotion) {
            return PotionUtils.getColor(OptionConverters.toJava(content.nbt())
                .map(PotionUtils::getAllEffects)
                .orElse(List.of())
            );
        } else {
            throw new IllegalArgumentException("Unknown fluid type " + content);
        }
    }
}
