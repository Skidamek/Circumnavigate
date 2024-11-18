/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.mixin.debug;

import com.fexl.circumnavigate.storage.TransformerRequests;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	//TODO Fix properly to solve #24
	@Inject(method = "stopServer", at = @At("TAIL"))
	public void stopServer(CallbackInfo ci) {
		TransformerRequests.hasWork = false;
	}
}
