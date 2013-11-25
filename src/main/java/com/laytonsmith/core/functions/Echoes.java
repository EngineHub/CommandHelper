


package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 *
 * @author layton
 */
public class Echoes {
    public static String docs(){
        return "These functions allow you to echo information to the screen";
    }
    @api(environments={CommandHelperEnvironment.class})
	@noboilerplate 
	public static class die extends AbstractFunction implements Optimizable {
        public Integer []numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException{
            if(args.length == 0){
                throw new CancelCommandException("", t);
            }
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < args.length; i++){
                b.append(args[i].val());
            }
            try{
				if(env.hasEnv(CommandHelperEnvironment.class)){
					Static.SendMessage(env.getEnv(CommandHelperEnvironment.class).GetCommandSender(), b.toString(), t);
				} else {
					String mes = Static.MCToANSIColors(b.toString());
					if(mes.matches("(?m).*\033.*")){
						//We have terminal colors, we need to reset them at the end
						mes += TermColors.reset();
					}
					System.out.println(mes);
				}
            } finally{
                throw new CancelCommandException("", t);
            }
        }

        public ExceptionType[] thrown(){
            return new ExceptionType[]{};
        }

        public String getName(){ return "die"; }
        public String docs(){
            return "nothing {[var1, var2...,]} Kills the command immediately, without completing it. A message is optional, but if provided, displayed to the user.";
        }

        public boolean isRestricted() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
				OptimizationOption.TERMINAL
			);
		}				
    }
    
	//Technically it needs CommandHelperEnvironment, but we have special exception handling in case we're running
	//in cmdline mode.
    @api(environments={})
	@noboilerplate 
	public static class msg extends AbstractFunction{

        public String getName() {
            return "msg";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(final Target t, Environment env, final Construct... args) throws CancelCommandException, ConfigRuntimeException {
			StringBuilder b = new StringBuilder();
			for(int i = 0; i < args.length; i++){
				b.append(args[i].val());
			}
			if(env.hasEnv(CommandHelperEnvironment.class)){
				final MCCommandSender p = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				Static.SendMessage(p, b.toString(), t);
			} else {
				String mes = Static.MCToANSIColors(b.toString());
				if(mes.matches("(?m).*\033.*")){
					//We have terminal colors, we need to reset them at the end
					mes += TermColors.reset();
				}
				System.out.println(mes);
				System.out.flush();
			}
            return new CVoid(t);
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public String docs() {
            return "void {var1, [var2...]} Echoes a message to the player running the command";
        }

        public boolean isRestricted() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }
    
    }
    
    @api public static class tmsg extends AbstractFunction{

        public String getName() {
            return "tmsg";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args.length < 2){
                throw new ConfigRuntimeException("You must send at least 2 arguments to tmsg", ExceptionType.InsufficientArgumentsException, t);
            }
			MCCommandSender p;
			if (Static.getConsoleName().equals(args[0].val())) {
				p = Static.getServer().getConsole();
			} else {
				p = Static.GetPlayer(args[0], t);
			}
            if(p == null){
                throw new ConfigRuntimeException("The player " + args[0].val() + " is not online", ExceptionType.PlayerOfflineException, t);
            }
            StringBuilder b = new StringBuilder();
            for(int i = 1; i < args.length; i++){
                b.append(args[i].val());
            }
            Static.SendMessage(p, b.toString(), t);
//            int start = 0;
//            String s = b.toString();
//            while(true){
//                if(start >= s.length()) break;
//                p.sendMessage(s.substring(start, start + 100 >= s.length()?s.length():start + 100));
//                start += 100;
//            }
            return new CVoid(t);
        }

        public String docs() {
            return "void {player, msg, [...]} Displays a message on the specified players screen, similar to msg, but targets a specific user.";
        }
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.PlayerOfflineException, ExceptionType.InsufficientArgumentsException};
        }

        public boolean isRestricted() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }
    }
    
    @api public static class color extends AbstractFunction implements Optimizable {
		
		private Map<String, CString> colors = new TreeMap<String, CString>();
		private static final String symbols = "0123456789abcdefABCDEFmMnNoOlLkKrR";
		public static final Set<Character> COLOR_SYMBOLS;
		static {
			Set<Character> temp = new TreeSet<Character>();
			for(Character c : symbols.toCharArray()){
				temp.add(c);
			}
			COLOR_SYMBOLS = Collections.unmodifiableSet(temp);
		}

        public String getName() {
            return "color";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String color = null;
			String val = args[0].nval();
			if (val == null) {
				return new CString(MCChatColor.WHITE.toString(), t);
			}
            if(colors.containsKey(val)){
				return colors.get(val);
			}
            try{
                color = MCChatColor.valueOf(val.toUpperCase()).toString();
            } catch(IllegalArgumentException e){}
            String a = val.toLowerCase();
            if(a.equals("10")){
                a = "a";
            } else if(a.equals("11")){
                a = "b";
            } else if(a.equals("12")){
                a = "c";
            } else if(a.equals("13")){
                a = "d";
            } else if(a.equals("14")){
                a = "e";
            } else if(a.equals("15")){
                a = "f";
            } else if(a.equals("random")){
                a = "k";
            } else if(a.equals("bold")){
                a = "l";
            } else if(a.equals("strike") || a.equals("strikethrough")){
                a = "m";
            } else if(a.equals("underline") || a.equals("underlined")){
                a = "n";
            } else if(a.equals("italic") || a.equals("italics")){
                a = "o";
            } else if(a.equals("plain white") || a.equals("plainwhite") || a.equals("plain_white")){
                a = "r";
            }
			
			//////////////////////////////////////////////////////////////
			// IMPORTANT                                                //
			// Be sure to update COLOR_SYMBOLS if this list is updated! //
			//////////////////////////////////////////////////////////////
			if("".equals(a.trim())){
				//If the value is empty string, set the color to white.
				color = MCChatColor.WHITE.toString();
			}
            if(color == null){
                try{
                    Character p = String.valueOf(a).charAt(0);
                    MCChatColor cc = MCChatColor.getByChar(p);
                    if(cc == null){
                        cc = MCChatColor.WHITE;
                    }
                    color = cc.toString();
                } catch(NumberFormatException e){}
            }            
            
            if(color == null){
                color = MCChatColor.WHITE.toString();
            }
			//Until we get a compilation environment going, this must be removed so we can optimize it out.
//            if(env.GetCustom("cmdline") instanceof Boolean && (Boolean)env.GetCustom("cmdline") == true){
//                color = Static.MCToANSIColors(color);
//            }
			CString ret = new CString(color, t);
			colors.put(val, ret);
            return ret;
        }

        public String docs() {
            String [] b = new String[MCChatColor.values().length];
            for(int i = 0; i < b.length; i++){
                b[i] = MCChatColor.values()[i].name(); Enum e = null;
            }
            return "string {name} Returns the color modifier given a color name. If the given color name isn't valid, white is used instead."
					+ " The list of valid colors is: " + StringUtils.Join(b, ", ", ", or ") + ", in addition the integers 0-15 will work,"
                    + " or the hex numbers from 0-F, and k, l, m, n, o, and r, which represent styles. Unlike manually putting in the color symbol,"
                    + " using this function will return the platform's color code, so if you are wanting to keep your scripts platform independant,"
                    + " it is a much better idea to use this function as opposed to hard coding your own color codes.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
        }

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
						OptimizationOption.CONSTANT_OFFLINE,
						OptimizationOption.CACHE_RETURN
			);
		}
				
    }
    
    @api public static class strip_colors extends AbstractFunction{

        public String getName() {
            return "strip_colors";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "string {toStrip} Strips all the color codes from a given string";
        }

        public ExceptionType[] thrown() {
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return false;
        }
        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
            return new CString(MCChatColor.stripColor(args[0].val()), t);
        }
        
    }
    
    @api(environments={CommandHelperEnvironment.class})
	public static class chat extends AbstractFunction{

        public String getName() {
            return "chat";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(final Target t, final Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			MCPlayer p = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
			if(p != null){
				p.chat(args[0].val());
			} else {
				throw new ConfigRuntimeException("Console cannot chat. Use something like broadcast() instead.", ExceptionType.PlayerOfflineException, t);
			}
            return new CVoid(t);
        }

        public String docs() {
            return "void {string} Echoes string to the chat, as if the user simply typed something into the chat bar. This function cannot"
					+ " be run from console, a PlayerOfflineException is thrown if attempted. Use broadcast() instead.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api public static class chatas extends AbstractFunction{

        public String getName() {
            return "chatas";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {player, msg} Sends a chat message to the server, as the given player. Otherwise the same as the chat"
                    + " function";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_0_2;
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            final MCPlayer player = Static.GetPlayer(args[0], t);
            Static.AssertPlayerNonNull(player, t);
			player.chat(args[1].val());
            return new CVoid(t);
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api public static class broadcast extends AbstractFunction{

        public String getName() {
            return "broadcast";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {message, [permission]} Broadcasts a message to all players on the server."
            		+ " If permission is given, only players with that permission will see the broadcast.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.NullPointerException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CNull){
                throw new ConfigRuntimeException("Trying to broadcast null won't work", ExceptionType.NullPointerException, t);
            }
            final MCServer server = Static.getServer();
            String permission = null;
            if (args.length == 2 && !(args[1] instanceof CNull)) {
            	permission = args[1].val();
            }
            if (permission == null) {
            	server.broadcastMessage(args[0].val());
            } else {
            	server.broadcastMessage(args[0].val(), permission);
            }
            return new CVoid(t);
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api 
    @noboilerplate
    public static class console extends AbstractFunction{

        public String getName() {
            return "console";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {message, [prefix]} Logs a message to the console. If prefix is true, prepends \"CommandHelper:\""
                    + " to the message. Default is true.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        
        public CHVersion since() {
            return CHVersion.V3_0_2;
        }

        public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String mes = args[0].val();
            boolean prefix = true;
            if(args.length > 1){
                prefix = Static.getBoolean(args[1]);
            }
            mes = (prefix?"CommandHelper: ":"") + Static.MCToANSIColors(mes);
            if(mes.matches("(?m).*\033.*")){
                //We have terminal colors, we need to reset them at the end
                mes += TermColors.reset();
            }
            System.out.println(mes);
            return new CVoid(t);
        }
        public Boolean runAsync(){
            return null;
        }
        
    }        
	
	@api
	public static class colorize extends AbstractFunction implements Optimizable {

		public ExceptionType[] thrown() {
			return null;
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		color color = new color();
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Construct text = args[0];
			String symbol = "&";
			if(args.length == 2){
				symbol = args[1].val();
			}
			if(text instanceof CString){
				String stext = text.val();
				StringBuilder b = new StringBuilder();
				int sl = symbol.length();
				for(int i = 0; i < stext.length(); i++){
					if(i + sl >= stext.length()){
						if(i < stext.length()){
							b.append(stext.substring(i));
							break;
						}
					} else {
						String subsequence1 = stext.substring(i, i + sl);
						if(!symbol.equals(subsequence1)){
							b.append(stext.charAt(i));
							continue;
						}
						try{
							String subsequence2 = stext.substring(i + sl, i + (sl * 2));
							if(subsequence2.equals(subsequence1)){
								b.append(subsequence1);
								i += (sl * 2) - 1;
								continue;
							}
						} catch(IndexOutOfBoundsException e){
							//Ignored, it just means there aren't enough characters to do a second
							//subsequence
						}
						Character c;
						try{
							c = stext.charAt(i + sl);
						} catch(IndexOutOfBoundsException e){
							b.append(stext.charAt(i + sl - 1));
							break;
						}
						if(color.COLOR_SYMBOLS.contains(c)){
							b.append(color.exec(t, environment, new CString(c, t)));
							i += sl;
							continue;
						} else {
							b.append(subsequence1);
							i += sl - 1;
							continue;
						}
					}
				}
				return new CString(b.toString(), t);
			} else {
				return text;
			}
		}

		public String getName() {
			return "colorize";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "mixed {text, [symbol]} Replaces all the colorizable text in the string. For instance,"
					+ " colorize('&aText') would be equivalent to (color('a').'Text'). By default, the"
					+ " symbol is '&', but that can be any arbitrary string that you specify. If text is not"
					+ " a string, that value is simply returned. If you need to \"escape\" a symbol, (that is"
					+ " have a literal symbol followed by a letter that is a valid color) just repeat the symbol"
					+ " twice, for instance '&&c' would return a literal '&c' instead of a red modifier.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_CONSTANT);
		}
		
	}
}
