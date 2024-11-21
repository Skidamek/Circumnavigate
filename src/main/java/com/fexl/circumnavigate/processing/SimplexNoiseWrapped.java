/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class SimplexNoiseWrapped extends SimplexNoise {
	private static final double SQRT_3 = Math.sqrt(3.0);
	private static final double F2 = 0.5 * (SQRT_3 - 1.0);
	private static final double G2 = (3.0 - SQRT_3) / 6.0;

	public SimplexNoiseWrapped(RandomSource random) {
		super(random);
	}

	public double getValueNew(double x, double y) {
		// Skew the input space to determine which simplex cell we're in
		double skewedSum = (x + y) * F2;
		int gridX = Mth.floor(x + skewedSum);
		int gridY = Mth.floor(y + skewedSum);

		// Unskew the coordinates to the cell's origin
		double unskewedSum = (gridX + gridY) * G2;
		double localX = gridX - unskewedSum;
		double localY = gridY - unskewedSum;

		// Calculate the position relative to the cell
		double offsetX = x - localX;
		double offsetY = y - localY;

		// Determine which corners of the simplex to calculate
		int corner1, corner2;
		if (offsetX > offsetY) {
			corner1 = 1; // x is further from the origin
			corner2 = 0;
		} else {
			corner1 = 0;
			corner2 = 1;
		}

		// Calculate the offsets to the corners
		double cornerOffsetX1 = offsetX - corner1 + G2;
		double cornerOffsetY1 = offsetY - corner2 + G2;
		double cornerOffsetX2 = offsetX - 1.0 + 2.0 * G2;
		double cornerOffsetY2 = offsetY - 1.0 + 2.0 * G2;

		// Hashing to determine gradient indices
		int hashedX = gridX & 0xFF;
		int hashedY = gridY & 0xFF;
		int gradientIndex1 = this.p(hashedX + this.p(hashedY)) % 12;
		int gradientIndex2 = this.p(hashedX + corner1 + this.p(hashedY + corner2)) % 12;
		int gradientIndex3 = this.p(hashedX + 1 + this.p(hashedY + 1)) % 12;

		// Calculate contributions from each corner
		double noiseContribution1 = this.getCornerNoise3D(gradientIndex1, offsetX, offsetY, 0.0, 0.5);
		double noiseContribution2 = this.getCornerNoise3D(gradientIndex2, cornerOffsetX1, cornerOffsetY1, 0.0, 0.5);
		double noiseContribution3 = this.getCornerNoise3D(gradientIndex3, cornerOffsetX2, cornerOffsetY2, 0.0, 0.5);

		// Combine contributions and scale the result
		return 70.0 * (noiseContribution1 + noiseContribution2 + noiseContribution3);
	}

	public double getValueNew(double x, double y, double z) {
		// Constants for skewing and unskewing
		double skewFactor = 1.0 / 3.0;
		double unskewFactor = 1.0 / 6.0;

		// Skew the input space to find the grid cell
		double skewedSum = (x + y + z) * skewFactor;
		int gridX = Mth.floor(x + skewedSum);
		int gridY = Mth.floor(y + skewedSum);
		int gridZ = Mth.floor(z + skewedSum);

		// Unskew the coordinates to the cell's origin
		double unskewedSum = (gridX + gridY + gridZ) * unskewFactor;
		double localX = gridX - unskewedSum;
		double localY = gridY - unskewedSum;
		double localZ = gridZ - unskewedSum;

		// Calculate the position relative to the cell
		double offsetX = x - localX;
		double offsetY = y - localY;
		double offsetZ = z - localZ;

		// Determine which corners of the simplex to calculate
		int corner1, corner2, corner3, corner4, corner5, corner6;

		if (offsetX >= offsetY) {
			if (offsetY >= offsetZ) {
				corner1 = 1; corner2 = 0; corner3 = 0; // x, y, z ordering
				corner4 = 1; corner5 = 1; corner6 = 0;
			} else if (offsetX >= offsetZ) {
				corner1 = 1; corner2 = 0; corner3 = 0;
				corner4 = 0; corner5 = 1; corner6 = 1;
			} else {
				corner1 = 0; corner2 = 0; corner3 = 1;
				corner4 = 1; corner5 = 0; corner6 = 1;
			}
		} else if (offsetY < offsetZ) {
			corner1 = 0; corner2 = 0; corner3 = 1;
			corner4 = 0; corner5 = 1; corner6 = 1;
		} else if (offsetX < offsetZ) {
			corner1 = 0; corner2 = 1; corner3 = 0;
			corner4 = 0; corner5 = 1; corner6 = 1;
		} else {
			corner1 = 0; corner2 = 1; corner3 = 0;
			corner4 = 1; corner5 = 1; corner6 = 0;
		}

		// Calculate corner offsets
		double cornerOffsetX1 = offsetX - corner1 + unskewFactor;
		double cornerOffsetY1 = offsetY - corner2 + unskewFactor;
		double cornerOffsetZ1 = offsetZ - corner3 + unskewFactor;

		double cornerOffsetX2 = offsetX - corner4 + skewFactor;
		double cornerOffsetY2 = offsetY - corner5 + skewFactor;
		double cornerOffsetZ2 = offsetZ - corner6 + skewFactor;

		// Prepare the offsets for the last corner
		double lastCornerX = offsetX - 1.0 + 0.5;
		double lastCornerY = offsetY - 1.0 + 0.5;
		double lastCornerZ = offsetZ - 1.0 + 0.5;

		// Hashing to determine gradient indices
		int hashedX = gridX & 0xFF;
		int hashedY = gridY & 0xFF;
		int hashedZ = gridZ & 0xFF;
		int gradientIndex1 = this.p(hashedX + this.p(hashedY + this.p(hashedZ))) % 12;
		int gradientIndex2 = this.p(hashedX + corner1 + this.p(hashedY + corner2 + this.p(hashedZ + corner3))) % 12;
		int gradientIndex3 = this.p(hashedX + corner4 + this.p(hashedY + corner5 + this.p(hashedZ + corner6))) % 12;
		int gradientIndex4 = this.p(hashedX + 1 + this.p(hashedY + 1 + this.p(hashedZ + 1))) % 12;

		// Calculate contributions from each corner
		double noiseContribution1 = this.getCornerNoise3D(gradientIndex1, offsetX, offsetY, offsetZ, 0.6);
		double noiseContribution2 = this.getCornerNoise3D(gradientIndex2, cornerOffsetX1, cornerOffsetY1, cornerOffsetZ1, 0.6);
		double noiseContribution3 = this.getCornerNoise3D(gradientIndex3, cornerOffsetX2, cornerOffsetY2, cornerOffsetZ2, 0.6);
		double noiseContribution4 = this.getCornerNoise3D(gradientIndex4, lastCornerX, lastCornerY, lastCornerZ, 0.6);

		// Combine contributions and scale the result
		return 32.0 * (noiseContribution1 + noiseContribution2 + noiseContribution3 + noiseContribution4);
	}
}
