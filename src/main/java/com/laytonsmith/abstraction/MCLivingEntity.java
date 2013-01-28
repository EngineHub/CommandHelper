
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCProjectileType;
import com.laytonsmith.core.constructs.Target;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author layton
 */
public interface MCLivingEntity extends MCEntity {

	public void addEffect(int potionID, int strength, int seconds, Target t);
	public boolean removeEffect(int potionID);
	public List<MCEffect> getEffects();
    public void damage(int amount);
    public void damage(int amount, MCEntity source);
    public double getEyeHeight();
    public double getEyeHeight(boolean ignoreSneaking);
    public MCLocation getEyeLocation();
    public int getHealth();
    public MCPlayer getKiller();
    public int getLastDamage();
    public List<MCBlock> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance);
    public List<MCBlock> getLineOfSight(HashSet<Byte> transparent, int maxDistance);
    public int getMaxHealth();
    public int getMaximumAir();
    public int getMaximumNoDamageTicks();
    public int getNoDamageTicks();
    public int getRemainingAir();
    public MCBlock getTargetBlock(HashSet<Byte> transparent, int maxDistance);
    public MCProjectile launchProjectile(MCProjectileType projectile);
    public void setHealth(int health);
    public void setLastDamage(int damage);
    public void setMaximumAir(int ticks);
    public void setMaximumNoDamageTicks(int ticks);
    public void setNoDamageTicks(int ticks);
    public void setRemainingAir(int ticks);
    public Map<MCEquipmentSlot, MCItemStack> getEquipment();
    public void setEquipment(Map<MCEquipmentSlot, MCItemStack> emap);

	public static class MCEffect{

		private int potionID;
		private int strength;
		private int secondsRemaining;
		public MCEffect(int potionID, int strength, int secondsRemaining){
			this.potionID = potionID;
			this.strength = strength;
			this.secondsRemaining = secondsRemaining;
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

	}
}
