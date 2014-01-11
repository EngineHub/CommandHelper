
package com.laytonsmith.PureUtilities;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Layton
 */
public interface Persistence {
    /**
     * Loads the database from disk. This is automatically called when setValue or getValue is called.
     * @throws Exception
     */
    public void load() throws Exception;
    
    /**
     * Causes the database to be saved to disk.
	 * @param dm Optional, but if provided, activates a daemon thread.
     * @throws IOException
     */
    public void save(DaemonManager dm) throws Exception;
    
    /**
     * Adds or modifies the value of the key. Typically, this convention should be followed:
     * <pre>
     * key1.key2.key3...
     * </pre>
     * To make this usage easier, the function automatically namespaces the values for you. A sample
     * usage might be:
     * <pre>
     * setValue(new String[]{"playerName", "value"}, value);
     * </pre>
     *
     * When using namespaces in this way, the isNamespaceSet function becomes available to you.
     * Since plugin values are global, you can use this to interact with other plugins. Caution should
     * be used when interacting with other plugin's values though.
     * @param key The key for this particular value
     * @param value The value to store. If value is null, the key is simply removed.
     * @return The object that was in this key, or null if the value did not exist.
     */
    public String setValue(DaemonManager dm, String[] key, String value);
    
    /**
     * Returns the value of a particular key
     * @param key
     * @return
     */
    public String getValue(String[] key);
    
    /**
     * Checks to see if a particular key is set. Unlike isNamespaceSet, this requires that
     * the exact key be specified to see if it exists.
     * @param key
     * @return
     */
    public boolean isKeySet(String[] key);
    
    /**
     * Returns whether or not a particular namespace value is set. For instance, if the
     * value plugin.myPlugin.players.playerName.data is set, then the call to
     * <code>isNamespaceSet(new String[]{"plugin", "myPlugin"})</code> would return
     * <code>true</code>
     * @param partialKey
     * @return
     */
    public boolean isNamespaceSet(String[] partialKey);
    
    /**
     * Returns all the matched namespace entries.
     * @param partialKey The partial name of the keys you wish to return
     * @return An ArrayList of Map.Entries.
     */
    public List<Map.Entry<String, Object>> getNamespaceValues(String[] partialKey);
    
    /**
     * Prints all of the stored values to the specified stream.
     */
    public void printValues(PrintStream out);
    
    /**
     * Clears out all of the data from this persistence object. For the love
     * of God, don't call this from anywhere but the data manager!
     */
    public void clearAllData();
}
