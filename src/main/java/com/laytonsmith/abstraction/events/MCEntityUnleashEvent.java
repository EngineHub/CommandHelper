package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityUnleashEvent extends BindableEvent {

	CString getReason();

}
