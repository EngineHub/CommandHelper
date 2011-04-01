/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.ConfigCompileException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CFunction;
import com.laytonsmith.aliasengine.User;
import com.sk89q.commandhelper.CommandHelperPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class FunctionList {

    private static ArrayList<Function> functions = new ArrayList<Function>();
    private User u;
    private static Class[] internalFunctions = {die.class};

    static {
        //Initialize all our functions as soon as we start up
        initFunctions();
    }

    private static void initFunctions() {
        //Register internal classes first, so they can't be overridden
        for (Class c : internalFunctions) {
            registerFunction(c);
        }
        //Now pull all the jars from plugins/CommandHelper/functions
        File f = new File("plugins/CommandHelper/functions");
        f.mkdirs();
        for(File jar : f.listFiles()){
            PluginLoader.loadJars(jar.getAbsolutePath());
        }
        PluginLoader.getLoader().
    }

    public static void registerFunction(Class c) {
        try {
            Function f;
            List<Class> interfaces = Arrays.asList(c.getInterfaces());
            if (!interfaces.contains(Function.class)) {
                throw new ConfigRuntimeException("Registered Functions must extend the " + Function.class.getCanonicalName()
                        + " class. (Functions must implement the Function object)");
            }
            try {
                f = (Function) c.newInstance();
            } catch (Exception ex) {
                throw new ConfigRuntimeException();
            }

            for (Function m : functions) {
                if (m.getName().equals(f.getName())) {
                    throw new ConfigRuntimeException("A function with the name " + f.getName() + " already exists.");
                }
            }
            System.out.println("Loaded " + f.getName());
            functions.add(f);
        } catch (ConfigRuntimeException e) {
            CommandHelperPlugin.logger.log(Level.SEVERE, "The function {0} could not be loaded."
                    + " Any scipts that rely on this function will fail.", c.getCanonicalName());
        }
    }

    public Function getFunction(Construct c) throws ConfigCompileException {
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

    public FunctionList(User u) {
        this.u = u;
    }
}
