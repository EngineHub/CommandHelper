package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCKnowledgeBookMeta;
import com.laytonsmith.abstraction.MCNamespacedKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.KnowledgeBookMeta;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCKnowledgeBookMeta extends BukkitMCItemMeta implements MCKnowledgeBookMeta {

	private final KnowledgeBookMeta kbm;

	public BukkitMCKnowledgeBookMeta(KnowledgeBookMeta im) {
		super(im);
		kbm = im;
	}

	@Override
	public boolean hasRecipes() {
		return kbm.hasRecipes();
	}

	@Override
	public List<MCNamespacedKey> getRecipes() {
		List<MCNamespacedKey> keys = new ArrayList<>();
		for(NamespacedKey key : kbm.getRecipes()) {
			keys.add(new BukkitMCNamespacedKey(key));
		}
		return keys;
	}

	@Override
	public void setRecipes(List<MCNamespacedKey> recipes) {
		List<NamespacedKey> keys = new ArrayList<>(recipes.size());
		for(MCNamespacedKey key : recipes) {
			keys.add((NamespacedKey) key.getHandle());
		}
		kbm.setRecipes(keys);
	}
}
