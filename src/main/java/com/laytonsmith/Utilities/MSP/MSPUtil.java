package com.laytonsmith.Utilities.MSP;

/**
 * Contains static utility methods for the MSP package.
 *
 */
public final class MSPUtil {

	private MSPUtil() {
	}

	public static String getCapabilityName(CapabilityList.Capability capability) {
		return capability.namespace() + "." + capability.name();
	}
}
