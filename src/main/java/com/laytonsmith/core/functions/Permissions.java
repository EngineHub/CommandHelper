/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.sk89q.wepif.PermissionsResolverManager;

/**
 *
 * @author Layton
 */
public class Permissions {
    @api public static class has_permission extends AbstractFunction{

        public String docs() {
            return "boolean {[player], permissionName} Using the built in permissions system, checks to see if the player has a particular permission."
                    + " This is simply passed through to the permissions system. This function does not throw a PlayerOfflineException, because"
                    + " it works with offline players, but that means that names must be an exact match. If you notice, this function isn't"
                    + " restricted. However, it IS restricted if the player attempts to check another player's permissions. If run from"
                    + " the console, will always return true.";
        }

        public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
            String player = null;
            String permission = null;
            
            if(args.length == 1){
                if(environment.GetCommandSender() instanceof MCConsoleCommandSender){
                    //Console always has permission
                    return new CBoolean(true, t);
                }
            	
                player = environment.GetPlayer().getName();
                permission = args[0].val();
            } else {
                player = args[0].val();
                permission = args[1].val();
            }
            
            if(environment.GetPlayer() != null && !environment.GetPlayer().getName().equals(player)){
                if(!Static.hasCHPermission(getName(), environment)){
                    throw new ConfigRuntimeException("You do not have permission to use the " + getName() + " function.",
                    	Exceptions.ExceptionType.InsufficientPermissionException, t);
                }
            }
            
            if(player.toLowerCase() == "~console") {
            	//Console always has permission
            	return new CBoolean(true, t);
            }
            
            PermissionsResolverManager perms = Static.getPermissionsResolverManager();
            return new CBoolean(perms.hasPermission(player, permission), t);
        }

        public String getName() {
            return "has_permission";
        }

        public boolean isRestricted() {
            return false;
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public boolean preResolveVariables() {
            return true;
        }

        public Boolean runAsync() {
            return false;
        }

        public CHVersion since() {
            return CHVersion.V3_3_0;
        }

        public Exceptions.ExceptionType[] thrown() {
            return new Exceptions.ExceptionType[]{Exceptions.ExceptionType.InsufficientPermissionException};
        }
        
    }
    public static String docs(){
        return "Provides access to the server's underlying permissions system. Permissions functionality is only as good as the management"
                + " system in place, however, and so not all functions may be supported on a given system.";
    }
}
