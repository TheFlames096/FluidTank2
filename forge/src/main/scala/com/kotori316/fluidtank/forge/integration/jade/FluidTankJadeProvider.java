package com.kotori316.fluidtank.forge.integration.jade;

import com.kotori316.fluidtank.integration.tooltip.TooltipContent;
import com.kotori316.fluidtank.tank.TileTank;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

class FluidTankJadeProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TileTank tileTank) {
            var content = TooltipContent.getTooltipTextJava(
                accessor.getServerData(),
                tileTank,
                config.get(TooltipContent.JADE_CONFIG_SHORT()),
                config.get(TooltipContent.JADE_CONFIG_COMPACT()),
                Minecraft.getInstance().getLocale()
            );
            tooltip.addAll(content);
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor) {
        TooltipContent.addServerData(compoundTag, accessor.getBlockEntity());
    }

    @Override
    public ResourceLocation getUid() {
        return TooltipContent.JADE_TOOLTIP_UID();
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.BODY;
    }
}
