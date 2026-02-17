package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCMaterial;

import java.util.ArrayList;
import java.util.List;

public interface MCRecipeChoice {

	class MaterialChoice implements MCRecipeChoice {
		List<MCMaterial> choices = new ArrayList<>();

		public void addMaterial(MCMaterial material) {
			choices.add(material);
		}

		public List<MCMaterial> getMaterials() {
			return choices;
		}
	}

	class ExactChoice implements MCRecipeChoice {
		List<MCItemStack> choices = new ArrayList<>();

		public void addItem(MCItemStack itemStack) {
			choices.add(itemStack);
		}

		public List<MCItemStack> getItems() {
			return choices;
		}
	}
}
