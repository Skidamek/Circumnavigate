/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.interfaceInjects;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.injected.LevelTransformerInjector;
import net.minecraft.server.level.ChunkTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects a transformer and accessor methods in ChunkTracker so it can be used in all subclasses instantiated from DistanceManager.
 */
@Mixin(ChunkTracker.class)
public class ChunkTrackerInjectorMixin implements LevelTransformerInjector {
	@Unique
	private WorldTransformer transformer = null;

	@Override
	public WorldTransformer getTransformer() {
		return transformer;
	}

	@Override
	public void setTransformer(WorldTransformer transformer) {
		this.transformer = transformer;
	}
}
