package com.kotori316.fluidtank.render;

import com.kotori316.fluidtank.FluidTankCommon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ReservoirModel extends Model {
    public static final ModelLayerLocation LOCATION = new ModelLayerLocation(new ResourceLocation(FluidTankCommon.modId, "reservoir"), "main");
    private static final String CONTAINER = "container";
    private static final String FLUID = "fluid";
    private final ModelPart root;
    private final ModelPart container;

    public ReservoirModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.root = root;
        this.container = root.getChild(CONTAINER);
    }

    public static LayerDefinition createDefinition() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(CONTAINER,
            CubeListBuilder.create().texOffs(0, 0).addBox(2, 0, 0, 12, 16, 1),
            PartPose.ZERO
        );

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
