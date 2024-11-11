/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.mixin.client.debug;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Changes the debug chunk borders so that they appear purple at the world borders.
 */
@Mixin(ChunkBorderRenderer.class)
public class ChunkBorderRendererMixin {
	private static final int CELL_BORDER = FastColor.ARGB32.color((int)255, (int)0, (int)155, (int)155);
	private static final int YELLOW = FastColor.ARGB32.color((int)255, (int)255, (int)255, (int)0);

	private static final int DARK_PURPLE = FastColor.ARGB32.color((int)255, (int)75, (int)0, (int)130);
	private static final int PURPLE = FastColor.ARGB32.color((int)255, (int)255, (int)0, (int)255);
	private static final int PURPLE_CLEAR = FastColor.ARGB32.color((int)0, (int)255, (int)0, (int)255);

	//TODO: computationally expensive?
	private boolean onBounds(int chunkPos, int iter, int bounds, int width) {
		if(bounds == 0) {
			return chunkPos == 0 && iter == 0;
		}
		return (chunkPos + iter/16) % (width) == bounds;
	}

	//TODO doesn't work in worlds where the min and max bounds aren't opposites (i.g. -32 -> 32)
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void render(PoseStack poseStack, MultiBufferSource buffer, double camX, double camY, double camZ, CallbackInfo ci) {
		ChunkBorderRenderer thiz = (ChunkBorderRenderer) (Object) this;

		ChunkBorderRendererAccessorMixin am = (ChunkBorderRendererAccessorMixin) (Object) thiz;

		WorldTransformer transformer = am.getMinecraft().level.getTransformer();

		ci.cancel();

		int k;
		int j;
		Entity entity = am.getMinecraft().gameRenderer.getMainCamera().getEntity();
		float f = (float)((double)am.getMinecraft().level.getMinBuildHeight() - camY);
		float g = (float)((double)am.getMinecraft().level.getMaxBuildHeight() - camY);
		ChunkPos chunkPos = entity.chunkPosition();
		float h = (float)((double)chunkPos.getMinBlockX() - camX);
		float i = (float)((double)chunkPos.getMinBlockZ() - camZ);
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(1.0));
		Matrix4f matrix4f = poseStack.last().pose();

		//Red lines showing distant chunk borders
		for (j = -16; j <= 32; j += 16) {
			for (k = -16; k <= 32; k += 16) {
				vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + (float)k).setColor(1.0f, 0.0f, 0.0f, 0.0f);
				//Set purple
				if(onBounds(chunkPos.x, j, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x, j, transformer.xChunkBoundMax, transformer.xWidth) || onBounds(chunkPos.z, k, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z, k, transformer.zChunkBoundMax, transformer.zWidth)) {
					vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + (float)k).setColor(DARK_PURPLE);
					vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + (float)k).setColor(DARK_PURPLE);
				}
				//Set red
				else {
					vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + (float)k).setColor(1.0f, 0.0f, 0.0f, 0.5f);
					vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + (float)k).setColor(1.0f, 0.0f, 0.0f, 0.5f);
				}
				vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + (float)k).setColor(1.0f, 0.0f, 0.0f, 0.0f);
			}
		}

		//Yellow/green vertical for north and south
		for (j = 2; j < 16; j += 2) {
			k = j % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, h + (float)j, f, i).setColor(1.0f, 1.0f, 0.0f, 0.0f);
			vertexConsumer.addVertex(matrix4f, h + (float)j, f, i).setColor(k);
			vertexConsumer.addVertex(matrix4f, h + (float)j, g, i).setColor(k);
			vertexConsumer.addVertex(matrix4f, h + (float)j, g, i).setColor(1.0f, 1.0f, 0.0f, 0.0f);
			vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + 16.0f).setColor(1.0f, 1.0f, 0.0f, 0.0f);
			vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + 16.0f).setColor(k);
			vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + 16.0f).setColor(k);
			vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + 16.0f).setColor(1.0f, 1.0f, 0.0f, 0.0f);
		}

		//Yellow/green vertical for east and west
		for (j = 2; j < 16; j += 2) {
			k = j % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, h, f, i + (float)j).setColor(1.0f, 1.0f, 0.0f, 0.0f);
			vertexConsumer.addVertex(matrix4f, h, f, i + (float)j).setColor(k);
			vertexConsumer.addVertex(matrix4f, h, g, i + (float)j).setColor(k);
			vertexConsumer.addVertex(matrix4f, h, g, i + (float)j).setColor(1.0f, 1.0f, 0.0f, 0.0f);
			vertexConsumer.addVertex(matrix4f, h + 16.0f, f, i + (float)j).setColor(1.0f, 1.0f, 0.0f, 0.0f);
			vertexConsumer.addVertex(matrix4f, h + 16.0f, f, i + (float)j).setColor(k);
			vertexConsumer.addVertex(matrix4f, h + 16.0f, g, i + (float)j).setColor(k);
			vertexConsumer.addVertex(matrix4f, h + 16.0f, g, i + (float)j).setColor(1.0f, 1.0f, 0.0f, 0.0f);
		}

		//Yellow/green horizontal for all directions
		for (j = am.getMinecraft().level.getMinBuildHeight(); j <= am.getMinecraft().level.getMaxBuildHeight(); j += 2) {
			float l = (float)((double)j - camY);
			int m = j % 8 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, h, l, i).setColor(1.0f, 1.0f, 0.0f, 0.0f);
			vertexConsumer.addVertex(matrix4f, h, l, i).setColor(m);
			vertexConsumer.addVertex(matrix4f, h, l, i + 16.0f).setColor(m);
			vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i + 16.0f).setColor(m);
			vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i).setColor(m);
			vertexConsumer.addVertex(matrix4f, h, l, i).setColor(m);
			vertexConsumer.addVertex(matrix4f, h, l, i).setColor(1.0f, 1.0f, 0.0f, 0.0f);
		}

		vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(2.0));

		//Blue vertical for immediate chunk corners
		for (j = 0; j <= 16; j += 16) {
			for (int k2 = 0; k2 <= 16; k2 += 16) {
				//Set purple
				if(onBounds(chunkPos.x, j, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x, j, transformer.xChunkBoundMax, transformer.xWidth) || onBounds(chunkPos.z, k2, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z, k2, transformer.zChunkBoundMax, transformer.zWidth)) {vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + (float)k2).setColor(PURPLE_CLEAR);
					vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + (float)k2).setColor(PURPLE);
					vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + (float)k2).setColor(PURPLE);
					vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + (float)k2).setColor(PURPLE_CLEAR);
				}
				//Set red
				else {
					vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + (float)k2).setColor(0.25f, 0.25f, 1.0f, 0.0f);
					vertexConsumer.addVertex(matrix4f, h + (float)j, f, i + (float)k2).setColor(0.25f, 0.25f, 1.0f, 1.0f);
					vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + (float)k2).setColor(0.25f, 0.25f, 1.0f, 1.0f);
					vertexConsumer.addVertex(matrix4f, h + (float)j, g, i + (float)k2).setColor(0.25f, 0.25f, 1.0f, 0.0f);
				}
			}
		}

		//Blue horizontal for chunk segment borders
		for (j = am.getMinecraft().level.getMinBuildHeight(); j <= am.getMinecraft().level.getMaxBuildHeight(); j += 16) {
			float l = (float)((double)j - camY);

			vertexConsumer.addVertex(matrix4f, h, l, i).setColor(0.25f, 0.25f, 1.0f, 0.0f);

			//Northwest to southwest
			if(onBounds(chunkPos.x, 0, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x, 0, transformer.xChunkBoundMax, transformer.xWidth)) {
				vertexConsumer.addVertex(matrix4f, h, l, i).setColor(PURPLE);
				vertexConsumer.addVertex(matrix4f, h, l, i + 16.0f).setColor(PURPLE);
			}
			else {
				vertexConsumer.addVertex(matrix4f, h, l, i).setColor(0.25f, 0.25f, 1.0f, 1.0f);
				vertexConsumer.addVertex(matrix4f, h, l, i + 16.0f).setColor(0.25f, 0.25f, 1.0f, 1.0f);
			}

			//Southwest to southeast
			if(onBounds(chunkPos.z + 1, 0, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z + 1, 0, transformer.zChunkBoundMax, transformer.zWidth)) {
				vertexConsumer.addVertex(matrix4f, h, l, i + 16.0f).setColor(PURPLE);
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i + 16.0f).setColor(PURPLE);
			}
			else {
				vertexConsumer.addVertex(matrix4f, h, l, i + 16.0f).setColor(0.25f, 0.25f, 1.0f, 1.0f);
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i + 16.0f).setColor(0.25f, 0.25f, 1.0f, 1.0f);
			}

			//Southeast to northeast
			if(onBounds(chunkPos.x + 1, 0, transformer.xChunkBoundMin, transformer.xWidth) || onBounds(chunkPos.x + 1, 0, transformer.xChunkBoundMax, transformer.xWidth)) {
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i + 16.0f).setColor(PURPLE);
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i).setColor(PURPLE);
			}
			else {
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i + 16.0f).setColor(0.25f, 0.25f, 1.0f, 1.0f);
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i).setColor(0.25f, 0.25f, 1.0f, 1.0f);
			}

			//Northeast to northwest
			if(onBounds(chunkPos.z, 0, transformer.zChunkBoundMin, transformer.zWidth) || onBounds(chunkPos.z, 0, transformer.zChunkBoundMax, transformer.zWidth)) {
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i).setColor(PURPLE);
				vertexConsumer.addVertex(matrix4f, h, l, i).setColor(PURPLE);
			}
			else {
				vertexConsumer.addVertex(matrix4f, h + 16.0f, l, i).setColor(0.25f, 0.25f, 1.0f, 1.0f);
				vertexConsumer.addVertex(matrix4f, h, l, i).setColor(0.25f, 0.25f, 1.0f, 1.0f);
			}

			vertexConsumer.addVertex(matrix4f, h, l, i).setColor(0.25f, 0.25f, 1.0f, 0.0f);
		}
	}
}
