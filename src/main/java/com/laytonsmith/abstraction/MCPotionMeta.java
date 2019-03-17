package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.core.constructs.Target;
import java.util.List;

public interface MCPotionMeta extends MCItemMeta {

	MCPotionData getBasePotionData();

	void setBasePotionData(MCPotionData pd);

	boolean addCustomEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon, boolean force, Target t);

	boolean clearCustomEffects();

	List<MCEffect> getCustomEffects();

	boolean hasCustomEffect(MCPotionEffectType type);

	boolean hasCustomEffects();

	boolean removeCustomEffect(MCPotionEffectType type);

	boolean hasColor();

	MCColor getColor();

	void setColor(MCColor color);
}
