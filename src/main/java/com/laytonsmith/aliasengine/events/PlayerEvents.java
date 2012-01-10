/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.docs;


/**
 *
 * @author layton
 */
public class PlayerEvents {
    public static String docs(){
        return "Contains events related to a player";
    }
    
    @docs(type= docs.type.EVENT)
    public static class player_join extends AbstractEvent{
        
        public player_join(AbstractEventHandler handler){
            super(handler);
        }

        public String getName() {
            return "player_join";
        }

        public String docs() {
            return "{player: <string match> |"
                    + "join_message: <regex>} This event is called when a player logs in "
                    + "{player: The player's name | join_message: The default join message}"
                    + "{player|join_message}"
                    + "{join_message}";
        }

        public String since() {
            return "3.3.0";
        }
        
        public Type driver(){
            return Type.PLAYER_JOIN;
        }

        
        
    }
    
    @docs(type= docs.type.EVENT)
    public static class player_interact extends AbstractEvent{
        
        public player_interact(AbstractEventHandler handler){
            super(handler);
        }

        public String getName() {
            return "player_interact";
        }

        public String docs() {
            return "{block: <item match> If the block the player interacts with is this"
                    + " | button: <string match> left or right. If they left or right clicked |"
                    + " item: <item match> The item they are holding when they interacted |"
                    + " player: <string match> The player that triggered the event} "
                    + "Fires when a player left or right clicks a block or the air"
                    + "{action: One of either: left_click_block, right_click_block, left_click_air, or right_click_air |"
                    + "block: The id of the block they clicked, or 0 if they clicked the air. If they clicked the air, "
                    + " neither facing or location will be present. |"
                    + "player: The player associated with this event |"
                    + "facing: The (lowercase) face of the block they clicked. See <<jd:[bukkit]org.bukkit.block.BlockFace>> for"
                    + " the possible values |"
                    + "location: The (x, y, z, world) location of the block they clicked}"
                    + "{player|action|item|location|facing}"
                    + "{}";
        }

        public String since() {
            return "3.3.0";
        }

        public Type driver() {
            return Type.PLAYER_INTERACT;
        }

        
        
    }
}
