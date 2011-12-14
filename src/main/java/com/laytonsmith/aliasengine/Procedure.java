/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.exceptions.FunctionReturnException;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.sk89q.util.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Layton
 */
public class Procedure implements Cloneable {
    private String name;
    private Map<String, IVariable> varList;
    private Map<String, Construct> originals = new HashMap<String, Construct>();
    private List<IVariable> varIndex = new ArrayList<IVariable>();
    private GenericTreeNode<Construct> tree;

    
    public Procedure(String name, List<IVariable> varList, GenericTreeNode<Construct> tree, CFunction f){
        this.name = name;        
        this.varList = new HashMap<String, IVariable>();
        for(IVariable var : varList){
            this.varList.put(var.getName(), var);
            this.varIndex.add(var);
            this.originals.put(var.getName(), var.ival());
        }        
        this.tree = tree;
        if(!this.name.matches("^_[^_].*")){
            throw new ConfigRuntimeException("Procedure names must start with an underscore", ExceptionType.FormatException, f.getLineNum(), f.getFile());
        }
    }
    
    public String getName(){
        return name;
    }
    
    @Override
    public String toString(){
        return name + "(" + StringUtil.joinString(varList.keySet(), ", ", 0) + ")";
    }
    
    public Construct cexecute(List<GenericTreeNode<Construct>> args, Env env){
        List<Construct> list = new ArrayList<Construct>();
        for(GenericTreeNode<Construct> arg : args){
            list.add(env.GetScript().seval(arg, env));
        }
        return execute(list, env);
    }
    public Construct execute(List<Construct> args, Env env){
        env.SetVarList(new IVariableList());
        CArray array = new CArray(0, null);        
        for(String key : originals.keySet()){
            Construct c = originals.get(key);
            env.GetVarList().set(new IVariable(key, c, 0, null));
            array.push(c);
        }
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        Script fakeScript = new Script(null, null, env);
        for(int i = 0; i < args.size(); i++){
            Construct c = args.get(i);
            array.set(i, c);
            if(varIndex.size() > i){
                String varname = varIndex.get(i).getName();
                env.GetVarList().set(new IVariable(varname, c, c.getLineNum(), c.getFile()));
            }
        }
        env.GetVarList().set(new IVariable("@arguments", array, 0, null));
        
        try{
            fakeScript.eval(tree, env);
        } catch(FunctionReturnException e){
            return e.getReturn();
        }
        return new CVoid(0, null);
    }
    
    @Override
    public Procedure clone() throws CloneNotSupportedException{
        Procedure clone = (Procedure) super.clone();
        if(this.varList != null) clone.varList = new HashMap<String, IVariable>(this.varList);
        if(this.tree != null) clone.tree = this.tree.clone();
        return clone;
    }
}
