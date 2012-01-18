package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
     * This is the data structure that the registry is stored in
     */
    private HashMap<String, Serializable> data = new HashMap<String, Serializable>();
    private boolean isLoaded = false;
    /**
     * The storage location of the persistance database. Note that it is package private,
     * so it can be changed.
     */
    File storageLocation;

    Object user;
    
    public SerializedPersistance(File database, Object user){
        storageLocation = database;
        this.user = user;
    }
    
    /**
     * Unless you're the data manager, don't use this method.
     * @return 
     */
    public HashMap<String, Serializable> rawData(){
        return data;
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
     * Loads the database from disk. This is automatically called when setValue or getValue is called.
     * @throws Exception
     */
    public synchronized void load() throws Exception {
        try {
            FileInputStream fis = null;
            ObjectInputStream in = null;
            fis = new FileInputStream(storageLocation);
            in = new ObjectInputStream(fis);
            HashMap<String, Serializable> tempData = (HashMap<String, Serializable>) in.readObject();
            String[] myNamespace;
            if(user != null){
                myNamespace = ("plugin." + user.getClass().getCanonicalName()).split("\\.");
            } else {
                //We're running from the command line
                data = tempData;
                return;
            }
            Iterator i = tempData.entrySet().iterator();
            while (i.hasNext()) {
                String[] key = ((Map.Entry)i.next()).getKey().toString().split("\\.");
                boolean match = true;
                for(int j = 0; j < myNamespace.length; j++){
                    if(key.length > myNamespace.length){
                        if(!key[j].equals(myNamespace[j])){
                            match = false;
                            break;
                        }
                    }
                }
                if(match){
                    data.put(getNamespace(key), tempData.get(getNamespace(key)));
                }
            }
            in.close();
            isLoaded = true;
        } catch (FileNotFoundException ex){
            //ignore this one
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Causes the database to be saved to disk
     * @throws IOException
     */
    public synchronized void save() throws Exception {
        try {
            HashMap<String, Serializable> tempData;
            try{
                FileInputStream fis = null;
                ObjectInputStream in = null;
                fis = new FileInputStream(storageLocation);
                in = new ObjectInputStream(fis);
                tempData = (HashMap<String, Serializable>) in.readObject();
            } catch(FileNotFoundException ex){
                tempData = new HashMap<String, Serializable>();
            }
            Iterator i = data.entrySet().iterator();
            ArrayList<String> toRemove = new ArrayList<String>();
            while (i.hasNext()) {
                String key = ((Map.Entry)i.next()).getKey().toString();
                if(data.get(key) == null){
                    tempData.remove(key);
                    toRemove.add(key);
                } else{
                    tempData.put(key, data.get(key));
                }
            }
            for(String s : toRemove){
                data.remove(s);
            }
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            storageLocation.getParentFile().mkdirs();
            if(!storageLocation.exists())
                storageLocation.createNewFile();
            fos = new FileOutputStream(storageLocation);
            out = new ObjectOutputStream(fos);
            out.writeObject(tempData);
            out.close();
            System.out.println("Persistance saved into " + this.hashCode());
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
            System.out.println("Putting in " + key);
            data.put(key, value);
        }
        return oldVal;
    }


    private synchronized Object getValue(String key) {
        //defer loading until we actually try and use the data structure
        if (isLoaded == false) {
            System.out.println("Loading values for " + user.getClass().getCanonicalName());
            try {
                load();
            } catch (Exception ex) {
                Logger.getLogger(SerializedPersistance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        key = "plugin." + user.getClass().getCanonicalName() + "." + key;
        if(data == null){
            return null;
        }
        return data.get(key);
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

    /**
     * Returns the value of a particular key
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
        k = "plugin." + user.getClass().getCanonicalName() + "." + k;
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
        m = "plugin." + user.getClass().getCanonicalName() + "." + m;
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
     * Returns all the matched namespace entries.
     * @param partialKey The partial name of the keys you wish to return
     * @return An ArrayList of Map.Entries.
     */
    public synchronized ArrayList<Map.Entry> getNamespaceValues(String[] partialKey){

        ArrayList<Map.Entry> matches = new ArrayList<Map.Entry>();
        String m = getNamespace(partialKey);
        m = "plugin." + user.getClass().getCanonicalName() + "." + m;
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

    public static void main(String[] args) throws Exception{
        SerializedPersistance p = new SerializedPersistance(new File("plugins/CommandHelper/persistance.ser"), new Object());
        p.setValue(new String[]{"player", "wraithguard01", "name"}, "wraithguard01");
        p.setValue(new String[]{"player", "wraithguard01", "age"}, "22");
        p.setValue(new String[]{"player", "other", "name"}, "other");
        System.out.println(p.getNamespaceValues(new String[]{"player", "wraithguard01", "age"}));
        System.out.println();
        p.save();
    }

}
