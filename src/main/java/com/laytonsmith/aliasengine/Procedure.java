/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CFunction;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
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
    private List<String> varList;
    private GenericTreeNode<Construct> tree;

    
    public Procedure(String name, List<String> varList, GenericTreeNode<Construct> tree, CFunction f){
        this.name = name;
        this.varList = varList;
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
        return name + "(" + StringUtil.joinString(varList, ", ", 0) + ")";
    }
    
    private int indexOf(String name){
        for(int i = 0; i < varList.size(); i++){
            if(varList.get(i).equals(name)){
                return i;
            }
        }
        return -1;
    }
    
    public void execute(List<Construct> variables, Player player, Map<String, Procedure> procStack){
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        Script fakeScript = new Script(null, null);
        fakeScript.varList = new IVariableList();
        fakeScript.knownProcs = procStack;
        CArray array = new CArray(0, null);
        for(Construct d : variables){
            array.push(d);
        }
        fakeScript.varList.set(new IVariable("@arguments", array, 0, null));
        for(GenericTreeNode<Construct> c : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
            if(c.getData() instanceof IVariable){
                int index = indexOf(((IVariable)c.getData()).getName());
                IVariable var = (IVariable)c.getData();
                if(index == -1){
                    if(!var.getName().equals("@arguments")){
                        var.setIval(new CString("", var.line_num, var.file));
                    } else {
                        var.setIval(fakeScript.varList.get("@arguments"));
                    }
                } else if(index > variables.size() - 1){
                    var.setIval(new CNull(0, null));
                } else {
                    var.setIval(variables.get(index));
                }
                fakeScript.varList.set(var);
            }
        }
        
        
        fakeScript.eval(tree, player);
    }
    
    @Override
    public Procedure clone() throws CloneNotSupportedException{
        Procedure clone = (Procedure) super.clone();
        if(this.varList != null) clone.varList = new ArrayList<String>(this.varList);
        if(this.tree != null) clone.tree = this.tree.clone();
        return clone;
    }
}
