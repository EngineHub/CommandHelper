package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCFireworkEffect;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.enums.bukkit.BukkitMCFireworkType;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;

import java.util.ArrayList;
import java.util.List;

public class BukkitMCFireworkEffect implements MCFireworkEffect {

	FireworkEffect fe;
	public BukkitMCFireworkEffect(FireworkEffect fe) {
		this.fe = fe;
	}

	@Override
	public boolean hasFlicker() {
		return fe.hasFlicker();
	}

	@Override
	public boolean hasTrail(){
		return fe.hasTrail();
	}

	@Override
	public List<MCColor> getColors() {
		List<Color> colors = fe.getColors();
		List<MCColor> c = new ArrayList<>();
		for(Color cc : colors){
			c.add(BukkitMCColor.GetMCColor(cc));
		}
		return c;
	}

	@Override
	public List<MCColor> getFadeColors() {
		List<Color> colors = fe.getFadeColors();
		List<MCColor> c = new ArrayList<>();
		for(Color cc : colors){
			c.add(BukkitMCColor.GetMCColor(cc));
		}
		return c;
	}

	@Override
	public MCFireworkType getType() {
		return BukkitMCFireworkType.getConvertor().getAbstractedEnum(fe.getType());
	}

	@Override
	public FireworkEffect getHandle(){
		return fe;
	}

	@Override
	public boolean equals(Object obj){
		return obj instanceof BukkitMCFireworkEffect && fe.equals(obj);
	}

	@Override
	public int hashCode(){
		return fe.hashCode();
	}

	@Override
	public String toString(){
		return fe.toString();
	}
}
