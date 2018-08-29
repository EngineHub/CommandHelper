package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCEntityType;
import com.laytonsmith.core.events.BindableEvent;

public interface MCProjectileHitEvent extends BindableEvent {

	MCProjectile getEntity();

	MCEntityType getEntityType();

	MCEntity getHitEntity();

	MCBlock getHitBlock();
}
