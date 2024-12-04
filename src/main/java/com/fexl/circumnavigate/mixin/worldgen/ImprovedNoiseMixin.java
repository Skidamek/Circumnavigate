/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ImprovedNoise.class)
public abstract class ImprovedNoiseMixin {
	//private final int seed = 2497518;
	//private final long randomSource = new WorldgenRandom(new LegacyRandomSource(seed)).nextLong();

	private static long lastTime = 0;

	@Final @Shadow private byte[] p;
	@Final @Shadow public double xo;
	@Final @Shadow public double yo;
	@Final @Shadow public double zo;

	@Shadow
	protected abstract double sampleAndLerp(int gridX, int gridY, int gridZ, double deltaX, double weirdDeltaY, double deltaZ, double deltaY);


	/**
	@Inject(method = "noise(DDDDD)D", at = @At("HEAD"), cancellable = true)
	public void noise(double x, double y, double z, double yScale, double yMax, CallbackInfoReturnable<Double> cir) {
		if(lastTime + 1000 < System.currentTimeMillis()) {
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			StringBuilder builder = new StringBuilder();
			for(StackTraceElement element : stackTraceElements) {
				builder.append(element).append(", ");
			}
			System.out.println(builder);
			lastTime = System.currentTimeMillis();
		}


	}**/
	/**
	@Inject(method = "noise(DDDDD)D", at = @At("HEAD"), cancellable = true)
	public void noise(double x, double y, double z, double yScale, double yMax, CallbackInfoReturnable<Double> cir) {
		cir.cancel();
		double d = x + this.xo;
		double e = y + this.yo;
		double f = z + this.zo;

		int i = Mth.floor(d);
		int j = Mth.floor(e);
		int k = Mth.floor(f);
		double g = d - (double)i;
		double h = e - (double)j;
		double l = f - (double)k;

		double n;
		if (yScale != 0.0) {
			double m;
			if (yMax >= 0.0 && yMax < h) {
				m = yMax;
			} else {
				m = h;
			}

			n = (double)Mth.floor(m / yScale + 1.0E-7F) * yScale;
		} else {
			n = 0.0;
		}

		//double sample = this.sampleAndLerp(i, j, k, g, h - n, l, h);
		//Assume yScale and yMax are 0.0
		double sample = this.sampleAndLerp(i, j, k, g, h - n, l, h);



		if(lastTime + 1000 < System.currentTimeMillis()) {
			//System.out.println("(" + x + ", " + y + ", " + z + "), " + yScale + ", " + yMax + ", " + sample);
			System.out.println("O: " + sample + ", M:" + OpenSimplex2S.noise3_ImproveXZ((long) (xo + yo + zo), i, j, k));
			lastTime = System.currentTimeMillis();
		}
		cir.setReturnValue(sample);
	}**/


	/**
	@Inject(method = "noise(DDDDD)D", at = @At("HEAD"), cancellable = true)
	public void noise(double x, double y, double z, double yScale, double yMax, CallbackInfoReturnable<Double> cir) {
		cir.cancel();

		//cir.setReturnValue((double)OpenSimplex2S.noise3_ImproveXZ((long) xo, x, y, z));
		//cir.setReturnValue((double) OpenSimplex2S.noise2((long) xo, x, z));
	}**/

	private final double xWidth = 256.0;
	private final double zWidth = 256.0;


	/**
	public double noise(double x, double y, double z, double yScale, double yMax) {
		//int i = Mth.floor(x);
		//int j = Mth.floor(y);
		//int k = Mth.floor(z);

		double xa = x / xWidth;
		double za = z / zWidth;

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double sample = OpenSimplex2S.noise4_Fallback((long) (xo + yo + zo), Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));


		if(lastTime + 1000 < System.currentTimeMillis()) {
			System.out.println("(" + i + ", " + j + ", " + k + "), " + yScale + ", " + yMax + ", " + ", (" + Math.sin(rxa) + ", " + Math.cos(rxa) + ", " + Math.sin(rza) + ", " + Math.cos(rza) + "), " + sample);
			lastTime = System.currentTimeMillis();
		}
		return sample;
	}**/

	/**
	public double noise(double x, double y, double z, double yScale, double yMax) {
		//Offset original values by seed
		double d = x + this.xo;
		double e = y + this.yo;
		double f = z + this.zo;

		//Integer of seed offset
		int i = Mth.floor(d);
		int j = Mth.floor(e);
		int k = Mth.floor(f);

		//Offset minus integer offset, equals 1>
		double g = d - (double)i;
		double h = e - (double)j;
		double l = f - (double)k;
		double n;
		if (yScale != 0.0) {
			double m;
			if (yMax >= 0.0 && yMax < h) {
				m = yMax;
			} else {
				m = h;
			}

			n = (double)Mth.floor(m / yScale + 1.0E-7F) * yScale;
		} else {
			n = 0.0;
		}

		return OpenSimplex2S.noise3_Fallback((long) (xo + yo + zo), g, h, l);
	}**/

	/**
	public double noise(double x, double y, double z, double yScale, double yMax) {
	    double d = x + this.xo;
	    double e = y + this.yo;
	    double f = z + this.zo;
		double n = 0.0;
		if (yScale != 0.0) {
			double m = (yMax >= 0.0 && yMax < y) ? yMax : y;
			n = (double) Mth.floor(m / yScale + 1.0E-7F) * yScale;
		}

		return OpenSimplex2S.noise3_Fallback(Mth., d, e - n, f);
	}**/


	/**
	public double noise(double x, double y, double z, double yScale, double yMax) {
		double d = x + this.xo;
		double e = y + this.yo;
		double f = z + this.zo;

		double xa = d / xWidth;
		double za = f / zWidth;

		double rxa = xa * 2.0 * Math.PI;
		double rza = za * 2.0 * Math.PI;

		double noise4 = OpenSimplex2S.noise4_ImproveXYZ_ImproveXZ((long) (xo + yo + zo), Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
		return OpenSimplex2S.noise2_ImproveX((long) (xo + yo + zo), noise4, e);
	}**/
	/**
	public double noise(double x, double y, double z, double yScale, double yMax) {
		double d = x + this.xo;
		double e = y + this.yo;
		double f = z + this.zo;

		int i = Mth.floor(d);
		int j = Mth.floor(e);
		int k = Mth.floor(f);
		double g = d - (double)i;
		double h = e - (double)j;
		double l = f - (double)k;
		double n;
		if (yScale != 0.0) {
			double m;
			if (yMax >= 0.0 && yMax < h) {
				m = yMax;
			} else {
				m = h;
			}

			n = (double)Mth.floor(m / yScale + 1.0E-7F) * yScale;
		} else {
			n = 0.0;
		}

		return this.sampleAndLerp(i, j, k, g, h - n, l, h);
	}**/

	/**
	private double sampleAndLerp(int gridX, int gridY, int gridZ, double deltaX, double weirdDeltaY, double deltaZ, double deltaY) {
		int permutedX = this.p(gridX);
		int permutedXAhead = this.p(gridX + 1);

		int permuteYAgainstX = this.p(permutedX + gridY);
		int permuteYAheadAgainstX = this.p(permutedX + gridY + 1);

		int permuteYAgainstXAhead = this.p(permutedXAhead + gridY);
		int permuteYAheadAgainstXAhead = this.p(permutedXAhead + gridY + 1);

		double d = gradDot(this.p(k + gridZ), deltaX, weirdDeltaY, deltaZ);
		double e = gradDot(this.p(m + gridZ), deltaX - 1.0, weirdDeltaY, deltaZ);
		double f = gradDot(this.p(l + gridZ), deltaX, weirdDeltaY - 1.0, deltaZ);
		double g = gradDot(this.p(n + gridZ), deltaX - 1.0, weirdDeltaY - 1.0, deltaZ);
		double h = gradDot(this.p(k + gridZ + 1), deltaX, weirdDeltaY, deltaZ - 1.0);
		double o = gradDot(this.p(m + gridZ + 1), deltaX - 1.0, weirdDeltaY, deltaZ - 1.0);
		double p = gradDot(this.p(l + gridZ + 1), deltaX, weirdDeltaY - 1.0, deltaZ - 1.0);
		double q = gradDot(this.p(n + gridZ + 1), deltaX - 1.0, weirdDeltaY - 1.0, deltaZ - 1.0);
		double r = Mth.smoothstep(deltaX);
		double s = Mth.smoothstep(deltaY);
		double t = Mth.smoothstep(deltaZ);
		return Mth.lerp3(r, s, t, d, e, f, g, h, o, p, q);
	}**/
}
