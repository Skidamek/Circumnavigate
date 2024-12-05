/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.accessors;

import com.fexl.circumnavigate.core.WorldTransformer;

public interface TransformerAccessor {
	WorldTransformer getTransformer();
	void setTransformer(WorldTransformer transformer);
}
