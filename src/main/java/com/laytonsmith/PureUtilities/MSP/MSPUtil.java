package com.laytonsmith.PureUtilities.MSP;

/**
 * Contains static utility methods for the MSP package.
 */
public class MSPUtil {
	
	private MSPUtil(){}

	public static String getCapabilityName(CapabilityList.Capability capability){
		return capability.namespace() + "." + capability.name();
	}
}
