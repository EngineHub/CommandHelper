package com.laytonsmith.persistance;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import java.io.IOException;
import java.util.List;

/**
 * All data sources must implement this interface. It provides methods to
 * gather data about a data source, gather data from the data source, and (possibly)
 * write to the data source. 
 * @author lsmith
 */
public interface DataSource extends Documentation {    
    
    /**
     * Returns a list of keys stored in this interface.
     * @return 
     */
    public List<String> keySet();
    
    /**
     * Retrieves a value from the data source. 
     * @param key
     * @return 
     */
    public String get(String [] key);
    
    /**
     * Sets a value in the data source.
     * @param key
     * @param value
     * @return True if the value was changed, false otherwise.
     * @throws ReadOnlyException If this data source is inherently read only, 
     * it will throw a read only exception if this method is called.
     */
    public boolean set(String [] key, String value) throws ReadOnlyException, IOException;
    
    /**
     * Instructs this data source to repopulate its internal structure based on
     * this data provided. The method will be called if the data source needs to refresh itself, though for inherently transient
     * data sources (or if the transient data source flag is set), this method may do nothing.
     * If the data source is unable to populate itself, it may throw an exception
     * informing the user that there is no way to read the data at this time.
     */
    public void populate() throws DataSourceException;   
    
    /**
     * For this instance of the data source, adds a modifier flag to the data source.
     * This does not preclude a data source from inherently having certain flags,
     * nor will having a flag in the array returned by invalidModifiers() preclude it
     * from being set here.
     * Some settings may not need to be inherently acted upon, however they may
     * be referenced for informational purposes if nothing else.
     * @param modifier 
     */
    public void addModifier(DataSourceModifier modifier);
    
    /**
     * If a data source always has a particular modifier, it should return those here.
     * This is used to determine when to display configuration warnings, if a modifier is
     * used in cases where it is implied. If the array would be empty, null may be returned.
     * @param modifier
     * @return 
     */
    public DataSourceModifier[] implicitModifiers();
    
    /**
     * If a data source has no possible way of acting on a modifier, it should return those here.
     * This is used to determine when to display configuration warnings, if a modifier is used in cases
     * where it can't be acted on. If the array would be empty, null may be returned.
     * @return 
     */
    public DataSourceModifier[] invalidModifiers();
    
    /**
     * Returns a list of modifiers attached to this data source instance.
     * @return 
     */
    public List<DataSourceModifier> getModifiers();
    
    /**
     * These are the valid modifiers for a generic connection. Not all data sources can support
     * all of these, and some are inherently present or unsupportable on certain connection types.
     */
    public enum DataSourceModifier implements Documentation{
        READONLY("Makes the connection read-only. That is to say, calls to store_data() on the keys mapped to this data source will always fail.", CHVersion.V3_3_1),
        TRANSIENT("The data from this source is not cached. Note that for file based data sources, this makes it incredibly inefficient for large data sources,"
                + " but makes it possible for multiple things to read and write to a source at the same time. If the connection is not read-only, a lock file will"
                + " be created while the file is being written to (which will be the filename with .lock appended), which should be respected by other applications"
                + " to prevent corruption. During read/write operations, if the lock file exists, the call to retrieve that data will block until the lock file"
                + " goes away. File based connections that are NOT transient are loaded up at startup, and only writes require file system access from that point"
                + " on. It is assumed that nothing else will be editing the data source, and so data is not re-read again, which means that leaving off the transient"
                + " flag makes connections much more efficient. Database driven connections are always transient. ", CHVersion.V3_3_1),
        HTTP("Makes the connection source be retrieved via http instead of assuming a local file. Connections via http are always read-only."
                + " If the connection is also transient, a call to get_value() cannot be used in synchronous mode, and will fail if async"
                + " mode is not used. ", CHVersion.V3_3_1),
        HTTPS("Makes the connection source be retrieved via https instead of assuming a local file. Connections via http are always read-only."
                + " If the connection is also transient, a call to get_value() cannot be used in synchronous mode, and will fail if async"
                + " mode is not used. ", CHVersion.V3_3_1),
        ASYNC("Forces retrievals to this connection to require asyncronous usage. This is handy if an otherwise blocking data source has gotten"
                + " too large to allow synchonous connections, or if you are using a medium/large data source transiently.", CHVersion.V3_3_1);        
        
        
        private CHVersion since;
        private String documentation;
        private DataSourceModifier(String documentation, CHVersion since){
            this.documentation = documentation;
            this.since = since;
        }

        public String getName() {
            return name().toLowerCase();
        }

        public String docs() {
            return documentation;
        }

        public CHVersion since() {
            return since;
        }
        
        public static boolean isModifier(String scheme){
            for(DataSourceModifier modifier : DataSourceModifier.values()){
                if(modifier.getName().equalsIgnoreCase(scheme)){
                    return true;
                }
            }
            return false;
        }
        
        public static DataSourceModifier getModifier(String scheme){
            return DataSourceModifier.valueOf(scheme.toUpperCase());
        }
    }
}
