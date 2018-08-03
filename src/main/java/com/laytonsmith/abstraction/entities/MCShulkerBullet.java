package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;

public interface MCShulkerBullet extends MCProjectile {

	void setTarget(MCEntity entity);

	MCEntity getTarget();

}
