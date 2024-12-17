/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.injected.NoiseScaling;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PerlinSimplexNoise.class)
public class PerlinSimplexNoiseMixin {
	@Shadow @Final private SimplexNoise[] noiseLevels;
	@Shadow @Final private double highestFreqValueFactor;
	@Shadow @Final private double highestFreqInputFactor;
	
	public double getValue(double x, double y, boolean useNoiseOffsets) {
		double d = 0.0;
		double e = this.highestFreqInputFactor;
		double f = this.highestFreqValueFactor;

		for (SimplexNoise simplexNoise : this.noiseLevels) {
			if (simplexNoise != null) {
				((NoiseScaling) (Object) simplexNoise).setNoiseScaling(e);
				((NoiseScaling) (Object) simplexNoise).setOffset(useNoiseOffsets);
				d += simplexNoise.getValue(x * e + (useNoiseOffsets ? simplexNoise.xo : 0.0), y * e + (useNoiseOffsets ? simplexNoise.yo : 0.0)) * f;
			}

			e /= 2.0;
			f *= 2.0;
		}

		return d;
	}
}
