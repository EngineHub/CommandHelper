
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCMobs;
import com.laytonsmith.abstraction.enums.MCSpawnReason;
import com.laytonsmith.abstraction.enums.MCTargetReason;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCSpawnReason;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 *
 * @author EntityReborn
 */
public class BukkitEntityEvents {
	
	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCCreatureSpawnEvent implements MCCreatureSpawnEvent {

		CreatureSpawnEvent e;
		public BukkitMCCreatureSpawnEvent(CreatureSpawnEvent event) {
			this.e = event;
		}
		
		public Object _GetObject() {
			return e;
		}

		public MCLivingEntity getEntity() {
			return (MCLivingEntity) BukkitConvertor.BukkitGetCorrectEntity(e.getEntity());
		}

		public MCLocation getLocation() {
			return new BukkitMCLocation(e.getLocation());
		}

		public MCSpawnReason getSpawnReason() {
			return BukkitMCSpawnReason.getConvertor().getAbstractedEnum(e.getSpawnReason());
		}
		
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
		
		public Object _GetObject() {
			return e;
		}

		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(e.getRightClicked());
		}

		public boolean isCancelled() {
			return e.isCancelled();
		}

		public void setCancelled(boolean cancelled) {
			e.setCancelled(cancelled);
		}

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
        
        public MCItemStack getItemDrop() {
            return new BukkitMCItemStack(e.getItemDrop().getItemStack());
        }
        
        public void setItem(MCItemStack stack) {
            BukkitMCItemStack s = (BukkitMCItemStack) stack;
            if(s.getTypeId() == 0) {
                e.getItemDrop().remove();
            } else {
                e.getItemDrop().setItemStack(s.__ItemStack());
            }
        }
        
        public boolean isCancelled() {
            return e.isCancelled();
        }
        
        public void setCancelled(boolean cancelled) {
            e.setCancelled(cancelled);
        }
        
        public MCPlayer getPlayer() {
            return new BukkitMCPlayer(e.getPlayer());
        }
        
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
		public int getRemaining() {
			return e.getRemaining();
		}

		public MCItemStack getItem() {
			return new BukkitMCItemStack(e.getItem().getItemStack());
		}

		public void setItem(MCItemStack stack) {
			BukkitMCItemStack s = (BukkitMCItemStack)stack;
				e.setCancelled(true);
				e.getItem().remove();
			if(s.getTypeId() == 0) {
				return;
			} else {
				e.getPlayer().getInventory().addItem(s.asItemStack());
				//and for added realism :)
				e.getPlayer().playSound(e.getItem().getLocation(), Sound.ITEM_PICKUP, 1, 2);
			}
		}

		public boolean isCancelled() {
			return e.isCancelled();
		}

		public void setCancelled(boolean cancelled) {
			e.setCancelled(cancelled);
		}

		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(e.getPlayer());
		}

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

        public Object _GetObject() {
            return event;
        }

        public MCDamageCause getCause() {
            return MCDamageCause.valueOf(event.getCause().name());
        }

        public MCEntity getEntity() {
            return BukkitConvertor.BukkitGetCorrectEntity(event.getEntity());
        }

        public int getDamage() {
            return event.getDamage();
        }

        public void setDamage(int damage) {
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

        public Object _GetObject() {
            return pie;
        }

        public MCEntity getTarget() {
            return BukkitConvertor.BukkitGetCorrectEntity(pie.getTarget());
        }

        public void setTarget(MCEntity target) {
        	if (target == null) {
        		pie.setTarget(null);
        	} else {
        		pie.setTarget(((BukkitMCEntity)target).asEntity());
        	}
        }

        public MCEntity getEntity() {
            return BukkitConvertor.BukkitGetCorrectEntity(pie.getEntity());
        }

        public MCEntityType getEntityType() {
            return BukkitConvertor.BukkitGetCorrectEntity(pie.getEntity()).getType();
        }

        public MCTargetReason getReason() {
            return MCTargetReason.valueOf(pie.getReason().name());
        }
    }
}
