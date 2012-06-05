package com.laytonsmith.abstraction.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCEntityDamageByEntityEvent extends BindableEvent{
	public EntityDamageEvent.DamageCause getCause();
	public Entity getDamagee();
	public Entity getDamager();
	public int getDamage();
	public void setDamage(int damage);
	
}