package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.entities.MCPanda;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.annotations.abstractionenum;
import com.laytonsmith.core.Static;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Panda;

public class BukkitMCPanda extends BukkitMCAgeable implements MCPanda {

	private Panda p;

	public BukkitMCPanda(Entity be) {
		super(be);
		p = (Panda) be;
	}

	@Override
	public Gene getMainGene() {
		return MCPanda.Gene.valueOf(p.getMainGene().name());
	}

	@Override
	public void setMainGene(Gene gene) {
		p.setMainGene(Panda.Gene.valueOf(gene.name()));
	}

	@Override
	public Gene getHiddenGene() {
		return MCPanda.Gene.valueOf(p.getHiddenGene().name());
	}

	@Override
	public void setHiddenGene(Gene gene) {
		p.setHiddenGene(Panda.Gene.valueOf(gene.name()));
	}

	@abstractionenum(
			implementation = Implementation.Type.BUKKIT,
			forAbstractEnum = MCPanda.Gene.class,
			forConcreteEnum = Panda.Gene.class
	)
	public static class BukkitMCPandaGene extends EnumConvertor<MCPanda.Gene, Panda.Gene> {

		private static BukkitMCPandaGene instance;

		public static BukkitMCPandaGene getConvertor() {
			if(instance == null) {
				instance = new BukkitMCPandaGene();
			}
			return instance;
		}

		@Override
		protected Panda.Gene getConcreteEnumCustom(MCPanda.Gene abstracted) {
			if(Static.getServer().getMinecraftVersion().lt(MCVersion.MC1_14)) {
				return null;
			}
			return super.getConcreteEnumCustom(abstracted);
		}
	}
}
