package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MCEntity extends MCMetadatable {

	boolean eject();

	float getFallDistance();

	int getFireTicks();

	MCEntityDamageEvent getLastDamageCause();

	MCLocation getLocation();

	int getMaxFireTicks();

	List<MCEntity> getNearbyEntities(double x, double y, double z);

	List<MCEntity> getPassengers();

	MCServer getServer();

	int getTicksLived();

	MCEntityType getType();

	UUID getUniqueId();

	MCEntity getVehicle();

	Vector3D getVelocity();

	void setVelocity(Vector3D v);

	MCWorld getWorld();

	boolean isDead();

	boolean isEmpty();

	boolean isInsideVehicle();

	boolean isOnGround();

	boolean leaveVehicle();

	void playEffect(MCEntityEffect type);

	void remove();

	void setFallDistance(float distance);

	void setFireTicks(int ticks);

	void setLastDamageCause(MCEntityDamageEvent event);

	boolean setPassenger(MCEntity passenger);

	void setTicksLived(int value);

	boolean teleport(MCEntity destination);

	boolean teleport(MCEntity destination, MCTeleportCause cause);

	boolean teleport(MCLocation location);

	boolean teleport(MCLocation location, MCTeleportCause cause);

	void setCustomName(String name);

	String getCustomName();

	void setCustomNameVisible(boolean visible);

	boolean isCustomNameVisible();

	boolean isGlowing();

	void setGlowing(Boolean glow);

	boolean hasGravity();

	void setHasGravity(boolean gravity);

	boolean isSilent();

	void setSilent(boolean silent);

	boolean isInvulnerable();

	void setInvulnerable(boolean invulnerable);

	Set<String> getScoreboardTags();

	boolean addScoreboardTag(String tag);

	boolean removeScoreboardTag(String tag);
}
