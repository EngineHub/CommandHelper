package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.bukkit.BukkitMCColor;
import com.laytonsmith.abstraction.entities.MCTextDisplay;
import org.bukkit.Color;
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
	public MCColor getBackgroundColor() {
		if(td.isDefaultBackground()) {
			return null;
		}
		Color color = td.getBackgroundColor();
		if(color == null) {
			return null;
		}
		return BukkitMCColor.GetMCColor(color);
	}

	@Override
	public void setBackgroundColor(MCColor color) {
		if(color == null) {
			td.setDefaultBackground(true);
			td.setBackgroundColor(null);
		} else {
			td.setDefaultBackground(false);
			td.setBackgroundColor(BukkitMCColor.GetColor(color));
		}
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
