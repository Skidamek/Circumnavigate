/* SPDX-License-Identifier: AGPL-3.0-only */

package com.fexl.circumnavigate;

import com.fexl.circumnavigate.client.storage.TransformersStorage;
import com.fexl.circumnavigate.network.packet.LevelWrappingPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CircumnavigateClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		//Read an incoming transformer from the server. Received during server login.
		ClientPlayNetworking.registerGlobalReceiver(LevelWrappingPayload.TYPE, ((payload, context) -> {
			context.client().execute(() -> {
					TransformersStorage.setTransformer(payload.levelKey(), payload.transformer());
			});
		}));
	}
}
