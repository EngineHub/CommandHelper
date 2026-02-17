package com.laytonsmith.abstraction;

import java.util.List;

public interface MCKnowledgeBookMeta extends MCItemMeta {

	boolean hasRecipes();

	List<MCNamespacedKey> getRecipes();

	void setRecipes(List<MCNamespacedKey> recipes);

}
