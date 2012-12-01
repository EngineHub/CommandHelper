

package com.laytonsmith.core.events.drivers;

import com.laytonsmith.PureUtilities.Geometry;
import com.laytonsmith.PureUtilities.Geometry.Point3D;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.abstraction.enums.MCDamageCause;
import com.laytonsmith.abstraction.enums.MCAction;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.abstraction.events.*;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.*;
import com.laytonsmith.core.events.BoundEvent.ActiveEvent;
import com.laytonsmith.core.events.Prefilters.PrefilterType;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.StringHandling;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

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
				//TODO: The event.getResult needs to be enum'd
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
		
        @Override
        public void preExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerLoginEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerLoginEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.getEnv(CommandHelperEnvironment.class).SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        @Override
        public void postExecution(Environment env, ActiveEvent activeEvent) {
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
                    + "{player: The player's name | join_message: The default join message | first_login: True if this is the first time"
                    + " the player has logged in.}"
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
                map.put("first_login", new CBoolean(ple.getPlayer().isNewPlayer(), Target.UNKNOWN));
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
            MCPlayerJoinEvent e = EventBuilder.instantiate(MCPlayerJoinEvent.class, Static.GetPlayer(manual.get("player").val(), Target.UNKNOWN), 
                    manual.get("join_message").val());
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
                    + "facing: The (lowercase) face of the block they clicked. (One of " + StringUtils.Join(MCBlockFace.values(), ", ", ", or ") + ") |"
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
            MCBlockFace bf = MCBlockFace.valueOf(manual.get("facing").val().toUpperCase());
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
            MCPlayer p = Static.GetPlayer(manual.get("player"), Target.UNKNOWN);
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

        @Override
        public void preExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerRespawnEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerRespawnEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.getEnv(CommandHelperEnvironment.class).SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        @Override
        public void postExecution(Environment env, ActiveEvent activeEvent) {
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
                    e.setDroppedExp((int)Static.getInt(value, Target.UNKNOWN));                    
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
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
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
	
	@Override
        public void preExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerQuitEvent){
                //Static lookups of the player don't seem to work here, but
                //the player is passed in with the event.
                MCPlayer player = ((MCPlayerQuitEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                env.getEnv(CommandHelperEnvironment.class).SetPlayer(player);
                Static.InjectPlayer(player);
            }
        }

        @Override
        public void postExecution(Environment env, ActiveEvent activeEvent) {
            if(activeEvent.getUnderlyingEvent() instanceof MCPlayerQuitEvent){
                MCPlayer player = ((MCPlayerQuitEvent)activeEvent.getUnderlyingEvent()).getPlayer();
                Static.UninjectPlayer(player);
            }
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
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
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
                                list.add(Static.GetPlayer(v, Target.UNKNOWN));                                
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
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
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
            MCPlayer player = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
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
	
	@api
	public static class player_move extends AbstractEvent{
		
		/*
		 * TODO: 
		 * 1. Add player prefilter
		 * 2. See if the same event can be fired with different from fields, so that
		 * one move only causes one "chain" of handlers to be fired.
		 * 3. Add both region points and named WG region support in the prefilters.
		 * (The sk_ functiosn can probably be used directly)
		 * 4. Figure out why the cancel() isn't working.
		 * 5. Tie this in to player teleport events. Probably set a prefilter that determines
		 * whether or not a teleport should count as a movement or not.
		 */
		
		private boolean threadRunning = false;
		private Set<Integer> thresholdList = new HashSet<Integer>();
		private Map<Integer, Map<String, MCLocation>> thresholds = new HashMap<Integer, Map<String, MCLocation>>();
		
		@Override
		public void bind(Map<String, Construct> prefilters) {
			if(prefilters.containsKey("threshold")){
				int i = (int)Static.getInt(prefilters.get("threshold"), Target.UNKNOWN);
				thresholdList.add(i);
			}
			if(!threadRunning){
				thresholdList.add(1);
				threadRunning = true;
				new Thread(new Runnable() {

					public void run() {
						outerLoop: while(true){
							MCPlayer players[] = Static.getServer().getOnlinePlayers();
							for(final MCPlayer p : players){
								//We need to loop through all the thresholds
								//and see if any of the points meet them. If so,
								//we know we need to fire the event. If none of them
								//match, carry on with the next player. As soon as
								//one matches though, we can't quit the loop, because
								//we have to set all the thresholds.
								thresholdLoop: for(Integer i : thresholdList){
									if(thresholds.containsKey(i) && thresholds.get(i).containsKey(p.getName())){
										final MCLocation last = thresholds.get(i).get(p.getName());
										final MCLocation current = p.asyncGetLocation();
										if(!p.getWorld().getName().equals(last.getWorld().getName())){
											//They moved worlds. simply put their new location in here, then
											//continue.
											thresholds.get(i).put(p.getName(), p.getLocation());
											continue thresholdLoop;
										}
										Point3D lastPoint = new Point3D(last.getX(), last.getY(), last.getZ());
										Point3D currentPoint = new Point3D(current.getX(), current.getY(), current.getZ());
										double distance = lastPoint.distance(currentPoint);
										if(distance > i){
											//We've met the threshold.
											//Well, we're still not sure. To run the prefilters on this thread,
											//we're gonna simulate a prefilter match now. We have to run this manually,
											//because each bind could have a different threshold, and it will be expecting
											//THIS from location. Other binds will be expecting other from locations.
											final MCPlayerMoveEvent fakeEvent = new MCPlayerMoveEvent() {
												boolean cancelled = false;
												public MCPlayer getPlayer() {
													return p;
												}

												public MCLocation getFrom() {
													return last;
												}

												public MCLocation getTo() {
													return current;
												}

												public Object _GetObject() {
													return null;
												}

												public void setCancelled(boolean state) {
													cancelled = state;
												}

												public boolean isCancelled() {
													return cancelled;
												}
											};
											//We need to run the prefilters on this thread, so we have
											//to do this all by hand.
											final SortedSet<BoundEvent> toRun = EventUtils.GetMatchingEvents(Driver.PLAYER_MOVE, player_move.this.getName(), fakeEvent, player_move.this);
											//Ok, now the events to be run need to actually be run on the main server thread, so let's run that now.
											try {
												StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>(){

													public Object call() throws Exception {
														EventUtils.FireListeners(toRun, player_move.this, fakeEvent);
														return null;
													}
												});
											} catch (Exception ex) {
												Logger.getLogger(PlayerEvents.class.getName()).log(Level.SEVERE, null, ex);
											}
											if(fakeEvent.isCancelled()){
												//Put them back at the from location
												p.teleport(last);
											} else {
												thresholds.get(i).put(p.getName(), current);
											}
										}
									} else {
										//If there is no location here, just put the current location in there.
										if(!thresholds.containsKey(i)){
											Map<String, MCLocation> map = new HashMap<String, MCLocation>();
											thresholds.put(i, map);
										}
										thresholds.get(i).put(p.getName(), p.asyncGetLocation());
									}
								}
								synchronized(player_move.this){
									try {
										//Throttle this thread just a little
										player_move.this.wait(10);
									} catch (InterruptedException ex) {
										//
									}
								}
							}
						}
					}
				}, "CommandHelperPlayerMoveEventRunner").start();
			}
		}

		public String getName() {
			return "player_move";
		}

		public String docs() {
			return "{player: <macro> The player that moved. Switching worlds does not trigger this event. "
					+ "| from: <custom> This should be a location array (x, y, z, world)."
					+ " The location is matched via block matching, so if the array's x parameter were 1, if the player"
					+ "moved from 1.3, that parameter would match."
                    + "| to: <custom> The location the player is now in. This should be a location array as well."
					+ "| threshold: <custom> The minimum distance the player must have travelled before the event"
					+ " will be triggered. This is based on the 3D distance, and is measured in block units.}"
                    + " This event is fired off AFTER a player has moved. This is a read only event because of this,"
					+ " however, the determination logic is run asynchronously from the main server thread, so general"
					+ " detection of a movement will not cause any lag, beyond lag caused by any other thread. Prefilters"
					+ " are extremely important to use however, because the prefilter code is also run asynchronously,"
					+ " however your code is not, and therefore, is slower. It is also advisable to use a threshold,"
					+ " so you are not firing an event every time a player moves. A threshold of 5 or 10 will likely"
					+ " be sufficient for all use cases, and should considerably reduce server thread resources. Though this"
					+ " event is read only, you can \"cancel\" the event by moving the player back to the from location,"
					+ " or otherwise \"change\" the location by using set_ploc(). Note that on a server with"
					+ " lots of players, this \"stride\" distance, that is, the distance a player will have moved"
					+ " before the event picks it up will be greater. The movement detection thread is slightly"
					+ " throttled."
                    + "{player | from: The location the player is coming from | to: The location the player is now in}"
                    + "{}"
                    + "{}";
		}

		@Override
		public void cancel(BindableEvent o, boolean state) {
			if(o instanceof MCPlayerMoveEvent){
				((MCPlayerMoveEvent)o).setCancelled(state);
			}
		}

		@Override
		public boolean isCancellable(BindableEvent o) {
			return true;
		}

		@Override
		public boolean isCancelled(BindableEvent o) {
			if(o instanceof MCPlayerMoveEvent){
				return ((MCPlayerMoveEvent)o).isCancelled();
			} else {
				return false;
			}
		}		
		
		

		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof MCPlayerMoveEvent){
				MCPlayerMoveEvent event = (MCPlayerMoveEvent)e;
				if(!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName())){
					return false;
				}
				if(prefilter.containsKey("threshold")){
					Point3D from = new Point3D(event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
					Point3D to = new Point3D(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
					double distance = from.distance(to);
					double pDistance = Static.getNumber(prefilter.get("threshold"), Target.UNKNOWN);
					if(pDistance > distance){
						return false;
					}
				}
				if(prefilter.containsKey("from")){
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("from"), event.getPlayer().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()){
						return false;
					}
				}
				if(prefilter.containsKey("to")){
					MCLocation pLoc = ObjectGenerator.GetGenerator().location(prefilter.get("to"), event.getPlayer().getWorld(), Target.UNKNOWN);
					MCLocation loc = event.getFrom();
					if(loc.getBlockX() != pLoc.getBlockX() || loc.getBlockY() != pLoc.getBlockY() || loc.getBlockZ() != pLoc.getBlockZ()){
						return false;
					}
				}
				return true;
				
			}
			return false ;
		}

		public BindableEvent convert(CArray manualObject) {
			MCPlayer p = Static.GetPlayer(manualObject.get("player"), Target.UNKNOWN);
			MCLocation from = ObjectGenerator.GetGenerator().location(manualObject.get("from"), p.getWorld(), manualObject.getTarget());
			MCLocation to = ObjectGenerator.GetGenerator().location(manualObject.get("to"), p.getWorld(), manualObject.getTarget());
			return EventBuilder.instantiate(MCPlayerMoveEvent.class, p, from, to);
		}

		
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof MCPlayerMoveEvent) {
                MCPlayerMoveEvent event = (MCPlayerMoveEvent) e;
                Map<String, Construct> map = evaluate_helper(e);
                //Fill in the event parameters
				map.put("player", new CString(event.getPlayer().getName(), Target.UNKNOWN));
                map.put("from", ObjectGenerator.GetGenerator().location(event.getFrom()));
                map.put("to", ObjectGenerator.GetGenerator().location(event.getTo()));
                return map;
            } else {
                throw new EventException("Cannot convert e to MCPlayerMovedEvent");
            }
		}

		public Driver driver() {
			return Driver.PLAYER_MOVE;
		}

		public boolean modifyEvent(String key, Construct value, BindableEvent event) {
			//Nothing can be modified, so always return false
			return false;
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
    
    
}
