package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.MCVehicle;

public interface MCHorse extends MCTameable, MCVehicle, MCInventoryHolder {
	public enum MCHorseVariant {
		HORSE, DONKEY, MULE, SKELETON, ZOMBIE
	}
	public enum MCHorseColor {
		BLACK, BROWN, CHESTNUT, CREAMY, DARK_BROWN, GRAY, WHITE
	}
	public enum MCHorsePattern {
		NONE, SOCKS, WHITEFIELD, WHITE_DOTS, BLACK_DOTS
	}
	
	public MCHorseVariant getVariant();
	public MCHorseColor getColor();
	public MCHorsePattern getPattern();
	
	public void setVariant(MCHorseVariant variant);
	public void setColor(MCHorseColor color);
	public void setPattern(MCHorsePattern pattern);
	
	public double getJumpStrength();
	public void setJumpStrength(double strength);
	
	public boolean hasChest();
	public void setHasChest(boolean hasChest);
	
	public int getDomestication();
	public int getMaxDomestication();
	public void setDomestication(int level);
	public void setMaxDomestication(int level);
}
