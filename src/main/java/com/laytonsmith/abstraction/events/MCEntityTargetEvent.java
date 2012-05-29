package com.laytonsmith.abstraction.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityTargetEvent;

import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCEntityTargetEvent extends BindableEvent{
    public Entity getTarget();
    public void setTarget(Entity target);
    public Entity getEntity();
    public EntityType getEntityType();
    public EntityTargetEvent.TargetReason getReason();
}