package com.laytonsmith.core.clientsupport;

import java.util.EnumSet;
import java.util.Set;

/**
 *
 */
public class PermissionGrants {

	private static PermissionGrants instance;

	/**
	 * Returns the instance of the PermissionGrants object for this server.
	 * @return
	 */
	public static PermissionGrants getInstance() {
		return PermissionGrants.instance;
	}

	/**
	 * Installs the PermissionGrants for this server. NOTE! It is extremely important
	 * that this is cleared and then re-set for each server connection.
	 * @param permissionGrants
	 */
	public static void setInstance(PermissionGrants permissionGrants) {
		PermissionGrants.instance = permissionGrants;
	}

	private final Set<ClientPermission> grantedPermissions;

	/**
	 * Creates a new PermissionGrants object. This does not install the instance.
	 * @param permissions The EnumSet of ClientPermissions.
	 */
	public PermissionGrants(Set<ClientPermission> permissions) {
		this.grantedPermissions = EnumSet.copyOf(permissions);
	}

	/**
	 * Returns if the given permission is granted.
	 * @param permission The permission to check.
	 * @return True, if it is granted.
	 */
	public boolean isGranted(ClientPermission permission) {
		return grantedPermissions.contains(permission);
	}
}
