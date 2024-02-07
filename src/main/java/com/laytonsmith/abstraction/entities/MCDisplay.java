package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCEntity;

public interface MCDisplay extends MCEntity {

	Billboard getBillboard();

	void setBillboard(Billboard billboard);

	Brightness getBrightness();

	void setBrightness(Brightness brightness);

	MCColor getGlowColorOverride();

	void setGlowColorOverride(MCColor color);

	float getDisplayHeight();

	void setDisplayHeight(float height);

	float getDisplayWidth();

	void setDisplayWidth(float width);

	int getInterpolationDurationTicks();

	void setInterpolationDurationTicks(int ticks);

	int getInterpolationDelayTicks();

	void setInterpolationDelayTicks(int ticks);

	/**
	 * Added in MC 1.20.2
	 * @return ticks
	 */
	int getTeleportDuration();

	/**
	 * Added in MC 1.20.2
	 * @param ticks
	 */
	void setTeleportDuration(int ticks);

	float getShadowRadius();

	void setShadowRadius(float radius);

	float getShadowStrength();

	void setShadowStrength(float strength);

	float getViewRange();

	void setViewRange(float range);

	public MCTransformation getTransformation();

	public void setTransformation(MCTransformation transformation);

	public void setTransformationMatrix(float[] mtrxf);

	enum Billboard {
		CENTER,
		FIXED,
		HORIZONTAL,
		VERTICAL
	}

	class Brightness {

		final int block;
		final int sky;

		public Brightness(int block, int sky) {
			this.block = block;
			this.sky = sky;
		}

		public int block() {
			return this.block;
		}

		public int sky() {
			return this.sky;
		}
	}
}
