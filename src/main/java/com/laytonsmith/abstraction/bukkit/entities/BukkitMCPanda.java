package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.entities.MCPanda;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.annotations.abstractionenum;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Panda;

public class BukkitMCPanda extends BukkitMCAnimal implements MCPanda {

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

	@Override
	public boolean isRolling() {
		return p.isRolling();
	}

	@Override
	public void setRolling(boolean rolling) {
		try {
			p.setRolling(rolling);
		} catch(NoSuchMethodError ex) {
			// probably before 1.19
		}
	}

	@Override
	public boolean isSneezing() {
		return p.isSneezing();
	}

	@Override
	public void setSneezing(boolean sneezing) {
		try {
			p.setSneezing(sneezing);
		} catch(NoSuchMethodError ex) {
			// probably before 1.19
		}
	}

	@Override
	public boolean isEating() {
		return p.isEating();
	}

	@Override
	public void setEating(boolean eating) {
		try {
			p.setEating(eating);
		} catch(NoSuchMethodError ex) {
			// probably before 1.19
		}
	}

	@Override
	public boolean isOnBack() {
		return p.isOnBack();
	}

	@Override
	public void setOnBack(boolean onBack) {
		try {
			p.setOnBack(onBack);
		} catch(NoSuchMethodError ex) {
			// probably before 1.19
		}
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
	}
}
