

package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.api.Platforms;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class FunctionList {

    private final static Map<api.Platforms, Map<String, FunctionBase>> functions = new EnumMap<api.Platforms, Map<String, FunctionBase>>(api.Platforms.class);
    private final static Map<String, Set<api.Platforms>> supportedPlatforms = new HashMap<String, Set<api.Platforms>>();
    static {
        for(api.Platforms p : api.Platforms.values()){
            functions.put(p, new HashMap<String, FunctionBase>());
        }
        //Initialize all our functions as soon as we start up
        initFunctions();
    }

    private static void initFunctions() {
        //Register internal classes first, so they can't be overridden
        Set<Class> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class);
		StringBuilder message = new StringBuilder();
        for(Class c : classes){
            String apiClass = (c.getEnclosingClass() != null
                    ? c.getEnclosingClass().getName().split("\\.")[c.getEnclosingClass().getName().split("\\.").length - 1]
                    : "<global>");
            if (FunctionBase.class.isAssignableFrom(c)) {
                try {
                    FunctionBase f = (FunctionBase) c.newInstance();
                    api api = f.getClass().getAnnotation(api.class);                    
                    api.Platforms [] platforms = api.platform();
					if(!api.enabled()){
						continue;
					}
                    if(supportedPlatforms.get(f.getName()) == null){
                        supportedPlatforms.put(f.getName(), EnumSet.noneOf(api.Platforms.class));
                    }
                    supportedPlatforms.get(f.getName()).addAll(Arrays.asList(platforms));
                    for(Platforms p : platforms){
                        registerFunction(f, apiClass, p, message);
                    }
                    //System.out.println("Loaded " + apiClass + "." + f.getName());
                } catch (InstantiationException ex) {
                    Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
                } catch(Throwable t){
                    if(t.getCause() instanceof ClassNotFoundException){
                        if(Prefs.DebugMode()){
                            System.err.println("Could not load " + c.getSimpleName() + "; Are you missing a dependency? (No further errors will happen, unless you try to use the function.)");
                        }
                    } else {
                        Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, "Something went wrong while trying to load up " + c.getSimpleName(),
                                t.getCause()!=null?t.getCause():t);
                    }
                    //Otherwise, they'll get the error later, when they try and use the function.
                }
            } else if(!api.ValidClasses.IsValid(c)){
                System.err.println("Invalid class found: " + c.getName() + ". Classes tagged with @api"
                        + " must extend a valid class type.");
            }
        }
        
        if(Prefs.DebugMode()){
			System.out.println(Implementation.GetServerType().getBranding() + ": Loaded the following functions: " + message.toString().trim());
			int size = 0;
			for(Map m : functions.values()){
				size += m.size();
			}
            System.out.println(Implementation.GetServerType().getBranding() + ": Loaded " + size + " function" + (functions.size()==1?"":"s"));
        }
    }

    
    public static void registerFunction(FunctionBase f, String apiClass, api.Platforms platform, StringBuilder message) {
        if(!apiClass.equals("Sandbox")){
            if(Prefs.DebugMode()){
                message.append(" \"").append(f.getName()).append("\"");
            }
        }
        try{
            functions.get(platform).put(f.getName(), f);
        } catch(UnsupportedOperationException e){
            //This function isn't done yet, and during production this is a serious problem,
            //but it will be caught when we test all the functions, so for now just ignore it,
            //since this function is called during initial initialization
        }
    }

    public static FunctionBase getFunction(String s) throws ConfigCompileException{
		return getFunction(new CFunction(s, Target.UNKNOWN));
	}	
	public static FunctionBase getFunction(String s, api.Platforms platform) throws ConfigCompileException{
		return getFunction(new CFunction(s, Target.UNKNOWN), platform);
	}
    public static FunctionBase getFunction(Construct c) throws ConfigCompileException{
        return getFunction(c, api.Platforms.INTERPRETER_JAVA);
    }
    public static FunctionBase getFunction(Construct c, api.Platforms platform) throws ConfigCompileException {
        if(platform == null){
            //Default to the Java interpreter
            platform = api.Platforms.INTERPRETER_JAVA;
        }
        if (c instanceof CFunction) {
            if(!functions.get(platform).containsKey(c.val()) || !supportedPlatforms.get(c.val()).contains(platform)){
                throw new ConfigCompileException("The function \"" + c.val() + "\" does not exist in the " + platform.platformName(),
                        c.getTarget());
            }
            return functions.get(platform).get(c.val());                          
        }
        throw new ConfigCompileException("Expecting CFunction type", c.getTarget());
    }

    public static List<FunctionBase> getFunctionList(api.Platforms platform) {
        if(platform == null){
            List<FunctionBase> list = new ArrayList<FunctionBase>();
            for(api.Platforms p : api.Platforms.values()){
                list.addAll(getFunctionList(p));
            }
            return list;
        }
        List<FunctionBase> f = new ArrayList<FunctionBase>();
        for(String name : functions.get(platform).keySet()){
            f.add(functions.get(platform).get(name));
        }
        return f;
    }
    
}
