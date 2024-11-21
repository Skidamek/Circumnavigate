/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.worldgen;

import com.fexl.circumnavigate.processing.worldgen.OpenSimplex2S;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ImprovedNoise.class)
public class ImprovedNoiseMixin {
	private final int randomSource = new WorldgenRandom(new LegacyRandomSource(597640159)).nextInt();
	private int xWidth = 16;
	private int zWidth = 16;

	public double noise(double x, double y, double z, double yScale, double yMax) {
		double xa = x / xWidth;
		double za = z / zWidth;

		double rxa = xa * 2 * Math.PI;
		double rza = za * 2 * Math.PI;

		return OpenSimplex2S.noise4_Fallback(randomSource, Math.sin(rxa), Math.cos(rxa), Math.sin(rza), Math.cos(rza))+y;
	}
}
