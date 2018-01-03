package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCBarColor;
import com.laytonsmith.abstraction.enums.MCBarStyle;

import java.util.List;

public interface MCBossBar extends AbstractionObject {

	String getTitle();
	void setTitle(String title);
	MCBarColor getColor();
	void setColor(MCBarColor color);
	MCBarStyle getStyle();
	void setStyle(MCBarStyle style);
	double getProgress();
	void setProgress(double progress);
	void addPlayer(MCPlayer player);
	void removePlayer(MCPlayer player);
	void removeAllPlayers();
	List<MCPlayer> getPlayers();
	boolean isVisible();
	void setVisible(boolean visible);

}
