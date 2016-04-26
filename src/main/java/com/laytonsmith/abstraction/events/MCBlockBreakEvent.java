package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCBlockBreakEvent extends BindableEvent {

    public MCPlayer getPlayer();

    public MCBlock getBlock();

	public int getExpToDrop();

	public void setExpToDrop(int exp);

}