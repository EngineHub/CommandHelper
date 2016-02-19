package com.laytonsmith.persistence.io;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.SSHWrapper;

import java.io.IOException;
import java.net.URI;

/**
 *
 * 
 */
public class SSHConnection implements ConnectionMixin{
	
	String connection;
	public SSHConnection(URI uri){
		connection = uri.getSchemeSpecificPart();
	}

	@Override
	public String getData() throws IOException {		
		return StreamUtils.GetString(SSHWrapper.SCPRead(connection));
	}

	@Override
	public void writeData(DaemonManager dm, String data) throws IOException, UnsupportedOperationException {
		SSHWrapper.SCPWrite(data, connection);
	}

	@Override
	public String getPath() throws UnsupportedOperationException {
		return connection;
	}
}
