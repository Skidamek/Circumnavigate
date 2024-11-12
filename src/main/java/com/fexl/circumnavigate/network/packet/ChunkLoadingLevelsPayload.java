/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;

public class ChunkLoadingLevelsPayload implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ChunkLoadingLevelsPayload> STREAM_CODEC = CustomPacketPayload.codec(ChunkLoadingLevelsPayload::write, ChunkLoadingLevelsPayload::new);
	public static final CustomPacketPayload.Type<ChunkLoadingLevelsPayload> TYPE = CustomPacketPayload.createType("debug/circumnavigate/chunk_loading_levels");

	private final HashMap<ChunkPos, Integer> levels;

	public ChunkLoadingLevelsPayload(HashMap<ChunkPos, Integer> levels) {
		this.levels = levels;
	}

	private ChunkLoadingLevelsPayload(FriendlyByteBuf buffer) {
		this.levels = decodeLevels(buffer);
	}

	private void write(FriendlyByteBuf buffer) {
		buffer.writeInt(levels.size());
		levels.keySet().forEach((chunkPos -> {
			buffer.writeChunkPos(chunkPos);
			buffer.writeInt(levels.get(chunkPos));
		}));
	}

	private HashMap<ChunkPos, Integer> decodeLevels(FriendlyByteBuf buffer) {
		int size = buffer.readInt();

		HashMap<ChunkPos, Integer> levels = new HashMap<>();

		for(int i = 0; i < size; i++) {
			levels.put(buffer.readChunkPos(), buffer.readInt());
		}

		return levels;
	}

	public HashMap<ChunkPos, Integer> getLevels() {
		return levels;
	}

	@Override
	public CustomPacketPayload.Type<ChunkLoadingLevelsPayload> type() {
		return TYPE;
	}
}
