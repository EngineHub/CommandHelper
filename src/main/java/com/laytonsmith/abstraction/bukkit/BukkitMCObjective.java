package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCObjective;
import com.laytonsmith.abstraction.MCScore;
import com.laytonsmith.abstraction.MCScoreboard;
import com.laytonsmith.abstraction.enums.MCDisplaySlot;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCDisplaySlot;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class BukkitMCObjective implements MCObjective {

	Objective o;
	public BukkitMCObjective(Objective obj) {
		o = obj;
	}

	@Override
	public String getCriteria() {
		return o.getCriteria();
	}

	@Override
	public String getDisplayName() {
		return o.getDisplayName();
	}

	@Override
	public MCDisplaySlot getDisplaySlot() {
		DisplaySlot ds = o.getDisplaySlot();
		if(ds == null) {
			return null;
		}
		return BukkitMCDisplaySlot.getConvertor().getAbstractedEnum(ds);
	}

	@Override
	public String getName() {
		return o.getName();
	}

	@Override
	public MCScore getScore(String entry) {
		return new BukkitMCScore(o.getScore(entry));
	}

	@Override
	public MCScoreboard getScoreboard() {
		return new BukkitMCScoreboard(o.getScoreboard());
	}

	@Override
	public boolean isModifiable() {
		return o.isModifiable();
	}

	@Override
	public void setDisplayName(String displayName) {
		o.setDisplayName(displayName);
	}

	@Override
	public void setDisplaySlot(MCDisplaySlot slot) {
		if(slot == null) {
			o.setDisplaySlot(null);
		} else {
			o.setDisplaySlot(BukkitMCDisplaySlot.getConvertor().getConcreteEnum(slot));
		}
	}

	@Override
	public void unregister() {
		o.unregister();
	}
}
