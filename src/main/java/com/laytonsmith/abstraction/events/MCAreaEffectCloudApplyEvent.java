package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCAreaEffectCloudApplyEvent extends BindableEvent {

    public List<MCLivingEntity> getAffectedEntities();

    public MCEntity getEntity();

}
