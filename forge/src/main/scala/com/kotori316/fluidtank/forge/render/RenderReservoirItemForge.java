package com.kotori316.fluidtank.forge.render;

import com.kotori316.fluidtank.contents.Tank;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.fluids.FluidLike;
import com.kotori316.fluidtank.fluids.VanillaFluid;
import com.kotori316.fluidtank.fluids.VanillaPotion;
import com.kotori316.fluidtank.forge.fluid.ForgeConverter;
import com.kotori316.fluidtank.render.RenderReservoirItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.List;
import java.util.Objects;

public final class RenderReservoirItemForge extends RenderReservoirItem {
    public static final RenderReservoirItemForge INSTANCE = new RenderReservoirItemForge();

    @Override
    public TextureAtlasSprite getFluidTexture(Tank<FluidLike> tank) {
        var fluid = FluidLike.asFluid(tank.content().content(), Fluids.WATER);
        var attributes = IClientFluidTypeExtensions.of(fluid);
        var location = attributes.getStillTexture(fluid.defaultFluidState(),
            Minecraft.getInstance().level, Objects.requireNonNull(Minecraft.getInstance().player).getOnPos());
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
            var stackColor = attributes.getTintColor(ForgeConverter.toStack(content));
            if (normal == stackColor) {
                return attributes.getTintColor(vanillaFluid.fluid().defaultFluidState(),
                    Minecraft.getInstance().level, Objects.requireNonNull(Minecraft.getInstance().player).getOnPos());
            } else {
                return stackColor;
            }
        } else if (content.content() instanceof VanillaPotion) {
            return PotionUtils.getColor(FluidAmountUtil.getTag(content)
                .map(PotionUtils::getAllEffects)
                .orElse(List.of())
            );
        } else {
            throw new IllegalArgumentException("Unknown fluid type " + content);
        }
    }
}
