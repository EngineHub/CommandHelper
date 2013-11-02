package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCEntityTargetEvent extends BindableEvent {

    public MCEntity getTarget();

    public void setTarget(MCEntity target);

    public MCEntity getEntity();

    public MCEntityType getEntityType();

}