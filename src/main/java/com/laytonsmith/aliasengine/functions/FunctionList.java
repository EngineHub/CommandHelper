/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.Constructs.CFunction;
import com.laytonsmith.aliasengine.User;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class FunctionList {

    private static ArrayList<Function> functions = new ArrayList<Function>();
    static {
        //Initialize all our functions as soon as we start up
        initFunctions();
    }

    private static void initFunctions() {
        //Register internal classes first, so they can't be overridden
        Class[] classes = ClassDiscovery.DiscoverClasses(FunctionList.class, null, null);
        for (int i = 0; i < classes.length; i++) {
            Annotation[] a = classes[i].getAnnotations();
            for (int j = 0; j < a.length; j++) {
                Annotation ann = a[j];
                if (ann.annotationType().equals(api.class)) {
                    Class api = classes[i];
                    String apiClass = (api.getEnclosingClass() != null
                            ? api.getEnclosingClass().getName().split("\\.")[api.getEnclosingClass().getName().split("\\.").length - 1]
                            : "<global>");
                    if (Function.class.isAssignableFrom(api)) {
                        try {
                            Function f = (Function) api.newInstance();
                            registerFunction(f, apiClass);
                            //System.out.println("Loaded " + apiClass + "." + f.getName());
                        } catch (InstantiationException ex) {
                            Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("@api functions must implement " + FunctionList.class.getPackage().getName() + ".Function! " + api.getSimpleName() + " cannot be loaded.");
                    }
                }
            }
        }
        
        if((Boolean)com.laytonsmith.aliasengine.Static.getPreferences().getPreference("debug-mode")){
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
            if((Boolean)com.laytonsmith.aliasengine.Static.getPreferences().getPreference("debug-mode")){
                System.out.println("CommandHelper: Loaded function \"" + f.getName() + "\"");
            }
        }
        functions.add(f);
    }

    public static Function getFunction(Construct c) throws ConfigCompileException {
        if (c instanceof CFunction) {
            for (Function m : functions) {
                if (m.getName().equals(c.val())) {
                    return m;
                }
            }
            throw new ConfigCompileException("The function \"" + c.val() + "\" does not exist");
        }
        throw new ConfigCompileException("Excpecting CFunction type");
    }

//    public FunctionList(User u) {
//        this.u = u;
//        if (functions.isEmpty()) {
//            initFunctions();
//        }
//    }

//    public Construct exec(String name, int line_num, Player p, Construct... args) throws ConfigCompileException, CancelCommandException {
//        for (Function f : functions) {
//            if (f.getName().equals(name)) {
//                return f.exec(line_num, p, args);
//            }
//        }
//        throw new ConfigCompileException("Function " + name + " is not defined");
//    }
//
//    public Integer[] numArgs(String name) throws ConfigCompileException {
//        for (Function f : functions) {
//            if (f.getName().equals(name)) {
//                return f.numArgs();
//            }
//        }
//        throw new ConfigCompileException("Function " + name + " is not defined");
//    }

    public static ArrayList<Function> getFunctionList() {
        return functions;
    }
    
}
