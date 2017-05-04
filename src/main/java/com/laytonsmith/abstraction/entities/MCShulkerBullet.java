package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCProjectile;

public interface MCShulkerBullet extends MCProjectile {

	void setTarget(MCEntity entity);
	MCEntity getTarget();

}
