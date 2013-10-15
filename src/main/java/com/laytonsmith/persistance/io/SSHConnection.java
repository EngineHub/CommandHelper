package com.laytonsmith.persistance.io;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.SSHWrapper;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.io.IOException;
import java.net.URI;

/**
 *
 * @author lsmith
 */
public class SSHConnection implements ConnectionMixin{
	
	String connection;
	public SSHConnection(URI uri){
		connection = uri.getSchemeSpecificPart();
	}

	public String getData() throws IOException {		
		return StreamUtils.GetString(SSHWrapper.SCPRead(connection));
	}

	public void writeData(DaemonManager dm, String data) throws IOException, UnsupportedOperationException {
		SSHWrapper.SCPWrite(data, connection);
	}

	public String getPath() throws UnsupportedOperationException {
		return connection;
	}
}
