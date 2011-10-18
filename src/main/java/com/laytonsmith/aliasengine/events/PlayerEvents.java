/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.api;
import java.util.Map;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 *
 * @author layton
 */
public class PlayerEvents {
    public static String docs(){
        return "Contains events related to a player";
    }
    
    @api public static class player_login implements Event{

        public String getName() {
            return "player_login";
        }

        public String docs() {
            return "{player_name: <string match>} This event is called when a player logs in "
                    + "{player_name: The player's name}";
        }

        public String since() {
            return "3.3.0";
        }
        
        public org.bukkit.event.Event.Type driver(){
            return org.bukkit.event.Event.Type.PLAYER_LOGIN;
        }

        public boolean matches(Map<String, Construct> prefilter, org.bukkit.event.Event e) {
            if(e instanceof PlayerLoginEvent){
                PlayerLoginEvent ple = (PlayerLoginEvent) e;
                if(prefilter.containsKey("player_name")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player_name").val())){
                        return false;
                    }
                }
                
                return true;
            }
            return false;
        }

        public void bind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void hook() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
