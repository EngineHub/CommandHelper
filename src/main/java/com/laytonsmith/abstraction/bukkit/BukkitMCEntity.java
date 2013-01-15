

package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

/**
 *
 * @author layton
 */
public class BukkitMCEntity extends BukkitMCMetadatable implements MCEntity {

    Entity e;
    public BukkitMCEntity(Entity e) {
    	super(e);
        this.e = e;
    }

    public Entity asEntity(){
        return e;
    }

    public boolean eject() {
		return e.eject();
	}

    public void fireEntityDamageEvent(MCDamageCause dc) {
        EntityDamageEvent ede = new EntityDamageEvent(e, EntityDamageEvent.DamageCause.valueOf(dc.name()), 9001);
        CommandHelperPlugin.self.getServer().getPluginManager().callEvent(ede);
    }

    public int getEntityId(){
        return e.getEntityId();
    }

    public float getFallDistance() {
		return e.getFallDistance();
	}

    public int getFireTicks() {
		return e.getFireTicks();
	}

    @Override
    public Object getHandle(){
        return e;
    }

    public MCEntityDamageEvent getLastDamageCause() {
        return new BukkitEntityEvents.BukkitMCEntityDamageEvent(e.getLastDamageCause());
    }

    public MCLivingEntity getLivingEntity() {
        if(e instanceof LivingEntity){
            return new BukkitMCLivingEntity((LivingEntity)e);
        }
        return null;
    }

    public MCLocation getLocation() {
        if(e.getLocation() == null){
            return null;
        }
        return new BukkitMCLocation(e.getLocation());
    }

	public int getMaxFireTicks() {
		return e.getMaxFireTicks();
	}

	public List<MCEntity> getNearbyEntities(double x, double y, double z) {
		List<Entity> lst = e.getNearbyEntities(x, y, z);
		List<MCEntity> retn = new ArrayList<MCEntity>();

		for(Entity e : lst) {
			retn.add(BukkitConvertor.BukkitGetCorrectEntity(e));
		}

		return retn;
	}

	public MCEntity getPassenger() {
		return BukkitConvertor.BukkitGetCorrectEntity(e.getPassenger());
	}

	public MCServer getServer() {
		return new BukkitMCServer(e.getServer());
	}

	public int getTicksLived() {
		return e.getTicksLived();
	}

	public MCEntityType getType() {
    	EntityType type = e.getType();
    	return MCEntityType.valueOf(type.name());
    }

	public UUID getUniqueId() {
		return e.getUniqueId();
	}

	public MCEntity getVehicle() {
		return new BukkitMCEntity(e);
	}

	public Velocity getVelocity() {
		Vector v = e.getVelocity();
		return new Velocity(v.length(), v.getX(), v.getY(), v.getZ());
	}

	public MCWorld getWorld() {
        if (e == null || e.getWorld() == null) {
            return null;
        }
        return new BukkitMCWorld(e.getWorld());
    }

	public boolean isDead() {
		return e.isDead();
	}

	public boolean isEmpty() {
		return e.isEmpty();
	}

	public boolean isInsideVehicle() {
		return e.isInsideVehicle();
	}

	public boolean isLivingEntity() {
        return e instanceof LivingEntity;
    }

	public boolean isTameable() {
        return e instanceof Tameable;
    }

	public boolean leaveVehicle() {
		return e.leaveVehicle();
	}

	public void playEffect(MCEntityEffect type) {
		e.playEffect(EntityEffect.valueOf(type.name()));
	}

	public void remove() {
		e.remove();
	}

	public void setFallDistance(float distance) {
		e.setFallDistance(distance);
	}

	public void setFireTicks(int ticks) {
		e.setFireTicks(ticks);
	}

	public void setLastDamageCause(MCEntityDamageEvent event) {
		e.setLastDamageCause((EntityDamageEvent)event._GetObject());
	}

	public boolean setPassenger(MCEntity passenger) {
		return e.setPassenger((Entity)passenger.getHandle());
	}

	public void setTicksLived(int value) {
		e.setTicksLived(value);
	}

	public void setVelocity(Velocity velocity) {
		Vector v = new Vector(velocity.x, velocity.y, velocity.z);
		e.setVelocity(v);
	}

	public boolean teleport(MCEntity destination) {
	    Entity ent = ((BukkitMCEntity)destination).asEntity();
		return e.teleport(ent.getLocation());
	}

	public boolean teleport(MCEntity destination, MCTeleportCause cause) {
		return e.teleport(((BukkitMCEntity)destination).asEntity(), TeleportCause.valueOf(cause.name()));
	}

	public boolean teleport(MCLocation location) {
		return e.teleport(((BukkitMCLocation)location).asLocation());
	}

	public boolean teleport(MCLocation location, MCTeleportCause cause) {
		return e.teleport(((BukkitMCLocation)location).asLocation(), TeleportCause.valueOf(cause.name()));
	}

	/**
	 * This only works with craftbukkit
	 * @return
	 */
	public MCLocation asyncGetLocation() {
		return new BukkitMCLocation(e.getLocation());
	}

}
