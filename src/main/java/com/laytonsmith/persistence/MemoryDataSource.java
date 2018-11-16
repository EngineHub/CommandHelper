package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a temporary data source that is in memory only, and is never written out to disk. All methods in
 * this class are thread safe.
 */
@datasource("mem")
public final class MemoryDataSource extends AbstractDataSource {

	private static final Map<String, Map<String, String>> DATABASE_POOL = new TreeMap<String, Map<String, String>>();

	/**
	 * Clears all data from all databases. Should be called when a natural reload type operation is called.
	 */
	public static synchronized void ClearDatabases() {
		for(String s : DATABASE_POOL.keySet()) {
			DATABASE_POOL.get(s).clear();
		}
		DATABASE_POOL.clear();
	}

	@Override
	public void disconnect() {
		ClearDatabases();
	}

	/**
	 * Retrieves the underlying database for a given database name. The actual database instance is returned, not a
	 * copy, and if the database doesn't exist, a new one is returned, therefore this will never return null.
	 *
	 * @param name
	 * @return
	 */
	public static synchronized Map<String, String> getDatabase(String name) {
		if(!DATABASE_POOL.containsKey(name)) {
			DATABASE_POOL.put(name, Collections.synchronizedMap(new TreeMap<String, String>()));
		}
		return DATABASE_POOL.get(name);
	}

	private String dbName;
	private List<Transaction> transactionList = new ArrayList<Transaction>();

	private static enum Action {
		CLEAR, SET
	}

	private static class Transaction {

		public Action action;
		public String key;
		public String value;
	}

	private synchronized void addTransaction(Transaction transaction) {
		//If the transaction list contains this key already, we can clear it
		//and add this one
		Iterator<Transaction> it = transactionList.iterator();
		while(it.hasNext()) {
			Transaction t = it.next();
			if(t.key.equals(transaction.key)) {
				it.remove();
			}
		}
		transactionList.add(transaction);
	}

	private synchronized void replayTransactions() {
		for(Transaction t : transactionList) {
			try {
				if(t.action == Action.CLEAR) {
					clearKey0(null, t.key.split("\\."));
				} else if(t.action == Action.SET) {
					set0(null, t.key.split("\\."), t.value);
				}
			} catch (Exception ex) {
				Logger.getLogger(MemoryDataSource.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		transactionList.clear();
	}

	private MemoryDataSource() {

	}

	public MemoryDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
		dbName = uri.getSchemeSpecificPart();
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		//Don't need to actively do anything
	}

	@Override
	protected synchronized void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		if(rollback) {
			transactionList.clear();
		} else {
			replayTransactions();
		}
	}

	@Override
	protected boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		String fKey = StringUtils.Join(key, ".");
		if(inTransaction()) {
			Transaction t = new Transaction();
			t.action = Action.SET;
			t.key = fKey;
			t.value = value;
			addTransaction(t);
		} else {
			getDatabase(dbName).put(fKey, value);
		}

		return true;
	}

	@Override
	protected synchronized String get0(String[] key) throws DataSourceException {
		String fKey = StringUtils.Join(key, ".");
		if(inTransaction()) {
			for(Transaction t : transactionList) {
				if(t.action == Action.SET && t.key.equals(fKey)) {
					return t.value;
				}
			}
			//Still not found, so it's an unaffected key.
			//Simply return from the real database.
		}
		return getDatabase(dbName).get(fKey);
	}

	@Override
	protected boolean hasKey0(String[] key) throws DataSourceException {
		return get0(key) != null;
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		String fKey = StringUtils.Join(key, ".");
		if(inTransaction()) {
			Transaction t = new Transaction();
			t.action = Action.CLEAR;
			t.key = fKey;
			addTransaction(t);
		} else {
			getDatabase(dbName).remove(StringUtils.Join(key, "."));
		}
	}

	@Override
	public Set<String> stringKeySet(String[] keyBase) throws DataSourceException {
		Set<String> keys = new TreeSet<String>();
		for(String[] key : keySet(keyBase)) {
			keys.add(StringUtils.Join(key, "."));
		}
		return keys;
	}

	@Override
	public synchronized Set<String[]> keySet(String[] keyBase) throws DataSourceException {
		Set<String> set = new HashSet<String>();
		for(String key : getDatabase(dbName).keySet()) {
			set.add(key);
		}
		//Now go through the transactions and add things that are set, and
		//remove things that are cleared
		if(inTransaction()) {
			for(Transaction t : transactionList) {
				if(t.action == Action.CLEAR) {
					set.remove(t.key);
				} else if(t.action == Action.SET) {
					set.add(t.key);
				}
			}
		}
		Set<String[]> ret = new HashSet<String[]>();
		String kb = StringUtils.Join(keyBase, ".");
		for(String key : set) {
			if(key.startsWith(kb)) {
				ret.add(key.split("\\."));
			}
		}
		return ret;
	}

	@Override
	public void populate() throws DataSourceException {
		//Ignored
	}

	@Override
	public DataSourceModifier[] implicitModifiers() {
		return new DataSourceModifier[]{};
	}

	@Override
	public DataSourceModifier[] invalidModifiers() {
		//No modifiers are appropriate on here
		return DataSourceModifier.values();
	}

	@Override
	public String docs() {
		return "Temporary Memory {mem:databaseName} Creates a temporary database that exists in memory only. Since keys across"
				+ " databases are always unique anyways, the name for databaseName is irrelevant, but is required, so"
				+ " \"mem:default\" is a recommended configuration. There are no guarantees to how long the data will stay around (in either"
				+ " how short of how long the data will be kept), except"
				+ " that it is guaranteed that within an execution unit, that data will continue to exist. This causes it to"
				+ " work much like import() and export(). Data stored this way is inaccessible to external processes, because it"
				+ " exists only in the process's memory space.";
	}

	@Override
	public MSVersion since() {
		return MSVersion.V3_3_1;
	}

}
