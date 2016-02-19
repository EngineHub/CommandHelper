package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCFireworkType;
import java.util.List;

public interface MCFireworkMeta extends MCItemMeta {

	int getStrength();
	void setStrength(int strength);

	boolean getFlicker();

	boolean getTrail();

	List<MCColor> getColors();

	List<MCColor> getFadeColors();

	MCFireworkType getType();

}
