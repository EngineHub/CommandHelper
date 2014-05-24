package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;

/**
 *
 * @author EntityReborn
 */
public interface MCEntityDamageByEntityEvent extends MCEntityDamageEvent {
    public MCEntity getDamager();
}