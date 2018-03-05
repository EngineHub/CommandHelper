package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.enums.MCInstrument;

public interface MCNotePlayEvent extends MCBlockEvent {

	MCNote getNote();

	void setNote(MCNote note);

	MCInstrument getInstrument();

	void setInstrument(MCInstrument i);
}
