package com.laytonsmith.abstraction;

public interface MCFoodComponent extends AbstractionObject {
	int getNutrition();
	void setNutrition(int nutrition);
	float getSaturation();
	void setSaturation(float saturation);
	boolean getCanAlwaysEat();
	void setCanAlwaysEat(boolean canAlwaysEat);
}
