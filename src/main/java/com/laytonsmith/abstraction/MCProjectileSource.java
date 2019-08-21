package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.enums.MCEntityType;

public interface MCProjectileSource extends AbstractionObject {

	MCProjectile launchProjectile(MCEntityType projectile);

	MCProjectile launchProjectile(MCEntityType projectile, Vector3D init);
}
