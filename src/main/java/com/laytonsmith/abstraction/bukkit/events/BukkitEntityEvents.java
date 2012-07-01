/* To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.abstraction.events.MCEntityDamageEvent;
import com.laytonsmith.core.events.abstraction;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 *
 * @author EntityReborn
 */
public class BukkitEntityEvents {
	@abstraction(type = Implementation.Type.BUKKIT)
    public static class BukkitMCEntityDamageByEntityEvent implements MCEntityDamageByEntityEvent {

        EntityDamageByEntityEvent event;

        public BukkitMCEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
            event = e;
        }

        public Object _GetObject() {
            return event;
        }

        public MCDamageCause getCause() {
            return MCDamageCause.valueOf(event.getCause().name());
        }

        public int getDamage() {
            return event.getDamage();
        }

        public MCEntity getDamagee() {
            return BukkitConvertor.BukkitGetCorrectEntity(event.getEntity());
        }

        public MCEntity getDamager() {
            return BukkitConvertor.BukkitGetCorrectEntity(event.getDamager());
        }

        public void setDamage(int damage) {
            event.setDamage(damage);
        }
    }
	
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
		
		public int getDamage() {
            return event.getDamage();
        }
		
		public MCEntity getEntity() {
			return BukkitConvertor.BukkitGetCorrectEntity(event.getEntity());
		}
		
		public MCEntityType getEntityType() {
            return getEntity().getType();
        }
		
		public void setDamage(int damage) {
			event.setDamage(damage);
		}
	}

    @abstraction(type = Implementation.Type.BUKKIT)
    public static class BukkitMCTargetEvent implements MCEntityTargetEvent {

        public static BukkitMCTargetEvent _instantiate(Entity entity, LivingEntity target, EntityTargetEvent.TargetReason reason) {
            return new BukkitMCTargetEvent(new EntityTargetEvent(( (BukkitMCEntity) entity )._Entity(),
                    (LivingEntity) ( (BukkitMCLivingEntity) target ).getLivingEntity(), reason));
        }

        EntityTargetEvent pie;

        public BukkitMCTargetEvent(EntityTargetEvent e) {
            pie = e;
        }

        public Object _GetObject() {
            return pie;
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

        public MCEntity getTarget() {
            return BukkitConvertor.BukkitGetCorrectEntity(pie.getTarget());
        }

        public void setTarget(MCEntity target) {
            pie.setTarget(((BukkitMCEntity)target)._Entity());
        }
    }
}