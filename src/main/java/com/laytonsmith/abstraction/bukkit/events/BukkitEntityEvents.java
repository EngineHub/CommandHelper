/* To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.core.events.abstraction;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

/**
 *
 * @author EntityReborn
 */
public class BukkitEntityEvents {
	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCEntityDamageByEntityEvent implements MCEntityDamageByEntityEvent {
		EntityDamageByEntityEvent event;
		
		public BukkitMCEntityDamageByEntityEvent(EntityDamageByEntityEvent e){
            event = e;
        }
		
		public Object _GetObject() {
			return event;
		}

		public DamageCause getCause() {
			return event.getCause();
		}

		public Entity getDamagee() {
			return event.getEntity();
		}

		public Entity getDamager() {
			return event.getDamager();
		}

		public int getDamage() {
			return event.getDamage();
		}

		public void setDamage(int damage) {
			event.setDamage(damage);
		}
		
	}
	
	@abstraction(type=Implementation.Type.BUKKIT)
    public static class BukkitMCTargetEvent implements MCEntityTargetEvent{

        EntityTargetEvent pie;
        
        public BukkitMCTargetEvent(EntityTargetEvent e){
            pie = e;
        }
        
        public static BukkitMCTargetEvent _instantiate(Entity entity, LivingEntity target, EntityTargetEvent.TargetReason reason){
        	return new BukkitMCTargetEvent(new EntityTargetEvent(((BukkitMCEntity)entity)._Entity(), 
        			(LivingEntity) ((BukkitMCLivingEntity)target).getLivingEntity(), reason));
        }
        

        public Object _GetObject() {
            return pie;
        }

		public Entity getTarget() {
			return pie.getTarget();
		}

		public void setTarget(Entity target) {
			pie.setTarget(target);
		}

		public Entity getEntity() {
			return pie.getEntity();
		}

		public EntityType getEntityType() {
			return pie.getEntityType();
		}

		public TargetReason getReason() {
			return pie.getReason();
		}
        
    }
}