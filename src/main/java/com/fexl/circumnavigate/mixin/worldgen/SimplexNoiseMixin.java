/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SimplexNoise.class)
public abstract class SimplexNoiseMixin {
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

	/**
	public double getValue(double x, double y) {
		int i = Mth.floor(x);
		int j = Mth.floor(y);
		int xa = i / xWidth;
		int za = j / zWidth;

		double rxa = xa * 2 * Math.PI;
		double rza = za * 2 * Math.PI;

		return OpenSimplex2S.noise4_Fallback((long) (xo + yo + zo), Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
	}**/



	/**
	public double getValue(double x, double y, double z) {
		int i = Mth.floor(x);
		int j = Mth.floor(z);
		int xa = i / xWidth;
		int za = j / zWidth;

		double rxa = xa * 2 * Math.PI;
		double rza = za * 2 * Math.PI;

		return OpenSimplex2S.noise4_Fallback((long) (xo + yo + zo), Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza));
	}**/

	/**
	public double getValue(double x, double y) {
		return 70 * OpenSimplex2S.noise2((long) (xo + yo + zo), x, y);
	}

	public double getValue(double x, double y, double z) {
		return 32 * OpenSimplex2S.noise3_Fallback((long) (xo + yo + zo), x, y, z);
	}**/
}
