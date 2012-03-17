/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.api;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class FunctionList {

    private static Map<String, Function> functions = new HashMap<String, Function>();
    static {
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
            if (Function.class.isAssignableFrom(c)) {
                try {
                    Function f = (Function) c.newInstance();
                    registerFunction(f, apiClass);
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

    
    public static void registerFunction(Function f, String apiClass) {
        if(!apiClass.equals("Sandbox")){
            if(Prefs.DebugMode()){
                System.out.println("CommandHelper: Loaded function \"" + f.getName() + "\"");
            }
        }
        try{
            functions.put(f.getName(), f);
        } catch(UnsupportedOperationException e){
            //This function isn't done yet, and during production this is a serious problem,
            //but it will be caught when we test all the functions, so for now just ignore it,
            //since this function is called during initial initialization
        }
    }

    public static Function getFunction(Construct c) throws ConfigCompileException {
        if (c instanceof CFunction) {
            if(!functions.containsKey(c.val())){
                throw new ConfigCompileException("The function \"" + c.val() + "\" does not exist",
                        c.getTarget());
            } else {
                return functions.get(c.val());
            }              
        }
        throw new ConfigCompileException("Expecting CFunction type", c.getTarget());
    }

    public static List<Function> getFunctionList() {
        List<Function> f = new ArrayList<Function>();
        for(String name : functions.keySet()){
            f.add(functions.get(name));
        }
        return f;
    }
    
}
