package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author EntityReborn
 */
public interface MCSignChangeEvent extends BindableEvent {

    public MCBlock getBlock();

    public CString getLine(int index);

    public CArray getLines();

    public MCPlayer getPlayer();

    public void setLine(int index, String text);

    public void setLines(String[] lines);
}