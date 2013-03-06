package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkMeta;

public interface MCFirework extends MCEntity {

	public MCFireworkMeta getFireWorkMeta();
	public void setFireWorkMeta(MCFireworkMeta fm);
}
