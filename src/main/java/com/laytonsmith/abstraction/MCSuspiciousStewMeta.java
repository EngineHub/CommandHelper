package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.core.constructs.Target;

import java.util.List;

public interface MCSuspiciousStewMeta extends MCItemMeta {

	boolean addCustomEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon, boolean force, Target t);

	List<MCEffect> getCustomEffects();

}
