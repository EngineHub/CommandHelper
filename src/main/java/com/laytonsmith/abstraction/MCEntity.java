/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction;

import java.util.List;
import java.util.UUID;

import org.bukkit.util.Vector;

import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;

/**
 *
 * @author layton
 */
public interface MCEntity extends AbstractionObject{
    public abstract boolean eject();

    public void fireEntityDamageEvent(MCDamageCause dc);
    
    public int getEntityId();

    public abstract float getFallDistance();
    
    public abstract int getFireTicks();

	public abstract Object getHandle();

	public MCDamageCause getLastDamageCause();

	public abstract MCLocation getLocation();

	public abstract int getMaxFireTicks();

	public abstract List<MCMetadataValue> getMetadata(String arg0);

	public abstract List<MCEntity> getNearbyEntities(double arg0, double arg1,
			double arg2);

	public abstract MCEntity getPassenger();

	public abstract MCServer getServer();

	public abstract int getTicksLived();

	public MCEntityType getType();

	public abstract UUID getUniqueId();

	public abstract MCEntity getVehicle();

	public abstract MCVelocity getVelocity();

	public abstract MCWorld getWorld();

	public abstract boolean hasMetadata(String arg0);

	public abstract boolean isDead();

	public abstract boolean isEmpty();

	public abstract boolean isInsideVehicle();

	public abstract boolean leaveVehicle();

	public abstract void playEffect(MCEntityEffect arg0);

	public abstract void remove();

	public abstract void removeMetadata(String arg0, MCPlugin arg1);

	public abstract void setFallDistance(float arg0);

	public abstract void setFireTicks(int arg0);

	public abstract void setLastDamageCause(MCEntityDamageEvent arg0);

	public abstract void setMetadata(String arg0, MCMetadataValue arg1);

	public abstract boolean setPassenger(MCEntity arg0);

	public abstract void setTicksLived(int arg0);

	public abstract void setVelocity(Vector arg0);

	public abstract boolean teleport(MCEntity arg0);

	public abstract boolean teleport(MCEntity arg0, MCTeleportCause arg1);

	public abstract boolean teleport(MCLocation arg0);

	public abstract boolean teleport(MCLocation arg0, MCTeleportCause arg1);
}
