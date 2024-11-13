/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.debug;

import com.fexl.circumnavigate.storage.DebugInfo;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin {
	ChunkHolder thiz = (ChunkHolder) (Object) this;

	@Inject(method = "setTicketLevel", at = @At("HEAD"))
	public void setTicketLevel(int level, CallbackInfo ci) {
		//DebugInfo.chunkLoadingLevels.put(thiz.getPos(), level);
	}
}
