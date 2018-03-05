package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCBossBar;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.enums.MCBarColor;
import com.laytonsmith.abstraction.enums.MCBarStyle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCBossBar implements MCBossBar {

	private final BossBar bb;

	public BukkitMCBossBar(BossBar bb) {
		this.bb = bb;
	}

	@Override
	public Object getHandle() {
		return bb;
	}

	@Override
	public String getTitle() {
		return bb.getTitle();
	}

	@Override
	public void setTitle(String title) {
		bb.setTitle(title);
	}

	@Override
	public MCBarColor getColor() {
		return MCBarColor.valueOf(bb.getColor().name());
	}

	@Override
	public void setColor(MCBarColor color) {
		bb.setColor(BarColor.valueOf(color.name()));
	}

	@Override
	public MCBarStyle getStyle() {
		return MCBarStyle.valueOf(bb.getStyle().name());
	}

	@Override
	public void setStyle(MCBarStyle style) {
		bb.setStyle(BarStyle.valueOf(style.name()));
	}

	@Override
	public double getProgress() {
		return bb.getProgress();
	}

	@Override
	public void setProgress(double progress) {
		bb.setProgress(progress);
	}

	@Override
	public void addPlayer(MCPlayer player) {
		bb.addPlayer((Player) player.getHandle());
	}

	@Override
	public void removePlayer(MCPlayer player) {
		bb.removePlayer((Player) player.getHandle());
	}

	@Override
	public void removeAllPlayers() {
		bb.removeAll();
	}

	@Override
	public List<MCPlayer> getPlayers() {
		List<MCPlayer> players = new ArrayList<>();
		for(Player player : bb.getPlayers()) {
			players.add(new BukkitMCPlayer(player));
		}
		return players;
	}

	@Override
	public boolean isVisible() {
		return bb.isVisible();
	}

	@Override
	public void setVisible(boolean visible) {
		bb.setVisible(visible);
	}

	@Override
	public String toString() {
		return bb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BukkitMCBossBar && bb.equals(((BukkitMCBossBar) obj).bb);
	}

	@Override
	public int hashCode() {
		return bb.hashCode();
	}
}
