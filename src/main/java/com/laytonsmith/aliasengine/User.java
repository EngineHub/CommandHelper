/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import com.laytonsmith.PureUtilities.SerializedPersistance;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.abstraction.MCPlayer;

/**
 *
 * @author layton
 */
public class User {
    MCPlayer player;
    SerializedPersistance persist;
    

    public User(MCPlayer player, SerializedPersistance persist){
        //(new PermissionsResolverServerListener(perms)).register(this);
        this.player = player;
        this.persist = persist;
    }

    public int addAlias(String alias){
        try {
            ArrayList<Map.Entry> list = persist.getNamespaceValues(new String[]{player.getName(), "aliases"});
            System.out.println(list);
            Integer nextValue = 0;
            for (Map.Entry e : list) {
                String[] x = e.getKey().toString().split("\\.");
                Integer thisX = Integer.parseInt(x[x.length - 1]);
                nextValue = Math.max(thisX + 1, nextValue + 1);
                System.out.println("Next Value is: " + nextValue);
            }
            persist.setValue(new String[]{player.getName(), "aliases", nextValue.toString()}, alias);
            persist.save();
            return nextValue;
        } catch (Exception ex) {
            player.sendMessage("Could not add the alias. Please check the error logs for more information.");
            Logger.getLogger("Minecraft").log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    public String getAlias(int id){
        return (String) persist.getValue(new String[]{player.getName(), "aliases", Integer.toString(id)});
    }

    public String getAllAliases(){
        ArrayList<Map.Entry> al = persist.getNamespaceValues(new String[]{player.getName(), "aliases"});
        StringBuilder b = new StringBuilder();
        System.out.println(al);
        for(Map.Entry e : al){
            String [] key = e.getKey().toString().split("\\.");
            b.append(MCChatColor.AQUA)
                    .append(key[key.length - 1])
                    .append(":")
                    .append(e.getValue().toString().substring(0, Math.min(e.getValue().toString().length(), 45)))
                    .append(e.getValue().toString().length() > 45?"...":"")
                    .append("\n");
        }
        if(al.isEmpty()){
            b.append(MCChatColor.AQUA).append("You have no aliases defined");
        }
        return b.toString();
    }

    public ArrayList<String> getAliasesAsArray(){
        ArrayList<Map.Entry> al = persist.getNamespaceValues(new String[]{player.getName(), "aliases"});
        StringBuilder b = new StringBuilder();
        ArrayList<String> commands = new ArrayList<String>();
        for(Map.Entry e : al){
            String [] key = e.getKey().toString().split("\\.");
            b.append(e.getValue().toString()).append("\n");
            commands.add(b.toString());
            b = new StringBuilder();
        }
        return commands;
    }

    public int getTotalAliases(){
        return persist.getNamespaceValues(new String[]{player.getName(), "aliases"}).size();
    }

    public void removeAlias(int id){
        try {            
            persist.setValue(new String[]{player.getName(), "aliases", Integer.toString(id)}, null);
            persist.save();
        } catch (Exception ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
