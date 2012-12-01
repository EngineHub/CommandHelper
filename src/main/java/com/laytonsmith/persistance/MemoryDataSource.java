package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixin;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author lsmith
 */
@datasource("mem")
public final class MemoryDataSource extends AbstractDataSource {
	
	private static final Map<String, Map<String, String>> databasePool = new TreeMap<String, Map<String, String>>();
	
	/**
	 * Clears all data from all databases. Should be called when a natural reload type operation is called.
	 */
	public static void ClearDatabases(){
		//Loop through all the dbs, and clear each one, then clear the top level
		for(Map m : databasePool.values()){
			m.clear();
		}
		databasePool.clear();
	}
	
	private Map<String, String> database;
	private String dbName;
	
	private MemoryDataSource(){
		
	}
	
	public MemoryDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);
		dbName = uri.getSchemeSpecificPart();
	}
	
	private void init(){
		if(database == null){
			if(!databasePool.containsKey(dbName)){
				databasePool.put(dbName, new TreeMap<String, String>());
			}
			database = databasePool.get(dbName);		
		}
	}

	@Override
	protected boolean set0(String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		init();
		database.put(StringUtils.Join(key, "."), value);
		return true;
	}

	@Override
	protected String get0(String[] key, boolean bypassTransient) throws DataSourceException {
		init();
		return database.get(StringUtils.Join(key, "."));
	}

	@Override
	protected boolean hasKey0(String[] key) throws DataSourceException {
		init();
		return database.containsKey(StringUtils.Join(key, "."));
	}

	@Override
	protected void clearKey0(String[] key) throws ReadOnlyException, DataSourceException, IOException {
		init();
		database.remove(StringUtils.Join(key, "."));
	}
	

	@Override
	public Set<String> stringKeySet() throws DataSourceException {
		init();
		return new TreeSet<String>(database.keySet());
	}

	public Set<String[]> keySet() throws DataSourceException {
		init();
		Set<String[]> set = new HashSet<String[]>();
		for(String key : database.keySet()){
			set.add(key.split("\\."));
		}
		return set;
	}

	public void populate() throws DataSourceException {
		//Ignored
	}

	public DataSourceModifier[] implicitModifiers() {
		return new DataSourceModifier[]{};
	}

	public DataSourceModifier[] invalidModifiers() {
		//No modifiers are appropriate on here
		return DataSourceModifier.values();
	}

	public String docs() {
		return "Temporary Memory {mem:databaseName} Creates a temporary database that exists in memory only. Since keys across"
				+ " databases are always unique anyways, the name for databaseName is irrelevant, but is required, so"
				+ " \"mem:default\" is a recommended configuration. There are no guarantees to how long the data will stay around (in either"
				+ " how short of how long the data will be kept), except"
				+ " that it is guaranteed that within an execution unit, that data will continue to exist. This causes it to"
				+ " work much like import() and export(). Data stored this way is inaccessible to external processes, because it"
				+ " exists only in the process's memory space.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
	
}
