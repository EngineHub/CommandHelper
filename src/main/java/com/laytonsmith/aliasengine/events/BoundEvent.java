/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.laytonsmith.aliasengine.functions.exceptions.EventException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author layton
 */
public class BoundEvent {
    String id;
    String priority;
    Map<String, Construct> prefilter;
    String eventObjName;
    List<IVariable> vars;
    GenericTreeNode<Construct> tree;
    
    public BoundEvent(String name, CArray options, CArray prefilter, List<IVariable> vars,
            GenericTreeNode<Construct> tree) throws EventException{
        this.eventObjName = name;
        
        if(options != null && options.contains("id")){
            this.id = options.get("id").val();
        } else {
            //Generate a new event id
            id = name + ":" + EventHandler.GetUniqueID();
        }
        if(options != null && options.contains("priority")){
            this.priority = options.get("priority").val().toUpperCase();
        } else {
            this.priority = "NORMAL";
        }
        if(!(
                this.priority.equals("LOWEST") ||
                this.priority.equals("LOW") ||
                this.priority.equals("NORMAL") ||
                this.priority.equals("HIGH") ||
                this.priority.equals("HIGHEST")
                )){
            throw new EventException("Priority must be one of: LOWEST, LOW, NORMAL, HIGH, HIGHEST");
        }
        
        this.prefilter = new HashMap<String, Construct>();
        for(Construct key : prefilter.keySet()){
            String k = key.val();
            this.prefilter.put(k, prefilter.get(key, 0));
        }
        
        this.vars = vars;
        this.tree = tree;        
    }
}
