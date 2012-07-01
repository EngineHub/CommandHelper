/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit;

import java.util.List;
import java.util.UUID;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageEvent;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.commandhelper.CommandHelperPlugin;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 *
 * @author layton
 */
public abstract class BukkitMCEntity implements MCEntity {

    Entity e;
    public BukkitMCEntity(AbstractionObject a){
        this.e = ((Entity)a.getHandle());
    }
    
    public BukkitMCEntity(Entity e) {
        this.e = e;
    }
    
    public Entity _Entity(){
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

    public Object getHandle(){
        return e;
    }
    
    public MCDamageCause getLastDamageCause() {
        return MCDamageCause.valueOf(e.getLastDamageCause().getCause().name());
    }

    public MCLivingEntity getLivingEntity() {
        if(e instanceof LivingEntity){
            return new BukkitMCLivingEntity((LivingEntity)e);
        }
        return null;
    }
    
    public MCLocation getLocation() {
		return new BukkitMCLocation(e.getLocation());
	}

	public int getMaxFireTicks() {
		return e.getMaxFireTicks();
	}

	public List<MCMetadataValue> getMetadata(String arg0) {
		List<MetadataValue> lst = e.getMetadata(arg0);
		// TODO Implement
		return null;
	}

	public List<MCEntity> getNearbyEntities(double arg0, double arg1, double arg2) {
		List<Entity> lst = e.getNearbyEntities(arg0, arg1, arg2);
		// TODO Implement
		return null;
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
		return MCEntityType.valueOf(e.getType().name());
	}

	public UUID getUniqueId() {
		return e.getUniqueId();
	}
	
	public MCEntity getVehicle() {
		return BukkitConvertor.BukkitGetCorrectEntity(e.getVehicle());
	}

	public MCVelocity getVelocity(){
        org.bukkit.util.Vector vec = e.getVelocity();
        return new MCVelocity(vec.length(), vec.getX(), vec.getY(), vec.getZ());
    }

	public MCWorld getWorld() {
		return new BukkitMCWorld(e.getWorld());
	}

	public boolean hasMetadata(String arg0) {
		return e.hasMetadata(arg0);
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

	public void playEffect(MCEntityEffect arg0) {
		e.playEffect(EntityEffect.valueOf(arg0.name()));
	}

	public void remove() {
		e.remove();
	}

	public void removeMetadata(String arg0, MCPlugin arg1) {
		e.removeMetadata(arg0, (Plugin)arg1.getHandle());
	}

	public void setFallDistance(float arg0) {
		e.setFallDistance(arg0);
	}

	public void setFireTicks(int arg0) {
		e.setFireTicks(arg0);
	}

	public void setLastDamageCause(MCEntityDamageEvent arg0) {
		e.setLastDamageCause((EntityDamageEvent)arg0._GetObject());
	}

	public void setMetadata(String arg0, MCMetadataValue arg1) {
		e.setMetadata(arg0, (MetadataValue)arg1.getHandle());
	}
	
	public boolean setPassenger(MCEntity arg0) {
		return e.setPassenger((Entity)arg0.getHandle());
	}

	public void setTicksLived(int arg0) {
		e.setTicksLived(arg0);
	}

	public void setVelocity(Vector arg0) {
		e.setVelocity(arg0);
	}

	public boolean teleport(MCEntity arg0) {
		return e.teleport((Entity)arg0.getHandle());
	}
	
	public boolean teleport(MCEntity arg0, MCTeleportCause arg1) {
		return e.teleport((Entity)arg0.getHandle(), TeleportCause.valueOf(arg1.name()));
	}

	public boolean teleport(MCLocation arg0) {
		return e.teleport((Location)arg0.getHandle());
	}
	
	public boolean teleport(MCLocation arg0, MCTeleportCause arg1) {
		return e.teleport((Location)arg0.getHandle(), TeleportCause.valueOf(arg1.name()));
	}
}
