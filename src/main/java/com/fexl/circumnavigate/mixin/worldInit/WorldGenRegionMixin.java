/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldInit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(ServerLevel level, StaticCache2D cache, ChunkStep generatingStep, ChunkAccess center, CallbackInfo ci) {
		WorldGenRegion thiz = (WorldGenRegion) (Object) this;
		thiz.setTransformer(level.getTransformer());
	}
}
