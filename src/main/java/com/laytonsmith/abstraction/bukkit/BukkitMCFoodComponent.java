package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCFoodComponent;
import org.bukkit.inventory.meta.components.FoodComponent;

public class BukkitMCFoodComponent implements MCFoodComponent {

	private final FoodComponent foodComponent;

	public BukkitMCFoodComponent(FoodComponent foodComponent) {
		this.foodComponent = foodComponent;
	}

	@Override
	public int getNutrition() {
		return foodComponent.getNutrition();
	}

	@Override
	public void setNutrition(int nutrition) {
		foodComponent.setNutrition(nutrition);
	}

	@Override
	public float getSaturation() {
		return foodComponent.getSaturation();
	}

	@Override
	public void setSaturation(float saturation) {
		foodComponent.setSaturation(saturation);
	}

	@Override
	public boolean getCanAlwaysEat() {
		return foodComponent.canAlwaysEat();
	}

	@Override
	public void setCanAlwaysEat(boolean canAlwaysEat) {
		foodComponent.setCanAlwaysEat(canAlwaysEat);
	}

	@Override
	public Object getHandle() {
		return foodComponent;
	}

	@Override
	public String toString() {
		return foodComponent.toString();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof BukkitMCFoodComponent && foodComponent.equals(((BukkitMCFoodComponent) o).foodComponent);
	}

	@Override
	public int hashCode() {
		return foodComponent.hashCode();
	}
}
