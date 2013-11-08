package com.laytonsmith.core.snapins;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import java.net.URL;

/**
 * This should be implemented by permission enums. This provides a standard way
 * to get permission information, without tying them down to a single enum class,
 * so extensions can later provide different permissions, while 
 * @author Layton
 */
public interface PackagePermission extends Documentation {
	
	/**
	 * This is a specially defined PackagePermission that should be used by
	 * code that has no security implications, if a permission is required.
	 * During permission checks, if this permission is returned, then no
	 * permissions are requested from the installation.
	 */
	public static final PackagePermission NO_PERMISSIONS_NEEDED = new PackagePermission() {

		public String[] getNamespace() {
			return null;
		}

		public ThreatLevel getThreatLevel() {
			return null;
		}

		public String getName() {
			return null;
		}

		public String docs() {
			return null;
		}

		public CHVersion since() {
			return null;
		}

		@Override
		public URL getSourceJar() {
			return null;
		}
		
	};
	
	/**
	 * Returns the name of the permission. Namespaces are supported, which
	 * are may be displayed in a tree hierarchy in the UI, to allow for
	 * quicker selections.
	 * @return 
	 */
	String [] getNamespace();
	
	/**
	 * The threat level of this permission.
	 * @return 
	 */
	ThreatLevel getThreatLevel();
	
	/**
	 * This should return getNamespace()[getNamespace().length - 1].
	 * @return 
	 */
	String getName();
	
	/**
	 * PackagePermission can be defined by a package to have a certain requirement level.
	 * The strings in the manifest should match one of the values in this enum.
	 */
	public static enum Requirement implements Documentation {
		/**
		 * If this permission is not granted, the package will not be installed
		 */
		CRITICAL("If this permission is not granted, the package will not be installed", 
				CHVersion.V3_3_1),
		/**
		 * If this permission is not granted, large portions of the code will not work, however, the main purpose
		 * of the package will still work fine.
		 */
		IMPORTANT("If this permission is not granted, large portions of the code will not work, however, the main purpose"
				+ "of the package will still work fine.", CHVersion.V3_3_1),
		/**
		 * If this permission is not granted, some features of the package may not work, but all of the main features will work.
		 * User experience may be impacted though.
		 */
		OPTIONAL("If this permission is not granted, some features of the package may not work, but all of the main features will work."
				+ " User experience may be impacted though.", CHVersion.V3_3_1);

		private String docs;
		private CHVersion since;
		private Requirement(String docs, CHVersion since){
			this.docs = docs;
			this.since = since;
		}
		public String getName() {
			return name();
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
	
	/**
	 * Each permission must define its own threat level. This is used by the UI
	 * to display various information about the 
	 */
	public static enum ThreatLevel implements Documentation{
		/**
		 * No security risks could be introduced by granting this permission 
		 */
		TRIVIAL("No security risks could be introduced by granting this permission ", CHVersion.V3_3_1),
		
		/**
		 * A malicious package may be able to cause issues for the administrator, 
		 * but no permanent damage could happen to the system 
		 */
		CAUTIOUS("A malicious package may be able to cause issues for the administrator,"
				+ " but no permanent damage could happen to the system ", CHVersion.V3_3_1),
		/**
		 * Malicious packages could cause serious damage to a system if this
		 * permission is granted. You should be sure you trust this package 
		 * before granting this permission. 
		 */
		DANGEROUS("Malicious packages could cause serious damage to a system"
				+ " if this permission is granted. You should be sure you"
				+ " trust this package before granting this permission. ", CHVersion.V3_3_1);

		private String docs;
		private CHVersion since;
		private ThreatLevel(String docs, CHVersion since){
			this.docs = docs;
			this.since = since;
		}
		public String getName() {
			return name();
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
}
