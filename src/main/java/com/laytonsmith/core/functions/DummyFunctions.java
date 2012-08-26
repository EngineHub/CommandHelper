/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.sk89q.wepif.PermissionsResolverManager;
import java.util.Arrays;

/**
 *
 * @author Layton
 */
public class DummyFunctions {

//	@api public static class test_pgroup extends DummyFunction {
//
//		public Construct exec(Target t, Env environment, Construct... args) throws ConfigRuntimeException {
//			MCPlayer p = environment.GetPlayer();
//			if(args.length >= 1){
//				p = Static.GetPlayer(args[0]);
//			}
//			PermissionsResolverManager m = Static.getPermissionsResolverManager();			
//			System.out.println("According to the Permissions Resolver Manager, the following is returned for the player " + p.getName() + ":");
//			System.out.println("Groups: " + Arrays.toString(m.getGroups(p.getName())));
//			
//			return new CVoid(t);
//		}
//		
//	}
}
