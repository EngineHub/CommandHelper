
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlock;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author layton
 */
public interface MCLivingEntity extends MCEntity {

	public void addEffect(int potionID, int strength, int seconds);
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
    public MCProjectile launchProjectile(MCProjectile projectile);
    public void setHealth(int health);
    public void setLastDamage(int damage);
    public void setMaximumAir(int ticks);
    public void setMaximumNoDamageTicks(int ticks);
    public void setNoDamageTicks(int ticks);
    public void setRemainingAir(int ticks);
}
