

package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.api.Platforms;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Construct;
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
        Class[] classes = ClassDiscovery.GetClassesWithAnnotation(api.class);
        for(Class c : classes){
            String apiClass = (c.getEnclosingClass() != null
                    ? c.getEnclosingClass().getName().split("\\.")[c.getEnclosingClass().getName().split("\\.").length - 1]
                    : "<global>");
            if (FunctionBase.class.isAssignableFrom(c)) {
                try {
                    FunctionBase f = (FunctionBase) c.newInstance();
                    api api = f.getClass().getAnnotation(api.class);                    
                    api.Platforms [] platforms = api.platform();
                    if(supportedPlatforms.get(f.getName()) == null){
                        supportedPlatforms.put(f.getName(), new HashSet<api.Platforms>());
                    }
                    supportedPlatforms.get(f.getName()).addAll(Arrays.asList(platforms));
                    for(Platforms p : platforms){
                        registerFunction(f, apiClass, p);
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
            System.out.println("CommandHelper: Loaded " + functions.size() + " function" + (functions.size()==1?"":"s"));
        }
        
        

        //Now pull all the jars from plugins/CommandHelper/functions
        //TODO Finishing this has been defered until a later date
        //        File f = new File("plugins/CommandHelper/functions");
        //        f.mkdirs();
        //        PluginLoader.loadJars(f.getAbsolutePath());
        //        for(File file : f.listFiles()){
        //            try {
        //                Yaml yaml = new Yaml(new SafeConstructor());
        //                JarFile jar = new JarFile(file);
        //                JarEntry entry = jar.getJarEntry("main.yml");
        //                if (entry == null) {
        //                    throw new InvalidPluginException(new FileNotFoundException("Jar does not contain main.yml"));
        //                }
        //                InputStream stream = jar.getInputStream(entry);
        //                Map<String, Object> map = (Map<String, Object>)yaml.load(stream);
        //                System.out.println(map);
        //                stream.close();
        //                jar.close();
        //            } catch(InvalidPluginException ex){
        //                Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
        //            } catch (IOException ex) {
        //                Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        //        }

    }

    
    public static void registerFunction(FunctionBase f, String apiClass, api.Platforms platform) {
        if(!apiClass.equals("Sandbox")){
            if(Prefs.DebugMode()){
                System.out.println("CommandHelper: Loaded function \"" + f.getName() + "\"");
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
