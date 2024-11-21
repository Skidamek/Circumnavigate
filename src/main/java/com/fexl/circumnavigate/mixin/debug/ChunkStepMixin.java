/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.debug;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStep.class)
public class ChunkStepMixin {
	@Inject(method = "apply", at = @At("HEAD"), cancellable = true)
	public void apply(WorldGenContext worldGenContext, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(chunk.getPersistedStatus().isOrAfter(ChunkStatus.NOISE) && !chunk.getPersistedStatus().isAfter(ChunkStatus.CARVERS)) {
			if(chunk instanceof ProtoChunk protoChunk) {
				protoChunk.setPersistedStatus(ChunkStatus.getStatusList().get(chunk.getPersistedStatus().getIndex() + 1));
				cir.setReturnValue(CompletableFuture.completedFuture(protoChunk));
			}
		}
	}
}
