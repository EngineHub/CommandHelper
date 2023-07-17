package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCInteraction;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;

public class BukkitMCInteraction extends BukkitMCEntity implements MCInteraction {
	Interaction interaction;

	public BukkitMCInteraction(Entity i) {
		super(i);
		this.interaction = (Interaction) i;
	}

	@Override
	public double getWidth() {
		return interaction.getInteractionWidth();
	}

	@Override
	public void setWidth(double width) {
		interaction.setInteractionWidth((float) width);
	}

	@Override
	public double getHeight() {
		return interaction.getInteractionHeight();
	}

	@Override
	public void setHeight(double height) {
		interaction.setInteractionHeight((float) height);
	}

	@Override
	public boolean isResponsive() {
		return interaction.isResponsive();
	}

	@Override
	public void setResponsive(boolean response) {
		interaction.setResponsive(response);
	}

	@Override
	public MCPreviousInteraction getLastAttack() {
		PreviousInteraction pi = interaction.getLastAttack();
		if(pi == null) {
			return null;
		}
		return new MCPreviousInteraction(pi.getPlayer().getUniqueId(), pi.getTimestamp());
	}

	@Override
	public MCPreviousInteraction getLastInteraction() {
		PreviousInteraction pi = interaction.getLastInteraction();
		if(pi == null) {
			return null;
		}
		return new MCPreviousInteraction(pi.getPlayer().getUniqueId(), pi.getTimestamp());
	}
}
