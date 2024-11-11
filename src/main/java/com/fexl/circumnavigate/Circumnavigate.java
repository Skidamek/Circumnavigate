/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.network.packet.LevelWrappingPayload;
import com.fexl.circumnavigate.network.packet.LevelWrappingRequest;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;


public class Circumnavigate implements ModInitializer {
	public static final String MOD_ID = "circumnavigate";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(LevelWrappingPayload.TYPE, LevelWrappingPayload.STREAM_CODEC);
	}
}
