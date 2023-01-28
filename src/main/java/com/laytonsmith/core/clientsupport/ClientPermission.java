package com.laytonsmith.core.clientsupport;

import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.SimpleDocumentation;

/**
 *
 */
public enum ClientPermission {

	FILE_READ("CORE.FILE.READ", ThreatLevel.DANGEROUS, "This permission grants read access to the filesystem outside of the server's local cache.", MSVersion.V3_3_5),
	FILE_WRITE("CORE.FILE.WRITE", ThreatLevel.DANGEROUS, "This permission grants write access to the filesystem outside of the server's local cache.", MSVersion.V3_3_5),
	NETWORK("CORE.NETWORK", ThreatLevel.CAUTIOUS, "This permission grants raw network access. By default, scripts are allowed to"
			+ " communicate with the origin server, on the same url, but other network connections are blocked.", MSVersion.V3_3_5),
	GLOBAL_PERSISTENCE("CORE.GLOBAL_PERSISTENCE", ThreatLevel.CAUTIOUS, "This permission grants access to the global persistence storage."
			+ " This allows reads and writes of other server's data, but cannot be used to harm your computer directly.", MSVersion.V3_3_5),
	ALL("ALL", ThreatLevel.DANGEROUS, "Grants all permissions, giving the server unrestricted access to your computer.", MSVersion.V3_3_5,
		FILE_READ, FILE_WRITE, NETWORK, GLOBAL_PERSISTENCE),
	NONE("NONE", ThreatLevel.TRIVIAL, "This permission is granted automatically.", MSVersion.V3_3_5);

	/**
	 * This is the namespace for the permission
	 */
	private final String[] namespace;

	/**
	 * This is the threat level for the permission
	 */
	private final ThreatLevel threatLevel;
	private final String docs;
	private final MSVersion since;
	/**
	 * If a permission is implied, then it will automatically be granted as well, if the two permissions are in the same
	 * requirement category. For instance, giving CORE.FILE.RAW implies CORE.FILE.READ and CORE.FILE.WRITE, so those two
	 * won't be separately requested. TODO: Provide access to this data
	 */
	private final ClientPermission[] implied;

	private ClientPermission(String permission, ThreatLevel threatLevel, String docs, MSVersion since, ClientPermission... implied) {
		this.namespace = permission.split("\\.");
		this.threatLevel = threatLevel;
		this.docs = docs;
		this.since = since;
		if(implied == null) {
			this.implied = new ClientPermission[]{};
		} else {
			this.implied = implied;
		}
	}

	/**
	 * PackagePermission can be defined by a package to have a certain requirement level. The strings in the manifest
	 * should match one of the values in this enum.
	 */
	public static enum Requirement implements SimpleDocumentation {
		/**
		 * If this permission is not granted, the scripts will not be installed,
		 * and you will be unable to access the server.
		 */
		CRITICAL("If this permission is not granted, the scripts will not be installed, and you will be unable to"
				+ " access the server.",
				MSVersion.V3_3_5),
		/**
		 * If this permission is not granted, large portions of the code will not work, however, the main purpose of the
		 * package will still work fine.
		 */
		IMPORTANT("If this permission is not granted, large portions of the code will not work, however, the main"
				+ " purpose of the package will still work fine.", MSVersion.V3_3_5),
		/**
		 * If this permission is not granted, some features of the server may not work, but all of the main features
		 * will work. User experience may be impacted though.
		 */
		OPTIONAL("If this permission is not granted, some features of the server may not work, but all of the main"
				+ " features will work. User experience may be impacted though.", MSVersion.V3_3_5);

		private final String docs;
		private final MSVersion since;

		private Requirement(String docs, MSVersion since) {
			this.docs = docs;
			this.since = since;
		}

		@Override
		public String getName() {
			return name();
		}

		@Override
		public String docs() {
			return docs;
		}

		@Override
		public MSVersion since() {
			return since;
		}

	}

	/**
	 * Each permission must define its own threat level. This is used by the UI to display various information about the
	 * risk level of granting the permission.
	 */
	public static enum ThreatLevel implements SimpleDocumentation {
		/**
		 * No security risks could be introduced by granting this permission
		 */
		TRIVIAL("No security risks could be introduced by granting this permission, though it is possible"
				+ " for malicious scripts to create system instability, which can be solved by exiting"
				+ " the server, or at worst case, rebooting the computer. It is nonetheless possible"
				+ " that zero day exploits exist.", MSVersion.V3_3_1),
		/**
		 * A malicious package may be able to cause issues for the administrator, but no permanent damage could happen
		 * to the system
		 */
		CAUTIOUS("A malicious script may be able to cause issues for your system,"
				+ " but no permanent damage could happen to the system ", MSVersion.V3_3_1),
		/**
		 * Malicious packages could cause serious damage to a system if this permission is granted. You should be sure
		 * you trust this package before granting this permission.
		 */
		DANGEROUS("Malicious scripts could cause serious damage to your system"
				+ " if this permission is granted. You should be sure you"
				+ " fully trust the server before granting this permission.", MSVersion.V3_3_1);

		private final String docs;
		private final MSVersion since;

		private ThreatLevel(String docs, MSVersion since) {
			this.docs = docs;
			this.since = since;
		}

		@Override
		public String getName() {
			return name();
		}

		@Override
		public String docs() {
			return docs;
		}

		@Override
		public MSVersion since() {
			return since;
		}

	}
}
