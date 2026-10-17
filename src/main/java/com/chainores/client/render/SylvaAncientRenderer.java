package com.chainores.client.render;

import com.chainores.ChainOresMod;
import com.chainores.entity.SylvaAncientEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SylvaAncientRenderer extends BipedEntityRenderer<SylvaAncientEntity, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {
    private static final Identifier TEXTURE = ChainOresMod.id("textures/entity/sylva_ancient.png");

    public SylvaAncientRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 1.2f);
    }

    @Override
    public BipedEntityRenderState createRenderState() {
        return new BipedEntityRenderState();
    }

    @Override
    public Identifier getTexture(BipedEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(BipedEntityRenderState state, MatrixStack matrices) {
        matrices.scale(2.2f, 2.2f, 2.2f);
    }
}
