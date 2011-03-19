package com.laytonsmith.Persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

/**
 * This file allows for simple data storage across many different data sources. In general, the
 * most common methods used are getValue and setValue. Note that getValue, setValue, save, and
 * load are synchronized.
 * @author layton
 */
public class Persistance {

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
    
    public Persistance(File database, Plugin user){
        storageLocation = database;
        this.user = user;
    }

    /**
     * Private constructor, used for testing this class
     * @param database
     * @param user
     */
    private Persistance(File database, Object user){
        storageLocation = database;
        this.user = user;
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
            String[] myNamespace = ("plugin." + user.getClass().getCanonicalName()).split("\\.");
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
            while (i.hasNext()) {
                String key = ((Map.Entry)i.next()).getKey().toString();
                tempData.put(key, data.get(key));
            }
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            storageLocation.getParentFile().mkdirs();
            storageLocation.createNewFile();
            fos = new FileOutputStream(storageLocation);
            out = new ObjectOutputStream(fos);
            out.writeObject(tempData);
            out.close();
            System.out.println("Persistance saved");
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
                Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        key = "plugin." + user.getClass().getCanonicalName() + "." + key;
        Serializable oldVal = data.get(key);
        data.put(key, value);
        return oldVal;
    }


    private synchronized Object getValue(String key) {
        //defer loading until we actually try and use the data structure
        if (isLoaded == false) {
            try {
                load();
            } catch (Exception ex) {
                Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        key = "plugin." + user.getClass().getCanonicalName() + "." + key;
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
     * @param value The value to store
     * @return The object that was in this key, or null if the value did not exist.
     */
    public synchronized Object setValue(String[] key, Serializable value) {
        return setValue(getNamespace(key), value);
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
     * Prints all of the stored values to std out.
     */
    public synchronized void printValues() {
        Iterator i = data.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = ((Map.Entry) i.next());
            System.out.println(e.getKey()
                    + ":("
                    + data.get(e.getKey().toString()).getClass().getCanonicalName()
                    + ") "
                    + data.get(e.getKey().toString()).toString());
        }
    }

    public static void main(String[] args) throws Exception{
        Persistance p = new Persistance(new File("plugins/CommandHelper/persistance.ser"), new Object());
        p.setValue(new String[]{"player", "wraithguard01", "name"}, "wraithguard01");
        p.setValue(new String[]{"player", "wraithguard01", "age"}, "22");
        p.setValue(new String[]{"player", "other", "name"}, "other");
        System.out.println(p.getNamespaceValues(new String[]{"player"}));
        System.out.println();
        System.out.println(p.getNamespaceValues(new String[]{"player", "wraithguard01"}));
        p.save();
    }

}
