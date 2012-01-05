/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.api;
import com.laytonsmith.aliasengine.events.AbstractEventHandler;
import com.laytonsmith.aliasengine.events.AbstractEventMixin;
import com.laytonsmith.aliasengine.events.Prefilters;
import com.laytonsmith.aliasengine.events.Prefilters.PrefilterType;
import com.laytonsmith.aliasengine.events.abstraction;
import com.laytonsmith.aliasengine.exceptions.EventException;
import com.laytonsmith.aliasengine.exceptions.PrefilterNonMatchException;
import java.util.Map;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author layton
 */
public class BukkitPlayerEvents {
    @api
    @abstraction(load=com.laytonsmith.aliasengine.events.PlayerEvents.player_join.class,
            type=Implementation.Type.BUKKIT)
    public static class player_join implements AbstractEventHandler{
        public boolean matches(Map<String, Construct> prefilter, Object e) throws PrefilterNonMatchException {
            if(e instanceof PlayerJoinEvent){
                PlayerJoinEvent ple = (PlayerJoinEvent) e;
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player_name").val())){
                        return false;
                    }
                }                
                Prefilters.match(prefilter, "join_message", ple.getJoinMessage(), PrefilterType.REGEX);
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(Object e, AbstractEventMixin mixin) throws EventException {
            if(e instanceof PlayerJoinEvent){
                PlayerJoinEvent ple = (PlayerJoinEvent) e;
                Map<String, Construct> map = mixin.evaluate_helper(e);
                map.put("player", new CString(ple.getPlayer().getName(), 0, null));
                map.put("join_message", new CString(ple.getJoinMessage(), 0, null));
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
        }
        
        public Object convert(CArray manual){
            PlayerJoinEvent e = new PlayerJoinEvent(((BukkitMCPlayer)Static.GetPlayer(manual.get("player").val(), 0, null))._Player(), manual.get("join_message").val());
            return e;
        }

        public void modifyEvent(String key, Construct value, Object event) {
            if(event instanceof PlayerJoinEvent){
                PlayerJoinEvent pje = (PlayerJoinEvent)event;
                if(key.equals("join_message")){
                    pje.setJoinMessage(value.val());
                }
            }
        }
    }
}
