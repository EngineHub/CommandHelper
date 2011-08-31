/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.functions.exceptions.FunctionReturnException;
import com.sk89q.util.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

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
            throw new ConfigRuntimeException("Procedure names must start with an underscore", ExceptionType.FormatException, f.line_num, f.file);
        }
    }
    
    public String getName(){
        return name;
    }
    
    @Override
    public String toString(){
        return name + "(" + StringUtil.joinString(varList.keySet(), ", ", 0) + ")";
    }
    
    
    public Construct execute(List<Construct> variables, Player player, Map<String, Procedure> procStack, String label){
        resetVariables();
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        Script fakeScript = new Script(null, null);
        fakeScript.varList = new IVariableList();
        fakeScript.knownProcs = procStack;
        fakeScript.label = label;
        CArray array = new CArray(0, null);
        for(Construct d : variables){
            array.push(d);
        }
        fakeScript.varList.set(new IVariable("@arguments", array, 0, null));
        for(GenericTreeNode<Construct> c : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
            if(c.getData() instanceof IVariable){

                String varname = ((IVariable)c.getData()).getName();
                IVariable var = varList.get(((IVariable)c.getData()).getName());
                if(var == null){
                    var = new IVariable(varname, c.getData().line_num, c.getData().file);
                }
                if(varname.equals("@arguments")){
                    var.setIval(fakeScript.varList.get("@arguments"));
                }
                int index = indexOf(varname);
                if(index != -1){
                    //This variable has not been explicitly set, so we use the default
                    try{
                        var.setIval(Static.resolveConstruct(variables.get(index).val(), var.line_num, var.file));
                    } catch(ArrayIndexOutOfBoundsException e){
                        //var.setIval(new CNull(var.line_num, var.file));
                    }
                }
                fakeScript.varList.set(var);
            }
        }
        
        try{
            fakeScript.eval(tree, player);
        } catch(FunctionReturnException e){
            return e.getReturn();
        }
        return new CVoid(0, null);
    }
    
    private int indexOf(String name){
        for(IVariable v : varIndex){
            if(v.getName().equals(name)){
                return varIndex.indexOf(v);
            }
        }
        return -1;
    }
    
    private void resetVariables(){
        for(Map.Entry<String, IVariable> varEntry : varList.entrySet()){
            varEntry.getValue().setIval(originals.get(varEntry.getKey()));
        }
    }
    
    @Override
    public Procedure clone() throws CloneNotSupportedException{
        Procedure clone = (Procedure) super.clone();
        if(this.varList != null) clone.varList = new HashMap<String, IVariable>(this.varList);
        if(this.tree != null) clone.tree = this.tree.clone();
        return clone;
    }
}
