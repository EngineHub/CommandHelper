package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.bukkit.*;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockState;
import com.laytonsmith.abstraction.bukkit.entities.*;
import com.laytonsmith.abstraction.entities.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.*;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDamageCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCRegainReason;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCRemoveCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSpawnReason;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.constructs.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.util.Vector;

import java.util.*;

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
			if (e.getEntity() != null) {
				return BukkitConvertor.BukkitGetCorrectEntity(e.getEntity());
			}
			return null;
		}

		@Override
		public List<MCBlock> getBlocks() {
			List<MCBlock> ret = new ArrayList<>();
			for (Block b : e.blockList()) {
				ret.add(new BukkitMCBlock(b));
			}
			return ret;
		}

		@Override
		public void setBlocks(List<MCBlock> blocks) {
			e.blockList().clear();
			for (MCBlock b : blocks) {
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
			if (blk == null) {
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
			for (LivingEntity le : pse.getAffectedEntities()) {
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

			for (ItemStack is : islist) {
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
			if (e.getHand() == EquipmentSlot.HAND) {
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
			if (is == null || is.getType().equals(Material.AIR)) {
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
			if (is != null && !is.getType().equals(Material.AIR)) {
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
			if (target == null) {
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
			if (hbe instanceof HangingBreakByEntityEvent) {
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
		}

		@Override
		public MCLocation getFrom() {
			return new BukkitMCLocation(epe.getFrom());
		}

		@Override
		public MCLocation getTo() {
			if (epe.getTo() == null) {
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

		@Override
		public void useTravelAgent(boolean useTravelAgent) {
			epe.useTravelAgent(useTravelAgent);
		}

		@Override
		public MCTravelAgent getPortalTravelAgent() {
			return new BukkitMCTravelAgent(epe.getPortalTravelAgent());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCAreaEffectCloudApplyEvent implements MCAreaEffectCloudApplyEvent {

		AreaEffectCloudApplyEvent aeca;

		public BukkitMCAreaEffectCloudApplyEvent(Event e) {
			this.aeca = (AreaEffectCloudApplyEvent) e;
		}

		@Override
		public List<MCLivingEntity> getAffectedEntities() {
			List<MCLivingEntity> list = new ArrayList<>();
			for (LivingEntity le : aeca.getAffectedEntities())
				list.add(new BukkitMCLivingEntity(le));
			return list;
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(aeca.getEntity());
		}

		@Override
		public Object _GetObject() {
			return aeca;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCCreeperPowerEvent implements MCCreeperPowerEvent {

		CreeperPowerEvent cpe;

		public BukkitMCCreeperPowerEvent(Event e) {
			this.cpe = (CreeperPowerEvent) e;
		}

		@Override
		public String getCause() {
			return cpe.getCause().name();
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(cpe.getEntity());
		}

		@Override
		public MCLightningStrike getLightning() {
			return new BukkitMCLightningStrike(cpe.getLightning());
		}

		@Override
		public boolean isCancelled() {
			return cpe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			cpe.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return cpe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEnderdragonChangePhaseEvent implements MCEnderdragonChangePhaseEvent {

		EnderDragonChangePhaseEvent edcpe;

		public BukkitMCEnderdragonChangePhaseEvent(Event e) {
			this.edcpe = (EnderDragonChangePhaseEvent) e;
		}

		@Override
		public CString getCurrentPhase() {
			return new CString(edcpe.getCurrentPhase().name(), Target.UNKNOWN);
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(edcpe.getEntity());
		}

		@Override
		public CString getNewPhase() {
			return new CString(edcpe.getNewPhase().name(), Target.UNKNOWN);
		}

		@Override
		public boolean iscancelled() {
			return edcpe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			edcpe.setCancelled(cancelled);
		}

		@Override
		public void setNewPhase(EnderDragon.Phase newPhase) {
			edcpe.setNewPhase(newPhase);
		}

		@Override
		public Object _GetObject() {
			return edcpe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityAirChangeEvent implements MCEntityAirChangeEvent {

		EntityAirChangeEvent eace;

		public BukkitMCEntityAirChangeEvent(Event e){ this.eace = (EntityAirChangeEvent) e; }

		@Override
		public CInt getAmount() {
			return new CInt(eace.getAmount(), Target.UNKNOWN);
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(eace.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return eace.isCancelled();
		}

		@Override
		public void setAmount(int amount) {
			eace.setAmount(amount);
		}

		@Override
		public void setCancelled(boolean cancelled) {
			eace.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return eace;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityBreedEvent implements MCEntityBreedEvent {

		EntityBreedEvent ebe;

		public BukkitMCEntityBreedEvent(Event e){ this.ebe = (EntityBreedEvent) e; }

		@Override
		public MCItemStack getBredWith() {
			return new BukkitMCItemStack(ebe.getBredWith());
		}

		@Override
		public MCLivingEntity getBreeder() {
			return new BukkitMCLivingEntity(ebe.getBreeder());
		}

		@Override
		public MCLivingEntity getEntity() {
			return new BukkitMCLivingEntity(ebe.getEntity());
		}

		@Override
		public CInt getExperience() {
			return new CInt(ebe.getExperience(), Target.UNKNOWN);
		}

		@Override
		public MCLivingEntity getFather() {
			return new BukkitMCLivingEntity(ebe.getFather());
		}

		@Override
		public MCLivingEntity getMother() {
			return new BukkitMCLivingEntity(ebe.getMother());
		}

		@Override
		public boolean isCancelled() {
			return ebe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			ebe.setCancelled(cancelled);
		}

		@Override
		public void setExperience(int experience) {
			ebe.setExperience(experience);
		}

		@Override
		public Object _GetObject() {
			return ebe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityCreatePortalEvent implements MCEntityCreatePortalEvent {

		EntityCreatePortalEvent ecpe;

		public BukkitMCEntityCreatePortalEvent(Event e){ this.ecpe = (EntityCreatePortalEvent) e; }

		@Override
		public List<MCBlockState> getBlocks() {
			List<MCBlockState> list = new ArrayList<>();
			for(BlockState bs : ecpe.getBlocks())
				list.add(new BukkitMCBlockState(bs));
			return list;
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(ecpe.getEntity());
		}

		@Override
		public CString getPortalType() {
			return new CString(ecpe.getPortalType().name(), Target.UNKNOWN);
		}

		@Override
		public boolean isCancelled() {
			return ecpe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			ecpe.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return ecpe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityDropItemEvent implements MCEntityDropItemEvent {

		EntityDropItemEvent edie;

		public BukkitMCEntityDropItemEvent(Event e){ this.edie = (EntityDropItemEvent) e; }

		@Override
		public MCItem getItemDrop() {
			return new BukkitMCItem(edie.getItemDrop());
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(edie.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return edie.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			edie.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return edie;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityResurrectEvent implements MCEntityResurrectEvent {

		EntityResurrectEvent ere;

		public BukkitMCEntityResurrectEvent(Event e){ this.ere = (EntityResurrectEvent) e; }

		@Override
		public MCLivingEntity getEntity() {
			return new BukkitMCLivingEntity(ere.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return ere.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			ere.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return ere;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityShootBowEvent implements MCEntityShootBowEvent {

		EntityShootBowEvent esbe;

		public BukkitMCEntityShootBowEvent(Event e){ this.esbe = (EntityShootBowEvent) e; }

		@Override
		public MCItemStack getBow() {
			return new BukkitMCItemStack(esbe.getBow());
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(esbe.getEntity());
		}

		@Override
		public CDouble getForce() {
			return new CDouble(esbe.getForce(), Target.UNKNOWN);
		}

		@Override
		public MCEntity getProjectile() {
			return new BukkitMCEntity(esbe.getProjectile());
		}

		@Override
		public boolean isCancelled() {
			return esbe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			esbe.setCancelled(cancelled);
		}

		@Override
		public void setProjectile(Entity projectile) {
			esbe.setProjectile(projectile);
		}

		@Override
		public Object _GetObject() {
			return esbe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityTameEvent implements MCEntityTameEvent {

		EntityTameEvent ete;

		public BukkitMCEntityTameEvent(Event e){ this.ete = (EntityTameEvent) e; }

		@Override
		public MCLivingEntity getEntity() {
			return new BukkitMCLivingEntity(ete.getEntity());
		}

		@Override
		public MCAnimalTamer getOwner() {
			return new BukkitMCAnimalTamer(ete.getOwner());
		}

		@Override
		public boolean isCancelled() {
			return ete.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			ete.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return ete;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityTeleportEvent implements MCEntityTeleportEvent {

		EntityTeleportEvent ete;

		public BukkitMCEntityTeleportEvent(Event e){ this.ete = (EntityTeleportEvent) e; }

		@Override
		public MCLocation getFrom() {
			return new BukkitMCLocation(ete.getFrom());
		}

		@Override
		public MCLocation getTo() {
			return new BukkitMCLocation(ete.getTo());
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(ete.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return ete.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			ete.setCancelled(cancelled);
		}

		@Override
		public void setFrom(Location from) {
			ete.setFrom(from);
		}

		@Override
		public void setTo(Location to) {
			ete.setTo(to);
		}

		@Override
		public Object _GetObject() {
			return ete;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityToggleSwimEvent implements MCEntityToggleSwimEvent {

		EntityToggleSwimEvent etse;

		public BukkitMCEntityToggleSwimEvent(Event e){ this.etse = (EntityToggleSwimEvent) e; }

		@Override
		public boolean isSwimming() {
			return etse.isSwimming();
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(etse.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return etse.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			etse.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return etse;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCEntityUnleashEvent implements MCEntityUnleashEvent {

		EntityUnleashEvent eue;

		public BukkitMCEntityUnleashEvent(Event e){ this.eue = (EntityUnleashEvent) e; }

		@Override
		public CString getReason() {
			return new CString(eue.getReason().name(), Target.UNKNOWN);
		}

		@Override
		public Object _GetObject() {
			return eue;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCExplosionPrimeEvent implements MCExplosionPrimeEvent {

		ExplosionPrimeEvent epe;

		public BukkitMCExplosionPrimeEvent(Event e){ this.epe = (ExplosionPrimeEvent) e; }

		@Override
		public boolean getFire() {
			return epe.getFire();
		}

		@Override
		public CDouble getRadius() {
			return new CDouble(epe.getRadius(), Target.UNKNOWN);
		}

		@Override
		public MCEntity getEntity() {
			return new BukkitMCEntity(epe.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return epe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			epe.setCancelled(cancelled);
		}

		@Override
		public void setFire(boolean fire) {
			epe.setFire(fire);
		}

		@Override
		public void setRadius(float radius) {
			epe.setRadius(radius);
		}

		@Override
		public Object _GetObject() {
			return epe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCHorseJumpEvent implements MCHorseJumpEvent {

		HorseJumpEvent hje;

		public BukkitMCHorseJumpEvent(Event e){ this.hje = (HorseJumpEvent) e; }

		@Override
		public MCAbstractHorse getEntity() {
			return new BukkitMCAbstractHorse(hje.getEntity());
		}

		@Override
		public CDouble getPower() {
			return new CDouble(hje.getPower(), Target.UNKNOWN);
		}

		@Override
		public boolean isCancelled() {
			return hje.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			hje.setCancelled(cancelled);
		}

		@Override
		public void setPower(float power) {
			hje.setPower(power);
		}

		@Override
		public Object _GetObject() {
			return hje;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCItemMergeEvent implements MCItemMergeEvent {

		ItemMergeEvent ime;

		public BukkitMCItemMergeEvent(Event e){ this.ime = (ItemMergeEvent) e; }

		@Override
		public MCItem getEntity() {
			return new BukkitMCItem(ime.getEntity());
		}

		@Override
		public MCItem getTarget() {
			return new BukkitMCItem(ime.getTarget());
		}

		@Override
		public boolean isCancelled() {
			return ime.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			ime.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return ime;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPigZapEvent implements MCPigZapEvent {

		PigZapEvent pze;

		public BukkitMCPigZapEvent(Event e){ this.pze = (PigZapEvent) e; }

		@Override
		public MCPig getEntity() {
			return new BukkitMCPig(pze.getEntity());
		}

		@Override
		public MCLightningStrike getLightning() {
			return new BukkitMCLightningStrike(pze.getLightning());
		}

		@Override
		public MCPigZombie getPigZombie() {
			return new BukkitMCPigZombie(pze.getPigZombie());
		}

		@Override
		public boolean isCancelled() {
			return pze.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			pze.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return pze;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPigZombieAngerEvent implements MCPigZombieAngerEvent {

		PigZombieAngerEvent pzae;

		public BukkitMCPigZombieAngerEvent(Event e){ this.pzae = (PigZombieAngerEvent) e; }

		@Override
		public MCPigZombie getEntity() {
			return new BukkitMCPigZombie(pzae.getEntity());
		}

		@Override
		public CInt getNewAnger() {
			return new CInt(pzae.getNewAnger(), Target.UNKNOWN);
		}

		@Override
		public MCEntity getTarget() {
			return new BukkitMCEntity(pzae.getTarget());
		}

		@Override
		public boolean isCancelled() {
			return pzae.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			pzae.setCancelled(cancelled);
		}

		@Override
		public void setNewAnger(int newAnger) {
			pzae.setNewAnger(newAnger);
		}

		@Override
		public Object _GetObject() {
			return pzae;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCSheepDyeWoolEvent implements MCSheepDyeWoolEvent {

		SheepDyeWoolEvent sdwe;

		public BukkitMCSheepDyeWoolEvent(Event e){ this.sdwe = (SheepDyeWoolEvent) e; }

		@Override
		public DyeColor getColor() {
			return sdwe.getColor();
		}

		@Override
		public MCSheep getEntity() {
			return new BukkitMCSheep(sdwe.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return sdwe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			sdwe.setCancelled(cancelled);
		}

		@Override
		public void setColor(DyeColor color) {
			sdwe.setColor(color);
		}

		@Override
		public Object _GetObject() {
			return sdwe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCSheepRegrowWoolEvent implements MCSheepRegrowWoolEvent {

		SheepRegrowWoolEvent srwe;

		public BukkitMCSheepRegrowWoolEvent(Event e){ this.srwe = (SheepRegrowWoolEvent) e; }

		@Override
		public MCSheep getEnity() {
			return new BukkitMCSheep(srwe.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return srwe.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			srwe.setCancelled(cancelled);
		}

		@Override
		public Object _GetObject() {
			return srwe;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCSlimeSplitEvent implements MCSlimeSplitEvent {

		SlimeSplitEvent sse;

		public BukkitMCSlimeSplitEvent(Event e){ this.sse = (SlimeSplitEvent) e; }

		@Override
		public CInt getCount() {
			return new CInt(sse.getCount(), Target.UNKNOWN);
		}

		@Override
		public MCSlime getEntity() {
			return new BukkitMCSlime(sse.getEntity());
		}

		@Override
		public boolean isCancelled() {
			return sse.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			sse.setCancelled(cancelled);
		}

		@Override
		public void setCount(int count) {
			sse.setCount(count);
		}

		@Override
		public Object _GetObject() {
			return sse;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCVillagerAcquireTradeEvent implements MCVillagerAcquireTradeEvent {

		VillagerAcquireTradeEvent vate;

		public BukkitMCVillagerAcquireTradeEvent(Event e){ this.vate = (VillagerAcquireTradeEvent) e; }

		@Override
		public MCVillager getEntity() {
			return new BukkitMCVillager(vate.getEntity());
		}

		@Override
		public MerchantRecipe getRecipe() {
			return vate.getRecipe();
		}

		@Override
		public boolean isCancelled() {
			return vate.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancelled) {
			vate.setCancelled(cancelled);
		}

		@Override
		public void setRecipe(MerchantRecipe recipe) {
			vate.setRecipe(recipe);
		}

		@Override
		public Object _GetObject() {
			return vate;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCVillagerReplenishTradeEvent implements MCVillagerReplenishTradeEvent{

		VillagerReplenishTradeEvent vrte;

		public BukkitMCVillagerReplenishTradeEvent(Event e){ this.vrte = (VillagerReplenishTradeEvent) e; }

		@Override
		public CInt getBonus() {
			return new CInt(vrte.getBonus(), Target.UNKNOWN);
		}

		@Override
		public MCVillager getEntity() {
			return new BukkitMCVillager(vrte.getEntity());
		}

		@Override
		public MerchantRecipe getRecipe() {
			return vrte.getRecipe();
		}

		@Override
		public boolean isCancelled() {
			return vrte.isCancelled();
		}

		@Override
		public void setBonus(int bonus) {
			vrte.setBonus(bonus);
		}

		@Override
		public void setCancelled(boolean cancelled) {
			vrte.setCancelled(cancelled);
		}

		@Override
		public void setRecipe(MerchantRecipe recipe) {
			vrte.setRecipe(recipe);
		}

		@Override
		public Object _GetObject() {
			return vrte;
		}
	}
}
