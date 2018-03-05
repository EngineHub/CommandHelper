package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkMeta;

public interface MCFirework extends MCEntity {

	MCFireworkMeta getFireWorkMeta();

	void setFireWorkMeta(MCFireworkMeta fm);
}
