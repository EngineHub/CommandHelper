package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.enums.MCHorseColor;
import com.laytonsmith.abstraction.enums.MCHorseStyle;
import com.laytonsmith.abstraction.enums.MCHorseVariant;

public interface MCHorse extends MCTameable, MCVehicle, MCInventoryHolder {

	public MCHorseVariant getVariant();
	public MCHorseColor getColor();
	public MCHorseStyle getStyle();

	public void setVariant(MCHorseVariant variant);
	public void setColor(MCHorseColor color);
	public void setStyle(MCHorseStyle pattern);

	public double getJumpStrength();
	public void setJumpStrength(double strength);

	public boolean hasChest();
	public void setHasChest(boolean hasChest);

	public int getDomestication();
	public int getMaxDomestication();
	public void setDomestication(int level);
	public void setMaxDomestication(int level);
}