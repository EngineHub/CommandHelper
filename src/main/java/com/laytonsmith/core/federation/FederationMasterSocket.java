package com.laytonsmith.core.federation;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages the master socket connection. If there are multiple server
 * names within the same process, there only needs to be one master socket, and
 * then, there only needs to be a master socket if no other process on the
 * machine has the master socket for a given port. This class manages having a
 * master socket running for each listening port, but only if needed.
 */
public class FederationMasterSocket {
	private static FederationMasterSocket defaultInstance;

	/**
	 * Creates, if needed, the default instance of the master socket.
	 *
	 * @return
	 */
	public static FederationMasterSocket getFederationMasterSocket() {
		if (defaultInstance == null) {
			defaultInstance = new FederationMasterSocket();
		}
		return defaultInstance;
	}

	/**
	 * Clears the default instance of the master socket, and shuts down the old
	 * one if it was already created, and running.
	 */
	public static void clearFederationMasterSocket() {
		if (defaultInstance != null) {
			defaultInstance.closeAll();
			defaultInstance = null;
		}
	}

	private FederationMasterSocket() {
		//
	}

	private Map<Integer, ServerSocket> servers;
	private boolean closed = false;

	/**
	 * Closes all the master sockets, for all open ports.
	 */
	public void closeAll() {
		for (ServerSocket socket : servers.values()) {
			try {
				socket.close();
				closed = true;
			} catch (IOException ex) {
				Logger.getLogger(FederationMasterSocket.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Closes the master socket for the given port, but only that one. If there
	 * is no master socket on that port, nothing happens, and false is returned.
	 * If it was closed successfully, true is returned.
	 *
	 * @param port
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean close(int port) throws IOException {
		if (servers.containsKey(port)) {
			servers.get(port).close();
			servers.remove(port);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Ensures the master socket is open for the given port. If it is already up
	 * and running, nothing happens.
	 *
	 * @param pn The persistence network, for finding the registration of a particular server.
	 * @param port The port the master socket should be listening on.
	 * @throws java.io.IOException
	 */
	public void ensureMasterSocketOpen(final PersistenceNetwork pn, int port) throws IOException {
		if (!servers.containsKey(port) || servers.get(port).isClosed()) {
			if (Federation.available(port)) {
				// We're the first server to register on this port, so
				// we need to actually start it up.
				final ServerSocket masterSocket = new ServerSocket(port);
				servers.put(port, masterSocket);
				new Thread(new Runnable() {

					@Override
					public void run() {
						while (true) {
							try {
								final Socket s = masterSocket.accept();
								Thread connectionWatcher = new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											Thread.sleep(1000);
											// We weren't interrupted within the grace
											// period, which means the connection is taking
											// too long. Forcibly terminate it.
											try {
												if (s.isConnected()) {
													s.close();
												}
											} catch (IOException ex) {
												Logger.getLogger(FederationServer.class.getName()).log(Level.SEVERE, null, ex);
											}
										} catch (InterruptedException ex) {
											// Ok, it's good.
										}
									}
								}, "FederationMasterSocketConnectionWatcher-" + s.hashCode());
								connectionWatcher.start();

								FederationCommunication communicator = new FederationCommunication(new BufferedInputStream(s.getInputStream()),
										new BufferedOutputStream(s.getOutputStream()));
								String hello = communicator.readUnencryptedLine();
								if (!"HELLO".equals(hello)) {
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
								} catch (IllegalArgumentException ex) {
									// The version is unsupported. The client is newer than this server knows how
									// to deal with. So, write out the version error data, then close the socket and
									// continue.
									communicator.writeUnencryptedLine("VERSION BAD");
									byte[] errorMsg = ("The server does not support the version of this client (" + sVersion + ")!").getBytes("UTF-8");
									communicator.writeUnencryptedLine(Integer.toString(errorMsg.length));
									communicator.writeUnencrypted(errorMsg);
									s.close();
									connectionWatcher.interrupt();
									return;
								}
								// The rest of the code may vary based on the version.
								if (version == FederationVersion.V1_0_0) {
									String command = communicator.readUnencryptedLine();
									if ("GET PORT".equals(command)) {
										String serverName = communicator.readUnencryptedLine();
										String value = pn.get(new String[]{"federation", serverName});
										if(value != null){
											FederationRegistration reg = FederationRegistration.fromJSON(value);
											if(reg.updatedSince(Federation.DEAD_SERVER_TIMEOUT)){
												int port = reg.getPort();
												communicator.writeLine("OK");
												communicator.writeLine(Integer.toString(port));
												s.close();
												connectionWatcher.interrupt();
												return;
											}
										}
										byte[] errorMsg = ("The server \"" + serverName + "\" could not be found on this host.").getBytes("UTF-8");
										communicator.writeUnencryptedLine("ERROR");
										communicator.writeUnencryptedLine(Integer.toString(errorMsg.length));
										communicator.writeUnencrypted(errorMsg);
										s.close();
										connectionWatcher.interrupt();
									} else {
										// Programming error.
										s.close();
										connectionWatcher.interrupt();
									}
								}

							} catch (IOException | DataSourceException ex) {
								Logger.getLogger(FederationMasterSocket.class.getName()).log(Level.SEVERE, null, ex);
							}

						}
					}
				}, "FederationMasterSocket-Port " + port).start();
			}
		}
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable {
		super.finalize();
		if (!closed) {
			StreamUtils.GetSystemErr().println("FederationMasterSocket was not closed properly, and cleanup is having to be done in the finalize method!");
			closeAll();
		}
	}

}
