package com.laytonsmith.PureUtilities.MSP;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import java.net.URL;

/**
 * A Burst is a single transmission from client to server, or server to client.
 * @author lsmith
 */
public class Burst {
	
	BurstType type;
	String id;
	String value;
	//TODO: Is this type right?
	String valueChecksum;
	String rider;
	//TODO: Is this type right?
	String riderChecksum;
	
	public static enum BurstType implements Documentation {
		@RemoteCapability()
		META("Provides meta information to the client/server. The payload will be a json with further information", CHVersion.V3_3_1),
		@RemoteCapability()
		FUNCTION("This is a static function/procedure call. No rider information is provided, but the payload will"
				+ " be a json array, with [0] being the fully qualified function name, and [1..] being the json"
				+ " encoded arguments.", CHVersion.V3_3_1),
		@RemoteCapability()
		METHOD("This is an instance based method call. The rider will be the json encoded object that this method"
				+ " is being called on, and the payload will be the same as " + FUNCTION.name() + "'s payload.", CHVersion.V3_3_1),
		@RemoteCapability()
		RESPONSE("This is a response from a previous call. The payload will be the json encoded response.", CHVersion.V3_3_1),
		@RemoteCapability()
		VOID("This is a response from a previous call, but the function/procedure/method returned void. This"
				+ " response is simply to inform the client/server that the response succeeded, while minimizing"
				+ " the data transmitted", CHVersion.V3_3_1),
		@RemoteCapability()
		EXCEPTION("This is a response from a previous call, but the function/procedure/method returned with an exception."
				+ " The payload will be the exception type, and the rider will be the exception message", CHVersion.V3_3_1),
		@RemoteCapability()
		ERROR("While handling the request, the remote failed unexpectedly. The payload will simply contain error information"
				+ " in an unspecified format, which is intended to be helpful, but should not typically be shown to the"
				+ " end user.", CHVersion.V3_3_1)
		;
		
		String doc;
		CHVersion version;
		private BurstType(String doc, CHVersion version){
			this.doc = doc;
			this.version = version;
		}

		public String getName() {
			return name();
		}

		public String docs() {
			return doc;
		}

		public CHVersion since() {
			return version;
		}
		
		@Override
		public URL getSourceJar() {
			return null;
		}
	}
}
