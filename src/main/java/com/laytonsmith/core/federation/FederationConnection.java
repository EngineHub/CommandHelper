package com.laytonsmith.core.federation;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A FederationConnection represents the client side of a FederationConnection. All
 * reads and writes should go through this class. The underlying socket is managed
 * via this class. If the socket isn't connected, 
 */
public final class FederationConnection {
	
	/**
	 * While the server may need to support multiple Federation protocol versions,
	 * the client does not. We always use this version through this class.
	 */
	private static final String version = FederationVersion.V1_0_0.getVersionString();
	
	private final String serverName;
	private final String host;
	private final int masterPort;
	private final int timeout;
	private final String password;
	private Socket socket;
	private final Object socketLock = new Object();
	private long lastUpdated;
	
	private boolean closed = false;
	
	private final boolean isEncrypted;
	
	private FederationCommunication communicator;
	

	/**
	 * Constructs a new FederationConnection.
	 * @param serverName The server name.
	 * @param host The host that this should connect to.
	 * @param masterPort The master port that this should connect to.
	 * @param timeout The timeout for this connection.
	 */
	public FederationConnection(String serverName, String host, int masterPort, int timeout, String password) {
		this.serverName = serverName;
		this.host = host;
		this.masterPort = masterPort;
		this.timeout = timeout;
		this.password = password;
		//TODO: Once encryption is added, make this dynamic.
		isEncrypted = false;
	}

	/**
	 * The server name that this connection is to
	 * @return 
	 */
	public String getServerName() {
		return serverName;
	}	
	
	/**
	 * Write a line to the connection, but first, ensures the connection is
	 * valid. \n is considered to be the line ending character, not \r or
	 * \n\r.
	 * @param line 
	 * @throws java.io.IOException 
	 */
	public void writeLine(String line) throws IOException{
		ensureConnected();
		communicator.writeLine(line);
	}
	
	/**
	 * Reads a line from the connection, but first, ensures the connection is
	 * valid. \n is considered to be the line ending character, not \r or \n\r.
	 * @return The string line
	 * @throws java.io.IOException 
	 */
	public String readLine() throws IOException{
		ensureConnected();
		return communicator.readLine();
	}
	
	/**
	 * Writes raw bytes to the connection, but first, ensures the connection
	 * is valid.
	 * @param bytes 
	 * @throws java.io.IOException 
	 */
	public void writeBytes(byte[] bytes) throws IOException{
		ensureConnected();
		communicator.writeBytes(bytes);
	}
	
	/**
	 * Reads raw bytes from the connection, but first, ensures the connection
	 * is valid.
	 * @param size The number of bytes to read.
	 * @return A byte array, of size {@code size}.
	 * @throws java.io.IOException
	 */
	public byte[] readBytes(int size) throws IOException{
		ensureConnected();
		return communicator.readBytes(size);
	}
	
	/**
	 * Forces the connection to be established, if it isn't already. This method
	 * blocks until it is established (or fails, and it throws an exception).
	 * @throws IOException If the socket could not connect
	 * @throws SocketTimeoutException If the socket tried to connect, but the connection
	 * timed out.
	 */
	public synchronized void ensureConnected() throws IOException, SocketTimeoutException {
		if(socket == null || !socket.isConnected()){
			Socket masterSocket = new Socket();
			masterSocket.connect(new InetSocketAddress(host, masterPort), timeout);
			// Handshake with the server to get the correct port that we need to actually
			// connect on. There is no security required for this, as the connection security
			// deals with a connection to the final server, not whichever server the master socket
			// is running on.
			int port = -1;
			try(
				BufferedReader reader = new BufferedReader(new InputStreamReader(masterSocket.getInputStream(), "UTF-8"));
				PrintWriter out = new PrintWriter(masterSocket.getOutputStream(), true);	
			){
				out.println("HELLO");
				out.println(version);
				String versionOK = reader.readLine();
				if(!"VERSION OK".equals(versionOK)){
					// (If we get here, the message would have been VERSION BAD
					// Ok, what was the error?
					int errorMsgSize = Integer.parseInt(reader.readLine());
					byte[] errorMsg = new byte[errorMsgSize];
					masterSocket.getInputStream().read(errorMsg);
					String sErrorMsg = new String(errorMsg, "UTF-8");
					throw new IOException(sErrorMsg);
				}
				out.println("GET PORT");
				out.println(serverName);
				String portOK = reader.readLine();
				if(null != portOK)switch (portOK) {
					case "OK":
						try {
							port = Integer.parseInt(reader.readLine());
						} catch (NumberFormatException ex){
							//
						}	
						break;
					case "ERROR":
						int errorMsgSize = Integer.parseInt(reader.readLine());
						byte[] errorMsg = new byte[errorMsgSize];
						masterSocket.getInputStream().read(errorMsg);
						String sErrorMsg = new String(errorMsg, "UTF-8");
						throw new IOException(sErrorMsg);
				}
			}
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), timeout);
			// Ok, we have the correct port to connect to on this server, so reconnect with that now.
			communicator = new FederationCommunication(new BufferedInputStream(socket.getInputStream()),
				new BufferedOutputStream(socket.getOutputStream()));
			
			// Alright, the socket is connected to the correct server, so let's handshake with it.
			
			
			// These are the only bits that are never encrypted.
			// A HELLO is the first thing we ever send.
			communicator.writeUnencryptedLine("HELLO");
			// Version is next, then whether or not we're encrypting things.
			communicator.writeUnencryptedLine(version);
			String versionOK = communicator.readLine();
			if(!"VERSION OK".equals(versionOK)){
				// (If we get here, the message would have been VERSION BAD
				// Ok, what was the error?
				int errorMsgSize = Integer.parseInt(communicator.readLine());
				String errorMsg = new String(communicator.readBytes(errorMsgSize), "UTF-8");
				throw new IOException(errorMsg);
			}
			communicator.writeUnencryptedLine(isEncrypted?"1":"0");
			
			// The rest of the connection needs to be encoded, potentially, so we
			// use the other communicator methods.
			
			// Write out the server password. If it is null, write an empty string.
			communicator.writeLine(password == null ? "" : password);
			
			String response = communicator.readLine();
			if(null != response)switch (response) {
				case "OK":
					// The handshake succeeded, so we can return.
					return;
				case "ERROR":
					// The handshake did NOT succeed, so we need to read in the error, and
					// throw an exception.
					int errorMsgSize = Integer.parseInt(communicator.readLine());
					String errorMsg = new String(communicator.readBytes(errorMsgSize), "UTF-8");
					throw new IOException(errorMsg);
			}
		}
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		super.finalize();
		if (!closed) {
			StreamUtils.GetSystemErr().println("FederationConnection was not closed properly, and cleanup is having to be done in the finalize method!");
			closeSocket();
		}
	}

	/**
	 * Closes the underlying socket.
	 */
	public void closeSocket() {
		try {
			if(communicator != null){
				communicator.close();
			}
			
			socket.close();
		} catch (IOException ex) {
			Logger.getLogger(FederationConnection.class.getName()).log(Level.SEVERE, null, ex);
		}

		socket = null;
		closed = true;
	}
	
	/**
	 * The socket lock. This should be synchronized on while any socket
	 * communication session is occurring, to ensure that the socket will only
	 * be used by one thread at a time.
	 *
	 * @return
	 */
	public Object getSocketLock() {
		return socketLock;
	}

	/**
	 * Closes the socket if it has been inactive for {@code ms} milliseconds. If
	 * the socket is closed in this way, true is returned. Otherwise, if the
	 * socket will remain open, false is returned.
	 *
	 * @param ms
	 * @return
	 */
	public boolean closeIfInactive(long ms) {
		if (lastUpdated < System.currentTimeMillis() - ms) {
			return false;
		}
		closeSocket();
		return false;
	}

	private void updateLastUpdated() {
		this.lastUpdated = System.currentTimeMillis();
	}

}
