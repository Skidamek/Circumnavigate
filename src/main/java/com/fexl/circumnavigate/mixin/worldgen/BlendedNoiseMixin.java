/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlendedNoise.class)
public class BlendedNoiseMixin {
	@Shadow @Final private PerlinNoise minLimitNoise;
	@Shadow @Final private PerlinNoise maxLimitNoise;
	@Shadow @Final private PerlinNoise mainNoise;
	@Shadow @Final private double xzMultiplier;
	@Shadow @Final private double yMultiplier;
	@Shadow @Final private double xzFactor;
	@Shadow @Final private double yFactor;
	@Shadow @Final private double smearScaleMultiplier;
	@Shadow @Final private double maxValue;
	@Shadow @Final private double xzScale;
	@Shadow @Final private double yScale;

	private final double xWidth = 256.0;
	private final double zWidth = 256.0;

	private long source;

	@Inject(method = "<init>(Lnet/minecraft/util/RandomSource;DDDDD)V", at = @At("TAIL"))
	public void init(RandomSource random, double xzScale, double yScale, double xzFactor, double yFactor, double smearScaleMultiplier, CallbackInfo ci) {
		source = random.nextLong();
	}

	/**
	public double compute(DensityFunction.FunctionContext context) {
		int x = context.blockX();
		int y = context.blockY();
		int z = context.blockZ();

		double xa = x / xWidth;
		double za = z / zWidth;

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		//return OpenSimplex2S.noise2_ImproveX(context.blockX() + context.blockY() + context.blockZ(), noise4, y);
		return noise4;
	}**/
		/**
		double d = (double)context.blockX() * this.xzMultiplier;
		double e = (double)context.blockY() * this.yMultiplier;
		double f = (double)context.blockZ() * this.xzMultiplier;
		double g = d / this.xzFactor;
		double h = e / this.yFactor;
		double i = f / this.xzFactor;
		double j = this.yMultiplier * this.smearScaleMultiplier;
		double k = j / this.yFactor;
		double l = 0.0;
		double m = 0.0;
		double n = 0.0;
		boolean bl = true;
		double o = 1.0;

		for (int p = 0; p < 8; p++) {
			ImprovedNoise improvedNoise = this.mainNoise.getOctaveNoise(p);
			if (improvedNoise != null) {
				n += improvedNoise.noise(PerlinNoise.wrap(g * o), PerlinNoise.wrap(h * o), PerlinNoise.wrap(i * o), k * o, h * o) / o;
			}

			o /= 2.0;
		}

		double q = (n / 10.0 + 1.0) / 2.0;
		boolean bl2 = q >= 1.0;
		boolean bl3 = q <= 0.0;
		o = 1.0;

		for (int r = 0; r < 16; r++) {
			double s = PerlinNoise.wrap(d * o);
			double t = PerlinNoise.wrap(e * o);
			double u = PerlinNoise.wrap(f * o);
			double v = j * o;
			if (!bl2) {
				ImprovedNoise improvedNoise2 = this.minLimitNoise.getOctaveNoise(r);
				if (improvedNoise2 != null) {
					l += improvedNoise2.noise(s, t, u, v, e * o) / o;
				}
			}

			if (!bl3) {
				ImprovedNoise improvedNoise2 = this.maxLimitNoise.getOctaveNoise(r);
				if (improvedNoise2 != null) {
					m += improvedNoise2.noise(s, t, u, v, e * o) / o;
				}
			}

			o /= 2.0;
		}

		return Mth.clampedLerp(l / 512.0, m / 512.0, q) / 128.0;
	}**/
}
