/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NormalNoise.class)
public class NormalNoiseMixin {
	@Shadow @Final private double valueFactor;
	@Shadow @Final private PerlinNoise first;
	@Shadow @Final private PerlinNoise second;

	private final double xWidth = 256.0;
	private final double zWidth = 256.0;

	private long source;
	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(RandomSource random, NormalNoise.NoiseParameters parameters, boolean useLegacyNetherBiome, CallbackInfo ci) {
		source = random.nextLong();
	}

	/**
	public double getValue(double x, double y, double z) {
		double xa = x / xWidth;
		double za = z / zWidth;

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		//return OpenSimplex2S.noise2_ImproveX(source, noise4, y);
		double noise1 = OpenSimplex2S.noise2(source, 0, y);

		return (noise4 + noise1)/2.0;

		//double d = x * 1.0181268882175227;
		//double e = y * 1.0181268882175227;
		//double f = z * 1.0181268882175227;
		//return (this.first.getValue(x, y, z) + this.second.getValue(d, e, f)) * this.valueFactor;
	}**/

	public double getValue(double x, double y, double z) {
		return (this.first.getValue(x, y, z));
	}


}
