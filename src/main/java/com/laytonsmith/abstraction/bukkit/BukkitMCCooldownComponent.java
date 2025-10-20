package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCCooldownComponent;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.components.UseCooldownComponent;

public class BukkitMCCooldownComponent implements MCCooldownComponent {

	private final UseCooldownComponent cooldownComponent;

	public BukkitMCCooldownComponent(UseCooldownComponent foodComponent) {
		this.cooldownComponent = foodComponent;
	}

	@Override
	public float getSeconds() {
		return cooldownComponent.getCooldownSeconds();
	}

	@Override
	public void setSeconds(float seconds) {
		cooldownComponent.setCooldownSeconds(seconds);
	}

	@Override
	public String getCooldownGroup() {
		NamespacedKey group = cooldownComponent.getCooldownGroup();
		if(group == null) {
			return null;
		}
		return group.toString();
	}

	@Override
	public void setCooldownGroup(String cooldownGroup) {
		if(cooldownGroup == null) {
			cooldownComponent.setCooldownGroup(null);
		} else {
			cooldownComponent.setCooldownGroup(NamespacedKey.fromString(cooldownGroup, CommandHelperPlugin.self));
		}
	}

	@Override
	public Object getHandle() {
		return cooldownComponent;
	}

	@Override
	public String toString() {
		return cooldownComponent.toString();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof BukkitMCCooldownComponent && cooldownComponent.equals(((BukkitMCCooldownComponent) o).cooldownComponent);
	}

	@Override
	public int hashCode() {
		return cooldownComponent.hashCode();
	}
}
