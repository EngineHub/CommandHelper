/* To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.abstraction.enums.MCTargetReason;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 *
 * @author EntityReborn
 */
public class BukkitEntityEvents {
	
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
            e.getItemDrop().setItemStack(s.__ItemStack());
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
			e.getItem().setItemStack(s.__ItemStack());
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
            pie.setTarget(((BukkitMCEntity)target).asEntity());
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