package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.PureUtilities.SSHWrapper;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author cailin
 */
class RemoteDeploymentMethod implements DeploymentMethod {

	String remote;

	public RemoteDeploymentMethod(String remote) {
		this.remote = remote;
	}

	@Override
	public boolean deploy(InputStream data, String toLocation, String overrideIdRsa) throws IOException {
		// TODO We actually are assuming the system we're connecting to is linux, however, that may not
		// be true. We should have two RemoteDeploymentMethods for each OS so that we can support
		// either, and add an option to be able to override that if the remote system is not Linux.
		// There's no real way for us to guess the OS though, so we can't do that automatically. But
		// linux is a super good guess if we're using ssh. However, swapping to an rsync method may
		// make this moot anyways.
		toLocation = toLocation.replace("\\", "/");
		return SSHWrapper.SCPWrite(data, remote + toLocation, overrideIdRsa);
	}

	@Override
	public void finish() {
		SSHWrapper.closeSessions();
	}

	@Override
	public String getID() {
		return remote;
	}

}
