package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCFireworkMeta;

public interface MCFirework extends MCProjectile {

	MCFireworkMeta getFireWorkMeta();

	void setFireWorkMeta(MCFireworkMeta fm);
}
