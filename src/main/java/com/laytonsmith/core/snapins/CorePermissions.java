package com.laytonsmith.core.snapins;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.CHVersion;
import java.net.URL;

/**
 *
 * @author lsmith
 */
public enum CorePermissions implements PackagePermission {
	FILE_READ("CORE.FILE.READ", ThreatLevel.CAUTIOUS, "This permission grants read access to the filesystem, via the VFS only,"
			+ " therefore making it relatively safe compared to raw filesystem access.", CHVersion.V3_3_1),
	FILE_WRITE("CORE.FILE.WRITE", ThreatLevel.CAUTIOUS, "This permission grants write access to the filesystem, via the VFS only,"
			+ " therefore making it relatively safe compared to raw filesystem access.", CHVersion.V3_3_1),
	FILE_RAW("CORE.FILE.RAW", ThreatLevel.DANGEROUS, "This permission grants raw read/write access to the filesystem, outside of the"
			+ " VFS, which on most systems could allow for full control of the system", CHVersion.V3_3_1, new PackagePermission[]{FILE_READ, FILE_WRITE}),
	NETWORK("CORE.NETWORK", ThreatLevel.CAUTIOUS, "This permission grants raw network access. If filesystem access is not allowed,"
			+ " this is a relatively safe permission to allow, but can be misused by malicious code to transmit sensitive data.", CHVersion.V3_3_1),
	GLOBAL_PERSISTANCE("CORE.GLOBAL_PERSISTANCE", ThreatLevel.TRIVIAL, "This permission grants access to the global persistance storage."
			+ " This generally cannot be abused, other than to delete other package's data.", CHVersion.V3_3_1),
	;
	
	/**
	 * This is the namespace for the permission
	 */
	private final String[] namespace;
	/**
	 * This is the threat level for the permission
	 */
	private final ThreatLevel threatLevel;
	private final String docs;
	private final CHVersion since;
	/**
	 * If a permission is implied, then it will automatically be granted
	 * as well, if the two permissions are in the same requirement category.
	 * For instance, giving CORE.FILE.RAW implies CORE.FILE.READ and CORE.FILE.WRITE,
	 * so those two won't be separately requested.
	 * TODO: Provide access to this data
	 */
	private final PackagePermission[] implied;
	
	private CorePermissions(String permission, ThreatLevel threatLevel, String docs, CHVersion since) {
		this(permission, threatLevel, docs, since, (PackagePermission[])null);
	}
	
	private CorePermissions(String permission, ThreatLevel threatLevel, String docs, CHVersion since, PackagePermission implied){
		this(permission, threatLevel, docs, since, new PackagePermission[]{implied});
	}
	
	private CorePermissions(String permission, ThreatLevel threatLevel, String docs, CHVersion since, PackagePermission [] implied){
		this.namespace = permission.split("\\.");
		this.threatLevel = threatLevel;
		this.docs = docs;
		this.since = since;
		if(implied == null){
			this.implied = new PackagePermission[]{};
		} else {
			this.implied = implied;
		}
	}

	public String[] getNamespace() {
		String[] array = new String[namespace.length];
		System.arraycopy(namespace, 0, array, 0, array.length);
		return array;
	}

	public ThreatLevel getThreatLevel() {
		return threatLevel;
	}

	public String getName() {
		return StringUtils.Join(namespace, ".");
	}

	public String docs() {
		return docs;
	}

	public CHVersion since() {
		return since;
	}

	@Override
	public URL getSourceJar() {
		return null;
	}
	
}
