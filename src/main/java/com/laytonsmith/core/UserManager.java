/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Persistance;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.io.File;
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
    
    public int addAlias(String alias) throws ConfigCompileException {
        try{
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(alias, new File("User Alias")), new Env()).get(0).compile();
        } catch(IndexOutOfBoundsException e){
            throw new ConfigCompileException("Improperly formatted alias", new Target(0, new File("User Alias"), 0));
        }
        Persistance persist = Static.getPersistance();
        List<Map.Entry<String, Object>> list = persist.getNamespaceValues(new String[]{"user", name, "aliases"});
        Integer nextValue = 0;
        for (Map.Entry e : list) {
            String[] x = e.getKey().toString().split("\\.");
            Integer thisX = Integer.parseInt(x[x.length - 1]);
            nextValue = Math.max(thisX + 1, nextValue + 1);
        }
        persist.setValue(new String[]{"user", name, "aliases", nextValue.toString()}, alias);
        return nextValue;
    }
    
    public Script getAlias(int id) throws ConfigCompileException{
        String alias = (String)Static.getPersistance().getValue(new String[]{"user", name, "aliases", Integer.toString(id)});
        if(alias == null){
            return null;
        }
        return getAlias(alias);
    }
    
    private Script getAlias(String alias) throws ConfigCompileException{
        Env env = new Env();
        env.SetPlayer(Static.GetPlayer(name, Target.UNKNOWN));            
        List<Token> tokens;
        if(script_cache.containsKey(alias)){
            tokens = script_cache.get(alias);
        } else {
            tokens = MethodScriptCompiler.lex(alias, new File("User Alias"));            
        }
        return MethodScriptCompiler.preprocess(tokens, env).get(0);
    }
    
    public void delAlias(int id){
        Static.getPersistance().setValue(new String[]{"user", name, "aliases", Integer.toString(id)}, null);
    }
    
    public String getAllAliases(int page){
        if(page < 1){
            page = 1;
        }
        List<Map.Entry<String, Object>> al = Static.getPersistance().getNamespaceValues(new String[]{"user", name, "aliases"});
        StringBuilder b = new StringBuilder();

        for(Map.Entry e : al){
            String [] key = e.getKey().toString().split("\\.");
            b.append(MCChatColor.AQUA)
                    .append(key[key.length - 1])
                    .append(":")
                    .append(e.getValue().toString().substring(0, Math.min(e.getValue().toString().length(), 45)))
                    .append(e.getValue().toString().length() > 45?"...":"")
                    .append("\n");
        }
        if(al.isEmpty()){
            b.append(MCChatColor.AQUA).append("You have no aliases defined");
        }
        return b.toString();
    }
    
    public List<Script> getAllScripts(){
        List<Map.Entry<String, Object>> scripts = Static.getPersistance().getNamespaceValues(new String[]{"user", name, "aliases"});
        List<Script> list = new ArrayList<Script>();
        for(Map.Entry<String, Object> entry : scripts){
            try{
                list.add(getAlias((String)entry.getValue()));
            } catch(ConfigCompileException e){
                //Ignore this one
            }
        }
        return list;
    }
    
    
    
}
