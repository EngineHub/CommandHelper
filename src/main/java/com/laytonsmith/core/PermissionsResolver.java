package com.laytonsmith.core;

/**
 * A permissions resolver resolves whether a particular user (stored as a string) has
 * various permissions to do certain things, as well as polling for information
 * about the user.
 * 
 */
public interface PermissionsResolver {
	
	public static final String GLOBAL_PERMISSION = "*";
	
	/**
	 * Returns true if this user is in the specified group.
	 * @param user
	 * @param group
	 * @return 
	 */
	boolean inGroup(String user, String group);
	
	/**
	 * Returns true if the user has the specified permission. <code>data</code>
	 * is used by the particular instance if more data is needed.
	 * @param user
	 * @param permission
	 * @param data
	 * @return 
	 */
	boolean hasPermission(String user, String permission, Object data);
	
	/**
	 * Returns true if the user has the specified permission.
	 * @param user
	 * @param permission
	 * @return 
	 */
	boolean hasPermission(String user, String permission);
	
	/**
	 * Returns a list of groups the user is in.
	 * @param user
	 * @return 
	 */
	String[] getGroups(String user);
	
	/**
	 * A very permissive resolver, which always returns true for hasPermission.
	 * The "user" isn't in any groups.
	 */
	public static class PermissiveResolver implements PermissionsResolver{

		@Override
		public boolean inGroup(String user, String group) {
			return false;
		}

		@Override
		public boolean hasPermission(String user, String permission, Object data) {
			return true;
		}

		@Override
		public boolean hasPermission(String user, String permission) {
			return true;
		}				

		@Override
		public String[] getGroups(String user) {
			return new String[]{};
		}
		
	}
}
