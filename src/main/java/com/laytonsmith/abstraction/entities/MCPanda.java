package com.laytonsmith.abstraction.entities;

import com.laytonsmith.annotations.MEnum;

public interface MCPanda extends MCAgeable {

	@MEnum("com.commandhelper.PandaGene")
	enum Gene {
		AGGRESSIVE, BROWN, LAZY, NORMAL, PLAYFUL, WEAK, WORRIED
	}

	MCPanda.Gene getMainGene();
	void setMainGene(Gene gene);
	MCPanda.Gene getHiddenGene();
	void setHiddenGene(Gene gene);

}
