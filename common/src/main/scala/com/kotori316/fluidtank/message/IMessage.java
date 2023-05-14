package com.kotori316.fluidtank.message;

import com.google.common.base.CaseFormat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.kotori316.fluidtank.FluidTankCommon;

public interface IMessage<T extends IMessage<T>> {
    void write(FriendlyByteBuf buffer);

    default ResourceLocation getIdentifier() {
        return createIdentifier(getClass());
    }

    static ResourceLocation createIdentifier(Class<?> clazz) {
        return new ResourceLocation(FluidTankCommon.modId, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()));
    }
}
