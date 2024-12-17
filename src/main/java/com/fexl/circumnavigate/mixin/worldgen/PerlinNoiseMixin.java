/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.injected.NoiseScaling;
import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PerlinNoise.class)
public abstract class PerlinNoiseMixin {
	@Shadow
	public static double wrap(double value) {
		return 0;
	}

	@Shadow @Final private ImprovedNoise[] noiseLevels;
	@Shadow @Final private int firstOctave;
	@Shadow @Final private DoubleList amplitudes;
	@Shadow @Final private double lowestFreqValueFactor;
	@Shadow @Final private double lowestFreqInputFactor;
	@Shadow @Final private double maxValue;
	
	private final double xWidth = 256.0;
	private final double zWidth = 256.0;

	private long source;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(RandomSource random, Pair octavesAndAmplitudes, boolean useNewFactory, CallbackInfo ci) {
		source = random.nextLong();
	}

	public double getValue(double x, double y, double z, double yScale, double yMax, boolean useFixedY) {
		double d = 0.0;
		double e = this.lowestFreqInputFactor;
		double f = this.lowestFreqValueFactor;

		for (int i = 0; i < this.noiseLevels.length; i++) {
			ImprovedNoise improvedNoise = this.noiseLevels[i];
			if (improvedNoise != null) {
				((NoiseScaling) (Object) improvedNoise).setNoiseScaling(e);
				double g = improvedNoise.noise(wrap(x * e), useFixedY ? -improvedNoise.yo : wrap(y * e), wrap(z * e), yScale * e, yMax * e);

				///////
				/**
				long random = (long) (improvedNoise.xo + improvedNoise.yo + improvedNoise.zo);
				double xa = x / xWidth;
				double za = z / zWidth;

				double rxa = xa * 2.0 * Math.PI;
				double rza = za * 2.0 * Math.PI;

				double noise4 = OpenSimplex2S.noise4_Fallback(random, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
				double noise1 = OpenSimplex2S.noise2(random, 0, useFixedY ? -improvedNoise.yo : y);
				double g = (noise4 + noise1)/2.0;**/
				///////

				d += this.amplitudes.getDouble(i) * g * f;
			}

			e *= 2.0;
			f /= 2.0;
		}

		return d;
	}

	/**
	public double getValue(double x, double y, double z, double yScale, double yMax, boolean useFixedY) {

		double xa = x / xWidth;
		double za = z / zWidth;

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		//return OpenSimplex2S.noise2_ImproveX(source, noise4, y);
		return noise4;

		double d = 0.0;
		double e = this.lowestFreqInputFactor;
		double f = this.lowestFreqValueFactor;

		for (int i = 0; i < this.noiseLevels.length; i++) {
			ImprovedNoise improvedNoise = this.noiseLevels[i];
			if (improvedNoise != null) {
				double g = improvedNoise.noise(wrap(x * e), useFixedY ? -improvedNoise.yo : wrap(y * e), wrap(z * e), yScale * e, yMax * e);
				d += this.amplitudes.getDouble(i) * g * f;
			}

			e *= 2.0;
			f /= 2.0;
		}

		return d;
	}**/
}
