package com.laytonsmith.abstraction;

import java.util.List;

import com.laytonsmith.abstraction.MCLivingEntity.MCEffect;
import com.laytonsmith.core.constructs.Target;

public interface MCPotionMeta extends MCItemMeta {

	public boolean addCustomEffect(int potionID, int strength, int seconds, boolean ambient, boolean overwrite, Target t);
	public boolean clearCustomEffects();
	public List<MCEffect> getCustomEffects();
	public boolean hasCustomEffect(int id);
	public boolean hasCustomEffects();
	public boolean removeCustomEffect(int id);
	public boolean setMainEffect(int id);
}
