package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.enums.MCInstrument;

public interface MCNotePlayEvent extends MCBlockEvent {
	MCNote getNote();
	MCInstrument getInstrument();
}
