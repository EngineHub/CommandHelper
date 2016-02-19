package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.enums.MCRabbitType;

public interface MCRabbit extends MCAgeable {

	MCRabbitType getRabbitType();
	void setRabbitType(MCRabbitType type);
}
