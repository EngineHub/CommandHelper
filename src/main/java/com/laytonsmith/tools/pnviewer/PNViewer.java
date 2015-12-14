package com.laytonsmith.tools.pnviewer;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.UI.TextDialog;
import com.laytonsmith.PureUtilities.Common.AutoFlushObjectOutputStream;
import com.laytonsmith.PureUtilities.Common.MutableObject;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Common.TemplateBuilder;
import com.laytonsmith.PureUtilities.Common.UIUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 */
public class PNViewer extends javax.swing.JFrame {

	private Map<String, String> data;
	private VirtualPersistenceNetwork network;

	private Boolean isLocalConfig = null;

	// Remote connection data
	private Thread remoteSocketThread;
	private Socket remoteSocket;
	private ObjectOutputStream remoteOutput;
	private ObjectInputStream remoteInput;
	private ConfigurationLoaderDialog loaderDialog;

	// Local connection data
	private String localPath;

	/**
	 * Creates new form PNViewer
	 */
	public PNViewer() {
		initComponents();
		setTitle("Persistence Network Viewer");
		configurationFromLabel.setText("");
		namespaceLabel.setText("");
		keyLabel.setText("");
		valueTypeLabel.setText("");
		sourceLabel.setText("");
		try {
			setIconImage(ImageIO.read(PNViewer.class.getResourceAsStream("GearIcon.png")));
		} catch (IOException ex) {
			Logger.getLogger(PNViewer.class.getName()).log(Level.SEVERE, null, ex);
		}
		setStatus("Waiting for configuration to be loaded...", false);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (remoteSocketThread != null && remoteSocketThread.isAlive()) {
					int sel = JOptionPane.showConfirmDialog(PNViewer.this, "A connection to the remote server is still active, are you sure you wish to disconnect?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (sel == JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			}

		});

		loaderDialog = new ConfigurationLoaderDialog(PNViewer.this, true);
		loaderDialog.setLoaderDialogFinish(new ConfigurationLoaderDialog.LoaderDialogFinish() {

			@Override
			public void data(boolean isLocal, String localPath, String host, int port, String password, String remoteFile) {
				isLocalConfig = isLocal;
				if (isLocal) {
					loadFromLocal(localPath);
				} else {
					loadFromRemote(host, port, password, remoteFile);
				}
			}
		});
		loadFromConfigurationMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				UIUtils.centerWindowOnWindow(loaderDialog, PNViewer.this);
				loaderDialog.setVisible(true);
			}
		});
		closeRemoteConnectionMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (remoteSocketThread == null) {
					showError("No remote connection is established.");
				} else {
					setStatus("Closing remote connection...", false);
					try {
						remoteOutput.writeUTF("DISCONNECT");
					} catch (IOException ex) {
						log(ex);
						//Fallback in case a clean close doesn't work.
						try {
							remoteSocket.close();
						} catch (IOException ex1) {
							//
						}
					}
					remoteSocketThread.interrupt();
				}
			}
		});
		keyTree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				try {
					String[] key = new String[e.getNewLeadSelectionPath().getPathCount() - 1];
					for (int i = 1; i < e.getNewLeadSelectionPath().getPathCount(); i++) {
						key[i - 1] = (String) ((DefaultMutableTreeNode) e.getNewLeadSelectionPath().getPathComponent(i)).getUserObject();
					}
					showData(join(key), data.get(join(key)));
				} catch (NullPointerException ex) {
					// Ignore
				}
			}
		});

		keyTree.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = keyTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = keyTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (SwingUtilities.isRightMouseButton(e)) {
						StreamUtils.GetSystemOut().println("Right click on " + selPath);
					}
				}
			}

		});

		manageBookmarksMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ManageBookmarksDialog bd = new ManageBookmarksDialog(PNViewer.this, true);
				UIUtils.centerWindowOnWindow(bd, PNViewer.this);
				bd.setVisible(true);
			}
		});

		reloadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isLocalConfig == null) {
					showError("No configuration is loaded.");
				} else if (isLocalConfig == true) {
					loadFromLocal(localPath);
				} else {
					if (remoteSocketThread == null) {
						showError("Remote connection isn't established, cannot reload data");
					} else {
						try {
							remoteOutput.writeUTF("LOAD-DATA");
						} catch (IOException ex) {
							Logger.getLogger(PNViewer.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}
		});

		aboutMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TemplateBuilder builder = new TemplateBuilder();
				builder.addTemplate("version", new TemplateBuilder.Generator() {

					@Override
					public String generate(String... args) {
						return CHVersion.LATEST.toString();
					}
				});
				builder.addTemplate("implementation", new TemplateBuilder.Generator() {

					@Override
					public String generate(String... args) {
						try {
							return Implementation.GetServerType().getBranding();
						} catch(Exception ex){
							return "MethodScript";
						}
					}
				});
				builder.addTemplate("clientVersion", new TemplateBuilder.Generator() {

					@Override
					public String generate(String... args) {
						return Integer.toString(PROTOCOL_VERSION);
					}
				});
				String text = builder.build(StreamUtils.GetString(PNViewer.class.getResourceAsStream("AboutDialog.html")));
				TextDialog td = new TextDialog(PNViewer.this, true, text);
				UIUtils.centerWindowOnWindow(td, PNViewer.this);
				td.setVisible(true);
			}
		});

		helpMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TemplateBuilder builder = new TemplateBuilder();
				builder.addTemplate("wiki", new TemplateBuilder.Generator() {

					@Override
					public String generate(String... args) {
						return "http://wiki.sk89q.com/wiki/CommandHelper/Staged/" + args[0];
					}
				});
				builder.addTemplate("jarName", new TemplateBuilder.Generator() {

					@Override
					public String generate(String... args) {
						return new File(ClassDiscovery.GetClassContainer(PNViewer.class).getPath()).getName();
					}
				});
				String text = builder.build(StreamUtils.GetString(PNViewer.class.getResourceAsStream("HelpDialog.html")));
				TextDialog td = new TextDialog(PNViewer.this, false, text);
				UIUtils.centerWindowOnWindow(td, PNViewer.this);
				td.setVisible(true);
			}
		});

//		Map<String[], String> data = new HashMap<>();
//		Map<String, String> dataSources = new HashMap<>();
//		data.put(new String[]{"first"}, "\"1\"");
//		data.put(new String[]{"first", "first"}, "1");
//		data.put(new String[]{"first", "second"}, "1");
//		data.put(new String[]{"second", "first"}, "1");
//		for(String[] key : data.keySet()){
//			dataSources.put(join(key), "Test Source");
//		}
//		displayData(data, dataSources);
	}

	private String join(String[] key) {
		return StringUtils.Join(key, ".");
	}

	private void showData(final String key, String value) {
		String[] split = key.split("\\.");
		String[] namespace = new String[split.length - 1];
		String keyPart = null;
		for (int i = 0; i < split.length; i++) {
			if (i == split.length - 1) {
				keyPart = split[i];
			} else {
				namespace[i] = split[i];
			}
		}
		namespaceLabel.setText(join(namespace));
		keyLabel.setText(keyPart);
		if (value == null) {
			sourceLabel.setText("");
			valueTypeLabel.setText("(empty key)");
			valueTextArea.setText("");
		} else {
			sourceLabel.setText("(resolving)");
			new Thread(new Runnable() {

				@Override
				public void run() {
					final String source = network.getKeySource(key.split("\\.")).toString();
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							sourceLabel.setText(source);
						}
					});
				}
			}).start();
			Construct c = CNull.NULL;
			try {
				c = Construct.json_decode(value, Target.UNKNOWN);
			} catch (MarshalException ex) {
				Logger.getLogger(PNViewer.class.getName()).log(Level.SEVERE, null, ex);
			}
			valueTypeLabel.setText(new DataHandling.typeof().exec(Target.UNKNOWN, null, c).val());
			valueTextArea.setText(c.val());
		}
	}

	private void loadFromLocal(final String path) {
		localPath = path;
		new Thread(new Runnable() {

			@Override
			public void run() {
				setStatus("Loading from local file system", true);
				setProgress(null);
				try {
					final PersistenceNetwork pn = getPersistenceNetwork(path);
					VirtualPersistenceNetwork vpn = new VirtualPersistenceNetwork() {

						@Override
						public Map<String[], String> getAllData() throws DataSourceException {
							return pn.getNamespace(new String[0]);
						}

						@Override
						public URI getKeySource(String[] key) {
							return pn.getKeySource(key);
						}
					};
					displayData(vpn);
				} catch (URISyntaxException | IOException | DataSourceException ex) {
					Logger.getLogger(PNViewer.class.getName()).log(Level.SEVERE, null, ex);
					showError(ex.getMessage());
				}
			}
		}).start();
	}

	/**
	 * This version is the first thing sent across the wire. If the versions
	 * don't match, the connection won't succeed.
	 */
	private static final int PROTOCOL_VERSION = 2;

	private void loadFromRemote(final String host, final int port, final String password, final String remoteFile) {
		remoteSocketThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					try (Socket s = new Socket()) {
						s.connect(new InetSocketAddress(host, port), 30000);
						remoteSocket = s;
						setStatus("Connected to remote server", true);
						setProgress(null);
						try {
							final ObjectOutputStream os = new AutoFlushObjectOutputStream(s.getOutputStream());
							final ObjectInputStream is = new ObjectInputStream(s.getInputStream());

							remoteOutput = os;
							remoteInput = is;
							// Set up our initial data
							log("Writing client version: " + PROTOCOL_VERSION);
							os.writeInt(PROTOCOL_VERSION);
							switch (is.readUTF()) {
								case "VERSION-OK":
									break;
								default:
									showError("The server does not support this client's version.");
									return;
							}
							os.writeUTF(password);
							switch (is.readUTF()) {
								case "PASSWORD-OK":
									log("Password accepted by server.");
									break;
								default:
									showError("Server rejected our password. Check the password and try again.");
									return;
							}
							os.writeUTF("SET-REMOTE-FILE");
							os.writeUTF(remoteFile);
							final MutableObject<Map<String[], String>> data = new MutableObject<>();
							final MutableObject<URI> sourceURI = new MutableObject<>();
							VirtualPersistenceNetwork vpn = null;
							connection:
							while (!Thread.currentThread().isInterrupted() && s.isConnected()) {
								String serverCommand = is.readUTF();
								switch (serverCommand) {
									case "DISCONNECT":
										break connection;
									case "FILE-OK":
										os.writeUTF("LOAD-DATA");
										reloadButton.setEnabled(false);
										break;
									case "FILE-BAD":
										showError("Remote file doesn't exist, disconnecting.");
										break;
									case "LOAD-DATA":
										int size = is.readInt();
										setStatus("Downloading data from server...", true);
										setProgress(0);
										byte[] bdata = new byte[size];
										for (int i = 0; i < size; i++) {
											bdata[i] = is.readByte();
											setProgress((int) ((((double) i) / ((double) size)) * 100));
										}
										setStatus("Processing data from server", true);
										setProgress(null);
										ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bdata));
										try {
											Map<String[], String> d = (Map<String[], String>) ois.readObject();
											data.setObject(d);
											vpn = new VirtualPersistenceNetwork() {

												Map<String, URI> sources = new HashMap<>();

												@Override
												public Map<String[], String> getAllData() throws DataSourceException {
													return data.getObject();
												}

												@Override
												public URI getKeySource(String[] key) {
													String kk = join(key);
													if (!sources.containsKey(kk)) {
														try {
															sourceURI.setObject(null);
															os.writeUTF("KEY-SOURCE");
															os.writeUTF(kk);
															synchronized (sourceURI) {
																if (sourceURI.getObject() == null) {
																	try {
																		sourceURI.wait();
																	} catch (InterruptedException ex) {
																		//
																	}
																}
															}
															URI uri = sourceURI.getObject();
															sources.put(kk, uri);
														} catch (IOException ex) {
															showError(ex.getMessage());
															log(ex);
														}
													}
													return sources.get(kk);
												}
											};
											try {
												displayData(vpn);
												setStatus("Done.", false);
											} catch (DataSourceException ex) {
												log(ex);
												showError(ex.getMessage());
											}
										} catch (ClassNotFoundException ex) {
											log(ex);
											showError(ex.getMessage());
										}
										reloadButton.setEnabled(true);
										break;
									case "KEY-SOURCE":
										try {
											URI uri = (URI) is.readObject();
											sourceURI.setObject(uri);
											synchronized (sourceURI) {
												sourceURI.notifyAll();
											}
										} catch (ClassNotFoundException ex) {
											log(ex);
										}
										break;
									case "LOAD-ERROR":
										String message = is.readUTF();
										setStatus(message, false);
										showError(message);
										reloadButton.setEnabled(true);
										break;
									default:
										showError("Server sent unrecognized command, disconnecting.");
										log("Unrecognized command: " + serverCommand);
										break connection;
								}
							}
							log("Closing connection.");
						} catch (EOFException ex) {
							log(ex);
							showError("The server closed the connection unexpectedly.");
						}
					} catch (SocketTimeoutException ex) {
						showError("Connection timed out, check your settings and try again.");
					} finally {
						setStatus("Connection to remote server closed.", false);
						remoteOutput = null;
						remoteInput = null;
						reloadButton.setEnabled(true);
					}
				} catch (IOException ex) {
					showError(ex.getMessage());
					Logger.getLogger(PNViewer.class.getName()).log(Level.SEVERE, null, ex);
				}
				remoteSocketThread = null;
			}
		});
		remoteSocketThread.start();
	}

	public static void startServer(int port, final String password) throws IOException {
		ServerSocket socket = new ServerSocket(port);
		log("Server started on port " + port + ". Type Ctrl+C to kill the server.");
		log("Process info: " + ManagementFactory.getRuntimeMXBean().getName());
		log("Server version: " + PROTOCOL_VERSION);
		connection:
		while (true) {
			log("Persistence Network Viewers may now connect to this server.");
			final Socket s = socket.accept();
			log("A client has connected from " + s.getInetAddress().toString());
			new Thread(new Runnable() {

				@Override
				public void run() {

					Thread waitThread = null;
					try {
						final AtomicBoolean dataReceieved = new AtomicBoolean(false);
						final AtomicBoolean longTimeout = new AtomicBoolean(false);
						waitThread = new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									while (true) {
										synchronized (dataReceieved) {
											dataReceieved.wait(longTimeout.get() ? 10 * 60 * 1000 : 10 * 1000);
										}
										if (dataReceieved.get() == false) {
											log("No response from client in too long, forcibly closing connection.");
											try {

												s.close();
											} catch (IOException ex) {
												//
											}
											break;
										}
										dataReceieved.set(false);
									}
								} catch (InterruptedException ex) {
									//
								}
							}
						});
						waitThread.start();
						ObjectInputStream is = new ObjectInputStream(s.getInputStream());
						ObjectOutputStream os = new AutoFlushObjectOutputStream(s.getOutputStream());
						int protocolVersion = is.readInt();
						if (protocolVersion != PROTOCOL_VERSION) {
							log("Client version unsupported: " + protocolVersion);
							os.writeUTF("VERSION-MISMATCH");
							return;
						} else {
							log("Client version supported: " + protocolVersion);
							os.writeUTF("VERSION-OK");
						}
						String clientPassword = is.readUTF();
						if (!password.equals(clientPassword)) {
							log("Client supplied the wrong password, disconnecting.");
							os.writeUTF("PASSWORD-BAD");
							os.writeUTF("DISCONNECT");
							return;
						} else {
							if (!"".equals(password)) {
								log("Client supplied the correct password");
							}
							os.writeUTF("PASSWORD-OK");
						}
						// They have now authed correctly, so we can up the idle time.
						longTimeout.set(true);

						// Now we need to create instance variables for the remainder of
						// the connection, that is, the meat of the connection.
						String remoteFile = null;
						PersistenceNetwork pn = null;

						connected:
						while (s.isConnected()) {
							String command = is.readUTF();
							log("Command received from client: " + command);
							dataReceieved.set(true);
							synchronized (dataReceieved) {
								dataReceieved.notifyAll();
							}
							switch (command) {
								case "DISCONNECT":
									// Write the disconnect out to the client as well, so
									// the client will gracefully disconnect.
									os.writeUTF("DISCONNECT");
									break connected;
								case "SET-REMOTE-FILE":
									remoteFile = is.readUTF();
									log("File set to " + remoteFile);
									if (new File(remoteFile).exists()) {
										log("File accepted.");
										os.writeUTF("FILE-OK");
									} else {
										log("File not accepted.");
										os.writeUTF("FILE-BAD");
										os.writeUTF("DISCONNECT");
									}
									break;
								case "LOAD-DATA":
									os.writeUTF("LOAD-DATA");
									try {
										// Load the data from the PN, and send it on
										pn = getPersistenceNetwork(remoteFile);
										Map<String[], String> data = pn.getNamespace(new String[0]);
										ByteArrayOutputStream baos = new ByteArrayOutputStream();
										ObjectOutputStream oos = new ObjectOutputStream(baos);
										oos.writeObject(data);
										oos.flush();
										byte[] output = baos.toByteArray();
										os.writeInt(output.length);
										os.write(output);
									} catch (URISyntaxException | DataSourceException ex) {
										os.writeUTF("LOAD-ERROR");
										os.writeUTF(ex.getMessage());
										log("Load error!");
										log(ex);
									}
									break;
								case "KEY-SOURCE":
									os.writeUTF("KEY-SOURCE");
									String key = is.readUTF();
									if (pn == null) {
										log("pn is null, can't get key source");
										os.writeUTF("DISCONNECT");
									} else {
										log("Requested source for key: " + key);
										URI uri = pn.getKeySource(key.split("\\."));
										log("Responding with: " + uri);
										os.writeObject(uri);
									}
									break;
								default:
									// Bad command, disconnect them.
									os.writeUTF("DISCONNECT");
									break connected;
							}
						}
					} catch (IOException ex) {
						// Disconnected
					} finally {
						try {
							s.close();
						} catch (IOException ex) {
							//
						}
						log("Client has disconnected.");
						if (waitThread != null) {
							waitThread.interrupt();
						}
					}
				}

			}).start();
		}
	}

	private static PersistenceNetwork getPersistenceNetwork(String configPath) throws URISyntaxException, IOException, DataSourceException {
		File config = new File(configPath);
		ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(config.getParentFile().getParentFile());
		return new PersistenceNetwork(config, new URI("sqlite://" + new File(config.getParentFile().getParentFile(), "persistence.db").toString().replace('\\', '/')), options);
	}

	/**
	 * Once the data is loaded, however that may take place, the data should be
	 * sent here, which will load the data into the viewer.
	 *
	 * @param data
	 */
	private void displayData(VirtualPersistenceNetwork pn) throws DataSourceException {
		this.network = pn;
		final Map<String[], String> data = pn.getAllData();
		final Map<String, String> dataSources = new HashMap<>();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				PNViewer.this.data = new HashMap<>();
				DefaultTreeModel model = (DefaultTreeModel) keyTree.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
				root.removeAllChildren();
				for (String[] key : data.keySet()) {
					PNViewer.this.data.put(join(key), data.get(key));
					DefaultMutableTreeNode node = root;
					outer:
					for (String n : key) {
						for (int i = 0; i < node.getChildCount(); i++) {
							DefaultMutableTreeNode at = (DefaultMutableTreeNode) node.getChildAt(i);
							if (n.equals(at.getUserObject())) {
								// This is our node, so recurse down this
								node = at;
								continue outer;
							}
						}
						// If we got here, then there is not an appropriate node at this level, so we needd
						// to create it.
						DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(n);
						node.add(newChild);
						node = newChild;
					}
				}
				model.reload(root);

				setStatus("Data loaded.", false);
			}
		});
	}

	/**
	 * Sets the status label. If the current operation is happening because the
	 * system is actively doing something, thinking should be set to true, and
	 * the progress bar will be set to visible (but will keep its existing
	 * state) otherwise, the progress bar is hidden.
	 *
	 * @param statusString
	 * @param thinking
	 */
	private void setStatus(final String statusString, final boolean thinking) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				statusLabel.setText(statusString);
				statusProgressBar.setVisible(thinking);
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException | InvocationTargetException ex) {
				log(ex);
			}
		}
	}

	/**
	 * Sets the progress bar percentage.
	 *
	 * @param i A percentage point, from 0 to 100. If null, it is set to be
	 * indeterminate.
	 */
	private void setProgress(final Integer i) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				statusProgressBar.setValue(i == null ? 100 : i);
				statusProgressBar.setIndeterminate(i == null);
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException | InvocationTargetException ex) {
				log(ex);
			}
		}
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private static void log(Throwable t) {
		log(StackTraceUtils.GetStacktrace(t));
	}

	private static void log(String message) {
		StreamUtils.GetSystemOut().println("[" + new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date()) + "]: " + message);
	}

	private static interface VirtualPersistenceNetwork {

		Map<String[], String> getAllData() throws DataSourceException;

		URI getKeySource(String[] key);

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        keyTree = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        namespaceLabel = new javax.swing.JLabel();
        keyLabel = new javax.swing.JLabel();
        valueTypeLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        valueTextArea = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        configurationFromLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        statusProgressBar = new javax.swing.JProgressBar();
        reloadButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadFromConfigurationMenu = new javax.swing.JMenuItem();
        closeRemoteConnectionMenu = new javax.swing.JMenuItem();
        bookmarksMenu = new javax.swing.JMenu();
        manageBookmarksMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenu1 = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setDividerLocation(200);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        keyTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        keyTree.setToolTipText("");
        keyTree.setRootVisible(false);
        jScrollPane1.setViewportView(keyTree);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jLabel1.setText("Namespace:");

        jLabel2.setText("Key:");

        jLabel3.setText("Value type:");

        jLabel4.setText("Value:");

        namespaceLabel.setText(".............");

        keyLabel.setText(".............");

        valueTypeLabel.setText("............");

        valueTextArea.setEditable(false);
        valueTextArea.setColumns(20);
        valueTextArea.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        valueTextArea.setLineWrap(true);
        valueTextArea.setRows(5);
        jScrollPane2.setViewportView(valueTextArea);

        jLabel6.setText("Source:");

        sourceLabel.setText("...........");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 722, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(namespaceLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(keyLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sourceLabel))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(valueTypeLabel)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(namespaceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(keyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(sourceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(valueTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jLabel5.setText("Configuration from:");

        configurationFromLabel.setText("...............");

        statusLabel.setText(".........");

        reloadButton.setText("Reload");
        reloadButton.setToolTipText("Reloads the data from the configuration");

        fileMenu.setText("File");

        loadFromConfigurationMenu.setText("Load from Configuration...");
        fileMenu.add(loadFromConfigurationMenu);

        closeRemoteConnectionMenu.setText("Close Remote Connection");
        fileMenu.add(closeRemoteConnectionMenu);

        jMenuBar1.add(fileMenu);

        bookmarksMenu.setText("Bookmarks");

        manageBookmarksMenu.setText("Manage Bookmarks...");
        bookmarksMenu.add(manageBookmarksMenu);
        bookmarksMenu.add(jSeparator1);

        jMenuBar1.add(bookmarksMenu);

        jMenu1.setText("Help");

        helpMenu.setText("Help...");
        jMenu1.add(helpMenu);

        aboutMenu.setText("About...");
        jMenu1.add(aboutMenu);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(configurationFromLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(reloadButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(configurationFromLabel)
                    .addComponent(reloadButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(PNViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				PNViewer pnViewer = new PNViewer();
				UIUtils.centerWindow(pnViewer);
				pnViewer.setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JMenu bookmarksMenu;
    private javax.swing.JMenuItem closeRemoteConnectionMenu;
    private javax.swing.JLabel configurationFromLabel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JTree keyTree;
    private javax.swing.JMenuItem loadFromConfigurationMenu;
    private javax.swing.JMenuItem manageBookmarksMenu;
    private javax.swing.JLabel namespaceLabel;
    private javax.swing.JButton reloadButton;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JProgressBar statusProgressBar;
    private javax.swing.JTextArea valueTextArea;
    private javax.swing.JLabel valueTypeLabel;
    // End of variables declaration//GEN-END:variables
}
