package com.laytonsmith.core.clientsupport;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;

/**
 *
 */
public abstract class GenericClientEnv extends CommandHelperEnvironment implements Environment.EnvironmentImpl, Cloneable {

	public GenericClientEnv() {
		this.commandSender = getPlayer();
	}

	public PermissionGrants getPermissionGrants() {
		return PermissionGrants.getInstance();
	}

	@Override
	public GenericClientEnv clone() throws CloneNotSupportedException {
		GenericClientEnv clone = (GenericClientEnv) super.clone();
		return clone;
	}

	public abstract MCPlayer getPlayer();

}
