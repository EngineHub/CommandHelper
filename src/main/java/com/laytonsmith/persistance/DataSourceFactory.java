package com.laytonsmith.persistance;

import com.laytonsmith.annotations.datasource;
import com.laytonsmith.PureUtilities.ClassDiscovery;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This utility class provides the means to interact with given data sources.
 * @author lsmith
 */
public class DataSourceFactory {
    
    public static DataSource GetDataSource(String uri) throws DataSourceException, URISyntaxException{
        return GetDataSource(new URI(uri));
    }
    
    public static DataSource GetDataSource(URI uri) throws DataSourceException{
        init();
        List<DataSource.DataSourceModifier> modifiers = new ArrayList<DataSource.DataSourceModifier>();
        while(DataSource.DataSourceModifier.isModifier(uri.getScheme())){
            modifiers.add(DataSource.DataSourceModifier.getModifier(uri.getScheme()));
            try {
                uri = new URI(uri.getSchemeSpecificPart());
            }
            catch (URISyntaxException ex) {
                throw new DataSourceException(null, ex);
            }
        }
        Class c = protocolHandlers.get(uri.getScheme());
        if(c == null){
            throw new DataSourceException("Invalid scheme: " + uri.getScheme());
        }
        try {
            DataSource ds = (DataSource)c.getConstructor(URI.class).newInstance(uri);
            for(DataSource.DataSourceModifier m : modifiers){
                ds.addModifier(m);
            }
            try{
                if(ds instanceof AbstractDataSource){
                    ((AbstractDataSource)ds).checkModifiers();
                }
            } catch(DataSourceException e){
                //TODO: Do something other than this
                System.err.println(e.getMessage());
            }
            return ds;
        } catch (Exception ex) {
            throw new DataSourceException("Could not instantiate a DataSource for " + c.getName(), ex);
        }        
    }
    
    public static String Get(DataSource ds, String [] key) throws DataSourceException{
        if(ds.getModifiers().contains(DataSource.DataSourceModifier.TRANSIENT)){
            ds.populate();
        }
        return ds.get(key);
    }
    
    public static void Set(DataSource ds, String [] key, String value){
        
    }
    
    private static Map<String, Class> protocolHandlers;
    private static void init(){
        if(protocolHandlers == null){
            protocolHandlers = new HashMap<String, Class>();
            Class [] classes = ClassDiscovery.GetClassesWithAnnotation(datasource.class);
            for(Class c : classes){
                if(DataSource.class.isAssignableFrom(c)){
                    protocolHandlers.put(((datasource)c.getAnnotation(datasource.class)).value(), c);
                } else {
                    throw new Error(c.getName() + " does not implement DataSource!");
                }
            }
        }
    }
}
