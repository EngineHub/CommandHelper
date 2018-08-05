package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.core.constructs.Target;
import java.util.List;

public interface MCPotionMeta extends MCItemMeta {

	MCPotionData getBasePotionData();

	void setBasePotionData(MCPotionData pd);

	boolean addCustomEffect(int id, int strength, int ticks, boolean ambient, boolean particles, boolean icon, boolean force, Target t);

	boolean clearCustomEffects();

	List<MCEffect> getCustomEffects();

	boolean hasCustomEffect(int id);

	boolean hasCustomEffects();

	boolean removeCustomEffect(int id);
}
