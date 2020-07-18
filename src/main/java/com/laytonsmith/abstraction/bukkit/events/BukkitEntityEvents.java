package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import com.laytonsmith.abstraction.entities.MCHanging;
import com.laytonsmith.abstraction.entities.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCFirework;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHanging;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCItem;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCProjectile;
import com.laytonsmith.abstraction.entities.MCFirework;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCRegainReason;
import com.laytonsmith.abstraction.enums.MCRemoveCause;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.abstraction.enums.MCTargetReason;
import com.laytonsmith.abstraction.enums.MCUnleashReason;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDamageCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCRegainReason;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCRemoveCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSpawnReason;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCUnleashReason;
import com.laytonsmith.abstraction.events.MCCreatureSpawnEvent;
import com.laytonsmith.abstraction.events.MCEntityChangeBlockEvent;
import com.laytonsmith.abstraction.events.MCEntityDamageByEntityEvent;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.abstraction.events.MCEntityDeathEvent;
import com.laytonsmith.abstraction.events.MCEntityEnterPortalEvent;
import com.laytonsmith.abstraction.events.MCEntityExplodeEvent;
import com.laytonsmith.abstraction.events.MCEntityInteractEvent;
import com.laytonsmith.abstraction.events.MCEntityPortalEvent;
import com.laytonsmith.abstraction.events.MCEntityRegainHealthEvent;
import com.laytonsmith.abstraction.events.MCEntityTargetEvent;
import com.laytonsmith.abstraction.events.MCEntityToggleGlideEvent;
import com.laytonsmith.abstraction.events.MCEntityUnleashEvent;
import com.laytonsmith.abstraction.events.MCFireworkExplodeEvent;
import com.laytonsmith.abstraction.events.MCHangingBreakEvent;
import com.laytonsmith.abstraction.events.MCItemDespawnEvent;
import com.laytonsmith.abstraction.events.MCItemSpawnEvent;
import com.laytonsmith.abstraction.events.MCPlayerDropItemEvent;
import com.laytonsmith.abstraction.events.MCPlayerInteractAtEntityEvent;
import com.laytonsmith.abstraction.events.MCPlayerInteractEntityEvent;
import com.laytonsmith.abstraction.events.MCPlayerPickupItemEvent;
import com.laytonsmith.abstraction.events.MCPotionSplashEvent;
import com.laytonsmith.abstraction.events.MCProjectileHitEvent;
import com.laytonsmith.abstraction.events.MCProjectileLaunchEvent;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.Static;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.event.entity.EntityUnleashEvent;

public class BukkitEntityEvents {

	public static class BukkitMCItemDespawnEvent implements MCItemDespawnEvent {

		ItemDespawnEvent ide;

		public BukkitMCItemDespawnEvent(Event event) {
			ide = (ItemDespawnEvent) event;
		}

		@Override
		public Object _GetObject() {
			return ide;
		}

		@Override
		public MCItem getEntity() {
			return new BukkitMCItem(ide.getEntity());
		}

		@Override
		public MCLocation getLocation() {
			return new BukkitMCLocation(ide.getLocation());
		}
	}

	public static class BukkitMCItemSpawnEvent implements MCItemSpawnEvent {

		ItemSpawnEvent ise;

		public BukkitMCItemSpawnEvent(Event event) {
			ise = (ItemSpawnEvent) event;
		}

		@Override
		public Object _GetObject() {
			return ise;
		}

		@Override
		public MCItem getEntity() {
			return new BukkitMCItem(ise.getEntity());
		}

		@Override
		public MCLocation getLocation() {
			return new BukkitMCLocation(ise.getLocation());
		}
	}

	public static class BukkitMCEntityExplodeEvent implements MCEntityExplodeEvent {

		EntityExplodeEvent e;

		public BukkitMCEntityExplodeEvent(Event event) {
			e = (EntityExplodeEvent) event;
		}

		@Override
		public Object _GetObject() {
			return e;
		}

		@Override
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(e.getEntity());
		}

		@Override
		public List<MCBlock> getBlocks() {
			List<MCBlock> ret = new ArrayList<>();
			for(Block b : e.blockList()) {
				ret.add(new BukkitMCBlock(b));
			}
			return ret;
		}

		@Override
		public void setBlocks(List<MCBlock> blocks) {
			e.blockList().clear();
			for(MCBlock b : blocks) {
				e.blockList().add(((BukkitMCBlock) b).__Block());
			}
		}

		@Override
		public MCLocation getLocation() {
			return new BukkitMCLocation(e.getLocation());
		}

		@Override
		public float getYield() {
			return e.getYield();
		}

		@Override
		public void setYield(float power) {
			e.setYield(power);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCProjectileHitEvent implements MCProjectileHitEvent {

		ProjectileHitEvent phe;

		public BukkitMCProjectileHitEvent(Event event) {
			phe = (ProjectileHitEvent) event;
		}

		@Override
		public Object _GetObject() {
			return phe;
		}

		@Override
		public MCProjectile getEntity() {
			return new BukkitMCProjectile(phe.getEntity());
		}

		@Override
		public MCEntityType getEntityType() {
			return BukkitMCEntityType.valueOfConcrete(phe.getEntityType());
		}

		@Override
		public MCEntity getHitEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(phe.getHitEntity());
		}

		@Override
		public MCBlock getHitBlock() {
			Block blk = phe.getHitBlock();
			if(blk == null) {
				return null;
			}
			return new BukkitMCBlock(blk);
		}

		public static BukkitMCProjectileHitEvent _instantiate(MCProjectile p) {
			return new BukkitMCProjectileHitEvent(
					new ProjectileHitEvent(((BukkitMCProjectile) p).asProjectile()));
		}

	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCProjectileLaunchEvent implements MCProjectileLaunchEvent {

		ProjectileLaunchEvent ple;

		public BukkitMCProjectileLaunchEvent(Event event) {
			ple = (ProjectileLaunchEvent) event;
		}

		@Override
		public Object _GetObject() {
			return ple;
		}

		@Override
		public MCProjectile getEntity() {
			return (MCProjectile) BukkitConvertor.BukkitGetCorrectEntity(ple.getEntity());
		}

		@Override
		public MCEntityType getEntityType() {
			return BukkitMCEntityType.valueOfConcrete(ple.getEntityType());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPotionSplashEvent extends BukkitMCProjectileHitEvent
			implements MCPotionSplashEvent {

		PotionSplashEvent pse;

		public BukkitMCPotionSplashEvent(Event event) {
			super(event);
			pse = (PotionSplashEvent) event;
		}

		@Override
		public Object _GetObject() {
			return pse;
		}

		@Override
		public Set<MCLivingEntity> getAffectedEntities() {
			Set<MCLivingEntity> ret = new HashSet<>();
			for(LivingEntity le : pse.getAffectedEntities()) {
				ret.add((MCLivingEntity) BukkitConvertor.BukkitGetCorrectEntity(le));
			}
			return ret;
		}

		@Override
		public double getIntensity(MCLivingEntity le) {
			return pse.getIntensity(((BukkitMCLivingEntity) le).asLivingEntity());
		}

		@Override
		public void setIntensity(MCLivingEntity le, double intensity) {
			pse.setIntensity(((BukkitMCLivingEntity) le).asLivingEntity(), intensity);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityDeathEvent implements MCEntityDeathEvent {

		EntityDeathEvent e;

		public BukkitMCEntityDeathEvent(Event e) {
			this.e = (EntityDeathEvent) e;
		}

		@Override
		public Object _GetObject() {
			return e;
		}

		@Override
		public int getDroppedExp() {
			return e.getDroppedExp();
		}

		@Override
		public List<MCItemStack> getDrops() {
			List<ItemStack> islist = e.getDrops();
			List<MCItemStack> drops = new ArrayList<>();

			for(ItemStack is : islist) {
				drops.add(new BukkitMCItemStack(is));
			}

			return drops;
		}

		@Override
		public void clearDrops() {
			e.getDrops().clear();
		}

		@Override
		public void addDrop(MCItemStack is) {
			e.getDrops().add(((BukkitMCItemStack) is).__ItemStack());
		}

		@Override
		public MCLivingEntity getEntity() {
			return new BukkitMCLivingEntity(e.getEntity());
		}

		@Override
		public void setDroppedExp(int exp) {
			e.setDroppedExp(exp);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCCreatureSpawnEvent implements MCCreatureSpawnEvent {

		CreatureSpawnEvent e;

		public BukkitMCCreatureSpawnEvent(Event event) {
			this.e = ((CreatureSpawnEvent) event);
		}

		@Override
		public Object _GetObject() {
			return e;
		}

		@Override
		public MCLivingEntity getEntity() {
			return (MCLivingEntity) BukkitConvertor.BukkitGetCorrectEntity(e.getEntity());
		}

		@Override
		public MCLocation getLocation() {
			return new BukkitMCLocation(e.getLocation());
		}

		@Override
		public MCSpawnReason getSpawnReason() {
			return BukkitMCSpawnReason.getConvertor().getAbstractedEnum(e.getSpawnReason());
		}

		@Override
		public void setType(MCEntityType type) {
			e.setCancelled(true);
			e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.valueOf(type.name()));
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerInteractEntityEvent implements MCPlayerInteractEntityEvent {

		PlayerInteractEntityEvent e;

		public BukkitMCPlayerInteractEntityEvent(Event event) {
			this.e = (PlayerInteractEntityEvent) event;
		}

		@Override
		public Object _GetObject() {
			return e;
		}

		@Override
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(e.getRightClicked());
		}

		@Override
		public boolean isCancelled() {
			return e.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			e.setCancelled(cancelled);
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(e.getPlayer());
		}

		@Override
		public MCEquipmentSlot getHand() {
			if(e.getHand() == EquipmentSlot.HAND) {
				return MCEquipmentSlot.WEAPON;
			}
			return MCEquipmentSlot.OFF_HAND;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerInteractAtEntityEvent extends BukkitMCPlayerInteractEntityEvent implements MCPlayerInteractAtEntityEvent {

		PlayerInteractAtEntityEvent e;

		public BukkitMCPlayerInteractAtEntityEvent(Event event) {
			super(event);
			this.e = (PlayerInteractAtEntityEvent) event;
		}

		@Override
		public Vector3D getClickedPosition() {
			Vector v = e.getClickedPosition();
			return new Vector3D(v.getX(), v.getY(), v.getZ());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerDropItemEvent implements MCPlayerDropItemEvent {

		PlayerDropItemEvent e;

		public BukkitMCPlayerDropItemEvent(Event e) {
			this.e = (PlayerDropItemEvent) e;
		}

		@Override
		public MCItem getItemDrop() {
			return new BukkitMCItem(e.getItemDrop());
		}

		@Override
		public void setItemStack(MCItemStack stack) {
			ItemStack is = (ItemStack) stack.getHandle();
			if(is == null || is.getType().equals(Material.AIR)) {
				e.getItemDrop().remove();
			} else {
				e.getItemDrop().setItemStack(is);
			}
		}

		@Override
		public boolean isCancelled() {
			return e.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			e.setCancelled(cancelled);
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(e.getPlayer());
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerPickupItemEvent implements MCPlayerPickupItemEvent {

		EntityPickupItemEvent e;

		public BukkitMCPlayerPickupItemEvent(Event e) {
			this.e = (EntityPickupItemEvent) e;
		}

		@Override
		public int getRemaining() {
			return e.getRemaining();
		}

		@Override
		public MCItem getItem() {
			return new BukkitMCItem(e.getItem());
		}

		@Override
		public void setItemStack(MCItemStack stack) {
			ItemStack is = (ItemStack) stack.getHandle();
			e.setCancelled(true);
			e.getItem().remove();
			if(is != null && !is.getType().equals(Material.AIR)) {
				((Player) e.getEntity()).getInventory().addItem(is);
				//and for added realism :)
				e.getEntity().getWorld().playSound(e.getItem().getLocation(),
						Sound.ENTITY_ITEM_PICKUP, 1, 2);
			}
		}

		@Override
		public boolean isCancelled() {
			return e.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			e.setCancelled(cancelled);
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(e.getEntity());
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityDamageEvent implements MCEntityDamageEvent {

		EntityDamageEvent event;

		public BukkitMCEntityDamageEvent(Event e) {
			event = (EntityDamageEvent) e;
		}

		@Override
		public Object _GetObject() {
			return event;
		}

		@Override
		public MCDamageCause getCause() {
			return BukkitMCDamageCause.getConvertor().getAbstractedEnum(event.getCause());
		}

		@Override
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(event.getEntity());
		}

		@Override
		public double getFinalDamage() {
			return event.getFinalDamage();
		}

		@Override
		public double getDamage() {
			return event.getDamage();
		}

		@Override
		public void setDamage(double damage) {
			event.setDamage(damage);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityDamageByEntityEvent extends BukkitMCEntityDamageEvent implements MCEntityDamageByEntityEvent {

		EntityDamageByEntityEvent event;

		public BukkitMCEntityDamageByEntityEvent(Event e) {
			super(e);
			event = (EntityDamageByEntityEvent) e;
		}

		@Override
		public MCEntity getDamager() {
			return BukkitConvertor.BukkitGetCorrectEntity(event.getDamager());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCTargetEvent implements MCEntityTargetEvent {

		EntityTargetEvent pie;

		public BukkitMCTargetEvent(Event e) {
			pie = (EntityTargetEvent) e;
		}

		public static BukkitMCTargetEvent _instantiate(Entity entity, LivingEntity target, EntityTargetEvent.TargetReason reason) {
			return new BukkitMCTargetEvent(new EntityTargetEvent(((BukkitMCEntity) entity).getHandle(),
					(LivingEntity) ((BukkitMCLivingEntity) target).getLivingEntity(), reason));
		}

		@Override
		public Object _GetObject() {
			return pie;
		}

		@Override
		public MCEntity getTarget() {
			return BukkitConvertor.BukkitGetCorrectEntity(pie.getTarget());
		}

		@Override
		public void setTarget(MCEntity target) {
			if(target == null) {
				pie.setTarget(null);
			} else {
				pie.setTarget(((BukkitMCEntity) target).getHandle());
			}
		}

		@Override
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(pie.getEntity());
		}

		@Override
		public MCEntityType getEntityType() {
			return BukkitConvertor.BukkitGetCorrectEntity(pie.getEntity()).getType();
		}

		@Override
		public MCTargetReason getReason() {
			return MCTargetReason.valueOf(pie.getReason().name());
		}
	}

	public static class BukkitMCEntityEnterPortalEvent implements MCEntityEnterPortalEvent {

		EntityPortalEnterEvent epe;

		public BukkitMCEntityEnterPortalEvent(Event event) {
			epe = (EntityPortalEnterEvent) event;
		}

		@Override
		public Object _GetObject() {
			return epe;
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(epe.getEntity());
		}

		@Override
		public MCLocation getLocation() {
			return new BukkitMCLocation(epe.getLocation());
		}
	}

	public static class BukkitMCEntityChangeBlockEvent implements MCEntityChangeBlockEvent {

		EntityChangeBlockEvent ecb;

		public BukkitMCEntityChangeBlockEvent(Event event) {
			ecb = (EntityChangeBlockEvent) event;
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(ecb.getEntity());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(ecb.getBlock());
		}

		@Override
		public MCMaterial getTo() {
			return new BukkitMCMaterial(ecb.getTo());
		}

		@Override
		public MCBlockData getBlockData() {
			return new BukkitMCBlockData(ecb.getBlockData());
		}

		@Override
		public boolean isCancelled() {
			return ecb.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			ecb.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return ecb;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityInteractEvent implements MCEntityInteractEvent {

		EntityInteractEvent eie;

		public BukkitMCEntityInteractEvent(Event event) {
			eie = (EntityInteractEvent) event;
		}

		@Override
		public Object _GetObject() {
			return eie;
		}

		@Override
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(eie.getEntity());
		}

		@Override
		public MCBlock getBlock() {
			return new BukkitMCBlock(eie.getBlock());
		}

		@Override
		public boolean isCancelled() {
			return eie.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			eie.setCancelled(cancelled);
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCHangingBreakEvent implements MCHangingBreakEvent {

		HangingBreakEvent hbe;

		public BukkitMCHangingBreakEvent(Event event) {
			hbe = (HangingBreakEvent) event;
		}

		@Override
		public Object _GetObject() {
			return hbe;
		}

		@Override
		public MCHanging getEntity() {
			return new BukkitMCHanging(hbe.getEntity());
		}

		@Override
		public MCRemoveCause getCause() {
			return BukkitMCRemoveCause.getConvertor().getAbstractedEnum(hbe.getCause());
		}

		@Override
		public MCEntity getRemover() {
			if(hbe instanceof HangingBreakByEntityEvent) {
				return BukkitConvertor.BukkitGetCorrectEntity(((HangingBreakByEntityEvent) hbe).getRemover());
			} else {
				return null;
			}
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityToggleGlideEvent implements MCEntityToggleGlideEvent {

		EntityToggleGlideEvent e;

		public BukkitMCEntityToggleGlideEvent(Event e) {
			this.e = (EntityToggleGlideEvent) e;
		}

		@Override
		public Object _GetObject() {
			return e;
		}

		@Override
		public boolean isGliding() {
			return e.isGliding();
		}

		@Override
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(e.getEntity());
		}

		@Override
		public MCEntityType getEntityType() {
			return BukkitConvertor.BukkitGetCorrectEntity(e.getEntity()).getType();
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCFireworkExplodeEvent implements MCFireworkExplodeEvent {

		FireworkExplodeEvent e;

		public BukkitMCFireworkExplodeEvent(Event e) {
			this.e = (FireworkExplodeEvent) e;
		}

		@Override
		public Object _GetObject() {
			return e;
		}

		@Override
		public MCFirework getEntity() {
			return new BukkitMCFirework(e.getEntity());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityRegainHealthEvent implements MCEntityRegainHealthEvent {

		EntityRegainHealthEvent e;

		public BukkitMCEntityRegainHealthEvent(Event e) {
			this.e = (EntityRegainHealthEvent) e;
		}

		@Override
		public Object _GetObject() {
			return e;
		}

		@Override
		public double getAmount() {
			return e.getAmount();
		}

		@Override
		public void setAmount(double amount) {
			e.setAmount(amount);
		}

		@Override
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(e.getEntity());
		}

		@Override
		public MCRegainReason getRegainReason() {
			return BukkitMCRegainReason.getConvertor().getAbstractedEnum(e.getRegainReason());
		}
	}

	public static class BukkitMCEntityPortalEvent implements MCEntityPortalEvent {

		EntityPortalEvent epe;

		public BukkitMCEntityPortalEvent(Event event) {
			epe = (EntityPortalEvent) event;
		}

		@Override
		public Object _GetObject() {
			return epe;
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(epe.getEntity());
		}

		@Override
		public void setTo(MCLocation newloc) {
			World w = (World) newloc.getWorld().getHandle();
			Location loc = new Location(w, newloc.getX(), newloc.getY(), newloc.getZ());
			epe.setTo(loc);
			if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_14)) {
				// Use travel agent if setting location
				useTravelAgent();
			}
		}

		@Override
		public MCLocation getFrom() {
			return new BukkitMCLocation(epe.getFrom());
		}

		@Override
		public MCLocation getTo() {
			if(epe.getTo() == null) {
				return null;
			}
			return new BukkitMCLocation(epe.getTo());
		}

		@Override
		public void setCancelled(boolean state) {
			epe.setCancelled(state);
		}

		@Override
		public boolean isCancelled() {
			return epe.isCancelled();
		}

		// 1.13.2 support
		private Object getTravelAgent() {
			return ReflectionUtils.invokeMethod(PlayerPortalEvent.class, epe, "getPortalTravelAgent");
		}

		// 1.13.2 support
		private void useTravelAgent() {
			ReflectionUtils.set(PlayerPortalEvent.class, epe, "useTravelAgent", true);
		}

		@Override
		public int getSearchRadius() {
			try {
				return epe.getSearchRadius();
			} catch (NoSuchMethodError ex) {
				// prior to 1.15.1
			}
			try {
				Object ta = getTravelAgent();
				return (int) ReflectionUtils.invokeMethod(ta, "getSearchRadius");
			} catch (ReflectionUtils.ReflectionException ex) {
				// after 1.13.2
			}
			return 128; // default, though this can be modified on some servers
		}

		@Override
		public void setSearchRadius(int radius) {
			try {
				epe.setSearchRadius(radius);
			} catch (NoSuchMethodError ex) {
				// prior to 1.15.1
			}
			try {
				useTravelAgent();
				Object ta = getTravelAgent();
				ReflectionUtils.set(ta.getClass(), ta, "searchRadius", radius);
			} catch (ReflectionUtils.ReflectionException ex) {
				// after 1.13.2
			}
		}
	}
	
	public static class BukkitMCEntityUnleashEvent implements MCEntityUnleashEvent {
		
		EntityUnleashEvent ide;

		public BukkitMCEntityUnleashEvent(Event event) {
			ide = (EntityUnleashEvent) event;
		}

		@Override
		public Object _GetObject() {
			return ide;
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(ide.getEntity());
		}
		
		@Override
		public MCUnleashReason getReason() {
			return BukkitMCUnleashReason.getConvertor().getAbstractedEnum(ide.getReason());
		}
	}
}
