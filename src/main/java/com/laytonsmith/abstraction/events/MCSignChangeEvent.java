package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCSignChangeEvent extends BindableEvent {

    public MCPlayer getPlayer();

    public MCBlock getBlock();

    public CString getLine(int index);

    public void setLine(int index, String text);

    public void setLines(String[] lines);

    public CArray getLines();
}