package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCTameable;
import com.laytonsmith.abstraction.MCVehicle;
import com.laytonsmith.annotations.MEnum;

public interface MCHorse extends MCTameable, MCVehicle, MCInventoryHolder {
	@MEnum("HorseVariant")
	enum MCHorseVariant {
		HORSE, DONKEY, MULE, SKELETON, ZOMBIE
	}
	@MEnum("HorseColor")
	enum MCHorseColor {
		BLACK, BROWN, CHESTNUT, CREAMY, DARK_BROWN, GRAY, WHITE
	}
	@MEnum("HorsePattern")
	enum MCHorsePattern {
		NONE, SOCKS, WHITEFIELD, WHITE_DOTS, BLACK_DOTS
	}

	MCHorseVariant getVariant();
	MCHorseColor getColor();
	MCHorsePattern getPattern();

	void setVariant(MCHorseVariant variant);
	void setColor(MCHorseColor color);
	void setPattern(MCHorsePattern pattern);

	double getJumpStrength();
	void setJumpStrength(double strength);

	boolean hasChest();
	void setHasChest(boolean hasChest);

	int getDomestication();
	int getMaxDomestication();
	void setDomestication(int level);
	void setMaxDomestication(int level);

	// Inventory
	void setSaddle(MCItemStack stack);
	MCItemStack getSaddle();
	void setArmor(MCItemStack stack);
	MCItemStack getArmor();
}
