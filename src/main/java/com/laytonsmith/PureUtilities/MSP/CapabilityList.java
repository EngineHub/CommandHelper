package com.laytonsmith.PureUtilities.MSP;

import java.util.HashMap;
import java.util.Map;

/**
 * A capability list is a list of known supported actions by a remote. The remote is free to update this list at any
 * time, and capabilities can have various states.
 *
 */
public class CapabilityList {

	private Map<Capability, CapabilityValue> caps = new HashMap<Capability, CapabilityValue>();
	private Connection connection;

	/**
	 * Creates a new CapabilityList object.
	 *
	 * @param connection The connection to the server. This will be used to automatically determine server capabilites
	 * when required.
	 */
	public CapabilityList(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Clears out this capability list.
	 */
	public void clear() {
		caps.clear();
	}

	public void setCapability(Capability capability, CapabilityValue value) {
		if(value.serverReturnable()) {
			caps.put(capability, value);
		} else {
			throw new RuntimeException("An error occured during runtime, the server returned an invalid capability: " + value);
		}
	}

	/**
	 * Returns true if the server supports this capability. If the capability is dynamic or unknown, it is looked up
	 * from the server.
	 *
	 * @param capability
	 * @return
	 */
	public CapabilityValue hasCapability(Capability capability) {
		CapabilityValue value = caps.get(capability);
		if(value == null) {
			//If the capability is unknown, we need to look it up.
			setCapability(capability, connection.getCapability(capability));
			return hasCapability(capability);
		} else if(value == CapabilityValue.DYNAMIC) {
			//Do a one time lookup
			return connection.getCapability(capability);
		}
		return value;
	}

	/**
	 * A capability is intended to be an enum, but since the valid values may vary from version to version, an interface
	 * is defined instead, which various enum values should implement. All capabilities are required to return a
	 * namespace as well, which, in combination with the name, should uniquely identify this capability. Generally, the
	 * namespace should return the fully qualified class name.
	 */
	public interface Capability {

		/**
		 * The namespace of the capability.
		 *
		 * @return
		 */
		String namespace();

		/**
		 * The name of the capability.
		 *
		 * @return
		 */
		String name();

	}

	public static enum CapabilityValue {
		/**
		 * This capability is always supported by this server, and the client should never request if this is supported
		 * or not. (It still may fail, but that is unexpected). This is generally reserved for core operations, but may
		 * be also used for other requests. If the request fails, it will not automatically switch the value of this
		 * capability, however, the server may still actively change the capability value.
		 */
		ALWAYS_SUPPORTED(true),
		/**
		 * This capability is never supported by this server, and the client should never request if this is supported
		 * or not. (It still may succeed, but that is unexpected). This is generally reserved for the "default" case,
		 * where a server doesn't know about a capability at all. If the client still attempts the operation, and it
		 * succeeds, it will not automatically switch the value of this capability, however, the server may still
		 * actively change the capability value.
		 */
		ALWAYS_UNSUPPORTED(true),
		/**
		 * This capability is supported, but given certain runtime conditions, it may change (for instance, lack of
		 * permissions). If the client attempts the operation and it doesn't succeed, the value will automatically
		 * change to UNSUPPORTED, and the server may actively change this.
		 */
		SUPPORTED(true),
		/**
		 * This capability is unsupported, but given certain runtime conditions, it may change (for instance, elevation
		 * of permissions). If the client forces the operation and it does succeed, the value will change automatically
		 * to SUPPORTED, and the server may actively change this.
		 */
		UNSUPPORTED(true),
		/**
		 * This capability is requested from the server each time it is used, and that new value is temporarily used to
		 * determine support or not.
		 */
		DYNAMIC(true),
		/**
		 * This is the "default" value, that is, if a client does not know if a server supports this capabilty, it works
		 * like {
		 *
		 * @see #DYNAMIC}, but the server's response will be cached.
		 */
		UNKNOWN(false);

		private final boolean serverReturnable;

		private CapabilityValue(boolean serverReturnable) {
			this.serverReturnable = serverReturnable;
		}

		/**
		 * Returns true if the server can return this value during a request for capabilities.
		 *
		 * @return
		 */
		public boolean serverReturnable() {
			return serverReturnable;
		}
	}
}
