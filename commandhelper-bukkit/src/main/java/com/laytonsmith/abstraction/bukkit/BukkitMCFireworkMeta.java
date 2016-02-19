package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCFireworkMeta;
import com.laytonsmith.abstraction.enums.MCFireworkType;
import com.laytonsmith.abstraction.bukkit.enums.BukkitMCFireworkType;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;

public class BukkitMCFireworkMeta extends BukkitMCItemMeta implements MCFireworkMeta {

	FireworkMeta fm;
	public BukkitMCFireworkMeta(FireworkMeta im) {
		super(im);
		fm = im;
	}

	@Override
	public FireworkMeta asItemMeta() {
		return fm;
	}

	@Override
	public int getStrength() {
		return fm.getPower();
	}

	@Override
	public void setStrength(int strength) {
		fm.setPower(strength);
	}

	@Override
	public boolean getFlicker() {
		for(FireworkEffect effect : fm.getEffects()){
			if(effect.hasFlicker()){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean getTrail() {
		for(FireworkEffect effect : fm.getEffects()){
			if(effect.hasTrail()){
				return true;
			}
		}
		return false;
	}

	@Override
	public List<MCColor> getColors() {
		for(FireworkEffect effect : fm.getEffects()){
			List<Color> colors = effect.getColors();
			List<MCColor> c = new ArrayList<>();
			for(Color cc : colors){
				c.add(BukkitMCColor.GetMCColor(cc));
			}
			return c;
		}
		return new ArrayList<>();
	}

	@Override
	public List<MCColor> getFadeColors() {
		for(FireworkEffect effect : fm.getEffects()){
			List<Color> colors = effect.getFadeColors();
			List<MCColor> c = new ArrayList<>();
			for(Color cc : colors){
				c.add(BukkitMCColor.GetMCColor(cc));
			}
			return c;
		}
		return new ArrayList<>();
	}

	@Override
	public MCFireworkType getType() {
		for(FireworkEffect effect : fm.getEffects()){
			return BukkitMCFireworkType.getConvertor().getAbstractedEnum(effect.getType());
		}
		// This is the default type
		return null;
	}

}
