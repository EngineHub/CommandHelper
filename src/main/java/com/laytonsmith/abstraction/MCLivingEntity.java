package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCPotionEffectType;
import com.laytonsmith.core.constructs.Target;

import java.util.HashSet;
import java.util.List;

public interface MCLivingEntity extends MCEntity, MCProjectileSource {

	boolean addEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon);

	boolean removeEffect(MCPotionEffectType type);

	void removeEffects();

	List<MCEffect> getEffects();

	void damage(double amount);

	void damage(double amount, MCEntity source);

	boolean getCanPickupItems();

	boolean getRemoveWhenFarAway();

	/**
	 * With the addition of ArmorStands, this can be null. At the time of this writing, ArmorStands are the only
	 * LivingEntity with such a limitation, and a workaround has been added.
	 */
	MCEntityEquipment getEquipment();

	double getEyeHeight();

	double getEyeHeight(boolean ignoreSneaking);

	MCLocation getEyeLocation();

	double getHealth();

	MCPlayer getKiller();

	double getLastDamage();

	MCEntity getLeashHolder();

	MCLivingEntity getTarget(Target t);

	MCBlock getTargetBlock(HashSet<MCMaterial> transparent, int maxDistance);

	MCBlock getTargetSpace(int maxDistance);

	List<MCBlock> getLineOfSight(HashSet<MCMaterial> transparent, int maxDistance);

	boolean hasLineOfSight(MCEntity other);

	double getMaxHealth();

	int getMaximumAir();

	int getMaximumNoDamageTicks();

	int getNoDamageTicks();

	int getRemainingAir();

	boolean isGliding();

	boolean isLeashed();

	boolean hasAI();

	void resetMaxHealth();

	void setCanPickupItems(boolean pickup);

	void setRemoveWhenFarAway(boolean remove);

	void setHealth(double health);

	void setLastDamage(double damage);

	void setLeashHolder(MCEntity holder);

	void setMaxHealth(double health);

	void setMaximumAir(int ticks);

	void setMaximumNoDamageTicks(int ticks);

	void setNoDamageTicks(int ticks);

	void setRemainingAir(int ticks);

	void setTarget(MCLivingEntity target, Target t);

	void setGliding(Boolean glide);

	void setAI(Boolean ai);

	boolean isCollidable();

	void setCollidable(boolean collidable);

	/**
	 * Kills the entity. In some cases, this will be equivalent to setHealth(0), but may not be, so this method should
	 * be used instead.
	 */
	void kill();

	boolean isTameable();

	double getAttributeValue(MCAttribute attr);

	double getAttributeDefault(MCAttribute attr);

	double getAttributeBase(MCAttribute attr);

	void setAttributeBase(MCAttribute attr, double base);

	void resetAttributeBase(MCAttribute attr);

	List<MCAttributeModifier> getAttributeModifiers(MCAttribute attr);

	void addAttributeModifier(MCAttributeModifier modifier);

	void removeAttributeModifier(MCAttributeModifier modifier);

	class MCEffect {

		private MCPotionEffectType type;
		private int strength;
		private int ticksRemaining;
		private boolean ambient;
		private boolean particles;
		private boolean icon;

		public MCEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient) {
			this(type, strength, ticks, ambient, true, true);
		}

		public MCEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles) {
			this(type, strength, ticks, ambient, particles, particles);
		}

		public MCEffect(MCPotionEffectType type, int strength, int ticks, boolean ambient, boolean particles, boolean icon) {
			this.type = type;
			this.strength = strength;
			this.ticksRemaining = ticks;
			this.ambient = ambient;
			this.particles = particles;
			this.icon = icon;
		}

		public MCPotionEffectType getPotionEffectType() {
			return type;
		}

		public int getStrength() {
			return strength;
		}

		public int getTicksRemaining() {
			return ticksRemaining;
		}

		public boolean isAmbient() {
			return ambient;
		}

		public boolean hasParticles() {
			return particles;
		}

		public boolean showIcon() {
			return icon;
		}
	}
}
