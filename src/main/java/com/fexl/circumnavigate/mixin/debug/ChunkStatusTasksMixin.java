/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.debug;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {
	@Unique private final static boolean STRUCTURE_STARTS         = true;
	@Unique private final static boolean STRUCTURE_REFERENCES     = true;
	@Unique private final static boolean BIOMES                   = true;
	@Unique private final static boolean NOISE                    = true;
	@Unique private final static boolean SURFACE                  = true;
	@Unique private final static boolean CARVERS                  = true;
	@Unique private final static boolean FEATURES                 = true;
	@Unique private final static boolean SPAWN                    = true;
	
	@Inject(method = "generateStructureStarts", at = @At("HEAD"), cancellable = true)
	private static void generateStructureStarts(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!STRUCTURE_STARTS) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}

	@Inject(method = "generateStructureReferences", at = @At("HEAD"), cancellable = true)
	private static void generateStructureReferences(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!STRUCTURE_REFERENCES) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}

	@Inject(method = "generateBiomes", at = @At("HEAD"), cancellable = true)
	private static void generateBiomes(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!BIOMES) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}

	@Inject(method = "generateNoise", at = @At("HEAD"), cancellable = true)
	private static void generateNoise(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!NOISE) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}
	
	@Inject(method = "generateSurface", at = @At("HEAD"), cancellable = true)
	private static void generateSurface(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!SURFACE) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}

	@Inject(method = "generateCarvers", at = @At("HEAD"), cancellable = true)
	private static void generateCarvers(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!CARVERS) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}

	@Inject(method = "generateFeatures", at = @At("HEAD"), cancellable = true)
	private static void generateFeatures(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!FEATURES) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}

	@Inject(method = "generateSpawn", at = @At("HEAD"), cancellable = true)
	private static void generateSpawn(WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		if(!SPAWN) {
			cir.setReturnValue(CompletableFuture.completedFuture(chunk));
		}
	}
	
	
}
