/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.sk89q.util.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private boolean possiblyConstant = false;

    
    public Procedure(String name, List<IVariable> varList, GenericTreeNode<Construct> tree, Target t){
        this.name = name;        
        this.varList = new HashMap<String, IVariable>();
        for(IVariable var : varList){
            this.varList.put(var.getName(), var);
            this.varIndex.add(var);
            this.originals.put(var.getName(), var.ival());
        }        
        this.tree = tree;
        if(!this.name.matches("^_[^_].*")){
            throw new ConfigRuntimeException("Procedure names must start with an underscore", ExceptionType.FormatException, t);
        }
        //Let's look through the tree now, and see if this is possibly constant or not.
        //If it is, it may or may not help us during compilation, but if it's not,
        //we can be sure that we cannot inline this in any way.
        this.possiblyConstant = checkPossiblyConstant(tree);
    }
    
    private boolean checkPossiblyConstant(GenericTreeNode<Construct> tree){
        if(!tree.data.isDynamic()){
            //If it isn't dynamic, it certainly could be constant
            return true;
        } else if(tree.data instanceof IVariable){
            //Variables will return true for isDynamic, but they are technically constant, because
            //they are being declared in this scope, or passed in. An import() would break this
            //contract, but import() itself is dynamic, so this is not an issue.
            return true;
        } else if(tree.data instanceof CFunction){
            try {
                //If the function itself is not optimizable, we needn't recurse.
                FunctionBase fb = FunctionList.getFunction(tree.data);
                if(fb instanceof Function){
                    Function f = (Function)fb;
                    if(f instanceof DataHandling._return){
                        return true; //This is a special exception
                    }
                    //If it's optimizable, it's possible. If it's restricted, it doesn't matter, because
                    //we can't optimize it out anyways, because we need to do the permission check
                    if((f.canOptimizeDynamic() || f.canOptimize()) && !f.isRestricted()){
                        //Ok, well, we have to check the children first.
                        for(GenericTreeNode<Construct> child : tree.getChildren()){
                            if(!checkPossiblyConstant(child)){
                                return false; //Nope, since our child can't be constant, neither can we
                            }
                        }
                        //They all check out, so, yep, we could possibly be constant
                        return true;
                    } else {
                        return false; //Nope. Doesn't matter if the children are or not
                    }
                } else {
                    return false;
                }
            } catch (ConfigCompileException ex) {
                //Uh. This should never happen.
                Logger.getLogger(Procedure.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        } else {
            //Uh. Ok, well, nope.
            return false;
        }
    }
    
    public boolean isPossiblyConstant(){
        return this.possiblyConstant;
    }
    
    public String getName(){
        return name;
    }
    
    @Override
    public String toString(){
        return name + "(" + StringUtil.joinString(varList.keySet(), ", ", 0) + ")";
    }
    
    /**
     * Convenience wrapper around executing a procedure if the parameters are in
     * tree mode still.
     * @param args
     * @param env
     * @return 
     */
    public Construct cexecute(List<GenericTreeNode<Construct>> args, Env env){
        List<Construct> list = new ArrayList<Construct>();
        for(GenericTreeNode<Construct> arg : args){
            list.add(env.GetScript().seval(arg, env));
        }
        return execute(list, env);
    }
    
    /**
     * Executes this procedure, with the arguments that were passed in
     * @param args
     * @param env
     * @return 
     */
    public Construct execute(List<Construct> args, Env env){
        env.SetVarList(new IVariableList());
        CArray array = new CArray(Target.UNKNOWN);        
        for(String key : originals.keySet()){
            Construct c = originals.get(key);
            env.GetVarList().set(new IVariable(key, c, Target.UNKNOWN));
            array.push(c);
        }
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        Script fakeScript = Script.GenerateScript(tree, env.GetLabel());//new Script(null, null);        
        for(int i = 0; i < args.size(); i++){
            Construct c = args.get(i);
            array.set(i, c);
            if(varIndex.size() > i){
                String varname = varIndex.get(i).getName();
                env.GetVarList().set(new IVariable(varname, c, c.getTarget()));
            }
        }
        env.GetVarList().set(new IVariable("@arguments", array, Target.UNKNOWN));
        
        try{
            fakeScript.eval(tree, env);
        } catch(FunctionReturnException e){
            return e.getReturn();
        }
        return new CVoid(Target.UNKNOWN);
    }
    
    @Override
    public Procedure clone() throws CloneNotSupportedException{
        Procedure clone = (Procedure) super.clone();
        if(this.varList != null) clone.varList = new HashMap<String, IVariable>(this.varList);
        if(this.tree != null) clone.tree = this.tree.clone();
        return clone;
    }
}
