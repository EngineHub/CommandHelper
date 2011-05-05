/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import org.bukkit.ChatColor;
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
                throw new CancelCommandException(args[0].val());
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
            int start = 0;
            String s = b.toString();
            while(true){
                if(start >= s.length()) break;
                p.sendMessage(s.substring(start, start + 100 >= s.length()?s.length():start + 100));
                start += 100;
            }
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
            int start = 0;
            String s = b.toString();
            while(true){
                if(start >= s.length()) break;
                p.sendMessage(s.substring(start, start + 100 >= s.length()?s.length():start + 100));
                start += 100;
            }
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
                    + " The list of valid color names can be found in the ChatColor class, and case doesn't matter.";
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
    }
    
    @api public static class chat implements Function{

        public String getName() {
            return "chat";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            p.chat(args[0].val());
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
            p.getServer().broadcastMessage(args[0].val());
            return new CVoid(line_num);
        }
        
    }
}
