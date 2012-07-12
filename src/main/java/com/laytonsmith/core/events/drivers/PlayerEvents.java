/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events.drivers;

import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.StringHandling;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author layton
 */
public class PlayerEvents {
    public static String docs(){
        return "Contains events related to a player";
    }
    
    @api
    public static class player_prelogin extends AbstractEvent {

		public String getName() {
			return "player_prelogin";
		}

		public String docs() {
			return "{player: <string match>} "
					+ "This event is called when a player is about to be authed. "
					+ "This event only fires if your server is in online mode. "
                    + "This event cannot be cancelled. Instead, you can deny them by setting "
					+ "'result' to KICK_BANNED, KICK_WHITELIST, KICK_OTHER, or KICK_FULL. "
                    + "The default for 'result' is ALLOWED. When setting 'result', you "
                    + "can specify the kick message by modifying 'kickmsg'. "
                    + "{player: The player's name | kickmsg: The default kick message | "
                    + "ip: the player's IP address | result: the default response to their logging in}"
                    + "{kickmsg|result}"
                    + "{player|kickmsg|ip|result}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent)e;
                if(prefilter.containsKey("player")){
                    if(!event.getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }      
			}
			
			return true;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                
                map.put("player", new CString(event.getName(), Target.UNKNOWN));
                map.put("ip", new CString(event.getIP(), Target.UNKNOWN));
                map.put("result", new CString(event.getResult(), Target.UNKNOWN));
                map.put("kickmsg", new CString(event.getKickMessage(), Target.UNKNOWN));
                
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerPreLoginEvent");
            }
		}

		public Driver driver() {
			return Driver.PLAYER_PRELOGIN;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent e) {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent)e;
                if (key.equals("result")) {
                	String[] possible = new String[] {"ALLOWED", "KICK_WHITELIST",
                			"KICK_BANNED", "KICK_FULL", "KICK_OTHER"};
                	if(Arrays.asList(possible).contains(value.val().toUpperCase())) {
                		event.setResult(value.val().toUpperCase());
                	}
                } else if (key.equals("kickmsg")) {
                	event.setKickMessage(value.val());
                }
			}
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
    	
    }
    
    @api
    public static class player_login extends AbstractEvent {

		public String getName() {
			return "player_login";
		}

		public String docs() {
			return "{player: <string match>} "
					+ "This event is called when a player is about to log in. "
                    + "This event cannot be cancelled. Instead, you can deny them by setting "
					+ "'result' to KICK_BANNED, KICK_WHITELIST, KICK_OTHER, or KICK_FULL. "
                    + "The default for 'result' is ALLOWED. When setting 'result', you "
                    + "can specify the kick message by modifying 'kickmsg'. "
                    + "{player: The player's name | kickmsg: The default kick message | "
                    + "ip: the player's IP address | result: the default response to their logging in}"
                    + "{kickmsg|result}"
                    + "{player|kickmsg|ip|result}";
		}

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e)
				throws PrefilterNonMatchException {
			if(e instanceof MCPlayerPreLoginEvent){
                MCPlayerPreLoginEvent event = (MCPlayerPreLoginEvent)e;
                if(prefilter.containsKey("player")){
                    if(!event.getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }      
			}
			
			return true;
		}

		public BindableEvent convert(CArray manualObject) {
			return null;
		}

		public Map<String, Construct> evaluate(BindableEvent e)
				throws EventException {
			if(e instanceof MCPlayerLoginEvent){
                MCPlayerLoginEvent event = (MCPlayerLoginEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                
                map.put("player", new CString(event.getName(), Target.UNKNOWN));
                map.put("ip", new CString(event.getIP(), Target.UNKNOWN));
                map.put("result", new CString(event.getResult(), Target.UNKNOWN));
                map.put("kickmsg", new CString(event.getKickMessage(), Target.UNKNOWN));
                
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
		}

		public Driver driver() {
			return Driver.PLAYER_LOGIN;
		}

		public boolean modifyEvent(String key, Construct value,
				BindableEvent e) {
			if(e instanceof MCPlayerLoginEvent){
                MCPlayerLoginEvent event = (MCPlayerLoginEvent)e;
                if (key.equals("result")) {
                	String[] possible = new String[] {"ALLOWED", "KICK_WHITELIST",
                			"KICK_BANNED", "KICK_FULL", "KICK_OTHER"};
                	if(Arrays.asList(possible).contains(value.val().toUpperCase())) {
                		event.setResult(value.val().toUpperCase());
                	}
                } else if (key.equals("kickmsg")) {
                	event.setKickMessage(value.val());
                }
			}
			
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
        public void preExecution(Env env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerLoginEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerLoginEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        public void postExecution(Env env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerLoginEvent){
                MCPlayer player = ((MCPlayerLoginEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                Static.UninjectPlayer(player);
            }
        }
    	
    }
    
    @api
    public static class player_join extends AbstractEvent{

        public String getName() {
            return "player_join";
        }

        public String docs() {
            return "{player: <string match> |"
                    + "join_message: <regex>} This event is called when a player logs in. "
                    + "Setting join_message to null causes it to not be displayed at all. Cancelling "
                    + "the event does not prevent them from logging in. Instead, you should just kick() them."
                    + "{player: The player's name | join_message: The default join message}"
                    + "{join_message}"
                    + "{player|join_message}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
        public Driver driver(){
            return Driver.PLAYER_JOIN;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if(e instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent ple = (MCPlayerJoinEvent) e;
                if(prefilter.containsKey("player")){
                    if(!ple.getPlayer().getName().equals(prefilter.get("player").val())){
                        return false;
                    }
                }                
                Prefilters.match(prefilter, "join_message", ple.getJoinMessage(), Prefilters.PrefilterType.REGEX);
                return true;
            }
            return false;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent ple = (MCPlayerJoinEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //map.put("player", new CString(ple.getPlayer().getName(), Target.UNKNOWN));
                map.put("join_message", new CString(ple.getJoinMessage(), Target.UNKNOWN));
                return map;
            } else{
                throw new EventException("Cannot convert e to PlayerLoginEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if(event instanceof MCPlayerJoinEvent){
                MCPlayerJoinEvent pje = (MCPlayerJoinEvent)event;
                if(key.equals("join_message")){
                    if(value instanceof CNull){
                        pje.setJoinMessage(null);
                        return pje.getJoinMessage() == null;
                    } else {
                        pje.setJoinMessage(value.val());
                        return pje.getJoinMessage().equals(value.val());
                    }
                }
            }
            return false;
        }
        
        public BindableEvent convert(CArray manual){
            MCPlayerJoinEvent e = EventBuilder.instantiate(MCPlayerJoinEvent.class, Static.GetPlayer(manual.get("player").val(), Target.UNKNOWN), manual.get("join_message").val());
            return e;
        }
        
    }
    
    @api
    public static class player_interact extends AbstractEvent{

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
                    + "{}"
                    + "{player|action|item|location|facing}";
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Driver driver() {
            return Driver.PLAYER_INTERACT;
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent)e;
                if(((MCPlayerInteractEvent)e).getAction().equals(MCAction.PHYSICAL)){
                    return false;
                }
                if(prefilter.containsKey("button")){
                    if(pie.getAction().equals(MCAction.LEFT_CLICK_AIR) || pie.getAction().equals(MCAction.LEFT_CLICK_BLOCK)){
                        if(!prefilter.get("button").val().toLowerCase().equals("left")){
                            return false;
                        }
                    }
                    if(pie.getAction().equals(MCAction.RIGHT_CLICK_AIR) || pie.getAction().equals(MCAction.RIGHT_CLICK_BLOCK)){
                        if(!prefilter.get("button").val().toLowerCase().equals("right")){
                            return false;
                        }
                    }
                }
                
                Prefilters.match(prefilter, "item", Static.ParseItemNotation(pie.getItem()), PrefilterType.ITEM_MATCH);
                Prefilters.match(prefilter, "block", Static.ParseItemNotation(pie.getClickedBlock()), PrefilterType.ITEM_MATCH);
                Prefilters.match(prefilter, "player", pie.getPlayer().getName(), PrefilterType.MACRO);
                
                return true;
            }
            return false;
        }

        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if(e instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //map.put("player", new CString(pie.getPlayer().getName(), Target.UNKNOWN));
                MCAction a = pie.getAction();
                map.put("action", new CString(a.name().toLowerCase(), Target.UNKNOWN));
                map.put("block", new CString(Static.ParseItemNotation(pie.getClickedBlock()), Target.UNKNOWN));
                if(a == MCAction.LEFT_CLICK_AIR || a == MCAction.LEFT_CLICK_BLOCK){
                    map.put("button", new CString("left", Target.UNKNOWN));
                } else {
                    map.put("button", new CString("right", Target.UNKNOWN));
                }
                if(a == MCAction.LEFT_CLICK_BLOCK || a == MCAction.RIGHT_CLICK_BLOCK){
                    map.put("facing", new CString(pie.getBlockFace().name().toLowerCase(), Target.UNKNOWN));
                    MCBlock b = pie.getClickedBlock();
                    map.put("location", new CArray(Target.UNKNOWN, new CInt(b.getX(), Target.UNKNOWN),
                            new CInt(b.getY(), Target.UNKNOWN), new CInt(b.getZ(), Target.UNKNOWN), 
                            new CString(b.getWorld().getName(), Target.UNKNOWN)));
                }
                map.put("item", new CString(Static.ParseItemNotation(pie.getItem()), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerInteractEvent");
            }
        }
        
        public BindableEvent convert(CArray manual){
            MCPlayer p = Static.GetPlayer(manual.get("player"), Target.UNKNOWN);
            MCAction a = MCAction.valueOf(manual.get("action").val().toUpperCase());
            MCItemStack is = Static.ParseItemNotation("player_interact event", manual.get("item").val(), 1, Target.UNKNOWN);
            MCBlock b = ObjectGenerator.GetGenerator().location(manual.get("location"), null, Target.UNKNOWN).getBlock();
            MCBlockFace bf = MCBlockFace.valueOf(manual.get("facing").val());
            MCPlayerInteractEvent e = EventBuilder.instantiate(MCPlayerInteractEvent.class, p, a, is, b, bf);            
            return e;
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if(event instanceof MCPlayerInteractEvent){
                MCPlayerInteractEvent pie = (MCPlayerInteractEvent)event;
                
            }
            return false;
        }
        
    }
    
    @api
    public static class player_spawn extends AbstractEvent {        
        
        public String getName() {
            return "player_spawn";
        }
        
        public String docs() {
            return "{x: <expression>| y: <expression>| z: <expression>| world: <string match>| player: <macro>}"
                    + "Fires when a player respawns. Technically during this time, the player is not considered to be"
                    + " 'online'. This can cause problems if you try to run an external command with run() or something."
                    + " CommandHelper takes into account the fact that the player is offline, and works around this, so"
                    + " all CH functions should respond correctly, as if the player was online, however other plugins or"
                    + " plain text commands that are run may not."
                    + "{player: The player that is respawning | "
                    + "location: The location they are going to respawn at}"
                    + "{location}"
                    + "{player|location}";
        }
        
        public Driver driver() {
            return Driver.PLAYER_SPAWN;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent event = (MCPlayerRespawnEvent) e;
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);
                Prefilters.match(prefilter, "x", event.getRespawnLocation().getBlockX(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "y", event.getRespawnLocation().getBlockY(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "z", event.getRespawnLocation().getBlockZ(), PrefilterType.EXPRESSION);
                Prefilters.match(prefilter, "world", event.getRespawnLocation().getWorld().getName(), PrefilterType.STRING_MATCH);
                return true;
            }
            return false;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent event = (MCPlayerRespawnEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //the helper puts the player in for us
                CArray location = ObjectGenerator.GetGenerator().location(event.getRespawnLocation());
                map.put("location", location);
                return map;
            } else {
                throw new EventException("Cannot convert e to PlayerRespawnEvent");
            }
        }
        
        public BindableEvent convert(CArray manual) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            MCPlayer p = Static.GetPlayer(manual.get("player"));
            MCLocation l = ObjectGenerator.GetGenerator().location(manual.get("location"), p.getWorld(), Target.UNKNOWN);
            MCPlayerRespawnEvent e = EventBuilder.instantiate(MCPlayerRespawnEvent.class, p, l, false);
            return e;
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerRespawnEvent) {
                MCPlayerRespawnEvent e = (MCPlayerRespawnEvent) event;
                if (key.equals("location")) {
                    //Change this parameter in e to value
                    e.setRespawnLocation(ObjectGenerator.GetGenerator().location(value, e.getPlayer().getWorld(), Target.UNKNOWN));                    
                    return true;
                }
            }
            return false;
        }        

        public void preExecution(Env env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerRespawnEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerRespawnEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        public void postExecution(Env env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerRespawnEvent){
                MCPlayer player = ((MCPlayerRespawnEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                Static.UninjectPlayer(player);
            }
        }
    }
        
    
    @api
    public static class player_death extends AbstractEvent {
        
        public String getName() {
            return "player_death";
        }
        
        public String docs() {
            return "{player: <macro>}"
                    + "Fired when a player dies."
                    + "{player: The player that died | drops: An array of the dropped items"
                    + "| xp: The xp that will be dropped | cause: The cause of death | death_message: The"
                    + " death message}"
                    + "{xp|drops: An array of item objects, or null. The items to be dropped"
                    + " are replaced with the given items, not added to|death_message the death message,"
                    + " or null to remove it entirely}"
                    + "{player | drops | death_message}";
        }
        
        public Driver driver() {
            return Driver.PLAYER_DEATH;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerDeathEvent) {
                MCPlayerDeathEvent event = (MCPlayerDeathEvent) e;
                Prefilters.match(prefilter, "player", ((MCPlayer)event.getEntity()).getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }

        //We have an actual event now, change it into a Map
        //that will end up being the @event object
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerDeathEvent) {
                MCPlayerDeathEvent event = (MCPlayerDeathEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                CArray ca = new CArray(Target.UNKNOWN);
                for(MCItemStack is : event.getDrops()){                    
                    ca.push(ObjectGenerator.GetGenerator().item(is, Target.UNKNOWN));
                }
                MCPlayer p = (MCPlayer)event.getEntity();
                map.put("drops", ca);
                map.put("xp", new CInt(event.getDroppedExp(), Target.UNKNOWN));
                if(event instanceof MCPlayerDeathEvent){
                    map.put("death_message", new CString(((MCPlayerDeathEvent)event).getDeathMessage(), Target.UNKNOWN));
                }
                try{
                    map.put("cause", new CString(event.getEntity().getLastDamageCause().getCause().name(), Target.UNKNOWN));
                } catch(NullPointerException ex){
                    map.put("cause", new CString(MCDamageCause.CUSTOM.name(), Target.UNKNOWN));
                }
                map.put("location", ObjectGenerator.GetGenerator().location(p.getLocation()));
                return map;
            } else {
                throw new EventException("Cannot convert e to EntityDeathEvent");
            }
        }
        
        public BindableEvent convert(CArray manual) {
            //For firing off the event manually, we have to convert the CArray into an
            //actual object that will trigger it
            String splayer = manual.get("player").val();
            List<MCItemStack> list = new ArrayList<MCItemStack>();
            String deathMessage = manual.get("death_message").val();
            CArray clist = (CArray)manual.get("drops");
            for(String key : clist.keySet()){
                list.add(ObjectGenerator.GetGenerator().item(clist.get(key), clist.getTarget()));
            }            
            MCPlayerDeathEvent e = EventBuilder.instantiate(MCPlayerDeathEvent.class, Static.GetPlayer(splayer, Target.UNKNOWN), list,
                    0, deathMessage);
            return e;
        }

        //Given the paramters, change the underlying event
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerDeathEvent) {
                MCPlayerDeathEvent e = (MCPlayerDeathEvent) event;
                if (key.equals("xp")) {
                    //Change this parameter in e to value
                    e.setDroppedExp((int)Static.getInt(value));                    
                    return true;
                }
                if(key.equals("drops")){
                    if(value instanceof CNull){
                        value = new CArray(Target.UNKNOWN);
                    }
                    if(!(value instanceof CArray)){
                        throw new ConfigRuntimeException("drops must be an array, or null", Exceptions.ExceptionType.CastException, value.getTarget());
                    }
                    e.clearDrops();
                    CArray drops = (CArray) value;
                    for(String dropID : drops.keySet()){
                        e.addDrop(ObjectGenerator.GetGenerator().item(drops.get(dropID), Target.UNKNOWN));
                    }
                    return true;
                }
                if(event instanceof MCPlayerDeathEvent && key.equals("death_message")){
                    ((MCPlayerDeathEvent)event).setDeathMessage(value.nval());
                    return true;
                }
            }
            return false;
        }
    }
    
    @api
    public static class player_quit extends AbstractEvent {
        
        public String getName() {
            return "player_quit";
        }
        
        public String docs() {
            return "{player: <macro>}"
                    + "Fired when any player quits."
                    + "{message: The message to be sent}"
                    + "{message}"
                    + "{player|message}";
        }
        
        public Driver driver() {
            return Driver.PLAYER_QUIT;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerQuitEvent) {
                //As a very special case, if this player is currently in interpreter mode, we do not want to
                //intercept their chat event
                if(CommandHelperPlugin.self.interpreterListener.isInInterpreterMode(((MCPlayerQuitEvent)e).getPlayer())){
                    throw new PrefilterNonMatchException();
                }
                
                Prefilters.match(prefilter, "player", ((MCPlayerQuitEvent)e).getPlayer().getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }
        
        public BindableEvent convert(CArray manualObject) {
            //Get the parameters from the manualObject
            MCPlayer player = Static.GetPlayer(manualObject.get("player"));
            String message = manualObject.get("message").nval();
            
            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class, 
                player, message);
            return e;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerQuitEvent) {
                MCPlayerQuitEvent event = (MCPlayerQuitEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerQuitEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerQuitEvent) {
                MCPlayerQuitEvent e = (MCPlayerQuitEvent)event;
                if("message".equals(key)){
                    e.setMessage(value.nval());
                }
                return true;
            }
            return false;
        }
    }
    
    @api
    public static class player_chat extends AbstractEvent {
        
        public String getName() {
            return "player_chat";
        }
        
        public String docs() {
            return "{player: <macro>}"
                    + "Fired when any player attempts to send a chat message."
                    + "{message: The message to be sent | recipients}"
                    + "{message|recipients: An array of"
                    + " players that will recieve the chat message. If a player doesn't exist"
                    + " or is offline, and is in the array, it is simply ignored, no"
                    + " exceptions will be thrown.}"
                    + "{player|message}";
        }
        
        public Driver driver() {
            return Driver.PLAYER_CHAT;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }
        
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerChatEvent) {
                //As a very special case, if this player is currently in interpreter mode, we do not want to
                //intercept their chat event
                if(CommandHelperPlugin.self.interpreterListener.isInInterpreterMode(((MCPlayerChatEvent)e).getPlayer())){
                    throw new PrefilterNonMatchException();
                }
                Prefilters.match(prefilter, "player", ((MCPlayerChatEvent)e).getPlayer().getName(), PrefilterType.MACRO);
                return true;
            }
            return false;
        }
        
        public BindableEvent convert(CArray manualObject) {
            //Get the parameters from the manualObject
            MCPlayer player = Static.GetPlayer(manualObject.get("player"));
            String message = manualObject.get("message").nval();
            
            BindableEvent e = EventBuilder.instantiate(MCPlayerChatEvent.class, 
                player, message);
            return e;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent event = (MCPlayerChatEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("message", new CString(event.getMessage(), Target.UNKNOWN));
                CArray ca = new CArray(Target.UNKNOWN);
                for(MCPlayer recipient : event.getRecipients()){
                    ca.push(new CString(recipient.getName(), Target.UNKNOWN));
                }
                map.put("recipients", ca);
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerChatEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerChatEvent) {
                MCPlayerChatEvent e = (MCPlayerChatEvent)event;
                if("message".equals(key)){
                    e.setMessage(value.nval());
                }
                if("recipients".equals(key)){
                    if(value instanceof CArray){
                        List<MCPlayer> list = new ArrayList<MCPlayer>();
                        for(String index : ((CArray)value).keySet()){
                            Construct v = ((CArray)value).get(index);
                            try{
                                list.add(Static.GetPlayer(v));                                
                            } catch(ConfigRuntimeException ex){
                                //Ignored
                            }
                        }
                        e.setRecipients(list);
                    } else {
                        throw new ConfigRuntimeException("recipients must be an array", Exceptions.ExceptionType.CastException, value.getTarget());
                    }
                }
                return true;
            }
            return false;
        }
    }
    
    @api
    public static class player_command extends AbstractEvent {
        
        public String getName() {
            return "player_command";
        }
        
        public String docs() {
            return "{command: <string match> The entire command the player ran "
                    + "| prefix: <string match> Just the first part of the command, i.e. '/cmd' in '/cmd blah blah'"
                    + "| player: <macro> The player using the command}"
                    + "This event is fired off when a player runs any command at all. This actually fires before normal "
                    + " CommandHelper aliases, allowing you to insert control before defined aliases, even."
                    + "{command: The entire command | prefix: Just the prefix of the command}"
                    + "{command}"
                    + "{command}";
        }
        
        public Driver driver() {
            return Driver.PLAYER_COMMAND;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCPlayerCommandEvent) {
                MCPlayerCommandEvent event = (MCPlayerCommandEvent) e;
                String command = event.getCommand();
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);   
                if(prefilter.containsKey("command") && !command.equals(event.getCommand())){
                    return false;
                }
                if(prefilter.containsKey("prefix")){
                    StringHandling.parse_args pa = new StringHandling.parse_args();
                    CArray ca = (CArray)pa.exec(Target.UNKNOWN, null, new CString(command, Target.UNKNOWN));
                    if(ca.size() > 0){
                        if(!ca.get(0).val().equals(prefilter.get("prefix").val())){
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        
        public BindableEvent convert(CArray manualObject) {
            MCPlayer player = Static.GetPlayer(manualObject.get("player"));
            String command = manualObject.get("command").nval();
            
            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class, 
                player, command);
            return e;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCPlayerCommandEvent) {
                MCPlayerCommandEvent event = (MCPlayerCommandEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("command", new CString(event.getCommand(), Target.UNKNOWN));
                
                StringHandling.parse_args pa = new StringHandling.parse_args();
                CArray ca = (CArray)pa.exec(Target.UNKNOWN, null, new CString(event.getCommand(), Target.UNKNOWN));
                map.put("prefix", new CString(ca.get(0).val(), Target.UNKNOWN));
                
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerCommandEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCPlayerCommandEvent) {
                MCPlayerCommandEvent e = (MCPlayerCommandEvent) event;
                
                if("command".equals(key)){
                    e.setCommand(value.val());
                }
                
                return true;
            }
            return false;
        }

        @Override
        public void cancel(BindableEvent o, boolean state) {
            ((MCPlayerCommandEvent)o).cancel();
        }                
    }
    
    @api
    public static class world_changed extends AbstractEvent {
        
        public String getName() {
            return "world_changed";
        }
        
        public String docs() {
            return "{player: <macro> The player that switched worlds "
                    + "| from: <string match> The world the player is coming from "
                    + "| to: <string match> The world the player is now in}"
                    + "This event is fired off when a player changes worlds"
                    + "{player | from: The world the player is coming from | to: The world the player is now in}"
                    + "{}"
                    + "{player, from}";
        }
        
        public Driver driver() {
            return Driver.WORLD_CHANGED;
        }
        
        public CHVersion since() {
            return CHVersion.V3_3_1;
        }
        
        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            if (e instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent event = (MCWorldChangedEvent) e;
                Prefilters.match(prefilter, "player", event.getPlayer().getName(), PrefilterType.MACRO);   
                Prefilters.match(prefilter, "from", event.getFrom().getName(), PrefilterType.STRING_MATCH);
                Prefilters.match(prefilter, "to", event.getTo().getName(), PrefilterType.STRING_MATCH);
                return true;
            }
            return false;
        }
        
        public BindableEvent convert(CArray manualObject) {
            MCPlayer player = Static.GetPlayer(manualObject.get("player"));
            MCWorld from = Static.getServer().getWorld(manualObject.get("from").val());
            
            BindableEvent e = EventBuilder.instantiate(MCPlayerCommandEvent.class, 
                player, from);
            return e;
        }
        
        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            if (e instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent event = (MCWorldChangedEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
                map.put("from", new CString(event.getFrom().getName(), Target.UNKNOWN));
                map.put("to", new CString(event.getTo().getName(), Target.UNKNOWN));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCWorldChangedEvent");
            }
        }
        
        public boolean modifyEvent(String key, Construct value, BindableEvent event) {
            if (event instanceof MCWorldChangedEvent) {
                MCWorldChangedEvent e = (MCWorldChangedEvent) event;
                
                return true;
            }
            return false;
        }
                        
    }
    
    
}
