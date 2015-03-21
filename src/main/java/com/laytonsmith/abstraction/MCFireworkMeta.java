package com.laytonsmith.abstraction;

import java.util.List;

public interface MCFireworkMeta extends MCItemMeta {

	int getStrength();
	void setStrength(int strength);

	void addEffect(MCFireworkEffect effect);
	void addEffects(MCFireworkEffect... effects);

	void clearEffects();

	MCFireworkMeta clone();

	List<MCFireworkEffect> getEffects();

	// Seems like we could just .size() the above...
	int getEffectsSize();

	boolean hasEffects();

	void removeEffect(int index);

}
