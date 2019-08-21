package com.laytonsmith.core.functions;

/**
 *
 */
public class Federation {

	public static String docs() {
		return "The Federation class of functions allows for servers to connect to other servers, and run code on the remote machine.";
	}

//	@api
//	@noboilerplate
//	@seealso(federation_remote_allow.class)
//	@hide("Until this is finished, it is hidden.")
//	public static class federation_remote_connect extends AbstractFunction {
//
//		@Override
//		public Exceptions.ExceptionType[] thrown() {
//			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
//		}
//
//		@Override
//		public boolean isRestricted() {
//			return true;
//		}
//
//		@Override
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
//			final CArray connection_data = Static.AssertType(CArray.class, args, 0, this, t);
//			final CClosure remote_callback = Static.AssertType(CClosure.class, args, 1, this, t);
//			final CClosure local_callback;
//			if(args.length >= 3) {
//				local_callback = Static.AssertType(CClosure.class, args, 2, this, t);
//			} else {
//				local_callback = null;
//			}
//			final String host;
//			final String server_name;
//			final int timeout;
//			final String password;
//			final File remote_public_key;
//			final int master_port;
//			if(connection_data.containsKey("host")) {
//				host = connection_data.get("host").val();
//			} else {
//				host = "localhost";
//			}
//			if(connection_data.containsKey("server_name")) {
//				server_name = connection_data.get("server_name").val();
//			} else {
//				throw new ConfigRuntimeException("", ExceptionType.FormatException, t);
//			}
//			if(connection_data.containsKey("timeout")) {
//				timeout = Static.getInt32(connection_data.get("timeout"), t);
//			} else {
//				timeout = 30;
//			}
//			if(connection_data.containsKey("password")) {
//				password = connection_data.get("password").val();
//			} else {
//				password = null;
//			}
//			if(connection_data.containsKey("remote_public_key")) {
//				remote_public_key = Static.GetFileFromArgument(connection_data.get("remote_public_key").val(), environment, t, null);
//			} else {
//				remote_public_key = null;
//			}
//			if(connection_data.containsKey("master_port")) {
//				master_port = Static.getInt32(connection_data.get("master_port"), t);
//			} else {
//				master_port = com.laytonsmith.core.federation.Federation.MASTER_PORT;
//			}
//			final DaemonManager dm = environment.getEnv(GlobalEnv.class).GetDaemonManager();
//			// All the arguments are now parsed. Kick off a new thread.
//
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					Socket s;
//					// Search through the existing connections, and try to find it. If it's already connected, we'll reuse that socket.
//					synchronized (FederationConnection.connectionEstablishmentLock) {
//						if(federationConnections.containsKey(server_name)) {
//							s = federationConnections.get(server_name).socket;
//						} else {
//							s = new Socket();
//							try {
//								s.connect(new InetSocketAddress(host, master_port), timeout);
//							} catch (SocketTimeoutException ex) {
//								if(local_callback != null) {
//									StaticLayer.GetConvertor().runOnMainThreadLater(dm, new Runnable() {
//
//										@Override
//										public void run() {
//											local_callback.execute(CNull.NULL, CBoolean.TRUE, CNull.NULL);
//										}
//									});
//								}
//								return;
//							} catch (IOException ex) {
//								//Could not connect for some other reason
//								if(local_callback != null) {
//									final CArray exception = ObjectGenerator.GetGenerator().exception(new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex), t);
//									StaticLayer.GetConvertor().runOnMainThreadLater(dm, new Runnable() {
//
//										@Override
//										public void run() {
//											local_callback.execute(CNull.NULL, CBoolean.TRUE, exception);
//										}
//									});
//								}
//								return;
//							}
//							// Now the socket is connected, but we need to handshake with the server, and possibly connect to another
//							// server, if the master isn't the right connection.
//							try(
//								PrintWriter out = new PrintWriter(s.getOutputStream(), true);
//								BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
//							){
//								// As always, first the version we're talking in.
//								out.println(FederationVersion.V1_0_0.getVersionString());
//								// Then the hello
//								out.println("HELLO");
//							} catch (IOException ex) {
//								Logger.getLogger(Federation.class.getName()).log(Level.SEVERE, null, ex);
//							}
//						}
//					}
//					// Ok, the socket is valid, connected, and the handshake is running. We can start writing the data out.
//				}
//			}, Implementation.GetServerType().getBranding() + "-Remote-Connect (" + server_name + ")").start();
//
//			return new CVoid(t);
//		}
//
//		@Override
//		public String getName() {
//			return "federation_remote_connect";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{2, 3};
//		}
//
//		@Override
//		public String docs() {
//			return "void {connection_data, remote_callback, [local_callback(remoteReturn, timeoutError, exception)]} Connects to the specified server if not already background connected, and runs"
//					+ " the specified remote_callback closure on the remote server. ----"
//					+ " The connection data is an array of connection parameters, which are used to find the server and connect. Connections"
//					+ " may be cached and kept alive for future commands. The connection data is an array with the following parameters:\n"
//					+ "\n"
//					+ "{|\n"
//					+ "|-\n"
//					+ "! Key !! Type !! Default (if optional) !! Description\n"
//					+ "|-\n"
//					+ "| host || string || \"localhost\" || The host machine. This should be an IP address or a hostname.\n"
//					+ "|-\n"
//					+ "| server_name || string || <required> || The server name. This will be specified via the {{function|federation_remote_allow}}"
//					+ " function.\n"
//					+ "|-\n"
//					+ "| timeout || int || 30 || The timeout, in seconds, before the connection attempt will fail. If the server does not respond"
//					+ " in this time period, the local_callback will be called with an error (if provided).\n"
//					+ "|-\n"
//					+ "| password || string || null || If the remote requires a password (local or not) this should be set.\n"
//					+ "|-\n"
//					+ "| private_key || string || null || The file system path to the RSA private key that will be used to attempt"
//					+ " to connect, if the remote server expects this server to provide one. This path is not subject to the base-dir"
//					+ " restriction.\n"
//					+ "|-\n"
//					+ "| remote_public_key || string || null || The file system path to the RSA public key of the remote server. If using"
//					+ " public/private keypairs, this ensures that you are connecting to the server you think. This is an optional level"
//					+ " of security, but is recommended for remote host connections. This path is not subject to the base-dir restriction.\n"
//					+ "|-\n"
//					+ "| master_port || int || " + MASTER_PORT + " || The master port of the remote. This value must match the remote for the connection"
//					+ " to succeed.\n"
//					+ "|}"
//					+ "\n"
//					+ "The remote callback is a closure, which is run with the current variable environment, but other environment factors (current"
//					+ " player, current event, current interval/timeout, etc.) are lost. The remote callback is run as the console/system user on the"
//					+ " remote system, though it is checked that the current script could elevate to an administrative user, as if {{function|sudo}} "
//					+ " were being run, before"
//					+ " the connection will succeed. No parameters are sent to the remote callback, though if the remote callback returns a value,"
//					+ " it is retrieved from the remote system, and handed off to the local_callback (if provided). The local_callback is run once"
//					+ " the remote callback finishes (or a connection timeout occurs). It should accept three parameters, the mixed remoteReturn,"
//					+ " the boolean timeoutError, and the array exception. If there is a timeout error, remoteReturn will be null,"
//					+ " timeoutError will be true, and exception will be null. If the connection could otherwise not succeed (invalid host, etc),"
//					+ " then remoteReturn will be null, timeoutError will be true, and exception will have an exception."
//					+ " If the connection succeeded timeoutError will always be false."
//					+ " If the connection succeeded, and the closure did not cause an exception, and it returned a value, that value will be"
//					+ " provided as remoteReturn, and exception will be null; if the closure caused an exception, remoteReturn will be null,"
//					+ " and exception will contain the exception that was generated on the remote. If the remote connection succeeded, but was"
//					+ " refused by the remote server, remoteReturn will be null, and timeoutError will be false, but exception will not be null."
//					+ " The connection could be refused for several reasons, see {{function|federation_remote_allow}} for further details on"
//					+ " how to allow connections.";
//		}
//
//		@Override
//		public Version since() {
//			return CHVersion.V3_3_1;
//		}
//
//		@Override
//		public ExampleScript[] examples() throws ConfigCompileException {
//			return new ExampleScript[]{
//				new ExampleScript("Basic usage, connecting to another local server",
//				"federation_remote_connect(array(\n"
//				+ "\thost: 'localhost',\n"
//				+ "\tserver_name: 'myServer2',\n"
//				+ "\tpassword: 'server2password'\n"
//				+ "), closure(){\n"
//				+ "return(all_players())\n"
//				+ "}, closure(@remoteReturn, @timeoutError, @exception){\n"
//				+ "\tif(@exception){\n"
//				+ "\t\tthrow(@exception);\n"
//				+ "\t}\n"
//				+ "\tif(@timeoutError){\n"
//				+ "\t\tmsg('A timeout error occured.');\n"
//				+ "\t\treturn();\n"
//				+ "\t}\n"
//				+ "\t// else the remoteReturn value is usable\n"
//				+ "\tif(@remoteReturn !== null){\n"
//				+ "\t\tmsg(@remoteReturn);\n"
//				+ "\t}"
//				+ "}",
//				"(Assuming the connection succeeded, then a list of all players on the other server will print out.)"),};
//		}
//
//	}
//
//	@api
//	@noboilerplate
//	@seealso({federation_remote_connect.class, federation_remote_revoke.class})
//	@hide("Until this is finished, it is hidden.")
//	public static class federation_remote_allow extends AbstractFunction {
//
//		@Override
//		public Exceptions.ExceptionType[] thrown() {
//			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.RangeException};
//		}
//
//		@Override
//		public boolean isRestricted() {
//			return true;
//		}
//
//		@Override
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Construct exec(Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
//			final String server_name;
//			final String password;
//			final File authorized_keys;
//			final String allow_from;
//			final int master_port;
//
//			if(!(args[0].isInstanceOf(CArray.TYPE))) {
//				throw new Exceptions.CastException("Expecting argument 1 to be an array.", t);
//			}
//
//			CArray connection_details = (CArray) args[0];
//			if(connection_details.containsKey("server_name")) {
//				server_name = connection_details.get("server_name").val();
//			} else {
//				throw new Exceptions.FormatException("server_name is a required key in " + getName(), t);
//			}
//			if(connection_details.containsKey("password")) {
//				password = connection_details.get("password").val();
//			} else {
//				password = null;
//			}
//			if(connection_details.containsKey("authorized_keys")) {
//				authorized_keys = Static.GetFileFromArgument(connection_details.get("authorized_keys").val(), environment, t, null);
//			} else {
//				authorized_keys = null;
//			}
//			if(connection_details.containsKey("allow_from")) {
//				allow_from = connection_details.get("allow_from").val();
//			} else {
//				throw new Exceptions.FormatException("allow_from is a required key in " + getName(), t);
//			}
//			if(connection_details.containsKey("master_port")) {
//				master_port = Static.getInt32(connection_details.get("master_port"), t);
//			} else {
//				master_port = MASTER_PORT;
//			}
//			if(master_port < 0 || master_port > 65535) {
//				throw new Exceptions.RangeException("master_port must be between 0 and 65535.", t);
//			}
//
//			if(federationServers.containsKey(server_name)) {
//				// Quick check to see if the server is already registered on this server. We still can't assume that it's globally
//				// unregistered though, we have to check in the registration database.
//				throw new Exceptions.FormatException("A server with the name \"" + server_name + "\" is already registered.", t);
//			}
//
//			final PersistenceNetwork pn = environment.getEnv(GlobalEnv.class).GetPersistenceNetwork();
//			final DaemonManager dm = environment.getEnv(GlobalEnv.class).GetDaemonManager();
//			// Argument parsing and validation is now done.
//			final boolean is_master;
//			try {
//				if(pn.hasKey(new String[]{"federation", server_name})) {
//					FederationRegistration reg = FederationRegistration.fromJSON(pn.get(new String[]{"federation", server_name}));
//					if(reg.updatedSince(DEAD_SERVER_TIMEOUT * 1000)) {
//						// There's already a server that has reported in in the last X seconds, so we need
//						// to error out.
//						throw new Exceptions.FormatException("A server with the name \"" + server_name + "\" is already registered.", t);
//					}
//				}
//				is_master = available(master_port);
//				RegisterServer(pn, dm, server_name, master_port, is_master);
//			} catch (DataSourceException | IllegalArgumentException | IOException ex) {
//				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
//			} catch (ReadOnlyException ex) {
//				HandleReadOnlyException(ex);
//				return null; //Won't happen.
//			}
//
//			// Now we definitively know that we are the only server on the system named server_name, so let's register it real quick.
//			// Add the server to our local connection map
//			final FederationServer server = new FederationServer(server_name, password, authorized_keys, allow_from, master_port);
//			federationServers.put(server_name, server);
//
//			synchronized (serverCountLock) {
//				if(serverCount == 0) {
//					dm.activateThread(null);
//				}
//				serverCount++;
//			}
//
//			if(is_master) {
//				// We have to do this on the main thread, otherwise it runs a higher risk of becoming unavailable due
//				// to threading issues.
//				StartMasterSocket(pn, dm, server_name, master_port);
//			} else {
//				// At this stage, we're going to handle everything else async, since all the user
//				// parameters have been validated, and everything after this will be considered
//				// a programming error.
//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						while(true) {
//							try {
//								// Else, we need to ask the server for a port, then listen on that port.
//								int port;
//								Socket masterSocket = new Socket("localhost", master_port);
//								try(
//										OutputStream os = masterSocket.getOutputStream();
//										InputStream is = new BufferedInputStream(masterSocket.getInputStream());
//										PrintWriter out = new PrintWriter(os, true);
//										BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));) {
//									out.println(FederationVersion.V1_0_0.getVersionString());
//									out.println("GET PORT");
//									port = Integer.parseInt(reader.readLine());
//								}
//								// Update the registration's data on this server.
//								RegisterServer(pn, dm, server_name, port, false);
//								// Close the connection to the master now, and open up a new connection on the port it gave us.
//								ServerSocket socket = new ServerSocket(port);
//								server.setServerSocket(socket);
//								// We're also going to set up a heartbeat thread, which will update our heartbeat every X seconds in the registration table.
//								AddShutdownHook(pn, dm, socket, server_name);
//								AddHeartbeatThread(pn, dm, server_name, socket);
//								while(!socket.isClosed()) {
//									Socket s = socket.accept();
//									HandleConnection(pn, s);
//								}
//							} catch (ConnectException ex) {
//								// The master socket has been denied, so let's try to start up the master again.
//								ServerSocket s = StartMasterSocket(pn, dm, server_name, master_port);
//								// If the master socket is now us, we can break.
//								if(s.getLocalPort() == master_port) {
//									break;
//								} else {
//									// Otherwise restart ourselves.
//									continue;
//								}
//							} catch (IOException | DataSourceException ex) {
//								Logger.getLogger(Federation.class.getName()).log(Level.SEVERE, null, ex);
//							} catch (ReadOnlyException ex) {
//								try {
//									HandleReadOnlyException(ex);
//								} catch (ConfigRuntimeException ee) {
//									ConfigRuntimeException.HandleUncaughtException(ee, environment);
//								}
//							}
//							break;
//						}
//					}
//
//				}, Implementation.GetServerType().getBranding() + "-Federation-Allow-" + server_name).start();
//			}
//			return new CVoid(t);
//		}
//
//		@Override
//		public String getName() {
//			return "federation_remote_allow";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{1};
//		}
//
//		@Override
//		public String docs() {
//			return "void {connection_details} Allows a remote connection to be established to this server, using {{function|federation_remote_connect}}"
//					+ " ---- "
//					+ "The connection_details is an array which expects the following parameters:\n\n"
//					+ "{|\n"
//					+ "|-\n"
//					+ "! Key !! Type !! Default (if optional) !! Description\n"
//					+ "|-\n"
//					+ "| server_name || string || <required> || The name that this server should register as. If the"
//					+ " server name is already registered on this host, it will cause a FormatException to be thrown.\n"
//					+ "|-\n"
//					+ "| password || string || null || The server password. Any remotes trying to connect to this server"
//					+ " must provide the password in order to successfully connect. This method is less secure than using"
//					+ " PKI (public/private keypairs) but can be used in addition for an added layer of security."
//					+ "|-\n"
//					+ "| authorized_keys || string || null || The path to the server's authorized keys list. If provided, the remote"
//					+ " is required to also specify their private key, and the connections will succeed only if the remote is added"
//					+ " to this file. This file should follow the same format as SSH's authorized keys file, meaning if the remote"
//					+ " could ssh into the server, this connection would also work, though it is not required that the file be the"
//					+ " same as SSH's file. This file path is not subject to the base-dir restriction.\n"
//					+ "|-\n"
//					+ "| allow_from || string || <required> || A comma separated list of remotes' IP addresses that are allowed to connect."
//					+ " The typical default is probably \"localhost\", but that must be specified specifically. If the string is \"*\", then"
//					+ " connections from all remotes will be allowed. If this is the case, it is extremely highly recommended to use the"
//					+ " authorized_keys mechanism. If this is set to \"*\", and authorized_keys is null, a warning is issued.\n"
//					+ "|-\n"
//					+ "| master_port || int || " + MASTER_PORT + " || The master port that will be bound to if this server determines it is the"
//					+ " master server for that port. In general, this should be the same for all servers, but in order for a remote"
//					+ " to connect to this server, it must be using the same master port.\n"
//					+ "|}\n\n"
//					+ "A server may register multiple unique server names. Each server name may contain various connection restrictions, and"
//					+ " the connection restrictions will be followed for the server name that the remote connects to.\n\n"
//					+ "Until a call to federation_remote_allow() is made, the server will not be listening for connections from remotes. Once"
//					+ " it is called, the server will begin listening for connections, though it will actively deny any connections that don't"
//					+ " meet the connection criteria. The first server to start up a listener will begin listening on port " + MASTER_PORT + " (or whichever is"
//					+ " specified as the master port), and will"
//					+ " act as the master server for that machine. It will direct requests that are meant for other machines to them as they"
//					+ " come in.";
//		}
//
//		@Override
//		public Version since() {
//			return CHVersion.V3_3_1;
//		}
//
//	}
//
//	@api
//	@noboilerplate
//	@seealso({federation_remote_allow.class, federation_remote_connect.class})
//	@hide("This is hidden until finished.")
//	public static class federation_remote_revoke extends AbstractFunction {
//
//		@Override
//		public ExceptionType[] thrown() {
//			return new ExceptionType[]{ExceptionType.FormatException};
//		}
//
//		@Override
//		public boolean isRestricted() {
//			return true;
//		}
//
//		@Override
//		public Boolean runAsync() {
//			return null;
//		}
//
//		@Override
//		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
//			String server_name = args[0].val();
//
//			for(FederationServer server : federationServers.values()) {
//				if(server.server_name.equals(server_name)) {
//					try {
//						// Simply closing the socket should shutdown all the appropriate threads correctly.
//						server.getServerSocket().close();
//					} catch (IOException ex) {
//						throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
//					}
//					break;
//				}
//			}
//			federationServers.remove(server_name);
//			synchronized (serverCountLock) {
//				serverCount--;
//				if(serverCount == 0) {
//					environment.getEnv(GlobalEnv.class).GetDaemonManager().deactivateThread(null);
//				}
//			}
//
//			return new CVoid(t);
//		}
//
//		@Override
//		public String getName() {
//			return "federation_remote_revoke";
//		}
//
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[]{1};
//		}
//
//		@Override
//		public String docs() {
//			return "void {serverName} Given a previously registered server name in this server, this disconnects that connection"
//					+ " from all remotes, and prevents future requests from occuring. This \"closes\" the connections allowed by"
//					+ " {{function|federation_remote_allow}}. ---- If the server name doesn't exist on this server, (or has been"
//					+ " previously disconnected) a FormatException is thrown. If this was the last connection on this server, the"
//					+ " listener is shutdown as well, and future calls from remotes will timeout.";
//		}
//
//		@Override
//		public Version since() {
//			return CHVersion.V3_3_1;
//		}
//
//	}
}
