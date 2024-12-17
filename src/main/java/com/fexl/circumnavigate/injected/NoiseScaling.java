/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.injected;

public interface NoiseScaling {
	void setNoiseScaling(double noiseScaling);
	void setXScaling(double xScaling);
	void setZScaling(double zScaling);
	void setOffset(boolean isOffset);
}