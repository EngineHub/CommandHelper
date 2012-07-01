package com.laytonsmith.PureUtilities;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file allows for simple data storage across many different data sources. In general, the
 * most common methods used are getValue and setValue. Note that getValue, setValue, save, and
 * load are synchronized.
 * @author layton
 */
public class SerializedPersistance implements Persistance{

    /**
     * Combines the String array into a single string
     * @param key
     * @return
     */
    private synchronized static String getNamespace(String[] key) {
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
    public static void main(String[] args) throws Exception{
        SerializedPersistance p = new SerializedPersistance(new File("plugins/CommandHelper/persistance.ser"), new Object());
        p.setValue(new String[]{"player", "wraithguard01", "name"}, "wraithguard01");
        p.setValue(new String[]{"player", "wraithguard01", "age"}, "22");
        p.setValue(new String[]{"player", "other", "name"}, "other");
        System.out.println(p.getNamespaceValues(new String[]{"player", "wraithguard01", "age"}));
        System.out.println();
        p.save();
    }
    /**
     * This is the data structure that the registry is stored in
     */
    private HashMap<String, Serializable> data = new HashMap<String, Serializable>();
    
    private boolean isLoaded = false;
    
    /**
     * The storage location of the persistance database. 
     */
    private File storageLocation;
    
    public SerializedPersistance(File database){
        storageLocation = database;
    }
    
    /**
     * @deprecated 
     * @param database
     * @param user 
     */
    public SerializedPersistance(File database, Object user){
        storageLocation = database;
    }

    /**
     * Unless you're the data manager, and you <em>really</em> want to clear
     * out the entire database, don't use this method. You must manually call
     * save after this, if you wish the changes to be written out to disk.
     */
    public void clearAllData(){
        data = new HashMap<String, Serializable>();
    }

    /**
     * Returns all the matched namespace entries.
     * @param partialKey The partial name of the keys you wish to return
     * @return An ArrayList of Map.Entries.
     */
    public synchronized List<Map.Entry<String, Object>> getNamespaceValues(String[] partialKey){

        List<Map.Entry<String, Object>> matches = new ArrayList<Map.Entry<String, Object>>();
        String m = getNamespace(partialKey);
        partialKey = m.split("\\.");
        if(!isLoaded){
            try {
                load();
            } catch (Exception ex) {
                Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Iterator i = data.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
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


    private synchronized Object getValue(String key) {
        //defer loading until we actually try and use the data structure
        if (isLoaded == false) {
            try {
                load();
            } catch (Exception ex) {
                Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(data == null){
            return null;
        }
        return data.get(key);
    }


    /**
     * Returns the value of a particular key, or null if the
     * key doesn't exist
     * @param key
     * @return
     */
    public synchronized Object getValue(String[] key) {
        return getValue(getNamespace(key));
    }

    /**
     * Checks to see if a particular key is set. Unlike isNamespaceSet, this requires that
     * the exact key be specified to see if it exists.
     * @param key
     * @return
     */
    public synchronized boolean isKeySet(String[] key) {
        String k = getNamespace(key);
        return data.containsKey(k);
    }

    /**
     * Returns whether or not a particular namespace value is set. For instance, if the
     * value plugin.myPlugin.players.playerName.data is set, then the call to
     * <code>isNamespaceSet(new String[]{"plugin", "myPlugin"})</code> would return
     * <code>true</code>
     * @param partialKey
     * @return
     */
    public synchronized boolean isNamespaceSet(String[] partialKey) {
        String m = getNamespace(partialKey);
        partialKey = m.split("\\.");
        Iterator i = data.entrySet().iterator();
        while (i.hasNext()) {
            String key = ((Map.Entry)i.next()).getKey().toString();
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
     * Loads the database from disk. This is automatically called when setValue or getValue is called.
     * @throws Exception
     */
    public synchronized void load() throws Exception {
        try {
            if(!isLoaded){
                FileInputStream fis = null;
                ObjectInputStream in = null;
                fis = new FileInputStream(storageLocation);
                in = new ObjectInputStream(fis);
                data = (HashMap<String, Serializable>) in.readObject();
                in.close();
                isLoaded = true;
            }            
        } catch (FileNotFoundException ex){
            //ignore this one
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Prints all of the stored values to the specified print stream.
     */
    public synchronized void printValues(PrintStream out) {
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

    /**
     * Unless you're the data manager, don't use this method.
     * @return 
     */
    public HashMap<String, Serializable> rawData(){
        return data;
    }

    /**
     * Causes the database to be saved to disk
     * @throws IOException
     */
    public synchronized void save() throws Exception {
        try {            
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            storageLocation.getParentFile().mkdirs();
            if(!storageLocation.exists())
                storageLocation.createNewFile();
            fos = new FileOutputStream(storageLocation);
            out = new ObjectOutputStream(fos);
            out.writeObject(data);
            out.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * You should not usually use this method. Please see <code>setValue(String[] key, Serializable value)</code>
     */
    private synchronized Object setValue(String key, Serializable value) {
        //defer loading until we actually try and use the data structure
        if (isLoaded == false) {
            try {
                load();
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
            }
        }
        Serializable oldVal = data.get(key);
        if(value == null){
            data.remove(key);
        } else {
            data.put(key, value);
        }
        try {
            save();
        } catch (Exception ex) {
            Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return oldVal;
    }

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
    public synchronized Object setValue(String[] key, Object value) {
        return setValue(getNamespace(key), (Serializable) value);
    }

}
