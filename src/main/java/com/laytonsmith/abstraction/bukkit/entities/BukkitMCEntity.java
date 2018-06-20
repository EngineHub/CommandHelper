package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.core.Static;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BukkitMCEntity extends BukkitMCMetadatable implements MCEntity {

	Entity e;

	public BukkitMCEntity(Entity e) {
		super(e);
		this.e = e;
	}

	@Override
	public boolean eject() {
		return e.eject();
	}

	@Override
	public float getFallDistance() {
		return e.getFallDistance();
	}

	@Override
	public int getFireTicks() {
		return e.getFireTicks();
	}

	@Override
	public Entity getHandle() {
		return e;
	}

	@Override
	public MCEntityDamageEvent getLastDamageCause() {
		EntityDamageEvent ldc = e.getLastDamageCause();
		if(ldc == null) {
			return null;
		}
		if(ldc instanceof EntityDamageByEntityEvent) {
			return new BukkitEntityEvents.BukkitMCEntityDamageByEntityEvent(ldc);
		}
		return new BukkitEntityEvents.BukkitMCEntityDamageEvent(ldc);
	}

	public MCLivingEntity getLivingEntity() {
		if(e instanceof LivingEntity) {
			return new BukkitMCLivingEntity(e);
		}
		return null;
	}

	@Override
	public MCLocation getLocation() {
		if(e.getLocation() == null) {
			return null;
		}
		return new BukkitMCLocation(e.getLocation());
	}

	@Override
	public int getMaxFireTicks() {
		return e.getMaxFireTicks();
	}

	@Override
	public List<MCEntity> getNearbyEntities(double x, double y, double z) {
		List<Entity> lst = e.getNearbyEntities(x, y, z);
		List<MCEntity> retn = new ArrayList<>();

		for(Entity e : lst) {
			retn.add(BukkitConvertor.BukkitGetCorrectEntity(e));
		}

		return retn;
	}

	@Override
	public MCEntity getPassenger() {
		// Deprecated in 1.11.2
		return BukkitConvertor.BukkitGetCorrectEntity(e.getPassenger());
	}

	@Override
	public MCServer getServer() {
		return new BukkitMCServer(e.getServer());
	}

	@Override
	public int getTicksLived() {
		return e.getTicksLived();
	}

	@Override
	public MCEntityType getType() {
		return BukkitMCEntityType.valueOfConcrete(e.getType());
	}

	@Override
	public UUID getUniqueId() {
		return e.getUniqueId();
	}

	@Override
	public MCEntity getVehicle() {
		return BukkitConvertor.BukkitGetCorrectEntity(e.getVehicle());
	}

	@Override
	public Vector3D getVelocity() {
		Vector v = e.getVelocity();
		return new Vector3D(v.getX(), v.getY(), v.getZ());
	}

	@Override
	public MCWorld getWorld() {
		if(e == null || e.getWorld() == null) {
			return null;
		}
		return new BukkitMCWorld(e.getWorld());
	}

	@Override
	public boolean isDead() {
		return e.isDead();
	}

	@Override
	public boolean isEmpty() {
		return e.isEmpty();
	}

	@Override
	public boolean isInsideVehicle() {
		return e.isInsideVehicle();
	}

	@Override
	public boolean isOnGround() {
		return e.isOnGround();
	}

	public boolean isLivingEntity() {
		return e instanceof LivingEntity;
	}

	public boolean isTameable() {
		return e instanceof Tameable;
	}

	@Override
	public boolean leaveVehicle() {
		return e.leaveVehicle();
	}

	@Override
	public void playEffect(MCEntityEffect type) {
		e.playEffect(EntityEffect.valueOf(type.name()));
	}

	@Override
	public void remove() {
		e.remove();
	}

	@Override
	public void setFallDistance(float distance) {
		e.setFallDistance(distance);
	}

	@Override
	public void setFireTicks(int ticks) {
		e.setFireTicks(ticks);
	}

	@Override
	public void setLastDamageCause(MCEntityDamageEvent event) {
		e.setLastDamageCause((EntityDamageEvent) event._GetObject());
	}

	@Override
	public boolean setPassenger(MCEntity passenger) {
		return e.setPassenger((Entity) passenger.getHandle());
	}

	@Override
	public void setTicksLived(int value) {
		e.setTicksLived(value);
	}

	@Override
	public void setVelocity(Vector3D velocity) {
		Vector v = new Vector(velocity.X(), velocity.Y(), velocity.Z());
		e.setVelocity(v);
	}

	@Override
	public boolean teleport(MCEntity destination) {
		Entity ent = ((BukkitMCEntity) destination).getHandle();
		return e.teleport(ent.getLocation());
	}

	@Override
	public boolean teleport(MCEntity destination, MCTeleportCause cause) {
		return e.teleport(((BukkitMCEntity) destination).getHandle(), TeleportCause.valueOf(cause.name()));
	}

	@Override
	public boolean teleport(MCLocation location) {
		return e.teleport(((BukkitMCLocation) location).asLocation());
	}

	@Override
	public boolean teleport(MCLocation location, MCTeleportCause cause) {
		return e.teleport(((BukkitMCLocation) location).asLocation(), TeleportCause.valueOf(cause.name()));
	}

	@Override
	public String getCustomName() {
		if(Static.getServer().getMinecraftVersion().ordinal() < MCVersion.MC1_8.ordinal()) {
			if(e instanceof LivingEntity) {
				return e.getCustomName();
			} else {
				throw new IllegalArgumentException("This can only be used on LivingEntities prior to MC1.8.");
			}
		}
		return e.getCustomName();
	}

	@Override
	public boolean isCustomNameVisible() {
		if(Static.getServer().getMinecraftVersion().ordinal() < MCVersion.MC1_8.ordinal()) {
			if(e instanceof LivingEntity) {
				return e.isCustomNameVisible();
			} else {
				throw new IllegalArgumentException("This can only be used on LivingEntities prior to MC1.8.");
			}
		}
		return e.isCustomNameVisible();
	}

	@Override
	public void setCustomName(String name) {
		if(Static.getServer().getMinecraftVersion().ordinal() < MCVersion.MC1_8.ordinal()) {
			if(e instanceof LivingEntity) {
				e.setCustomName(name);
			} else {
				throw new IllegalArgumentException("This can only be used on LivingEntities prior to MC1.8.");
			}
		}
		e.setCustomName(name);
	}

	@Override
	public void setCustomNameVisible(boolean visible) {
		if(Static.getServer().getMinecraftVersion().ordinal() < MCVersion.MC1_8.ordinal()) {
			if(e instanceof LivingEntity) {
				e.setCustomNameVisible(visible);
			} else {
				throw new IllegalArgumentException("This can only be used on LivingEntities prior to MC1.8.");
			}
		}
		e.setCustomNameVisible(visible);
	}

	@Override
	public boolean isGlowing() {
		try {
			return e.isGlowing();
		} catch (NoSuchMethodError ex) {
			// Probably before 1.9
			return false;
		}
	}

	@Override
	public void setGlowing(Boolean glow) {
		try {
			e.setGlowing(glow);
		} catch (NoSuchMethodError ex) {
			// Probably before 1.9
		}
	}

	@Override
	public boolean hasGravity() {
		try {
			return e.hasGravity();
		} catch (NoSuchMethodError ex) {
			// Probably before 1.10
			return true;
		}
	}

	@Override
	public void setHasGravity(boolean gravity) {
		try {
			e.setGravity(gravity);
		} catch (NoSuchMethodError ex) {
			// Probably before 1.10
		}
	}

	@Override
	public boolean isSilent() {
		try {
			return e.isSilent();
		} catch (NoSuchMethodError ex) {
			// Probably before 1.9.4
			return false;
		}
	}

	@Override
	public void setSilent(boolean silent) {
		try {
			e.setSilent(silent);
		} catch (NoSuchMethodError ex) {
			// Probably before 1.9.4
		}
	}

	@Override
	public boolean isInvulnerable() {
		try {
			return e.isInvulnerable();
		} catch (NoSuchMethodError ex) {
			// Probably before 1.9.2
			return false;
		}
	}

	@Override
	public void setInvulnerable(boolean invulnerable) {
		try {
			e.setInvulnerable(invulnerable);
		} catch (NoSuchMethodError ex) {
			// Probably before 1.9.2
		}
	}

	@Override
	public Set<String> getScoreboardTags() {
		try {
			return e.getScoreboardTags();
		} catch (NoSuchMethodError ex) {
			// Probably before 1.10.2
			return Collections.emptySet();
		}
	}

	@Override
	public boolean addScoreboardTag(String tag) {
		try {
			return e.addScoreboardTag(tag);
		} catch (NoSuchMethodError ex) {
			// Probably before 1.10.2
			return false;
		}
	}

	@Override
	public boolean removeScoreboardTag(String tag) {
		try {
			return e.removeScoreboardTag(tag);
		} catch (NoSuchMethodError ex) {
			// Probably before 1.10.2
			return false;
		}
	}

}
