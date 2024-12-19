/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.injected.NoiseScaling;
import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ImprovedNoise.class)
public abstract class ImprovedNoiseMixin implements NoiseScaling {
	ImprovedNoise thiz = (ImprovedNoise) (Object) this;
	//private final int seed = 2497518;
	//private final long randomSource = new WorldgenRandom(new LegacyRandomSource(seed)).nextLong();

	private static long lastTime = 0;

	@Final @Shadow private byte[] p;
	@Final @Shadow public double xo;
	@Final @Shadow public double yo;
	@Final @Shadow public double zo;

	private long source;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(RandomSource random, CallbackInfo ci) {
		source = random.nextLong();
	}

	public double noise(double x, double y, double z, double yScale, double yMax) {
		int intY = Mth.floor(y);
		double deltaY = y - intY;

		double n;
		if (yScale != 0.0) {
			double m;
			if (yMax >= 0.0 && yMax < deltaY) {
				m = yMax;
			} else {
				m = deltaY;
			}

			n = (double)Mth.floor(m / yScale + 1.0E-7F) * yScale;
		} else {
			n = 0.0;
		}

		double xa = ((x - xAdd) / xMul) / (xWidth);
		double za = ((z - zAdd) / zMul) / (zWidth);

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		double noise1 = OpenSimplex2S.noise2(source, 0, y - n);
		return (noise4 + noise1)/2.0;
	}

	double xMul = 1;
	double zMul = 1;
	double xAdd = 0;
	double zAdd = 0;

	public void setMul(double noiseScaling) {
		this.xMul = noiseScaling;
		this.zMul = noiseScaling;
	}

	public void setXMul(double xMul) {
		this.xMul = xMul;
	}

	public void setZMul(double zMul) {
		this.zMul = zMul;
	}

	public void setXAdd(double xAdd) {
		this.xAdd = xAdd;
	}

	public void setZAdd(double zAdd) {
		this.zAdd = zAdd;
	}

	private final double xWidth = 1024.0;
	private final double zWidth = 1024.0;
}
