package com.laytonsmith.abstraction;

import java.util.UUID;

public interface MCPlayerProfile extends AbstractionObject {

	String getName();

	UUID getId();

	MCProfileProperty getProperty(String key);

	void setProperty(MCProfileProperty property);

}
