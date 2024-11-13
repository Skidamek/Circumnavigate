/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.blockHandle.blocks;

import com.fexl.circumnavigate.processing.BlockPosWrapped;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.SignalGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SignalGetter.class)
public class SignalGetterMixin {
	SignalGetter thiz = (SignalGetter) (Object) this;

	@ModifyVariable(method = "getControlInputSignal", at = @At("HEAD"), index = 1, argsOnly = true)
	public BlockPos modifyBlockPos(BlockPos blockPos) {
		if(thiz.isClientSide()) return blockPos;
		return new BlockPosWrapped(blockPos, thiz.getTransformer());
	}

	@ModifyVariable(method = "getSignal", at = @At("HEAD"), index = 1, argsOnly = true)
	public BlockPos modifyBlockPos2(BlockPos blockPos) {
		if(thiz.isClientSide()) return blockPos;
		return new BlockPosWrapped(blockPos, thiz.getTransformer());
	}

	@ModifyVariable(method = "hasNeighborSignal", at = @At("HEAD"), index = 1, argsOnly = true)
	public BlockPos modifyBlockPos3(BlockPos blockPos) {
		if(thiz.isClientSide()) return blockPos;
		return new BlockPosWrapped(blockPos, thiz.getTransformer());
	}


}
