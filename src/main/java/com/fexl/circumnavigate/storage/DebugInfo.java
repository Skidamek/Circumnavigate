/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.storage;

import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;

public class DebugInfo {
	public static HashMap<ChunkPos, Integer> chunkLoadingLevels = new HashMap<>();
}
