/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.entityHandle;

import com.fexl.circumnavigate.core.WorldTransformer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow private Level level;

	@Shadow public abstract double getX();

	Entity thiz = (Entity) (Object) this;

	/**
	 * Modifies the inputted X position of the entity to be within the wrapping bounds
	 */
	@ModifyVariable(method = "setPosRaw", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public double wrapX(double x) {
		if (level.isClientSide()) return x;
		return level.getTransformer().xTransformer.wrapCoordToLimit(x);
	}

	/**
	 * Modifies the inputted Z position of the entity to be within the wrapping bounds
	 */
	@ModifyVariable(method = "setPosRaw", at = @At("HEAD"), ordinal = 2, argsOnly = true)
	public double wrapZ(double z) {
		if (level.isClientSide()) return z;
		return level.getTransformer().zTransformer.wrapCoordToLimit(z);
	}

	/**
	 * Checks if an entity is colliding with a block. Modified to support wrapped worlds.
	 */
	@Redirect(method = "isColliding", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/Shapes;joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z"))
	public boolean wrapAABB(VoxelShape shape1, VoxelShape shape2, BooleanOp resultOperator) {
		if (level.isClientSide()) return Shapes.joinIsNotEmpty(shape1, shape2, resultOperator);

		WorldTransformer transformer = this.level.getTransformer();
		VoxelShape result = Shapes.create(transformer.translateAABBFromBounds(shape1.bounds(), shape2.bounds()));

		return Shapes.joinIsNotEmpty(shape1, result, resultOperator);
	}

	@Inject(method = "distanceTo", at = @At("HEAD"), cancellable = true)
	public void wrapDistanceSquared1(Entity entity, CallbackInfoReturnable<Float> cir) {
		if(level.isClientSide) return;
		cir.cancel();
		cir.setReturnValue(Mth.sqrt((float)level.getTransformer().distanceToSqrWrappedCoord(entity.getX(), entity.getY(), entity.getZ(), thiz.getX(), thiz.getY(), thiz.getZ())));
	}

	@Inject(method = "distanceToSqr(DDD)D", at = @At("HEAD"), cancellable = true)
	public void wrapDistanceSquared2(double x, double y, double z, CallbackInfoReturnable<Double> cir) {
		if(level.isClientSide) return;
		cir.cancel();
		cir.setReturnValue(level.getTransformer().distanceToSqrWrappedCoord(x, y, z, thiz.getX(), thiz.getY(), thiz.getZ()));
	}

	@Inject(method = "distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", at = @At("HEAD"), cancellable = true)
	public void wrapDistanceSquared3(Vec3 vec, CallbackInfoReturnable<Double> cir) {
		if(level.isClientSide) return;
		cir.cancel();
		cir.setReturnValue(level.getTransformer().distanceToSqrWrappedCoord(vec, new Vec3(thiz.getX(), thiz.getY(), thiz.getZ())));
	}


}
