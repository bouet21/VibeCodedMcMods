package com.chainores.client.render;

import com.chainores.ChainOresMod;
import com.chainores.entity.CosmicSovereignEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CosmicSovereignRenderer extends BipedEntityRenderer<CosmicSovereignEntity, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {
    private static final Identifier TEXTURE = ChainOresMod.id("textures/entity/cosmic_sovereign.png");

    public CosmicSovereignRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 1.8f);
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
        matrices.scale(2.8f, 2.8f, 2.8f);
    }
}
