package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.MemoryMapFileUtil;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file allows for simple data storage across many different data sources.
 * In general, the most common methods used are getValue and setValue. Note that
 * getValue, setValue, save, and load are synchronized.
 *
 *
 */
@datasource("ser")
public class SerializedPersistence extends AbstractDataSource {

	private SerializedPersistence(){

	}

	/**
	 * This is the data structure that the registry is stored in. It is a
	 * HashMap, not a Map, since we are depending on the implementation to
	 * remain constant, since it is serialized. Do not change this ever, or it
	 * will break all current serialized databases.
	 */
	private HashMap<String, String> data = new HashMap<String, String>();
	private HashMap<String, String> transactionData = new HashMap<String, String>();
	private boolean isLoaded = false;
	private boolean finishedInitializing = false;
	private static RunnableQueue queue = new RunnableQueue("SerializedPersistenceQueue");
	/**
	 * The storage location of the persistence database.
	 */
	private File storageLocation;

	public SerializedPersistence(File database) throws DataSourceException {
		super(database.toURI(), new ConnectionMixinFactory.ConnectionMixinOptions());
		storageLocation = database;
	}

	public SerializedPersistence(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
		String file;
		try {
			file = getConnectionMixin().getPath();
		} catch (IOException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
		storageLocation = new File(file);
		finishedInitializing = true;
	}

	/**
	 * Unless you're the data manager, don't use this method.
	 *
	 * @return
	 */
	public Map<String, String> rawData() {
		return data;
	}

	/**
	 * Loads the database from disk. This is automatically called when setValue
	 * or getValue is called.
	 *
	 * @throws Exception
	 */
	private void load() throws Exception {
			if (!isLoaded) {
				queue.invokeAndWait(new Callable<Object>(){

				@Override
					public Object call() throws Exception {
						try {
							FileInputStream fis = null;
							ObjectInputStream in = null;
							try{
								if(!storageLocation.exists()){
									storageLocation.createNewFile();
								}
								if(storageLocation.length() == 0){
									data = new HashMap<String, String>();
								} else {
									fis = new FileInputStream(storageLocation);
									in = new ObjectInputStream(fis);
									data = (HashMap<String, String>) in.readObject();
								}
								isLoaded = true;
							} catch(Throwable t){
								t.printStackTrace();
							} finally {
								if(fis != null){
									fis.close();
								}
								if(in != null){
									in.close();
								}
							}
						} catch (FileNotFoundException ex) {
							//ignore this one
						} catch (Exception ex) {
							throw ex;
						}
						return null;
					}
				});
			}
	}

	private byte[] byteData = ArrayUtils.EMPTY_BYTE_ARRAY;
	private MemoryMapFileUtil writer = null;
	private MemoryMapFileUtil.DataGrabber grabber = new MemoryMapFileUtil.DataGrabber() {

		@Override
		public byte[] getData() {
			return byteData;
		}
	};
	/**
	 * Causes the database to be saved to disk
	 *
	 * @throws IOException
	 */
	private void save(final DaemonManager dm) throws IOException{
		if(!inTransaction()){
			if(writer == null){
				writer = MemoryMapFileUtil.getInstance(storageLocation, grabber);
			}
			queue.invokeLater(dm, new Runnable() {
				@Override
				public void run() {
					ObjectOutputStream out = null;
					ByteArrayOutputStream baos = null;
					try {
						if (storageLocation.getParentFile() != null) {
							storageLocation.getParentFile().mkdirs();
						}
						if (!storageLocation.exists()) {
							storageLocation.createNewFile();
						}
	//					fos = new FileOutputStream(storageLocation);
						baos = new ByteArrayOutputStream();
						out = new ObjectOutputStream(baos);
						out.writeObject(new HashMap(data));
						byteData = baos.toByteArray();
						writer.mark(dm);
					} catch (IOException ex) {
						Logger.getLogger(SerializedPersistence.class.getName()).log(Level.SEVERE, null, ex);
					} finally {
						try {
							if (baos != null) {
								baos.close();
							}
						} catch (IOException ex) {
							Logger.getLogger(SerializedPersistence.class.getName()).log(Level.SEVERE, null, ex);
						}
						try {
							if (out != null) {
								out.close();
							}
						} catch (IOException ex) {
							Logger.getLogger(SerializedPersistence.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			});
		}
	}

	/**
	 * You should not usually use this method. Please see
	 * <code>setValue(String[] key, Serializable value)</code>
	 */
	private String setValue(DaemonManager dm, String key, String value) {
		//defer loading until we actually try and use the data structure
		if (isLoaded == false) {
			try {
				load();
			} catch (Exception ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
			}
		}
		String oldVal = data.get(key);
		if (value == null) {
			data.remove(key);
		} else {
			data.put(key, value);
		}
		try {
			save(dm);
		} catch (Exception ex) {
			Logger.getLogger(SerializedPersistence.class.getName()).log(Level.SEVERE, null, ex);
		}
		return oldVal;
	}

	private String getValue(String key) {
		//defer loading until we actually try and use the data structure
		if (isLoaded == false) {
			try {
				load();
			} catch (Exception ex) {
				Logger.getLogger(SerializedPersistence.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (data == null) {
			return null;
		}
		return data.get(key);
	}

	/**
	 * Adds or modifies the value of the key. Typically, this convention should
	 * be followed:
	 * <pre>
	 * key1.key2.key3...
	 * </pre> To make this usage easier, the function automatically namespaces
	 * the values for you. A sample usage might be:
	 * <pre>
	 * setValue(new String[]{"playerName", "value"}, value);
	 * </pre>
	 *
	 * When using namespaces in this way, the isNamespaceSet function becomes
	 * available to you. Since plugin values are global, you can use this to
	 * interact with other plugins. Caution should be used when interacting with
	 * other plugin's values though.
	 *
	 * @param key The key for this particular value
	 * @param value The value to store. If value is null, the key is simply
	 * removed.
	 * @return The object that was in this key, or null if the value did not
	 * exist.
	 */
	private String setValue(DaemonManager dm, String[] key, String value) {
		return setValue(dm, getNamespace0(key), (String) value);
	}

	/**
	 * Combines the String array into a single string
	 *
	 * @param key
	 * @return
	 */
	private static String getNamespace0(String[] key) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < key.length; i++) {
			if (i > 0) {
				b.append(".").append(key[i]);
			} else {
				b.append(key[i]);
			}
		}
		return b.toString();
	}

	@Override
	public Set<String[]> keySet(String[] keyBase) {
		Set<String[]> list = new HashSet<String[]>();
		String kb = StringUtils.Join(keyBase, ".");
		for (String key : data.keySet()) {
			if(key.startsWith(kb)){
				list.add(key.split("\\."));
			}
		}
		return list;
	}

	@Override
	public String get0(String[] key) {
		return getValue(StringUtils.Join(key, "."));
	}

	@Override
	public boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, IOException {
		setValue(dm, key, value);
		save(dm);
		return true;
	}

	@Override
	public void populate() throws DataSourceException {
		if (!finishedInitializing) {
			return;
		}
		try {
			load();
		} catch (Exception ex) {
			throw new DataSourceException(null, ex);
		}
	}

	@Override
	public DataSourceModifier[] implicitModifiers() {
		return null;
	}

	@Override
	public DataSourceModifier[] invalidModifiers() {
		return new DataSourceModifier[]{DataSourceModifier.HTTP, DataSourceModifier.HTTPS, DataSourceModifier.PRETTYPRINT};
	}

	@Override
	public String docs() {
		return "Serialized Persistence {ser:///path/to/persistence.ser} The default type,"
				+ " this simply uses java serialization to store data. Extremely simple"
				+ " to use, it is less scalable than database driven solutions, but for"
				+ " a file based solution, is relatively efficient, since it is stored as"
				+ " binary data. This means that it cannot be easily edited however.";
	}

	@Override
	public CHVersion since() {
		return CHVersion.V3_0_2;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		//Save the existing data
		transactionData = (HashMap<String, String>) data.clone();
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		if(!rollback){
			save(dm);
		} else {
			data = transactionData;
		}
		transactionData = null;
	}

	@Override
	public void disconnect() {
		//
	}
}
