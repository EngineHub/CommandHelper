package com.laytonsmith.abstraction.entities;

import com.laytonsmith.annotations.MEnum;

public interface MCPanda extends MCAnimal {

	@MEnum("com.commandhelper.PandaGene")
	enum Gene {
		AGGRESSIVE, BROWN, LAZY, NORMAL, PLAYFUL, WEAK, WORRIED
	}

	MCPanda.Gene getMainGene();
	void setMainGene(Gene gene);
	MCPanda.Gene getHiddenGene();
	void setHiddenGene(Gene gene);
	boolean isRolling();
	void setRolling(boolean rolling);
	boolean isSneezing();
	void setSneezing(boolean sneezing);
	boolean isEating();
	void setEating(boolean eating);
	boolean isOnBack();
	void setOnBack(boolean onBack);

}
