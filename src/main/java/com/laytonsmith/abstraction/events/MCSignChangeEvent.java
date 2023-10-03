package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCSign;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.BindableEvent;

public interface MCSignChangeEvent extends BindableEvent {

	MCPlayer getPlayer();

	MCBlock getBlock();

	CString getLine(int index);

	void setLine(int index, String text);

	void setLines(String[] lines);

	CArray getLines(Environment env);

	MCSign.Side getSide();
}
