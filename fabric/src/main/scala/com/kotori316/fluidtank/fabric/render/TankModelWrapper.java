package com.kotori316.fluidtank.fabric.render;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.resources.model.BakedModel;

class TankModelWrapper extends ForwardingBakedModel {
    public TankModelWrapper(BakedModel originalModel) {
        this.wrapped = originalModel;
    }

    public void setModel(BakedModel newModel) {
        this.wrapped = newModel;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }
}
