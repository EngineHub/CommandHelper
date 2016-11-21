package com.laytonsmith.abstraction.entities;

import com.laytonsmith.annotations.MEnum;

public interface MCLlama extends MCChestedHorse {

	@MEnum("LlamaColor")
	enum MCLlamaColor {
		CREAMY, WHITE, BROWN, GRAY
	}

	MCLlamaColor getLlamaColor();
	void setLlamaColor(MCLlamaColor color);
}
