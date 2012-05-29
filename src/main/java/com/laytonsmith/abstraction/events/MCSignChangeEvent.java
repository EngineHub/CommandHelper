package com.laytonsmith.abstraction.events;

import org.bukkit.block.Block;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCSignChangeEvent extends BindableEvent{
    public MCPlayer getPlayer();
    public Block getBlock();
    public CString getLine(int index);
    public void setLine(int index, String text);
    public void setLines(String[] lines);
	public CArray getLines();
}