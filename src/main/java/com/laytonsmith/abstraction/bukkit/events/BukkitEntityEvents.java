package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCHanging;
import com.laytonsmith.abstraction.bukkit.BukkitMCItem;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCProjectile;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlock;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCRemoveCause;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.abstraction.enums.MCTargetReason;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSpawnReason;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDamageCause;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCEntityType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCRemoveCause;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author EntityReborn
 */
public class BukkitEntityEvents {
	
	public static class BukkitMCItemSpawnEvent implements MCItemSpawnEvent {

		ItemSpawnEvent ise;
		public BukkitMCItemSpawnEvent(ItemSpawnEvent event) {
			ise = event;
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
		public BukkitMCEntityExplodeEvent(EntityExplodeEvent event) {
			e = event;
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
			List<MCBlock> ret = new ArrayList<MCBlock>();
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
		public BukkitMCProjectileHitEvent(ProjectileHitEvent event) {
			phe = event;
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
			return BukkitMCEntityType.getConvertor().getAbstractedEnum(phe.getEntityType());
		}
		
		public static BukkitMCProjectileHitEvent _instantiate(MCProjectile p) {
			return new BukkitMCProjectileHitEvent(
					new ProjectileHitEvent(
							((BukkitMCProjectile) p).asProjectile()));
		}
		
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCProjectileLaunchEvent implements MCProjectileLaunchEvent {

		ProjectileLaunchEvent ple;

		public BukkitMCProjectileLaunchEvent(ProjectileLaunchEvent event) {
			ple = event;
		}

		@Override
		public Object _GetObject() {
			return ple;
		}

		@Override
		public MCProjectile getEntity() {
			return new BukkitMCProjectile(ple.getEntity());
		}

		@Override
		public MCEntityType getEntityType() {
			return BukkitMCEntityType.getConvertor().getAbstractedEnum(ple.getEntityType());
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPotionSplashEvent extends BukkitMCProjectileHitEvent
			implements MCPotionSplashEvent {
		
		PotionSplashEvent pse;
		public BukkitMCPotionSplashEvent(PotionSplashEvent event) {
			super(event);
			pse = event;
		}

		@Override
		public Object _GetObject() {
			return pse;
		}

		@Override
		public Set<MCLivingEntity> getAffectedEntities() {
			Set<MCLivingEntity> ret = new HashSet<MCLivingEntity>();
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
		public BukkitMCEntityDeathEvent(EntityDeathEvent e) {
			this.e = e;
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
            List<MCItemStack> drops = new ArrayList<MCItemStack>();
			
            for(ItemStack is : islist){
                drops.add(new BukkitMCItemStack(is));
            }
			
            return drops;
        }
		
		@Override
        public void clearDrops() {
            e.getDrops().clear();
        }
		
		@Override
        public void addDrop(MCItemStack is){
            e.getDrops().add(((BukkitMCItemStack)is).__ItemStack());
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
		public BukkitMCCreatureSpawnEvent(CreatureSpawnEvent event) {
			this.e = event;
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
		public void setType(MCMobs type) {
			e.setCancelled(true);
			e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.valueOf(type.name()));
		}
		
	}
	
	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCPlayerInteractEntityEvent implements MCPlayerInteractEntityEvent {

		PlayerInteractEntityEvent e;
		public BukkitMCPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
			this.e = event;
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
		
	}
	
    @abstraction(type = Implementation.Type.BUKKIT)
    public static class BukkitMCPlayerDropItemEvent implements MCPlayerDropItemEvent {
        PlayerDropItemEvent e;
        
        public BukkitMCPlayerDropItemEvent(PlayerDropItemEvent e) {
            this.e = e;
        }
        
		@Override
        public MCItem getItemDrop() {
            return new BukkitMCItem(e.getItemDrop());
        }
        
		@Override
        public void setItemStack(MCItemStack stack) {
            BukkitMCItemStack s = (BukkitMCItemStack) stack;
            if(s.getTypeId() == 0) {
                e.getItemDrop().remove();
            } else {
                e.getItemDrop().setItemStack(s.__ItemStack());
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
		PlayerPickupItemEvent e;
		
		public BukkitMCPlayerPickupItemEvent(PlayerPickupItemEvent e) {
			this.e = e;
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
			BukkitMCItemStack s = (BukkitMCItemStack)stack;
				e.setCancelled(true);
				e.getItem().remove();
			if(s.getTypeId() == 0) {
				return;
			} else {
				e.getPlayer().getInventory().addItem(s.asItemStack());
				//and for added realism :)
				e.getPlayer().getWorld().playSound(e.getItem().getLocation(), Sound.ITEM_PICKUP, 1, 2);
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
    public static class BukkitMCEntityDamageEvent implements MCEntityDamageEvent {

        EntityDamageEvent event;

        public BukkitMCEntityDamageEvent(EntityDamageEvent e) {
            event = e;
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

        public BukkitMCEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        	super(e);
            event = e;
        }

		@Override
        public MCEntity getDamager() {
            return BukkitConvertor.BukkitGetCorrectEntity(event.getDamager());
        }
    }

    @abstraction(type = Implementation.Type.BUKKIT)
    public static class BukkitMCTargetEvent implements MCEntityTargetEvent {

        EntityTargetEvent pie;

        public BukkitMCTargetEvent(EntityTargetEvent e) {
            pie = e;
        }

        public static BukkitMCTargetEvent _instantiate(Entity entity, LivingEntity target, EntityTargetEvent.TargetReason reason) {
            return new BukkitMCTargetEvent(new EntityTargetEvent(( (BukkitMCEntity) entity ).asEntity(),
                    (LivingEntity) ( (BukkitMCLivingEntity) target ).getLivingEntity(), reason));
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
        		pie.setTarget(((BukkitMCEntity)target).asEntity());
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

        public MCTargetReason getReason() {
            return MCTargetReason.valueOf(pie.getReason().name());
        }
    }

	public static class BukkitMCEntityEnterPortalEvent implements MCEntityEnterPortalEvent {
	
		EntityPortalEnterEvent epe;
		public BukkitMCEntityEnterPortalEvent(EntityPortalEnterEvent event) {
			epe = event;
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

		public BukkitMCEntityChangeBlockEvent(EntityChangeBlockEvent event) {
			ecb = event;
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
		public byte getData() {
			return ecb.getData();
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
	public static class BukkitMCHangingBreakEvent implements MCHangingBreakEvent {

		HangingBreakEvent hbe;

		public BukkitMCHangingBreakEvent(HangingBreakEvent event) {
			hbe = event;
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
}
