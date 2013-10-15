package com.laytonsmith.database;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class defines the highest level functionality that all database
 * connections provide. This class manages most of the lower level
 * connection management, including creating connections as needed, and closing them
 * after they aren't used for a while, as well as turning the verbose and type safe java methods
 * for SQL access to generic, scriptable types. Subclasses should always provide a no arg
 * constructor (though it can be private if desired) and this class will provide all the
 * connection details before a connection is actually established, and will otherwise
 * manage the connection details for the most part, delegating to the subclass
 * only when required.
 */
public abstract class DB {
	
	private static final Map<String, Class<? extends DB>> instanceTypes = new HashMap<String, Class<? extends DB>>();
	
	static {
		for(Class<DB> c : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(DBConnector.class, DB.class)){
			DBConnector dbc = c.getAnnotation(DBConnector.class);
			addDBInstance(dbc.value(), c);
		}
	}
	
	public static void addDBInstance(String type, Class<? extends DB> connector){
		instanceTypes.put(type, connector);
	}
	
	/**
	 * Returns a DB subclass given the DB type as a string. Further operations
	 * should be carried out through the given instance.
	 * @param type
	 * @return 
	 * @throws IllegalArgumentException If the string type is unsupported.
	 * @throws java.lang.InstantiationException
	 * @throws java.lang.IllegalAccessException
	 */
	public static DB getDBInstance(String type) throws IllegalArgumentException, InstantiationException, IllegalAccessException {
		for(String s : instanceTypes.keySet()){
			if(s.equalsIgnoreCase(type)){
				//Found it.
				return instanceTypes.get(s).newInstance();
			}
		}
		throw new IllegalArgumentException("Could not find an SQL adaptor for \"" + type + "\"");
	}
}
