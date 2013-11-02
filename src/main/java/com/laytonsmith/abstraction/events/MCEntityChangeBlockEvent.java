package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author MariuszT
 */
public interface MCEntityChangeBlockEvent extends BindableEvent {

	public MCEntity getEntity();

	public MCBlock getBlock();

	public MCMaterial getTo();

	public byte getData();

	public boolean isCancelled();

	public void setCancelled(boolean cancel);
}