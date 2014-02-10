package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
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

		public Velocity(double x, double y, double z){
			this(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)), x, y, z);
		}
		public Velocity(double magnitude, double x, double y, double z) {
			this.magnitude = magnitude;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Velocity add(Velocity vec) {
			this.x += vec.x;
			this.y += vec.y;
			this.z += vec.z;
			return this;
		}

		public Velocity multiply(Velocity vec) {
			this.x *= vec.x;
			this.y *= vec.y;
			this.z *= vec.z;
			return this;
		}

		public Velocity multiply(double m) {
			this.x *= m;
			this.y *= m;
			this.z *= m;
			return this;
		}

		public Velocity normalize() {
			double length = length();

			this.x /= length;
			this.y /= length;
			this.z /= length;
			return this;
		}

		public Velocity subtract(Velocity vec) {
			this.x -= vec.x;
			this.y -= vec.y;
			this.z -= vec.z;
			return this;
		}

		public double length() {
			return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
		}
	}

	public boolean eject();

	public void fireEntityDamageEvent(MCDamageCause dc);

	public int getEntityId();

	public float getFallDistance();

	public int getFireTicks();

	public MCEntityDamageEvent getLastDamageCause();

	public MCLocation getLocation();

	/**
	 * Unlike {@see MCEntity#getLocation}, this will work when not run on the server
	 * thread, but this does mean that the data recieved may be slightly outdated.
	 * @return
	 */
	public MCLocation asyncGetLocation();

	public int getMaxFireTicks();

	public List<MCEntity> getNearbyEntities(double x, double y, double z);

	public MCEntity getPassenger();

	public MCServer getServer();

	public int getTicksLived();

	public MCEntityType getType();

	public UUID getUniqueId();

	public MCEntity getVehicle();

	public Velocity getVelocity();
	
	public void setVelocity(Velocity v);

	public MCWorld getWorld();

	public boolean isDead();

	public boolean isEmpty();

	public boolean isInsideVehicle();
	
	public boolean isOnGround();

	public boolean leaveVehicle();

	public void playEffect(MCEntityEffect type);

	public void remove();

	public void setFallDistance(float distance);

	public void setFireTicks(int ticks);

	public void setLastDamageCause(MCEntityDamageEvent event);

	public boolean setPassenger(MCEntity passenger);

	public void setTicksLived(int value);

	public boolean teleport(MCEntity destination);

	public boolean teleport(MCEntity destination, MCTeleportCause cause);

	public boolean teleport(MCLocation location);

	public boolean teleport(MCLocation location, MCTeleportCause cause);
}
