package com.laytonsmith.core.federation;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the POJO functionality for the Federation system.
 */
public class Federation {

	private static Federation defaultFederation;

	/**
	 * Returns the default Federation object. If one isn't yet constructed, it is constructed and returned.
	 *
	 * @return
	 */
	public static Federation GetFederation() {
		if(defaultFederation == null) {
			defaultFederation = new Federation();

		}
		return defaultFederation;
	}

	/**
	 * Clears the default Federation object. If {@link #GetFederation()} is called after this, a new one will be
	 * constructed.
	 */
	public static void ClearFederation() {
		defaultFederation = null;
	}

	/**
	 * Default master port
	 */
	public static final int MASTER_PORT = 55423;
	/**
	 * Minimum slave server port number
	 */
	public static final int DYNAMIC_PORT_MINIMUM = 55424;
	/**
	 * Maximum slave server port number
	 */
	public static final int DYNAMIC_PORT_MAXIMUM = 56423;
	/**
	 * The timeout after which a server is considered "dead", that is, it's heartbeat hasn't connected in this many
	 * seconds.
	 */
	public static final int DEAD_SERVER_TIMEOUT = 10;
	/**
	 * The interval between heartbeats.
	 */
	public static final int HEARTBEAT_INTERVAL = 5;

	/**
	 * When this is 0, the shutdown hooks will de-register with the DaemonManager.
	 */
	private final int serverCount = 0;
	private final Object serverCountLock = new Object();

	private final Map<String, FederationConnection> federationConnections = new HashMap<>();
	private final Map<String, FederationServer> federationServers = new HashMap<>();
	private final Socket masterSocket = null;

//	/**
//	 * If the master socket dies, we need to establish a new one. This method
//	 * checks to see if the master server for the specified master port is up
//	 * and running, and if not, starts it. Regardless, the ServerSocket is
//	 * returned, and it is guaranteed to be running and waiting for connections
//	 * once this method returns.
//	 *
//	 * @param pn
//	 * @param dm
//	 * @param server_name If we end up needing to start a master socket, it does
//	 * double duty as both the master socket and the slave socket. In that case,
//	 * this is used, otherwise, it is ignored.
//	 * @param master_port
//	 * @return
//	 */
//	public synchronized void StartMasterSocket(final PersistenceNetwork pn, DaemonManager dm, String server_name, int master_port) {
//		if(!available(master_port)) {
//			//Something is already listening on this port, so we can assume the master socket is running.
//			return;
//		}
//		// Otherwise, it's not started, so start it, and then return.
//		final ServerSocket masterSocket;
//		try {
//			masterSocket = new ServerSocket(master_port);
//			AddShutdownHook(pn, dm, masterSocket, server_name);
//			// If we're the master, we need to set up the master server
//			// Now wait for connections, either from other servers requesting a port, or actual connections
//			// coming in for this server, which just happens to be a master server.
//			server.setServerSocket(masterSocket);
//			AddHeartbeatThread(pn, dm, server_name, masterSocket);
//		} catch (IOException ex) {
//			throw new ConfigRuntimeException(ex.getMessage(), Exceptions.ExceptionType.IOException, Target.UNKNOWN, ex);
//		}
//
//		// Now start the master thread listening
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				try {
//					while(!masterSocket.isClosed()) {
//						Socket s = masterSocket.accept();
//						HandleConnection(pn, s);
//					}
//				} catch (SocketException ex) {
//					// Not an error, the socket is just shutting down.
//				} catch (IOException ex) {
//					Logger.getLogger(com.laytonsmith.core.functions.Federation.class.getName()).log(Level.SEVERE, null, ex);
//				}
//			}
//		}, Implementation.GetServerType().getBranding() + "-Federation-Allow-" + server_name + " (Master)").start();
//		return masterSocket;
//	}
//
//	public void AddHeartbeatThread(final PersistenceNetwork pn, final DaemonManager dm, final String server_name, final ServerSocket socket) {
//		new Thread(new Runnable() {
//
//			@Override
//			@SuppressWarnings("SleepWhileInLoop")
//			public void run() {
//				while(!socket.isClosed()) {
//					// Just in case *our* master thread died, we want to restart it.
//					FederationServer us = null;
//					for(FederationServer s : federationServers.values()) {
//						if(s.server_name.equals(server_name)) {
//							us = s;
//							break;
//						}
//					}
//
//					assert us != null;
//
//					StartMasterSocket(pn, dm, server_name, us.master_port);
//					FederationRegistration reg;
//					try {
//						reg = FederationRegistration.fromJSON(pn.get(new String[]{"federation", server_name}));
//						reg.updateLastUpdated();
//						pn.set(dm, new String[]{"federation", server_name}, reg.toJSON());
//					} catch (DataSourceException | IllegalArgumentException | IOException ex) {
//						Logger.getLogger(com.laytonsmith.core.functions.Federation.class.getName()).log(Level.SEVERE, null, ex);
//					} catch (ReadOnlyException ex) {
//						HandleReadOnlyException(ex);
//					}
//					try {
//						Thread.sleep(HEARTBEAT_INTERVAL);
//					} catch (InterruptedException ex) {
//						Logger.getLogger(com.laytonsmith.core.functions.Federation.class
//								.getName()).log(Level.SEVERE, null, ex);
//					}
//				}
//				try {
//					// Socket is closed, remove ourselves from the registration database.
//					pn.clearKey(dm, new String[]{"federation", server_name});
//				} catch (DataSourceException | IOException | IllegalArgumentException ex) {
//					Logger.getLogger(com.laytonsmith.core.functions.Federation.class.getName()).log(Level.SEVERE, null, ex);
//				} catch (ReadOnlyException ex) {
//					HandleReadOnlyException(ex);
//					return;
//				}
//			}
//		}, Implementation.GetServerType().getBranding() + "-Federation-Heartbeat-" + server_name).start();
//
//	}
//
//	private static void AddShutdownHook(final PersistenceNetwork pn, final DaemonManager dm, final ServerSocket masterSocket, final String server_name) {
//		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {
//
//			@Override
//			public void run() {
//				// Add a shutdown hook to kill the master server.
//				if(!masterSocket.isClosed()) {
//					try {
//						try {
//							masterSocket.close();
//
//						} catch (IOException ex) {
//							Logger.getLogger(com.laytonsmith.core.functions.Federation.class
//									.getName()).log(Level.SEVERE, null, ex);
//						}
//
//						pn.clearKey(dm, new String[]{"federation", server_name});
//					} catch (DataSourceException | IOException ex) {
//						Logger.getLogger(com.laytonsmith.core.functions.Federation.class
//								.getName()).log(Level.SEVERE, null, ex);
//					} catch (ReadOnlyException ex) {
//						HandleReadOnlyException(ex);
//					} finally {
//						synchronized (serverCountLock) {
//							serverCount--;
//							if(serverCount == 0) {
//								dm.deactivateThread(null);
//							}
//						}
//					}
//				}
//			}
//		});
//	}
//
	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 */
	public static boolean available(int port) {
		if(port < 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if(ds != null) {
				ds.close();
			}

			if(ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}
//
//	private static void RegisterServer(PersistenceNetwork pn, DaemonManager dm, String serverName, int port, boolean is_master) throws DataSourceException, ReadOnlyException, IOException {
//		FederationRegistration reg = new FederationRegistration(serverName, is_master, port);
//		pn.set(dm, new String[]{"federation", serverName}, reg.toJSON());
//	}
//
//	@SuppressWarnings("ConvertToStringSwitch")
//	private static void HandleConnection(PersistenceNetwork pn, Socket s) throws IOException {
//		try (
//				BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
//				PrintWriter out = new PrintWriter(s.getOutputStream(), true);) {
//			FederationVersion version;
//			try {
//				version = FederationVersion.fromVersion(reader.readLine());
//			} catch (IllegalArgumentException ex) {
//				//Don't know the version it's reporting, so let's give up.
//				out.println("ERROR");
//				out.println(ex.getMessage());
//				out.close();
//				s.close();
//				return;
//			}
//			//Main command
//			String header = reader.readLine();
//			if("GET PORT".equals(header)) {
//				// This is a port request to the master port.
//				int newPort = -1;
//				portFinder:
//				while(true) {
//					// Generate a random port between the min and max values, and check in the
//					// registration to see if it is already in use.
//					Random r = new Random();
//					newPort = r.nextInt(DYNAMIC_PORT_MAXIMUM - DYNAMIC_PORT_MINIMUM) + DYNAMIC_PORT_MINIMUM;
//					if(!available(newPort)) {
//						continue;
//					}
//					Map<String[], String> servers;
//					try {
//						servers = pn.getNamespace(new String[]{"federation"});
//					} catch (DataSourceException | IllegalArgumentException ex) {
//						Logger.getLogger(com.laytonsmith.core.functions.Federation.class.getName()).log(Level.SEVERE, null, ex);
//						return;
//					}
//					for(String server : servers.values()) {
//						FederationRegistration reg = FederationRegistration.fromJSON(server);
//						if(reg.updatedSince(DEAD_SERVER_TIMEOUT * 1000)) {
//							if(reg.port == newPort) {
//								continue portFinder;
//							}
//						}
//					}
//					break;
//				}
//				out.println(newPort);
//			} else if("HELLO".equals(header)) {
//				// We are establishing the connection. We need to read in several things, namely the connection
//				// information, and ensure that this connection is allowed by the server settings. (That is, compared
//				// against information provided in federation_remote_allow).
//				// Once we determine if this is a request for another connection, we will either respond and close, or
//				// if we are the master socket, and the request is for another server, or we will move the socket to
//				// another thread and return. Either way, we need to return quickly, so that the socket can accept more
//				// threads. We need the FederationServer object so we can grab the allow parameters.
//				FederationServer server = null;
//				for(FederationServer srv : federationServers.values()) {
//					if(srv.serverSocket.getLocalPort() == s.getLocalPort()) {
//						server = srv;
//						break;
//					}
//				}
//				assert server != null;
//				if(version == FederationVersion.V1_0_0) {
//					String server_name = reader.readLine();
//					if(server.server_name.equals(server_name)) {
//						// This is the right server
//					}
//				}
//			} else if("SCRIPT".equals(header)) {
//				// Protocol Version
//				if(version == FederationVersion.V1_0_0) {
//					int contentLength = Integer.parseInt(reader.readLine());
//					char[] request = new char[contentLength];
//					reader.read(request);
//					String sRequest = new String(request);
//					System.out.println(sRequest);
//				}
//			}
//		}
//	}
//
//	private static void HandleReadOnlyException(ReadOnlyException ex) {
//		throw new ConfigRuntimeException("The \"federation\" namespace may not be set to read only."
//				+ " Check you persistence config file and ensure that that namespace is read/write. " + ex.getMessage(),
//				Exceptions.ExceptionType.IOException, Target.UNKNOWN);
//	}
}
