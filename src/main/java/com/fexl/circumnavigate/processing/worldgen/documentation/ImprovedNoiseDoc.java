/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing.worldgen.documentation;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

/**
 * Generates a single octave of Perlin noise.
 */
public final class ImprovedNoiseDoc {
	private static final float SHIFT_UP_EPSILON = 1.0E-7F;
	/**
	 * A permutation array used in noise calculation.
	 * This is populated with the values [0, 256) and shuffled per instance of {@code ImprovedNoise}.
	 *
	 * @see #p(int)
	 */
	/**
	private final byte[] p;
	public final double xo;
	public final double yo;
	public final double zo;

	public ImprovedNoiseDoc(RandomSource random) {
		this.xo = random.nextDouble() * 256.0;
		this.yo = random.nextDouble() * 256.0;
		this.zo = random.nextDouble() * 256.0;
		this.p = new byte[256];

		for (int i = 0; i < 256; i++) {
			this.p[i] = (byte)i;
		}

		for (int i = 0; i < 256; i++) {
			int j = random.nextInt(256 - i);
			byte b = this.p[i];
			this.p[i] = this.p[i + j];
			this.p[i + j] = b;
		}
	}

	public double noise(double x, double y, double z) {
		return this.noise(x, y, z, 0.0, 0.0);
	}

	@Deprecated
	public double noise(double x, double y, double z, double yScale, double yMax) {
		double offsetX = x + this.xo;
		double offsetY = y + this.yo;
		double offsetZ = z + this.zo;
		int intOffsetX = Mth.floor(offsetX);
		int intOffsetY = Mth.floor(offsetY);
		int intOffsetZ = Mth.floor(offsetZ);
		double normalizedOffsetX = offsetX - (double) intOffsetX;
		double normalizedOffsetY = offsetY - (double) intOffsetY;
		double normalizedOffsetZ = offsetZ - (double) intOffsetZ;
		double n;
		//If yScale is defined
		if (yScale != 0.0) {
			double m;
			//If yMax is positive or 0 and is less than normalizedOffsetY
			if (yMax >= 0.0 && yMax < normalizedOffsetY) {
				//p_pX1_Y is yMax
				m = yMax;
			} else {
				//p_pX1_Y is normalizedOffsetY
				m = normalizedOffsetY;
			}

			//0.0000001
			//Return integer as double

			n = (double)Mth.floor(m / yScale + 1.0E-7F) * yScale;
		//yScale not defined
		} else {
			n = 0.0;
		}

		return this.sampleAndLerp(intOffsetX, intOffsetY, intOffsetZ, normalizedOffsetX, normalizedOffsetY - n, normalizedOffsetZ, normalizedOffsetY);
	}

	//Dot product of GRADIENT (0->15) and factors
	private static double gradDot(int gradIndex, double xFactor, double yFactor, double zFactor) {
		return SimplexNoise.dot(SimplexNoise.GRADIENT[gradIndex & 15], xFactor, yFactor, zFactor);
	}

	private int p(int index) {
		return this.p[index & 0xFF] & 0xFF;
	}

	private double sampleAndLerp(int gridX, int gridY, int gridZ, double deltaX, double weirdDeltaY, double deltaZ, double deltaY) {
		//Obtain perm array values between 0 and 255

		// p(X)
		int pX = this.p(gridX);
		// p(X + 1)
		int pX1 = this.p(gridX + 1);

		// p(p(X) + Y)
		int p_pX_Y = this.p(pX + gridY);
		// p(p(X) + Y + 1)
		int p_pX_Y1 = this.p(pX + gridY + 1);

		// p(p(X + 1) + Y)
		int p_pX1_Y = this.p(pX1 + gridY);
		// p(p(X + 1) + Y + 1)
		int p_pX1_Y1 = this.p(pX1 + gridY + 1);

		//Dot product of
		double dot_pXY_Z_delta = gradDot(this.p(p_pX_Y + gridZ), deltaX, weirdDeltaY, deltaZ);
		//X Offset
		double dot_pX1Y_Z_delta_X1 = gradDot(this.p(p_pX1_Y + gridZ), deltaX - 1.0, weirdDeltaY, deltaZ);

		double dot_pXY1_Z_Y1 = gradDot(this.p(p_pX_Y1 + gridZ), deltaX, weirdDeltaY - 1.0, deltaZ);
		//X Offset
		double dot_pX1Y1_Z_X1_Y1 = gradDot(this.p(p_pX1_Y1 + gridZ), deltaX - 1.0, weirdDeltaY - 1.0, deltaZ);

		double dot_pXY_Z1_Z1 = gradDot(this.p(p_pX_Y + gridZ + 1), deltaX, weirdDeltaY, deltaZ - 1.0);
		//X Offset
		double dot_pX1Y_Z1_X1_Z1 = gradDot(this.p(p_pX1_Y + gridZ + 1), deltaX - 1.0, weirdDeltaY, deltaZ - 1.0);

		double dot_pXY1_Z1_Y1_Z1 = gradDot(this.p(p_pX_Y1 + gridZ + 1), deltaX, weirdDeltaY - 1.0, deltaZ - 1.0);
		//X Offset
		double dot_pX1Y1_Z1_X1_Y1_Z1 = gradDot(this.p(p_pX1_Y1 + gridZ + 1), deltaX - 1.0, weirdDeltaY - 1.0, deltaZ - 1.0);

		//ken perlin smoothstep
		double xSmooth = Mth.smoothstep(deltaX);
		double ySmooth = Mth.smoothstep(deltaY);
		double zSmooth = Mth.smoothstep(deltaZ);
		return Mth.lerp3(xSmooth, ySmooth, zSmooth, dot_pXY_Z_delta, dot_pX1Y_Z_delta_X1, dot_pXY1_Z_Y1, dot_pX1Y1_Z_X1_Y1, dot_pXY_Z1_Z1, dot_pX1Y_Z1_X1_Z1, dot_pXY1_Z1_Y1_Z1, dot_pX1Y1_Z1_X1_Y1_Z1);
	}

	private double sampleWithDerivative(int gridX, int gridY, int gridZ, double deltaX, double deltaY, double deltaZ, double[] noiseValues) {
		int i = this.p(gridX);
		int j = this.p(gridX + 1);
		int k = this.p(i + gridY);
		int l = this.p(i + gridY + 1);
		int m = this.p(j + gridY);
		int n = this.p(j + gridY + 1);
		int o = this.p(k + gridZ);
		int p = this.p(m + gridZ);
		int q = this.p(l + gridZ);
		int r = this.p(n + gridZ);
		int s = this.p(k + gridZ + 1);
		int t = this.p(m + gridZ + 1);
		int u = this.p(l + gridZ + 1);
		int v = this.p(n + gridZ + 1);
		int[] is = SimplexNoise.GRADIENT[o & 15];
		int[] js = SimplexNoise.GRADIENT[p & 15];
		int[] ks = SimplexNoise.GRADIENT[q & 15];
		int[] ls = SimplexNoise.GRADIENT[r & 15];
		int[] ms = SimplexNoise.GRADIENT[s & 15];
		int[] ns = SimplexNoise.GRADIENT[t & 15];
		int[] os = SimplexNoise.GRADIENT[u & 15];
		int[] ps = SimplexNoise.GRADIENT[v & 15];
		double d = SimplexNoise.dot(is, deltaX, deltaY, deltaZ);
		double e = SimplexNoise.dot(js, deltaX - 1.0, deltaY, deltaZ);
		double f = SimplexNoise.dot(ks, deltaX, deltaY - 1.0, deltaZ);
		double g = SimplexNoise.dot(ls, deltaX - 1.0, deltaY - 1.0, deltaZ);
		double h = SimplexNoise.dot(ms, deltaX, deltaY, deltaZ - 1.0);
		double w = SimplexNoise.dot(ns, deltaX - 1.0, deltaY, deltaZ - 1.0);
		double x = SimplexNoise.dot(os, deltaX, deltaY - 1.0, deltaZ - 1.0);
		double y = SimplexNoise.dot(ps, deltaX - 1.0, deltaY - 1.0, deltaZ - 1.0);
		double z = Mth.smoothstep(deltaX);
		double aa = Mth.smoothstep(deltaY);
		double ab = Mth.smoothstep(deltaZ);
		double ac = Mth.lerp3(z, aa, ab, (double)is[0], (double)js[0], (double)ks[0], (double)ls[0], (double)ms[0], (double)ns[0], (double)os[0], (double)ps[0]);
		double ad = Mth.lerp3(z, aa, ab, (double)is[1], (double)js[1], (double)ks[1], (double)ls[1], (double)ms[1], (double)ns[1], (double)os[1], (double)ps[1]);
		double ae = Mth.lerp3(z, aa, ab, (double)is[2], (double)js[2], (double)ks[2], (double)ls[2], (double)ms[2], (double)ns[2], (double)os[2], (double)ps[2]);
		double af = Mth.lerp2(aa, ab, e - d, g - f, w - h, y - x);
		double ag = Mth.lerp2(ab, z, f - d, x - h, g - e, y - w);
		double ah = Mth.lerp2(z, aa, h - d, w - e, x - f, y - g);
		double ai = Mth.smoothstepDerivative(deltaX);
		double aj = Mth.smoothstepDerivative(deltaY);
		double ak = Mth.smoothstepDerivative(deltaZ);
		double al = ac + ai * af;
		double am = ad + aj * ag;
		double an = ae + ak * ah;
		noiseValues[0] += al;
		noiseValues[1] += am;
		noiseValues[2] += an;
		return Mth.lerp3(z, aa, ab, d, e, f, g, h, w, x, y);
	}

	@VisibleForTesting
	public void parityConfigString(StringBuilder builder) {
		NoiseUtils.parityNoiseOctaveConfigString(builder, this.xo, this.yo, this.zo, this.p);
	}**/
}

