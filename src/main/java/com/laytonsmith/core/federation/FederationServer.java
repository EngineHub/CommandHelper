package com.laytonsmith.core.federation;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A FederationServer represents a server that is listening for remote connections.
 * Once a connection is established, a "sub socket" will be created, and that represents
 * a single connection to the client.
 */
public class FederationServer {
	private final String serverName;
	private final String password;
	private final File authorizedKeys;
	private final String allowFrom;
	private final int masterPort;
	private final Map<Socket, Long> subSockets = new HashMap<>();
	private ServerSocket serverSocket;
	private boolean closed = false;

	public FederationServer(String serverName, String password, File authorizedKeys, String allowFrom, int masterPort) {
		this.serverName = serverName;
		this.password = password;
		this.authorizedKeys = authorizedKeys;
		this.allowFrom = allowFrom;
		this.masterPort = masterPort;
	}

	/**
	 * Sets the ServerSocket that this server is listening on.
	 * @param serverSocket 
	 */
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	/**
	 * Gets the ServerSocket that this server is listening on.
	 * @return 
	 */
	public ServerSocket getServerSocket() {
		return this.serverSocket;
	}

	/**
	 * Returns the name of this server.
	 * @return 
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Returns the server password. If null, the server has no password.
	 * @return 
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns a link to the authorized keys file. This is a list of client
	 * keys that are allowed to connect to this server.
	 * @return 
	 */
	public File getAuthorizedKeys() {
		return authorizedKeys;
	}

	/**
	 * Returns the allow from string. This is a comma separated list of the IP
	 * or hostnames that are allowed.
	 * @return 
	 */
	public String getAllowFrom() {
		return allowFrom;
	}

	/**
	 * Returns the master port. This is NOT the same port as the one the server
	 * is currently listening on however, this is simply the master port for this
	 * server's federation.
	 * @return 
	 */
	public int getMasterPort() {
		return masterPort;
	}

	/**
	 * A sub socket is a list of sockets that are currently connected to this
	 * Server Socket.
	 *
	 * @param s
	 */
	public void addSubSocket(Socket s) {
		subSockets.put(s, System.currentTimeMillis());
	}

	/**
	 * Removes the sub socket from this server.
	 *
	 * @param s
	 */
	public void removeSubSocket(Socket s) {
		subSockets.remove(s);
	}
	
	/**
	 * Listens for connections. Each connection spawns a new thread. This method
	 * blocks until the server socket is closed.
	 * @throws java.io.IOException
	 */
	public void listenForConnections() throws IOException{
		while(!serverSocket.isClosed()){
			final Socket s = serverSocket.accept();
			addSubSocket(s);
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						FederationCommunication communicator = new FederationCommunication(
								new BufferedInputStream(s.getInputStream()),
								new BufferedOutputStream(s.getOutputStream()));

						// Before we verify that the connection is allowed, we want
						// to limit the amount of resources this can take. The
						// HELLO needs to complete relatively quickly, or it will
						// forcibly close the socket. This will prevent bad connections
						// from piling up.
						Thread connectionWatcher = new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(1000);
									// We weren't interrupted within the grace
									// period, which means the connection is taking
									// too long. Forcibly terminate it.
									try {
										if(s.isConnected()){
											s.close();
										}
									} catch (IOException ex) {
										Logger.getLogger(FederationServer.class.getName()).log(Level.SEVERE, null, ex);
									}
								} catch (InterruptedException ex) {
									// Ok, it's good. The connection may now
									// idle without risk of being killed.
								}
							}
						}, "FederationServerConnectionWatcher-" + s.hashCode());
						connectionWatcher.start();
						String hello = communicator.readUnencryptedLine();
						if(!"HELLO".equals(hello)){
							// Bad message. Close the socket immediately, and return.
							s.close();
							connectionWatcher.interrupt();
							return;
						}
						// Ohhai
						// Now get the version...
						FederationVersion version;
						String sVersion = communicator.readUnencryptedLine();
						try {
							version = FederationVersion.fromVersion(sVersion);
							communicator.writeUnencryptedLine("VERSION OK");
						} catch(IllegalArgumentException ex){
							// The version is unsupported. The client is newer than this server knows how
							// to deal with. So, write out the version error data, then close the socket and
							// continue.
							communicator.writeUnencryptedLine("VERSION BAD");
							byte [] errorMsg = ("The server does not support the version of this client (" + sVersion + ")!").getBytes("UTF-8");
							communicator.writeUnencryptedLine(Integer.toString(errorMsg.length));
							communicator.writeUnencrypted(errorMsg);
							s.close();
							connectionWatcher.interrupt();
							return;
						}
						// The rest of the code may vary based on the version.
						if(version == FederationVersion.V1_0_0){
							// Are we encrypted?
							// TODO: This is currently unused
							boolean isEncrypted = "1".equals(communicator.readUnencryptedLine());
							// The rest of the data is sent possibly encrypted, so we use
							// the normal form of the rest of these.
							String clientPassword = communicator.readLine();
							if(password != null){
								// If the password is required by the server, we need to
								// verify they got it correct. If it's not required, they
								// were going to send a line anyways.
								if(!password.equals(clientPassword)){
									// Oops, wrong guess, try again...
									communicator.writeLine("ERROR");
									byte [] errorMsg = ("Wrong password").getBytes("UTF-8");
									communicator.writeLine(Integer.toString(errorMsg.length));
									communicator.writeBytes(errorMsg);
									s.close();
									connectionWatcher.interrupt();
									return;
								}
							}
							// Alright, we're connected!
							communicator.writeLine("OK");
							// We now allow the connection to idle.
							connectionWatcher.interrupt();
							
						}
						
						
					} catch (IOException ex) {
						Logger.getLogger(FederationServer.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}, "FederationServer-" + serverName + "-Connection " + subSockets.size()).start();
		}
	}

	/**
	 * Closes and removes all sub sockets that have been inactive for X minutes.
	 *
	 * @param inactiveMS
	 */
	public void checkSocketTimeouts(long inactiveMS) {
		Iterator<Socket> it = subSockets.keySet().iterator();
		while (it.hasNext()) {
			Socket s = it.next();
			if (subSockets.get(s) < System.currentTimeMillis() - inactiveMS) {
				try {
					s.close();
				} catch (IOException ex) {
					Logger.getLogger(com.laytonsmith.core.functions.Federation.class.getName()).log(Level.SEVERE, null, ex);
				}
				it.remove();
			}
		}
	}

	/**
	 * Updates the activity of this socket.
	 *
	 * @param s
	 */
	public void updateSocketActivity(Socket s) {
		if (subSockets.containsKey(s)) {
			subSockets.put(s, System.currentTimeMillis());
		}
	}
	
	/**
	 * Closes all the sockets this server is currently using. This includes the
	 * sub sockets that have been spawned off, as well as the server socket. Once
	 * this is called, the reference to the FederationServer object should be
	 * lost, as the object becomes useless at that point.
	 */
	public void closeAllSockets(){
		try {
			serverSocket.close();
		} catch (IOException ex) {
			Logger.getLogger(FederationServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		for(Socket sub : subSockets.keySet()){
			try {
				sub.close();
			} catch (IOException ex) {
				Logger.getLogger(FederationServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		closed = true;
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		super.finalize();
		if(!closed){
			StreamUtils.GetSystemErr().println("FederationServer was not closed properly, and cleanup is having to be done in the finalize method!");
			closeAllSockets();
		}
	}

}
