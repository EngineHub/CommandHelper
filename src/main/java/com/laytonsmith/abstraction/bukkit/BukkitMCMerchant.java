package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCHumanEntity;
import com.laytonsmith.abstraction.MCMerchant;
import com.laytonsmith.abstraction.MCMerchantRecipe;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCHumanEntity;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCMerchant implements MCMerchant {

	private String title;
	private Merchant merchant;
	private List<String> keys;
	public BukkitMCMerchant(Merchant mer, String title) {
		merchant = mer;
		this.title = title;
		keys = new ArrayList<>();
		for(int i = 0; i < merchant.getRecipes().size(); i++) {
			keys.add(null);
		}
	}

	@Override
	public Merchant getHandle() {
		return merchant;
	}

	@Override
	public int hashCode() {
		return getHandle().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return getHandle().equals(obj);
	}

	@Override
	public String toString() {
		return getHandle().toString();
	}

	@Override
	public boolean isTrading() {
		return getHandle().isTrading();
	}

	@Override
	public MCHumanEntity getTrader() {
		HumanEntity he = getHandle().getTrader();
		if(he == null) {
			return null;
		}
		if(he instanceof Player) {
			return new BukkitMCPlayer(he);
		}
		return new BukkitMCHumanEntity(he);
	}

	@Override
	public List<MCMerchantRecipe> getRecipes() {
		List<MCMerchantRecipe> ret = new ArrayList<>();
		for(int i = 0; i < getHandle().getRecipes().size(); i++) {
			ret.add(new BukkitMCMerchantRecipe(getHandle().getRecipe(i), keys.get(i)));
		}
		return ret;
	}

	@Override
	public void setRecipes(List<MCMerchantRecipe> recipes) {
		List<MerchantRecipe> ret = new ArrayList<>();
		keys.clear();
		for(MCMerchantRecipe mr : recipes) {
			ret.add((MerchantRecipe) mr.getHandle());
			keys.add(mr.getKey());
		}
		getHandle().setRecipes(ret);
	}

	@Override
	public String getTitle() {
		return title;
	}
}
