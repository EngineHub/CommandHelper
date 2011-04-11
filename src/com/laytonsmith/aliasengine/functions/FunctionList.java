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
import java.util.ArrayList;
import org.bukkit.entity.Player;

/**
 *
 * @author layton
 */
public class FunctionList {

    private static ArrayList<Function> functions = new ArrayList<Function>();
    private User u;
    private static Function[] internalFunctions = {new Echoes.die(), new Echoes.msg(), new PlayerManangment.player(),
        new BasicLogic._equals(), new BasicLogic._if(), new StringHandling.concat(), new Minecraft.data_values(), new BasicLogic.pow(),
        new BasicLogic.add(), new BasicLogic.subtract(), new BasicLogic.multiply(), new BasicLogic.divide(),
        new BasicLogic.lt(), new BasicLogic.gt(), new BasicLogic.lte(), new BasicLogic.gte(), new BasicLogic.mod(),
        new StringHandling.read(), new Meta.runas(), new StringHandling.sconcat(), new PlayerManangment.all_players(),
        new DataHandling._for(), new DataHandling.assign(), new DataHandling.array(), new ArrayHandling.array_get(),
        new ArrayHandling.array_push(), new ArrayHandling.array_set(), new ArrayHandling.array_size(),
        new Meta.run()};

    static {
        //Initialize all our functions as soon as we start up
        initFunctions();
    }

    private static void initFunctions() {
        //Register internal classes first, so they can't be overridden
        for (Function c : internalFunctions) {
            registerFunction(c);
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
//            }
//        }
    }

    public static void registerFunction(Function f) {
        System.out.println("CommandHelper: Loaded function \"" + f.getName() + "\"");
        functions.add(f);
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

    public Construct exec(String name, int line_num, Player p, Construct ... args) throws ConfigCompileException, CancelCommandException{
        for(Function f : functions){
            if(f.getName().equals(name)){
                return f.exec(line_num, p, args);
            }
        }
        throw new ConfigCompileException("Function " + name + " is not defined");
    }

    public Integer[] numArgs(String name) throws ConfigCompileException{
        for(Function f : functions){
            if(f.getName().equals(name)){
                return f.numArgs();
            }
        }
        throw new ConfigCompileException("Function " + name + " is not defined");
    }
}
