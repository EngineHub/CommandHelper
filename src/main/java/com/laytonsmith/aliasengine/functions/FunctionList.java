/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.ConfigCompileException;
import com.laytonsmith.aliasengine.Constructs.CFunction;
import com.laytonsmith.aliasengine.User;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class FunctionList {

    private static ArrayList<Function> functions = new ArrayList<Function>();
    private User u;
//    private static Function[] internalFunctions = {new Echoes.die(), new Echoes.msg(), new PlayerManangment.player(),
//        new BasicLogic._equals(), new BasicLogic._if(), new StringHandling.concat(), new Minecraft.data_values(), new Math.pow(),
//        new Math.add(), new Math.subtract(), new Math.multiply(), new Math.divide(),
//        new BasicLogic.lt(), new BasicLogic.gt(), new BasicLogic.lte(), new BasicLogic.gte(), new Math.mod(),
//        new StringHandling.read(), new Meta.runas(), new StringHandling.sconcat(), new PlayerManangment.all_players(),
//        new DataHandling._for(), new DataHandling.assign(), new DataHandling.array(), new ArrayHandling.array_get(),
//        new ArrayHandling.array_push(), new ArrayHandling.array_set(), new ArrayHandling.array_size(),
//        new Meta.run(), new BasicLogic.and(), new BasicLogic.or(), new BasicLogic.not()};
    private static ArrayList<Function> iList = new ArrayList<Function>();

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
                    if (Arrays.asList(api.getInterfaces()).contains(Function.class)) {
                        try {
                            Function f = (Function) api.newInstance();
                            registerFunction(f);
                            //System.out.println("Loaded " + apiClass + "." + f.getName());
                        } catch (InstantiationException ex) {
                            Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(FunctionList.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("@api functions must implement " + FunctionList.class.getPackage().getName() + ".Function!");
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

    
    public static void registerFunction(Function f) {
        if((Boolean)com.laytonsmith.aliasengine.Static.getPreferences().getPreference("debug-mode")){
            System.out.println("CommandHelper: Loaded function \"" + f.getName() + "\"");
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
