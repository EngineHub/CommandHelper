

package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Persistance;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.persistance.DataSourceException;
import com.laytonsmith.persistance.PersistanceNetwork;
import com.laytonsmith.persistance.ReadOnlyException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages user aliases, and repeat commands.
 * @author layton
 */
public class UserManager {
    
    private static Map<String, UserManager> map = new HashMap<String, UserManager>();
    private static Map<String, List<Token>> script_cache = new HashMap<String, List<Token>>();
    
    public static UserManager GetUserManager(String name){
        if(!map.containsKey(name)){
            map.put(name, new UserManager(name));
        }
        return map.get(name);
    }
    
    public static void ClearUser(String name){
        if(map.containsKey(name)){
            map.remove(name);
        }
    }
    
    String name;
    String lastCommand;
    
    private UserManager(String name){
        this.name = name;
    }
    
    public void setLastCommand(String lastCommand){
        this.lastCommand = lastCommand;
    }
    
    public String getLastCommand(){
        return lastCommand;
    }
    
    public int addAlias(String alias, PersistanceNetwork persist) throws ConfigCompileException, DataSourceException, ReadOnlyException, IOException {
        try{
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(alias, new File("User Alias"))).get(0).compile();
        } catch(IndexOutOfBoundsException e){
            throw new ConfigCompileException("Improperly formatted alias", new Target(0, new File("User Alias"), 0));
        }
        Map<String[], String> list = persist.getNamespace(new String[]{"user", name, "aliases"});
        Integer nextValue = 0;
        for (String[] x : list.keySet()) {
            Integer thisX = Integer.parseInt(x[x.length - 1]);
            nextValue = Math.max(thisX + 1, nextValue + 1);
        }
        persist.set(new String[]{"user", name, "aliases", nextValue.toString()}, alias);
        return nextValue;
    }
    
    public Script getAlias(int id, Environment env) throws ConfigCompileException, DataSourceException{
        String alias = env.getEnv(GlobalEnv.class).GetPersistanceNetwork().get(new String[]{"user", name, "aliases", Integer.toString(id)});
        if(alias == null){
            return null;
        }
        return getAlias(alias);
    }
    
    private Script getAlias(String alias) throws ConfigCompileException{           
        List<Token> tokens;
        if(script_cache.containsKey(alias)){
            tokens = script_cache.get(alias);
        } else {
            tokens = MethodScriptCompiler.lex(alias, new File("User Alias"));
            script_cache.put(alias, tokens);
        }
        return MethodScriptCompiler.preprocess(tokens).get(0);
    }
    
    public void delAlias(int id, PersistanceNetwork persist) throws DataSourceException, ReadOnlyException, IOException{
        persist.set(new String[]{"user", name, "aliases", Integer.toString(id)}, null);
    }
    
    public String getAllAliases(int page, PersistanceNetwork persist) throws DataSourceException{
        if(page < 1){
            page = 1;
        }
        Map<String[], String> al = persist.getNamespace(new String[]{"user", name, "aliases"});
        StringBuilder b = new StringBuilder();

        for(String [] key : al.keySet()){
			String value = persist.get(key);
            b.append(MCChatColor.AQUA)
                    .append(key[key.length - 1])
                    .append(":")
                    .append(value.substring(0, Math.min(value.toString().length(), 45)))
                    .append(value.toString().length() > 45?"...":"")
                    .append("\n");
        }
        if(al.isEmpty()){
            b.append(MCChatColor.AQUA).append("You have no aliases defined");
        }
        return b.toString();
    }
    
    public List<Script> getAllScripts(PersistanceNetwork persist) throws DataSourceException{
        Map<String[], String> scripts = persist.getNamespace(new String[]{"user", name, "aliases"});
        List<Script> list = new ArrayList<Script>();
        for(String[] entry : scripts.keySet()){
            try{
                list.add(getAlias((String)scripts.get(entry)));
            } catch(ConfigCompileException e){
                //Ignore this one
            }
        }
        return list;
    }
    
    
    
}
