package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCEntity;

/**
 *
 * @author EntityReborn
 */
public interface MCEntityDamageByEntityEvent extends MCEntityDamageEvent {
    public MCEntity getDamager();
}