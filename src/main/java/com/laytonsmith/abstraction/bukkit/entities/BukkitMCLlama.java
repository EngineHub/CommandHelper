package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.bukkit.BukkitMCItemStack;
import com.laytonsmith.abstraction.entities.MCLlama;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;

public class BukkitMCLlama extends BukkitMCChestedHorse implements MCLlama {

	Llama l;

	public BukkitMCLlama(Entity t) {
		super(t);
		this.l = (Llama) t;
	}

	@Override
	public MCLlamaColor getLlamaColor() {
		return BukkitMCLlamaColor.getConvertor().getAbstractedEnum(l.getColor());
	}

	@Override
	public void setLlamaColor(MCLlamaColor color) {
		l.setColor(BukkitMCLlamaColor.getConvertor().getConcreteEnum(color));
	}

	@Override
	public void setSaddle(MCItemStack stack) {
		l.getInventory().setItem(1, ((BukkitMCItemStack) stack).asItemStack());
	}

	@Override
	public MCItemStack getSaddle() {
		return new BukkitMCItemStack(l.getInventory().getItem(1));
	}

	@abstractionenum(
			implementation = Implementation.Type.BUKKIT,
			forAbstractEnum = MCLlamaColor.class,
			forConcreteEnum = Llama.Color.class
	)
	public static class BukkitMCLlamaColor extends EnumConvertor<MCLlamaColor, Llama.Color> {

		private static BukkitMCLlamaColor instance;

		public static BukkitMCLlamaColor getConvertor() {
			if(instance == null) {
				instance = new BukkitMCLlamaColor();
			}
			return instance;
		}

		@Override
		protected Llama.Color getConcreteEnumCustom(MCLlamaColor abstracted) {
			if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_11)) {
				return null;
			}
			return super.getConcreteEnumCustom(abstracted);
		}
	}
}
