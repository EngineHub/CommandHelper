package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCTextDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAlignment;

public class BukkitMCTextDisplay extends BukkitMCDisplay implements MCTextDisplay {

	TextDisplay td;

	public BukkitMCTextDisplay(Entity e) {
		super(e);
		this.td = (TextDisplay) e;
	}

	@Override
	public MCTextDisplay.Alignment getAlignment() {
		return MCTextDisplay.Alignment.valueOf(td.getAlignment().name());
	}

	@Override
	public void setAlignment(MCTextDisplay.Alignment alignment) {
		td.setAlignment(TextAlignment.valueOf(alignment.name()));
	}

	@Override
	public boolean usesDefaultBackground() {
		return td.isDefaultBackground();
	}

	@Override
	public void setUsesDefaultBackground(boolean defaultBackground) {
		td.setDefaultBackground(defaultBackground);
	}

	@Override
	public int getLineWidth() {
		return td.getLineWidth();
	}

	@Override
	public void setLineWidth(int width) {
		td.setLineWidth(width);
	}

	@Override
	public boolean isVisibleThroughBlocks() {
		return td.isSeeThrough();
	}

	@Override
	public void setVisibleThroughBlocks(boolean visible) {
		td.setSeeThrough(visible);
	}

	@Override
	public boolean hasShadow() {
		return td.isShadowed();
	}

	@Override
	public void setHasShadow(boolean hasShadow) {
		td.setShadowed(hasShadow);
	}

	@Override
	public String getText() {
		return td.getText();
	}

	@Override
	public void setText(String text) {
		td.setText(text);
	}

	@Override
	public byte getOpacity() {
		return td.getTextOpacity();
	}

	@Override
	public void setOpacity(byte opacity) {
		td.setTextOpacity(opacity);
	}
}
