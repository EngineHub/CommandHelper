package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCRabbitType;

public interface MCRabbit extends MCAnimal {

	MCRabbitType getRabbitType();

	void setRabbitType(MCRabbitType type);
}
