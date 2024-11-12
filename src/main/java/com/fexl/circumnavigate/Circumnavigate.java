/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.network.packet.ChunkLoadingLevelsPayload;
import com.fexl.circumnavigate.network.packet.LevelWrappingPayload;
import com.fexl.circumnavigate.network.packet.LevelWrappingRequest;
import com.fexl.circumnavigate.storage.DebugInfo;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.HashMap;


public class Circumnavigate implements ModInitializer {
	public static final String MOD_ID = "circumnavigate";

	public static int tickCount = 0;
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.configurationS2C().register(LevelWrappingPayload.TYPE, LevelWrappingPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(ChunkLoadingLevelsPayload.TYPE, ChunkLoadingLevelsPayload.STREAM_CODEC);

		ServerTickEvents.END_SERVER_TICK.register((server -> {
			if(tickCount > 10) {
				server.getAllLevels().forEach((serverLevel -> {
					for(ServerPlayer player : serverLevel.players()) {
						ServerPlayNetworking.send(player, new ChunkLoadingLevelsPayload(DebugInfo.chunkLoadingLevels));
					}
				}));
				DebugInfo.chunkLoadingLevels = new HashMap<>();
				tickCount = 0;
			}
			else {
				tickCount += 1;
			}
		}));
	}
}
