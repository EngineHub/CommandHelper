package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.Target;

import java.util.HashSet;
import java.util.List;

public interface MCLivingEntity extends MCEntity, MCProjectileSource {
	void addEffect(int potionID, int strength, int ticks, boolean ambient, boolean particles, Target t);
	boolean removeEffect(int potionID);
	void removeEffects();
	/**
	 * Returns the maximum effect id, inclusive.
	 * @return 
	 */
	int getMaxEffect();
	List<MCEffect> getEffects();
	void damage(double amount);
	void damage(double amount, MCEntity source);
	boolean getCanPickupItems();
	boolean getRemoveWhenFarAway();

	/**
	 * With the addition of ArmorStands, this can be null. At the time of this writing,
	 * ArmorStands are the only LivingEntity with such a limitation, and a workaround has been added.
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
	MCBlock getTargetBlock(HashSet<Short> transparent, int maxDistance);
	MCBlock getTargetSpace(int maxDistance);
	List<MCBlock> getLineOfSight(HashSet<Short> transparent, int maxDistance);
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

	/**
	 * Kills the entity. In some cases, this will be equivalent to setHealth(0), but
	 * may not be, so this method should be used instead.
	 */
	void kill();

	class MCEffect{

		private int potionID;
		private int strength;
		private int ticksRemaining;
		private boolean ambient;
		private boolean particles;
		public MCEffect(int potionID, int strength, int ticks, boolean ambient){
			this.potionID = potionID;
			this.strength = strength;
			this.ticksRemaining = ticks;
			this.ambient = ambient;
			this.particles = true;
		}

		public MCEffect(int potionID, int strength, int ticks, boolean ambient, boolean particles){
			this.potionID = potionID;
			this.strength = strength;
			this.ticksRemaining = ticks;
			this.ambient = ambient;
			this.particles = particles;
		}

		public int getPotionID() {
			return potionID;
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
	}
}
