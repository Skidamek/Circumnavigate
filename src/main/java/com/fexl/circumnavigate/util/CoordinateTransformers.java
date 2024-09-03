/*
 * Copyright (c) 2024 Famro Fexl.
 * SPDX-License-Identifier: MIT
 */

package com.fexl.circumnavigate.util;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

/**
 * Transforms regular coordinates from and to wrapped coordinates.
 * Wrapped coordinates are limited by defined chunk bounds in all (x,z) directions.
 */
public class CoordinateTransformers {
	private final int lowerChunkBounds;
	private final int upperChunkBounds;

	private final int chunkWidth = LevelChunkSection.SECTION_WIDTH;

	public CoordinateTransformers(int lowerChunkBounds, int upperChunkBounds) {
		if (lowerChunkBounds > upperChunkBounds)
			throw new IllegalArgumentException("The Lower Chunk Bounds cannot be greater than the Upper Chunk Bounds!");
		//TODO: Determine the minimum bounds achievable. This is likely influenced by viewDistance < domainLength.
		//if (Math.abs(lowerChunkBounds - upperChunkBounds) < 0)
			//throw new IllegalArgumentException("The distance between the bounds is less than the required " + 0 + " chunks!");


		this.lowerChunkBounds = lowerChunkBounds;
		this.upperChunkBounds = upperChunkBounds;
	}

	/**
	 * For bounds equidistant from (0)
	 */
	public CoordinateTransformers(int chunkBounds) {
		this.lowerChunkBounds = -chunkBounds;
		this.upperChunkBounds = chunkBounds;
	}

	/**
	 * Wraps a coordinate around the bounds of an axis.<br>
	 * These bounds are inclusively defined as [lowerChunkBounds * chunkWidth, upperChunkBounds * chunkWidth]
	 */
	public double wrapCoordToLimit(double coord) {
		double domainLength = upperChunkBounds * chunkWidth - lowerChunkBounds * chunkWidth;
		return lowerChunkBounds * chunkWidth + ((coord - lowerChunkBounds * chunkWidth) % domainLength + domainLength) % domainLength;
	}

	public int wrapCoordToLimit(int coord) {
		return (int) this.wrapCoordToLimit((double) coord);
	}

	/**
	 * Wraps a chunk coordinate around the bounds of an axis.<br>
	 * These bounds are inclusively defined as [lowerChunkBounds, upperChunkBounds]
	 * <p>
	 *     For instance, if <code>lowerChunkBounds = -8</code> and <code>upperChunkBounds = 8</code>, example inputs and outputs are as follows:<br>
	 * @example
	 * Input: -9
	 * Output: 8
	 *
	 * @example
	 * Input: 9
	 * Output: -8
	 *
	 * @example
	 * Input: -8
	 * Output: -8
	 *
	 * @example
	 * Input: 8
	 * Output: 8
	 *
	 * @example
	 * Input: 4
	 * Output: 4
	 */
	public int wrapChunkToLimit(int chunkCoord) {
		int domainLength = upperChunkBounds - lowerChunkBounds;
		return lowerChunkBounds + ((chunkCoord - lowerChunkBounds) % domainLength + domainLength) % domainLength;
	}

	/**
	 * This method only works properly on wrapped coordinates that are >domainLength/2 from the refCoord, measured as a direct distance.
	 * @param refCoord The reference coordinate wrappedCoord should extend past when converting.
	 * @param wrappedCoord The wrapped coordinate that should be extended past the bounds of the wrapped graph, relative to the refCoord.
	 * @return The wrappedCoord converted to extend past the bounds of the graph, its extension-direction determined by refCoord.
	 */
	public double unwrapCoordFromLimit(double refCoord, double wrappedCoord) {
		double domainLength = upperChunkBounds * chunkWidth - lowerChunkBounds * chunkWidth;
		double wrappedRefCoord = wrapCoordToLimit(refCoord);

		double diff = wrappedCoord - wrappedRefCoord;

		double unwrappedCoord = refCoord + diff;

		// Adjust to ensure the unwrapped coordinate is correct
		while (unwrappedCoord < refCoord - domainLength / 2) {
			unwrappedCoord += domainLength;
		}
		while (unwrappedCoord > refCoord + domainLength / 2) {
			unwrappedCoord -= domainLength;
		}

		return unwrappedCoord;
	}

	public int unwrapCoordFromLimit(int refCoord, int wrappedCoord) {
		return (int)unwrapCoordFromLimit((double)refCoord, wrappedCoord);
	}

	public int unwrapChunkFromLimit(int refChunkCoord, int wrappedChunkCoord) {
		int domainLength = upperChunkBounds - lowerChunkBounds;
		int wrappedRefCoord = wrapChunkToLimit(refChunkCoord);

		int diff = wrappedChunkCoord - wrappedRefCoord;

		int unwrappedCoord = refChunkCoord + diff;

		// Adjust to ensure the unwrapped chunk coordinate is correct
		while (unwrappedCoord < refChunkCoord - domainLength / 2) {
			unwrappedCoord += domainLength;
		}
		while (unwrappedCoord > refChunkCoord + domainLength / 2) {
			unwrappedCoord -= domainLength;
		}
		return unwrappedCoord;
	}

	public boolean isCoordOverLimit(double coord) {
		if(coord > upperChunkBounds * chunkWidth || coord < lowerChunkBounds * chunkWidth)
			return true;
		else
			return false;
	}

	public boolean isCoordOverLimit(int coord) {
		return isCoordOverLimit((double) coord);
	}

	public boolean isChunkOverLimit(int chunkCoord) {
		if(chunkCoord > upperChunkBounds || chunkCoord < lowerChunkBounds)
			return true;
		else
			return false;
	}

	public boolean isCoordWithinLimitDistance(int coord, int distance) {
		if(coord + distance > upperChunkBounds * chunkWidth || coord - distance < lowerChunkBounds * chunkWidth)
			return true;
		else
			return false;
	}

	public boolean isChunkWithinLimitDistance(int chunkCoord, int chunkDistance) {
		return isCoordWithinLimitDistance(chunkCoord * chunkWidth, chunkDistance * chunkWidth);
	}


}
