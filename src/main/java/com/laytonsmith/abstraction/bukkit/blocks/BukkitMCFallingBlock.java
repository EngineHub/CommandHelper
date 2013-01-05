
package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCFallingBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadataValue;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlugin;
import com.laytonsmith.abstraction.bukkit.BukkitMCServer;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents;
import com.laytonsmith.abstraction.bukkit.events.BukkitEntityEvents.BukkitMCEntityDamageEvent;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityEffect;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTeleportCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

/**
 *
 * @author import
 */
public class BukkitMCFallingBlock implements MCFallingBlock {
	FallingBlock f;
	
	public BukkitMCFallingBlock(FallingBlock f) {
		this.f = f;
	}
	
	public byte getBlockData() {
		return f.getBlockData();
	}

	public int getBlockId() {
		return f.getBlockId();
	}

	public boolean getDropItem() {
		return f.getDropItem();
	}

	public MCMaterial getMaterial() {
		return new BukkitMCMaterial(f.getMaterial());
	}

	public void setDropItem(boolean drop) {
		f.setDropItem(drop);
	}

	public boolean eject() {
		return f.eject();
	}

	public void fireEntityDamageEvent(MCDamageCause dc) {
	}

	public int getEntityId() {
		return f.getEntityId();
	}

	public float getFallDistance() {
		return f.getFallDistance();
	}

	public int getFireTicks() {
		return f.getFireTicks();
	}

	public MCEntityDamageEvent getLastDamageCause() {
		return new BukkitEntityEvents.BukkitMCEntityDamageEvent(f.getLastDamageCause());
	}

	public MCLocation getLocation() {
		return new BukkitMCLocation(f.getLocation());
	}

	public int getMaxFireTicks() {
		return f.getMaxFireTicks();
	}

	public List<MCEntity> getNearbyEntities(double x, double y, double z) {
		List<Entity> near = f.getNearbyEntities(x, y, z);
		List<MCEntity> retn = new ArrayList<MCEntity>();
		
		for (Entity e : near) {
			retn.add(new BukkitMCEntity(e));
		}
		
		return retn;
	}

	public MCEntity getPassenger() {
		return new BukkitMCEntity(f.getPassenger());
	}

	public MCServer getServer() {
		return new BukkitMCServer(f.getServer());
	}

	public int getTicksLived() {
		return f.getTicksLived();
	}

	public MCEntityType getType() {
		return BukkitMCEntityType.getConvertor().getAbstractedEnum(f.getType());
	}

	public UUID getUniqueId() {
		return f.getUniqueId();
	}

	public MCEntity getVehicle() {
		return new BukkitMCEntity(f.getVehicle());
	}

	public Velocity getVelocity() {
		return new Velocity(f.getVelocity().getX(), f.getVelocity().getY(), f.getVelocity().getZ());
	}

	public void setVelocity(Velocity v) {
		Vector vect = new Vector();
		vect.setX(v.x);
		vect.setY(v.y);
		vect.setZ(v.z);
		
		f.setVelocity(vect);
	}

	public MCWorld getWorld() {
		return new BukkitMCWorld(f.getWorld());
	}

	public boolean isDead() {
		return f.isDead();
	}

	public boolean isEmpty() {
		return f.isEmpty();
	}

	public boolean isInsideVehicle() {
		return f.isInsideVehicle();
	}

	public boolean leaveVehicle() {
		return f.leaveVehicle();
	}

	public void playEffect(MCEntityEffect type) {
		f.playEffect(EntityEffect.valueOf(type.toString()));
	}

	public void remove() {
		f.remove();
	}

	public void setFallDistance(float distance) {
		f.setFallDistance(distance);
	}

	public void setFireTicks(int ticks) {
		f.setFireTicks(ticks);
	}

	public void setLastDamageCause(MCEntityDamageEvent event) {
		f.setLastDamageCause((EntityDamageEvent)((BukkitMCEntityDamageEvent)event)._GetObject());
	}

	public boolean setPassenger(MCEntity passenger) {
		return f.setPassenger(((BukkitMCEntity)passenger).asEntity());
	}

	public void setTicksLived(int value) {
		f.setTicksLived(value);
	}

	public boolean teleport(MCEntity destination) {
		return f.teleport(((BukkitMCEntity)destination).asEntity());
	}

	public boolean teleport(MCEntity destination, MCTeleportCause cause) {
		return f.teleport(((BukkitMCEntity)destination).asEntity(), TeleportCause.valueOf(cause.toString()));
	}

	public boolean teleport(MCLocation location) {
		return f.teleport(((BukkitMCLocation)location).asLocation());
	}

	public boolean teleport(MCLocation location, MCTeleportCause cause) {
		return f.teleport(((BukkitMCLocation)location).asLocation(), TeleportCause.valueOf(cause.toString()));
	}

	public List<MCMetadataValue> getMetadata(String metadataKey) {
		List<MCMetadataValue> retn = new ArrayList<MCMetadataValue>();
		
		for (MetadataValue m : f.getMetadata(metadataKey)) {
			retn.add(new BukkitMCMetadataValue(m));
		}
		
		return retn;
	}

	public boolean hasMetadata(String metadataKey) {
		return f.hasMetadata(metadataKey);
	}

	public void removeMetadata(String metadataKey, MCPlugin owningPlugin) {
		f.removeMetadata(metadataKey, ((BukkitMCPlugin)owningPlugin).getPlugin());
	}

	public void setMetadata(String metadataKey, MCMetadataValue newMetadataValue) {
		MetadataValue m = ((BukkitMCMetadataValue)newMetadataValue).getHandle();
		f.setMetadata(metadataKey, m);
	}

	public Object getHandle() {
		return f;
	}
	
}
