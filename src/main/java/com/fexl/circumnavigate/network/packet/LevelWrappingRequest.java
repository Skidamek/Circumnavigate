/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LevelWrappingRequest() implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, LevelWrappingRequest> STREAM_CODEC = CustomPacketPayload.codec(LevelWrappingRequest::write, LevelWrappingRequest::new);
	public static final CustomPacketPayload.Type<LevelWrappingRequest> TYPE = CustomPacketPayload.createType("debug/circumnavigate/wrapping_data_request");

	private LevelWrappingRequest(FriendlyByteBuf buffer) {
		this();
	}

	private void write(FriendlyByteBuf buffer) {
	}

	@Override
	public CustomPacketPayload.Type<LevelWrappingRequest> type() {
		return TYPE;
	}
}
