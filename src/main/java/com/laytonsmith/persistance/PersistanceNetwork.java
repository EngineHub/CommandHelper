package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.FileUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.util.FileUtil;

/**
 * A persistance network is a group of data sources that can act transparently
 * as a single data source. The defining feature of a network is the filter configuration, 
 * which defines where exactly all the keys are mapped to, and the type of data sources
 * in the network. To build a network, all you need it the configuration and a default
 * storage protocol. Everything else is handled accordingly. The main operations of a persistance
 * network are setting values, getting values, and getting multiple values at once, based on a
 * namespace match. All other aspects of how the data is stored and retrieved are abstracted,
 * so you needn't worry about any of those details.
 * @author lsmith
 */
public class PersistanceNetwork {
    
    private DataSourceFilter filter;
    private Map<URI, DataSource> dsCache;
    /**
     * Given a configuration and a default URI, constructs a new
     * persistance network. The defaultURI is used in the event that the
     * configuration does not specify a "**" key, to ensure that all keys
     * will be matched.
     * @param configuration
     * @param defaultURI 
     */
    public PersistanceNetwork(File configuration, URI defaultURI) throws FileNotFoundException, DataSourceException{
        this(FileUtility.read(configuration), defaultURI);
    }
    /**
     * Given a configuration and a default URI, constructs a new
     * persistance network. The defaultURI is used in the event that the
     * configuration does not specify a "**" key, to ensure that all keys
     * will be matched.
     * @param configuration 
     * @param defaultURI 
     */
    public PersistanceNetwork(String configuration, URI defaultURI) throws DataSourceException{
        filter = new DataSourceFilter(configuration, defaultURI);
        dsCache = new TreeMap<URI, DataSource>();
        //Data sources are lazily loaded, so we don't need to do anything right now to load them.
    }
    
    private DataSource getDataSource(URI uri) throws DataSourceException{        
        if(!dsCache.containsKey(uri)){
            dsCache.put(uri, DataSourceFactory.GetDataSource(uri));
        }
        return dsCache.get(uri);
    }
    
    public synchronized String get(String [] key) throws DataSourceException{        
        DataSource ds = getDataSource(filter.getConnection(key));
        return ds.get(key, false);
    }
    
    public synchronized boolean set(String [] key, String value) throws DataSourceException, ReadOnlyException, IOException{
        DataSource ds = getDataSource(filter.getConnection(key));
        return ds.set(key, value);
    }
    
    /**
     * This method returns a list of all keys and values that match the namespace.
     * If a.b.c is requested, then keys (and values) a.b.c.d and a.b.c.e would be returned.
     * @param namespace
     * @return 
     */
    public synchronized Map<String[], String> getNamespace(String [] namespace) throws DataSourceException{
        //TODO: Captures make this very hard to do, and in some cases, make it impossible. Without a comprehensive
        //list of possible keys, it's impossible to actually gather all the key's possible locations.
        List<URI> uris = filter.getAllConnections(namespace);
        //First we have to get the namespaces. We can get a list of all the connections
        //we need to search, then grab all the data in them, but then we need to use
        //just a plain get() to grab the data, based on each key, because we don't
        //want to accidentally grab a "hidden" value in another data source.
        List<String[]> keysToGrab = new ArrayList<String[]>();
        for(URI uri : uris){
            keysToGrab.addAll(getDataSource(uri).getNamespace(namespace));
        }
        //Ok, now the keys to grab are all populated, so let's walk through them and build our map
        Map<String[], String> map = new HashMap<String[], String>();
        for(String[] key : keysToGrab){
            map.put(key, get(key));
        }
        return map;
    }
}
