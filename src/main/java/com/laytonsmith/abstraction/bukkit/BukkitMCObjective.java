package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCOfflinePlayer;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class BukkitMCObjective implements MCObjective {

	Objective o;
	public BukkitMCObjective(Objective obj) {
		o = obj;
	}

	public String getCriteria() {
		return o.getCriteria();
	}

	public String getDisplayName() {
		return o.getDisplayName();
	}

	public MCDisplaySlot getDisplaySlot() {
		DisplaySlot ds = o.getDisplaySlot();
		if (ds == null) {
			return null;
		}
		return BukkitMCDisplaySlot.getConvertor().getAbstractedEnum(ds);
	}

	public String getName() {
		return o.getName();
	}

	public MCScore getScore(MCOfflinePlayer player) {
		return new BukkitMCScore(o.getScore((OfflinePlayer) player.getHandle()));
	}

	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(o.getScoreboard());
	}

	public boolean isModifiable() {
		return o.isModifiable();
	}

	public void setDisplayName(String displayName) {
		o.setDisplayName(displayName);
	}

	public void setDisplaySlot(MCDisplaySlot slot) {
		o.setDisplaySlot(BukkitMCDisplaySlot.getConvertor().getConcreteEnum(slot));
	}

	public void unregister() {
		o.unregister();
	}
}
