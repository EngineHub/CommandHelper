/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CNull;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.laytonsmith.aliasengine.functions.IVariableList;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import java.util.List;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class Procedure {
    private String name;
    private List<String> varList;
    private GenericTreeNode<Construct> tree;

    
    public Procedure(String name, List<String> varList, GenericTreeNode<Construct> tree){
        this.name = name;
        this.varList = varList;
        this.tree = tree;
        if(!this.name.matches("^_[^_].*")){
            throw new ConfigRuntimeException("Procedure names must start with an underscore", ExceptionType.FormatException, 0);
        }
    }
    
    public String getName(){
        return name;
    }
    
    private int indexOf(String name){
        for(int i = 0; i < varList.size(); i++){
            if(varList.get(i).equals(name)){
                return i;
            }
        }
        return -1;
    }
    
    public void execute(List<Construct> variables, Player player){
        GenericTree<Construct> root = new GenericTree<Construct>();
        root.setRoot(tree);
        Script fakeScript = new Script(null, null);
        fakeScript.varList = new IVariableList();
        CArray array = new CArray(0);
        for(Construct c : variables){
            array.push(c);
        }
        fakeScript.varList.set(new IVariable("@arguments", array, 0));
        for(GenericTreeNode<Construct> c : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
            if(c.getData() instanceof IVariable){
                int index = indexOf(((IVariable)c.getData()).name);
                IVariable var = (IVariable)c.getData();
                if(index == -1){
                    var.setIval(new CString("", var.line_num));
                } else if(index > variables.size() - 1){
                    var.setIval(new CNull(0));
                } else {
                    var.setIval(variables.get(index));
                }
                fakeScript.varList.set(var);
            }
        }
        
        
        fakeScript.eval(tree, player);
    }
}
