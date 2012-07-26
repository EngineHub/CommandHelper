/* To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.abstraction;
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