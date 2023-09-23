package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCNamespacedKey;
import org.bukkit.NamespacedKey;

public class BukkitMCNamespacedKey implements MCNamespacedKey {

	NamespacedKey nsk;

	public BukkitMCNamespacedKey(NamespacedKey nsk) {
		this.nsk = nsk;
	}

	@Override
	public Object getHandle() {
		return this.nsk;
	}

	@Override
	public String toString() {
		return this.nsk.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MCNamespacedKey && this.nsk.equals(((MCNamespacedKey) obj).getHandle());
	}

	@Override
	public int hashCode() {
		return this.nsk.hashCode();
	}
}
