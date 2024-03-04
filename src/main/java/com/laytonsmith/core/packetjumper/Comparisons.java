package com.laytonsmith.core.packetjumper;

/**
 *
 */
public final class Comparisons {

	private Comparisons() {
	}

	/**
	 * Returns true if the methodscript object is effectively equal to the packet object. This is not necessarily a
	 * completely straightforward equals check as the values are both boxed and sometimes of disparate types, such as
	 * Long and Integer, which requires more detailed handling.
	 *
	 * @param msObject
	 * @param packetObject
	 * @return
	 */
	public static boolean IsEqual(Object msObject, Object packetObject) {
		if(msObject instanceof Long msNumber && packetObject instanceof Number packetNumber) {
			return msNumber == packetNumber.longValue();
		}
		if(msObject instanceof Double msDouble && packetObject instanceof Number packetNumber) {
			return java.lang.Math.abs(msDouble - packetNumber.doubleValue()) < 0.0001;
		}
		if(packetObject == null || msObject == null) {
			return msObject == packetObject;
		}
		return msObject == packetObject || msObject.equals(packetObject);
	}
}
