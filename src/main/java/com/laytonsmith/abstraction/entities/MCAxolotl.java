package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.enums.MCAxolotlType;

public interface MCAxolotl extends MCLivingEntity {
	boolean isPlayingDead();
	void setPlayingDead(boolean playingDead);
	MCAxolotlType getAxolotlType();
	void setAxolotlType(MCAxolotlType type);
}
