package com.laytonsmith.PureUtilities.MSP;

import com.laytonsmith.PureUtilities.MSP.CapabilityList.Capability;
import com.laytonsmith.PureUtilities.MSP.CapabilityList.CapabilityValue;

/**
 * A Connection object represents some sort of connection to a remote. The
 * basic defined behavior is simply to do a connection, and then gather initial permission
 * and capability lists.
 * 
 */
public interface Connection {
	
	/**
	 * Connects to the remote. If the connection cannot be made, a ConnectionException
	 * is thrown. During the actual connection, if isAttemptSecure returned true,
	 * and the connection is actually insecure, it MUST fail, prior to transmission
	 * of any data.
	 * @throws Connection.ConnectionException
	 */
	public void connect() throws ConnectionException;
	
	/**
	 * Connects to the remote. If the connection cannot be made, a ConnectionException
	 * is thrown. The connection should succeed even if the connection is insecure,
	 * and isAttemptSecure returned true. This should normally not be called without
	 * a user's intervention however.
	 * @throws Connection.ConnectionException
	 */
	public void forceConnection() throws ConnectionException;
	
	/**
	 * Returns true iff a connection attempt to this server is anticipated
	 * to be secure. During the actual connection, if this method returned true,
	 * and the connection is actually insecure, it MUST fail, prior to transmission
	 * of any data.
	 * @return 
	 */
	public boolean isAttemptSecure();
	
	/**
	 * Requests the value of a single capability. This is a blocking
	 * call.
	 * @param capability
	 * @return 
	 */
	public CapabilityValue getCapability(Capability capability);
	
	public static class ConnectionException extends Exception {
		
		public static enum FailureReason{
			UNKNOWN,
			IOEXCEPTION,
			INSUFFICIENT_PERMISSIONS,
			INSECURE;
		}
		
		private final FailureReason reason;
		public ConnectionException(FailureReason reason) {
			super();
			this.reason = reason;
		}

		public ConnectionException(String message, FailureReason reason) {
			super(message);
			this.reason = reason;
		}

		public ConnectionException(Throwable cause, FailureReason reason) {
			super(cause);
			this.reason = reason;
		}

		public ConnectionException(String message, Throwable cause, FailureReason reason) {
			super(message, cause);
			this.reason = reason;
		}
		
		public FailureReason getFailureReason(){
			return this.reason;
		}
		
		
	}
}
