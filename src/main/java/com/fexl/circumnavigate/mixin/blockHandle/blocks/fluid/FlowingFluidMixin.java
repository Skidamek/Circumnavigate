package com.fexl.circumnavigate.mixin.blockHandle.blocks.fluid;

import com.fexl.circumnavigate.core.WorldTransformer;
import com.fexl.circumnavigate.processing.BlockPosWrapped;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
	@ModifyVariable(method = "spread", at = @At("HEAD"), argsOnly = true, index = 2)
	public BlockPos wrapSpread(BlockPos blockPos, @Local(argsOnly = true) Level level) {
		if(level.isClientSide) return blockPos;
		return new BlockPosWrapped(blockPos, level.getTransformer());
	}
}
