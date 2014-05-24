package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.enums.MCProjectileType;

/**
 *
 * @author jb_aero
 */
public interface MCProjectileSource extends AbstractionObject {
	
	public MCProjectile launchProjectile(MCProjectileType projectile);
	public MCProjectile launchProjectile(MCProjectileType projectile, Velocity init);
}
