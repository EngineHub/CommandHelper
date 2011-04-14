/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Static;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class Echoes {

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
            p.sendMessage(b.toString());
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
    
    }
}
