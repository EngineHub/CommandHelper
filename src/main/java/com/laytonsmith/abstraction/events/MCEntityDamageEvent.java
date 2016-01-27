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

    public double getFinalDamage();

    public double getDamage();

    public void setDamage(double damage);
}