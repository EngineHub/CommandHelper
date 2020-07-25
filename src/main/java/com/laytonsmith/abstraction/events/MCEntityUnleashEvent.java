package com.laytonsmith.abstraction.events;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.enums.MCUnleashReason;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityUnleashEvent extends BindableEvent {

		MCEntity getEntity();

		MCUnleashReason getReason();

}
