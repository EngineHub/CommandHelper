package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.entities.MCProjectile;
import com.laytonsmith.abstraction.enums.MCProjectileType;

public interface MCProjectileSource extends AbstractionObject {

	MCProjectile launchProjectile(MCProjectileType projectile);

	MCProjectile launchProjectile(MCProjectileType projectile, Vector3D init);
}
