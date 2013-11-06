package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.bukkit.BukkitMCVector;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

/**
 *
 * @author layton
 */
public abstract class BukkitMCEntity extends BukkitMCMetadatable implements MCEntity {

	public BukkitMCEntity(Entity entity) {
		super(entity);
	}

	@Override
	public Entity getHandle() {
		return (Entity) metadatable;
	}

    public boolean eject() {
		return getHandle().eject();
	}

    public void fireEntityDamageEvent(MCDamageCause dc) {
        EntityDamageEvent ede = new EntityDamageEvent(getHandle(), EntityDamageEvent.DamageCause.valueOf(dc.name()), 9001.0);
        CommandHelperPlugin.self.getServer().getPluginManager().callEvent(ede);
    }

    public int getEntityId(){
        return getHandle().getEntityId();
    }

    public float getFallDistance() {
		return getHandle().getFallDistance();
	}

    public int getFireTicks() {
		return getHandle().getFireTicks();
	}

	public MCEntityDamageEvent getLastDamageCause() {
		EntityDamageEvent ldc = getHandle().getLastDamageCause();
		if (ldc == null) {
			return null;
		}
		if (ldc instanceof EntityDamageByEntityEvent) {
			return new BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent(
					(EntityDamageByEntityEvent) ldc);
		}
		return new BukkitEntityEvents.BukkitMCEntityDamageEvent(ldc);
	}

    public MCLocation getLocation() {
		if(getHandle().getLocation() != null){
			return new BukkitMCLocation(getHandle().getLocation());
		} else {
			return null;
		}
    }

	public int getMaxFireTicks() {
		return getHandle().getMaxFireTicks();
	}

	public List<MCEntity> getNearbyEntities(double x, double y, double z) {
		List<Entity> lst = getHandle().getNearbyEntities(x, y, z);
		List<MCEntity> retn = new ArrayList<MCEntity>();

		for(Entity e : lst) {
			retn.add(BukkitConvertor.BukkitGetCorrectEntity(e));
		}

		return retn;
	}

	public MCEntity getPassenger() {
		return BukkitConvertor.BukkitGetCorrectEntity(getHandle().getPassenger());
	}

	public MCEntity getVehicle() {
		return BukkitConvertor.BukkitGetCorrectEntity(getHandle().getVehicle());
	}

	public MCServer getServer() {
		return new BukkitMCServer(getHandle().getServer());
	}

	public int getTicksLived() {
		return getHandle().getTicksLived();
	}

	public MCEntityType getType() {
		return BukkitMCEntityType.getConvertor().getAbstractedEnum(getHandle().getType());
	}

	public UUID getUniqueId() {
		return getHandle().getUniqueId();
	}

	public MCVector getVelocity() {
		return new BukkitMCVector(getHandle().getVelocity());
	}

	public MCWorld getWorld() {
		if (getHandle().getWorld() != null) {
			return new BukkitMCWorld(getHandle().getWorld());
		} else {
			return null;
		}
	}

	public boolean isDead() {
		return getHandle().isDead();
	}

	public boolean isEmpty() {
		return getHandle().isEmpty();
	}

	public boolean isInsideVehicle() {
		return getHandle().isInsideVehicle();
	}
	
	public boolean isOnGround() {
		return getHandle().isOnGround();
	}

	public boolean leaveVehicle() {
		return getHandle().leaveVehicle();
	}

	public void playEffect(MCEntityEffect type) {
		getHandle().playEffect(EntityEffect.valueOf(type.name()));
	}

	public void remove() {
		getHandle().remove();
	}

	public void setFallDistance(float distance) {
		getHandle().setFallDistance(distance);
	}

	public void setFireTicks(int ticks) {
		getHandle().setFireTicks(ticks);
	}

	public void setLastDamageCause(MCEntityDamageEvent event) {
		getHandle().setLastDamageCause((EntityDamageEvent)event._GetObject());
	}

	public boolean setPassenger(MCEntity passenger) {
		return getHandle().setPassenger((Entity) passenger.getHandle());
	}

	public void setTicksLived(int value) {
		getHandle().setTicksLived(value);
	}

	public void setVelocity(MCVector velocity) {
		getHandle().setVelocity((Vector) velocity.getHandle());
	}

	public boolean teleport(MCEntity destination) {
		return getHandle().teleport(((Entity) destination.getHandle()).getLocation());
	}

	public boolean teleport(MCEntity destination, MCTeleportCause cause) {
		return getHandle().teleport((Entity) destination.getHandle(), TeleportCause.valueOf(cause.name()));
	}

	public boolean teleport(MCLocation location) {
		return getHandle().teleport((Location) location.getHandle());
	}

	public boolean teleport(MCLocation location, MCTeleportCause cause) {
		return getHandle().teleport((Location) location.getHandle(), TeleportCause.valueOf(cause.name()));
	}

	/**
	 * This only works with craftbukkit
	 * @return
	 */
	public MCLocation asyncGetLocation() {
		return new BukkitMCLocation(getHandle().getLocation());
	}

}
