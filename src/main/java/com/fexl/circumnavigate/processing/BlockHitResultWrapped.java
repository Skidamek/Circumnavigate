/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.processing;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlockHitResultWrapped extends BlockHitResult {
	final WorldTransformer transformer;

	private final Vec3 location;
	private final Direction direction;
	private final boolean miss;
	private final boolean inside;

	private BlockHitResultWrapped(boolean miss, Vec3 location, Direction direction, BlockPos blockPos, boolean inside, WorldTransformer transformer) {
		super(transformer.translateVecToBounds(location), direction, transformer.translateBlockToBounds(blockPos), inside);
		this.miss = miss;
		this.location = location;
		this.direction = direction;
		this.inside = inside;
		this.transformer = transformer;
	}

	public BlockHitResultWrapped(BlockHitResult result, WorldTransformer transformer) {
		this(result.getType() == Type.MISS, result.getLocation(), result.getDirection(), result.getBlockPos(), result.isInside(), transformer);
	}

	@Override
	public @NotNull BlockHitResultWrapped withPosition(BlockPos pos) {
		return new BlockHitResultWrapped(this.miss, this.location, this.direction, pos, this.inside, this.transformer);
	}

	@Override
	public double distanceTo(Entity entity) {
		return this.transformer.distanceToSqrWrappedCoord(entity.getX(), entity.getY(), entity.getZ(), this.location.x, this.location.y, this.location.z);
	}
}
