/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.LineCallback;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.util.StringUtil;
import java.util.logging.Level;

/**
 *
 * @author layton
 */
public class Echoes {
    public static String docs(){
        return "These functions allow you to echo information to the screen";
    }
    @api public static class die extends AbstractFunction{
        public Integer []numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException{
            if(args.length == 0){
                throw new CancelCommandException("");
            }
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < args.length; i++){
                b.append(args[i].val());
            }
            try{
                Static.SendMessage(env.GetCommandSender(), b.toString(), t);
            } finally{
                throw new CancelCommandException("");
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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }
    }
    
    @api public static class msg extends AbstractFunction{

        public String getName() {
            return "msg";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < args.length; i++){
                b.append(args[i].val());
            }
            Static.SendMessage(env.GetCommandSender(), b.toString(), t);
//            int start = 0;
//            String s = b.toString();
//            while(true){
//                if(start >= s.length()) break;
//                p.sendMessage(s.substring(start, start + 100 >= s.length()?s.length():start + 100));
//                start += 100;
//            }
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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
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

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args.length < 2){
                throw new ConfigRuntimeException("You must send at least 2 arguments to tmsg", ExceptionType.InsufficientArgumentsException, t);
            }
            MCPlayer p = Static.GetPlayer(args[0]);
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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return false;
        }
    }
    
    @api public static class color extends AbstractFunction{

        public String getName() {
            return "color";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String color = MCChatColor.WHITE.toString();
            
            try{
                color = MCChatColor.valueOf(args[0].val().toUpperCase()).toString();
            } catch(IllegalArgumentException e){}
            String a = args[0].val().toUpperCase();
            if(a.equals("A")){
                a = "10";
            } else if(a.equals("B")){
                a = "11";
            } else if(a.equals("C")){
                a = "12";
            } else if(a.equals("D")){
                a = "13";
            } else if(a.equals("E")){
                a = "14";
            } else if(a.equals("F")){
                a = "15";
            }
            try{
                Integer p = Integer.parseInt(a);
                color = MCChatColor.getByCode(p).toString();
            } catch(NumberFormatException e){}
            
            return new CString(color, t);
        }

        public String docs() {
            String [] b = new String[MCChatColor.values().length];
            for(int i = 0; i < b.length; i++){
                b[i] = MCChatColor.values()[i].name(); Enum e = null;
            }
            return "string {name} Returns the color modifier given a color name. If the given color name isn't valid, white is used instead."
                    + " The list of valid color names can be found in the MCChatColor class, and case doesn't matter. For your reference,"
                    + " here is the list of valid colors: " + StringUtil.joinString(b, ", ", 0) + ", in addition the integers 0-15 will work, or the hex numbers from 0-F.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public CHVersion since() {
            return CHVersion.V3_0_1;
        }
        public Boolean runAsync(){
            return null;
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

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Boolean runAsync() {
            return false;
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            return new CString(MCChatColor.stripColor(args[0].val()), t);
        }
        
    }
    
    @api public static class chat extends AbstractFunction{

        public String getName() {
            return "chat";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(final Target t, final Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.SendMessage(new LineCallback() {

                public void run(String line) {
                    if(!(env.GetCommandSender() instanceof MCPlayer)){
                        throw new ConfigRuntimeException("The current player is not online, or this is being run from the console", ExceptionType.PlayerOfflineException, t);
                    }
                    (env.GetPlayer()).chat(line);
                }
            }, args[0].val());

            return new CVoid(t);
        }

        public String docs() {
            return "void {string} Echoes string to the chat, as if the user simply typed something into the chat bar.";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.PlayerOfflineException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_2;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            final MCPlayer player = Static.GetPlayer(args[0]);
            Static.SendMessage(new LineCallback() {

                public void run(String line) {
                    if(player != null){
                        player.chat(line);
                    }
                }
            }, args[1].val());
            
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
            return new Integer[]{1};
        }

        public String docs() {
            return "void {message} Broadcasts a message to all players on the server";
        }
        
        public ExceptionType[] thrown(){
            return new ExceptionType[]{ExceptionType.CastException};
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_1;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CNull){
                throw new ConfigRuntimeException("Trying to broadcast null won't work", ExceptionType.CastException, t);
            }
            final MCServer server = Static.getServer();
            Static.SendMessage(new LineCallback() {

                public void run(String line) {
                    server.broadcastMessage(line);
                }
            }, args[0].val());
            return new CVoid(t);
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api public static class console extends AbstractFunction{

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

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public CHVersion since() {
            return CHVersion.V3_0_2;
        }

        public Construct exec(Target t, Env env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String mes = args[0].val();
            boolean prefix = true;
            if(args.length > 1){
                prefix = Static.getBoolean(args[1]);
            }
            mes = Static.MCToANSIColors(mes);
            com.laytonsmith.core.Static.getLogger().log(Level.INFO, (prefix?"CommandHelper: ":"") + mes);
            return new CVoid(t);
        }
        public Boolean runAsync(){
            return null;
        }
        
    }
}
