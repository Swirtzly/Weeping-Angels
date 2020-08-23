package me.swirtzly.minecraft.angels.client.renders.tileentities;

import com.mojang.blaze3d.matrix.MatrixStack;

import me.swirtzly.minecraft.angels.client.models.block.SnowArmModel;
import me.swirtzly.minecraft.angels.common.tileentities.SnowArmTile;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

public class SnowArmTileRender extends TileEntityRenderer<SnowArmTile> {
	
	private SnowArmModel arm = new SnowArmModel();

	public SnowArmTileRender() {
		super(TileEntityRendererDispatcher.instance);
	}

	@Override
	public void render(SnowArmTile snowArmTile, float v, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int i, int i1) {
		matrixStack.push();
		matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));
		arm.render(0.0625f);
		matrixStack.pop();
	}
}
