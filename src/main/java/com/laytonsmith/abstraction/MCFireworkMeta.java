package com.laytonsmith.abstraction;

import java.util.List;

public interface MCFireworkMeta extends MCItemMeta {

	int getStrength();

	void setStrength(int strength);

	List<MCFireworkEffect> getEffects();

	void addEffect(MCFireworkEffect effect);

	void clearEffects();

}
