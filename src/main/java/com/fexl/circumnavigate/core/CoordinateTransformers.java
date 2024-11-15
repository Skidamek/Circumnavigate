/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate.core;

import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Transforms regular coordinates from and to wrapped coordinates.
 * Wrapped coordinates are limited by defined chunk bounds in all (x,z) directions.
 */
public class CoordinateTransformers {
	private final int lowerChunkBounds;
	private final int upperChunkBounds;

	private final int coordDomainLength;
	private final int chunkDomainLength;

	private final int chunkWidth = LevelChunkSection.SECTION_WIDTH;

	public CoordinateTransformers(int lowerChunkBounds, int upperChunkBounds) {
		this.lowerChunkBounds = lowerChunkBounds;
		this.upperChunkBounds = upperChunkBounds;

		this.coordDomainLength = upperChunkBounds * chunkWidth - lowerChunkBounds * chunkWidth;
		this.chunkDomainLength = upperChunkBounds - lowerChunkBounds;
	}

	/**
	 * For bounds equidistant from (0)
	 */
	public CoordinateTransformers(int chunkBounds) {
		this(-chunkBounds, chunkBounds);
	}

	/**
	 * Wraps a coordinate around the bounds of an axis.<br>
	 * These bounds are inclusively defined as [lowerChunkBounds * chunkWidth, upperChunkBounds * chunkWidth]
	 */
	public double wrapCoordToLimit(double coord) {
		//Short-circuit
		if(!isCoordOverLimit(coord)) return coord;

		double domainStart = lowerChunkBounds * chunkWidth;
		double wrappedCoord = (coord - domainStart) % coordDomainLength;

		// If wrappedCoord is negative, adjust it by adding coordDomainLength
		if (wrappedCoord < 0) {
			wrappedCoord += coordDomainLength;
		}

		return domainStart + wrappedCoord;
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
		//Short-circuit
		if(!isChunkOverLimit(chunkCoord)) return chunkCoord;

		int offset = chunkCoord - lowerChunkBounds;
		int wrappedCoord = offset % chunkDomainLength;

		// If wrappedCoord is negative, adjust it by adding chunkDomainLength
		if (wrappedCoord < 0) {
			wrappedCoord += chunkDomainLength;
		}

		return lowerChunkBounds + wrappedCoord;
	}

	/**
	 * This method only works properly on wrapped coordinates that are >domainLength/2 from the refCoord, measured as a direct distance.
	 * @param refCoord The reference coordinate wrappedCoord should extend past when converting.
	 * @param wrappedCoord The wrapped coordinate that should be extended past the bounds of the wrapped graph, relative to the refCoord.
	 * @return The wrappedCoord converted to extend past the bounds of the graph, its extension-direction determined by refCoord.
	 */
	public double unwrapCoordFromLimit(double refCoord, double wrappedCoord) {
		double wrappedRefCoord = wrapCoordToLimit(refCoord);

		double diff = wrappedCoord - wrappedRefCoord;

		double unwrappedCoord = refCoord + diff;

		// Adjust to ensure the unwrapped coordinate is correct
		if (unwrappedCoord < refCoord - coordDomainLength / 2) {
			unwrappedCoord += coordDomainLength;
		}
		if (unwrappedCoord > refCoord + coordDomainLength / 2) {
			unwrappedCoord -= coordDomainLength;
		}

		return unwrappedCoord;
	}

	public int unwrapCoordFromLimit(int refCoord, int wrappedCoord) {
		return (int)unwrapCoordFromLimit((double)refCoord, wrappedCoord);
	}

	public int unwrapChunkFromLimit(int refChunkCoord, int wrappedChunkCoord) {
		//Wrap the reference position
		int wrappedRefCoord = wrapChunkToLimit(refChunkCoord);

		//Difference between the reference and the chunk coord as wrapped
		int diff = wrappedChunkCoord - wrappedRefCoord;


		int unwrappedCoord = refChunkCoord + diff;

		 // Adjust to ensure the unwrapped chunk coordinate is correct
		 if (unwrappedCoord < refChunkCoord - chunkDomainLength / 2) {
		    unwrappedCoord += chunkDomainLength;
		 }
		 else if (unwrappedCoord > refChunkCoord + chunkDomainLength / 2) {
		    unwrappedCoord -= chunkDomainLength;
		 }
		return unwrappedCoord;
	}


	public double getDeltaBetween(double fromCoord, double toCoord) {
		double toCoordUnwrapped = unwrapCoordFromLimit(fromCoord, toCoord);

		return toCoordUnwrapped - fromCoord;
	}

	public boolean isCoordOverLimit(double coord) {
		if(coord >= upperChunkBounds * chunkWidth || coord < lowerChunkBounds * chunkWidth)
			return true;
		else
			return false;
	}

	public boolean isCoordOverLimit(int coord) {
		return isCoordOverLimit((double) coord);
	}

	public boolean isChunkOverLimit(int chunkCoord) {
		if(chunkCoord > upperChunkBounds - 1 || chunkCoord < lowerChunkBounds)
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
