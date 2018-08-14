package com.laytonsmith.tools.docgen.sitedeploy;

import com.laytonsmith.Utilities.SSHWrapper;
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
	public boolean deploy(InputStream data, String toLocation) throws IOException {
		return SSHWrapper.SCPWrite(data, remote + toLocation);
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
