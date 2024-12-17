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
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimplexNoise.class)
public abstract class SimplexNoiseMixin implements NoiseScaling {
	@Final @Shadow public double xo;
	@Final @Shadow public double yo;
	@Final @Shadow public double zo;
	@Final @Shadow private int[] p;
	@Final @Shadow private static double SQRT_3;
	@Final @Shadow private static double F2;
	@Final @Shadow private static double G2;
	@Final @Shadow abstract public int p(int index);
	@Final @Shadow abstract public double getCornerNoise3D(int gradientIndex, double x, double y, double z, double offset);

	private int xWidth = 256;
	private int zWidth = 256;

	private long source;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(RandomSource random, CallbackInfo ci) {
		source = random.nextLong();
	}

	public double getValue(double x, double y) {
		double xa = x - (isOffset ? this.xo : 0.0) / (xWidth * xScaling);
		double za = y - (isOffset ? this.zo : 0.0) / (zWidth * zScaling);

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		return OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
	}

	public double getValue(double x, double y, double z) {
		double xa = x - (isOffset ? this.xo : 0.0) / (xWidth * xScaling);
		double za = z - (isOffset ? this.zo : 0.0) / (zWidth * zScaling);

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_Fallback(source, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		double noise1 = OpenSimplex2S.noise2(source, 0, y);
		return (noise4 + noise1)/2.0;
	}

	double xScaling = 1;
	double zScaling = 1;
	boolean isOffset = false;

	public void setXScaling(double xScaling) {
		this.xScaling = xScaling;
	}

	public void setZScaling(double zScaling) {
		this.zScaling = zScaling;
	}

	public void setNoiseScaling(double noiseScaling) {
		this.xScaling = noiseScaling;
		this.zScaling = noiseScaling;
	}

	public void setOffset(boolean isOffset) {
		this.isOffset = isOffset;
	}

	/**
	public double getValue(double x, double y) {
		return 70 * OpenSimplex2S.noise2((long) (xo + yo + zo), x, y);
	}

	public double getValue(double x, double y, double z) {
		return 32 * OpenSimplex2S.noise3_Fallback((long) (xo + yo + zo), x, y, z);
	}**/
}
