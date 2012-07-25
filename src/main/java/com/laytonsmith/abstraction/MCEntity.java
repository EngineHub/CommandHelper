
package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import java.util.List;
import java.util.UUID;

/**
 * 
 * @author layton
 */
public interface MCEntity extends MCMetadatable {
	public static class Velocity {
		public double magnitude;
		public double x;
		public double y;
		public double z;

		public Velocity(double magnitude, double x, double y, double z) {
			this.magnitude = magnitude;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public boolean eject();

	public void fireEntityDamageEvent(MCDamageCause dc);

	public int getEntityId();

	public float getFallDistance();

	public int getFireTicks();

	public MCEntityDamageEvent getLastDamageCause();

	public MCLocation getLocation();

	public int getMaxFireTicks();

	public List<MCEntity> getNearbyEntities(double x, double y, double z);

	public MCEntity getPassenger();

	public MCServer getServer();

	public int getTicksLived();

	public MCEntityType getType();

	public UUID getUniqueId();

	public MCEntity getVehicle();

	public Velocity getVelocity();

	public MCWorld getWorld();

	public boolean isDead();

	public boolean isEmpty();

	public boolean isInsideVehicle();

	public boolean leaveVehicle();

	public void playEffect(MCEntityEffect type);

	public void remove();

	public void setFallDistance(float distance);

	public void setFireTicks(int ticks);

	public void setLastDamageCause(MCEntityDamageEvent event);

	public boolean setPassenger(MCEntity passenger);

	public void setTicksLived(int value);

	public void setVelocity(Velocity velocity);

	public boolean teleport(MCEntity destination);

	public boolean teleport(MCEntity destination, MCTeleportCause cause);

	public boolean teleport(MCLocation location);

	public boolean teleport(MCLocation location, MCTeleportCause cause);
}
