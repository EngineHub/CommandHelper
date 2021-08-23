package com.laytonsmith.abstraction;

import java.util.UUID;

public interface MCPlayerProfile extends AbstractionObject {

	String getName();

	String setName(String name);

	UUID getId();

	UUID setId(UUID id);

	MCProfileProperty getProperty(String key);

	void setProperty(MCProfileProperty property);

}
