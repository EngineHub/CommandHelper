package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCEntityDamageEvent extends BindableEvent {

    public MCDamageCause getCause();

    public MCEntity getEntity();

    public int getDamage();

    public void setDamage(int damage);
}