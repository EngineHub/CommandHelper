package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.MemoryMapFileUtil;
import com.laytonsmith.PureUtilities.Persistance;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file allows for simple data storage across many different data sources.
 * In general, the most common methods used are getValue and setValue. Note that
 * getValue, setValue, save, and load are synchronized.
 *
 * @author layton
 */
@datasource("ser")
public class SerializedPersistance extends AbstractDataSource implements Persistance {
	
	private SerializedPersistance(){
		
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
	private static RunnableQueue queue = new RunnableQueue("SerializedPersistanceQueue");
	/**
	 * The storage location of the persistance database.
	 */
	private File storageLocation;

	public SerializedPersistance(File database) throws DataSourceException {
		super(database.toURI(), new ConnectionMixinFactory.ConnectionMixinOptions());
		storageLocation = database;
	}

	public SerializedPersistance(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
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
	 * Unless you're the data manager, and you <em>really</em> want to clear out
	 * the entire database, don't use this method. You must manually call save
	 * after this, if you wish the changes to be written out to disk.
	 */
	public void clearAllData() {
		data = new HashMap<String, String>();
	}

	/**
	 * Loads the database from disk. This is automatically called when setValue
	 * or getValue is called.
	 *
	 * @throws Exception
	 */
	public void load() throws Exception {
			if (!isLoaded) {
				queue.invokeAndWait(new Callable<Object>(){

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

	private byte[] byteData = new byte[0];
	private MemoryMapFileUtil writer = null;
	private MemoryMapFileUtil.DataGrabber grabber = new MemoryMapFileUtil.DataGrabber() {

		public byte[] getData() {
			return byteData;
		}
	};
	/**
	 * Causes the database to be saved to disk
	 *
	 * @throws IOException
	 */
	@Override
	public void save(final DaemonManager dm) throws IOException{
		if(!inTransaction()){
			if(writer == null){
				writer = MemoryMapFileUtil.getInstance(storageLocation, grabber);
			}
			queue.invokeLater(dm, new Runnable() {
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
						Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
					} finally {
						try {
							if (baos != null) {
								baos.close();
							}
						} catch (IOException ex) {
							Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
						}
						try {
							if (out != null) {
								out.close();
							}
						} catch (IOException ex) {
							Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
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
			Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
		}
		return oldVal;
	}

	private String getValue(String key) {
		//defer loading until we actually try and use the data structure
		if (isLoaded == false) {
			try {
				load();
			} catch (Exception ex) {
				Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
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
	@Override
	public String setValue(DaemonManager dm, String[] key, String value) {
		return setValue(dm, getNamespace0(key), (String) value);
	}

	/**
	 * Returns the value of a particular key, or null if the key doesn't exist
	 *
	 * @param key
	 * @return
	 */
	public String getValue(String[] key) {
		return getValue(getNamespace0(key));
	}

	/**
	 * Checks to see if a particular key is set. Unlike isNamespaceSet, this
	 * requires that the exact key be specified to see if it exists.
	 *
	 * @param key
	 * @return
	 */
	public boolean isKeySet(String[] key) {
		String k = getNamespace0(key);
		return data.containsKey(k);
	}

	/**
	 * Returns whether or not a particular namespace value is set. For instance,
	 * if the value plugin.myPlugin.players.playerName.data is set, then the
	 * call to
	 * <code>isNamespaceSet(new String[]{"plugin", "myPlugin"})</code> would
	 * return
	 * <code>true</code>
	 *
	 * @param partialKey
	 * @return
	 */
	public boolean isNamespaceSet(String[] partialKey) {
		String m = getNamespace0(partialKey);
		partialKey = m.split("\\.");
		Iterator i = data.entrySet().iterator();
		while (i.hasNext()) {
			String key = ((Map.Entry) i.next()).getKey().toString();
			String[] namespace = key.split("\\.");
			boolean match = true;
			for (int k = 0; k < partialKey.length; k++) {
				if (namespace.length < k) {
					match = false;
					continue;
				}
				if (!namespace[k].equals(partialKey[k])) {
					match = false;
					continue;
				}
			}
			if (match) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all the matched namespace entries.
	 *
	 * @param partialKey The partial name of the keys you wish to return
	 * @return An ArrayList of Map.Entries.
	 */
	public List<Map.Entry<String, Object>> getNamespaceValues(String[] partialKey) {

		List<Map.Entry<String, Object>> matches = new ArrayList<Map.Entry<String, Object>>();
		String m = getNamespace0(partialKey);
		partialKey = m.split("\\.");
		if (!isLoaded) {
			try {
				load();
			} catch (Exception ex) {
				Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		Iterator i = data.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			String key = entry.getKey().toString();
			String[] namespace = key.split("\\.");
			boolean match = true;
			for (int k = 0; k < partialKey.length; k++) {
				if (namespace.length < partialKey.length) {
					match = false;
					continue;
				}
				if (!namespace[k].equals(partialKey[k])) {
					match = false;
					continue;
				}
			}
			if (match) {
				matches.add(entry);
			}
		}
		return matches;
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

	/**
	 * Prints all of the stored values to the specified print stream.
	 */
	public void printValues(PrintStream out) {
		try {
			out.println("Printing all persisted values:");
			load();
			Iterator i = data.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = ((Map.Entry) i.next());
				out.println(e.getKey()
						+ ": " + data.get(e.getKey().toString()).toString());
			}
			out.println("Done printing persisted values");
		} catch (Exception ex) {
			Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Set<String[]> keySet() {
		Set<String[]> list = new HashSet<String[]>();
		for (String key : data.keySet()) {
			list.add(key.split("\\."));
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

	public String docs() {
		return "Serialized Persistance {ser:///path/to/persistance.ser} The default type,"
				+ " this simply uses java serialization to store data. Extremely simple"
				+ " to use, it is less scalable than database driven solutions, but for"
				+ " a file based solution, is relatively efficient, since it is stored as"
				+ " binary data. This means that it cannot be easily edited however.";
	}

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
}
