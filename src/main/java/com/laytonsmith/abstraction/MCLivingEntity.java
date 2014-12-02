
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.Target;
import java.util.HashSet;
import java.util.List;

/**
 *
 * 
 */
public interface MCLivingEntity extends MCEntity, MCProjectileSource {

	public void addEffect(int potionID, int strength, int seconds, boolean ambient, Target t);
	public boolean removeEffect(int potionID);
	/**
	 * Returns the maximum effect id, inclusive.
	 * @return 
	 */
	public int getMaxEffect();
	public List<MCEffect> getEffects();
    public void damage(double amount);
    public void damage(double amount, MCEntity source);
	public boolean getCanPickupItems();
	public boolean getRemoveWhenFarAway();
	public MCEntityEquipment getEquipment();
    public double getEyeHeight();
    public double getEyeHeight(boolean ignoreSneaking);
    public MCLocation getEyeLocation();
    public double getHealth();
    public MCPlayer getKiller();
    public double getLastDamage();
    public MCEntity getLeashHolder();
	public MCLivingEntity getTarget(Target t);
	public MCBlock getTargetBlock(HashSet<Short> transparent, int maxDistance, boolean castToByte);
	public MCBlock getTargetBlock(HashSet<Byte> transparent, int maxDistance);
    public List<MCBlock> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance);
    public List<MCBlock> getLineOfSight(HashSet<Byte> transparent, int maxDistance);
	public boolean hasLineOfSight(MCEntity other);
    public double getMaxHealth();
    public int getMaximumAir();
    public int getMaximumNoDamageTicks();
    public int getNoDamageTicks();
    public int getRemainingAir();
	public boolean isLeashed();
	public void resetMaxHealth();
	public void setCanPickupItems(boolean pickup);
	public void setRemoveWhenFarAway(boolean remove);
    public void setHealth(double health);
    public void setLastDamage(double damage);
    public void setLeashHolder(MCEntity holder);
	public void setMaxHealth(double health);
    public void setMaximumAir(int ticks);
    public void setMaximumNoDamageTicks(int ticks);
    public void setNoDamageTicks(int ticks);
    public void setRemainingAir(int ticks);
	public void setTarget(MCLivingEntity target, Target t);

	/**
	 * Kills the entity. In some cases, this will be equivalent to setHealth(0), but
	 * may not be, so this method should be used instead.
	 */
	public void kill();

	public static class MCEffect{

		private int potionID;
		private int strength;
		private int secondsRemaining;
		private boolean ambient;
		public MCEffect(int potionID, int strength, int secondsRemaining, boolean ambient){
			this.potionID = potionID;
			this.strength = strength;
			this.secondsRemaining = secondsRemaining;
			this.ambient = ambient;
		}

		public int getPotionID() {
			return potionID;
		}

		public int getStrength() {
			return strength;
		}

		public int getSecondsRemaining() {
			return secondsRemaining;
		}
		
		public boolean isAmbient() {
			return ambient;
		}

	}
}
