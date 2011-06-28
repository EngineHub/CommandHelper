/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.PureUtilities.fileutility.LineCallback;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Static;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class Echoes {
    public static String docs(){
        return "These functions allow you to echo information to the screen";
    }
    @api public static class die implements Function{
        public Integer []numArgs() {
            return new Integer[] {0,1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException{
            if(args.length == 0){
                throw new CancelCommandException("");
            } else if(args.length == 1){
                Static.SendMessage(p, args[0].val());
                throw new CancelCommandException("");
            } else{
                return null;
            }
        }

        public String getName(){ return "die"; }
        public String docs(){
            return "nothing {[var1]} Kills the command immediately, without completing it. A message is optional, but if provided, displayed to the user.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return false;
        }
    }
    
    @api public static class msg implements Function{

        public String getName() {
            return "msg";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < args.length; i++){
                b.append(args[i].val());
            }
            Static.SendMessage(p, b.toString());
//            int start = 0;
//            String s = b.toString();
//            while(true){
//                if(start >= s.length()) break;
//                p.sendMessage(s.substring(start, start + 100 >= s.length()?s.length():start + 100));
//                start += 100;
//            }
            return new CVoid(line_num);
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
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return false;
        }
    
    }
    
    @api public static class tmsg implements Function{

        public String getName() {
            return "tmsg";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args.length < 2){
                throw new ConfigRuntimeException("You must send at least 2 arguments to tmsg");
            }
            p = p.getServer().getPlayer(args[0].val());
            if(p == null){
                throw new CancelCommandException("The player " + args[0].val() + " is not online");
            }
            StringBuilder b = new StringBuilder();
            for(int i = 1; i < args.length; i++){
                b.append(args[i].val());
            }
            Static.SendMessage(p, b.toString());
//            int start = 0;
//            String s = b.toString();
//            while(true){
//                if(start >= s.length()) break;
//                p.sendMessage(s.substring(start, start + 100 >= s.length()?s.length():start + 100));
//                start += 100;
//            }
            return new CVoid(line_num);
        }

        public String docs() {
            return "void {player, msg, [...]} Displays a message on the specified players screen, similar to msg, but targets a specific user.";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return false;
        }
    }
    
    @api public static class color implements Function{

        public String getName() {
            return "color";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String color = ChatColor.WHITE.toString();
            try{
                color = ChatColor.valueOf(args[0].val().toUpperCase()).toString();
            } catch(IllegalArgumentException e){}
            
            return new CString(color, line_num);
        }

        public String docs() {
            return "string {name} Returns the color modifier given a color name. If the given color name isn't valid, white is used instead."
                    + " The list of valid color names can be found in the ChatColor class, and case doesn't matter. For your reference,"
                    + " here is the list of valid colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD GRAY, DARK_GRAY,"
                    + " BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return null;
        }
    }
    
    @api public static class chat implements Function{

        public String getName() {
            return "chat";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, final Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            Static.SendMessage(new LineCallback() {

                public void run(String line) {
                    p.chat(line);
                }
            }, args[0].val());

            return new CVoid(line_num);
        }

        public String docs() {
            return "void {string} Echoes string to the chat, as if the user simply typed something into the chat bar.";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }
        public String since() {
            return "3.0.1";
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api public static class chatas implements Function{

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

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            final Player player = p.getServer().getPlayer(args[0].val());
            Static.SendMessage(new LineCallback() {

                public void run(String line) {
                    player.chat(line);
                }
            }, args[1].val());
            
            return new CVoid(line_num);
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api public static class broadcast implements Function{

        public String getName() {
            return "broadcast";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {message} Broadcasts a message to all players on the server";
        }

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.1";
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof CNull){
                throw new ConfigRuntimeException("Trying to broadcast null won't work");
            }
            final Server server = p.getServer();
            Static.SendMessage(new LineCallback() {

                public void run(String line) {
                    server.broadcastMessage(line);
                }
            }, args[0].val());
            return new CVoid(line_num);
        }
        public Boolean runAsync(){
            return false;
        }
        
    }
    
    @api public static class console implements Function{

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

        public boolean isRestricted() {
            return true;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return true;
        }

        public String since() {
            return "3.0.2";
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            String mes = args[0].val();
            boolean prefix = true;
            if(args.length > 1){
                prefix = Static.getBoolean(args[1]);
            }
            com.laytonsmith.aliasengine.Static.getLogger().log(Level.INFO, (prefix?"CommandHelper: ":"") + mes);
            return new CVoid(line_num);
        }
        public Boolean runAsync(){
            return null;
        }
        
    }
}
