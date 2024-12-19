/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.injected;

public interface NoiseScaling {
	void setMul(double noiseScaling);
	void setXMul(double xMul);
	void setZMul(double zMul);
	void setXAdd(double xAdd);
	void setZAdd(double zAdd);
}