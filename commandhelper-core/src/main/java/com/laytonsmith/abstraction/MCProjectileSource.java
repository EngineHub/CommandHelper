package com.laytonsmith.abstraction;

import com.laytonsmith.PureUtilities.Vector3D;
import com.laytonsmith.abstraction.enums.MCProjectileType;

/**
 *
 * @author jb_aero
 */
public interface MCProjectileSource extends AbstractionObject {
	
	public MCProjectile launchProjectile(MCProjectileType projectile);

	public MCProjectile launchProjectile(MCProjectileType projectile, Vector3D init);
}
