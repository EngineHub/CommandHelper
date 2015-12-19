package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.enums.MCInstrument;

public interface MCNotePlayEvent extends MCBlockEvent {

	public MCNote getNote();

	public void setNote(MCNote note);

	public MCInstrument getInstrument();

	public void setInstrument(MCInstrument i);
}
