/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class Vec3iWrapped extends Vec3i {
	final WorldTransformer transformer;

	public Vec3iWrapped(int x, int y, int z, WorldTransformer transformer) {
		super(transformer.xTransformer.wrapCoordToLimit(x), y, transformer.zTransformer.wrapCoordToLimit(z));
		this.transformer = transformer;
	}

	@Override
	protected Vec3i setX(int x) {
		return this.setX(transformer.xTransformer.wrapCoordToLimit(x));
	}

	@Override
	protected Vec3i setZ(int z) {
		return this.setZ(transformer.zTransformer.wrapCoordToLimit(z));
	}

	@Override
	public Vec3i offset(int dx, int dy, int dz) {
		return dx == 0 && dy == 0 && dz == 0 ? this : new Vec3iWrapped(this.getX() + dx, this.getY() + dy, this.getZ() + dz, this.transformer);
	}

	@Override
	public Vec3i multiply(int scalar) {
		if (scalar == 1) {
			return this;
		} else {
			return scalar == 0 ? ZERO : new Vec3iWrapped(this.getX() * scalar, this.getY() * scalar, this.getZ() * scalar, this.transformer);
		}
	}

	@Override
	public Vec3i relative(Direction direction, int distance) {
		return distance == 0
			? this
			: new Vec3iWrapped(this.getX() + direction.getStepX() * distance, this.getY() + direction.getStepY() * distance, this.getZ() + direction.getStepZ() * distance, this.transformer);
	}

	@Override
	public Vec3i relative(Direction.Axis axis, int amount) {
		if (amount == 0) {
			return this;
		} else {
			int i = axis == Direction.Axis.X ? amount : 0;
			int j = axis == Direction.Axis.Y ? amount : 0;
			int k = axis == Direction.Axis.Z ? amount : 0;
			return new Vec3iWrapped(this.getX() + i, this.getY() + j, this.getZ() + k, this.transformer);
		}
	}

	@Override
	public Vec3i cross(Vec3i vector) {
		return new Vec3iWrapped(
			this.getY() * vector.getZ() - this.getZ() * vector.getY(),
			this.getZ() * vector.getX() - this.getX() * vector.getZ(),
			this.getX() * vector.getY() - this.getY() * vector.getX(),
			this.transformer
		);
	}

	@Override
	public double distToCenterSqr(double x, double y, double z) {
		return transformer.distanceToSqrWrapped(x, y, z, this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5);
	}

	@Override
	public double distToLowCornerSqr(double x, double y, double z) {
		return transformer.distanceToSqrWrapped(x, y, z, this.getX(), this.getY(), this.getZ());
	}

	@Override
	public int distManhattan(Vec3i vector) {
		float f = transformer.xTransformer.wrapCoordToLimit(Math.abs(vector.getX() - this.getX()));
		float g = (float)Math.abs(vector.getY() - this.getY());
		float h = transformer.zTransformer.wrapCoordToLimit(Math.abs(vector.getZ() - this.getZ()));
		return (int)(f + g + h);
	}
}
