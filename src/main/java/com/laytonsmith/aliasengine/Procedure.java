/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

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
        for(GenericTreeNode<Construct> c : root.build(GenericTreeTraversalOrderEnum.PRE_ORDER)){
            if(c.getData() instanceof IVariable){
                int index = indexOf(((IVariable)c.getData()).name);
                IVariable var;
                if(index == -1){
                    ((IVariable)c.getData()).setIval(new CString("", ((IVariable)c.getData()).line_num));
                } else {
                    ((IVariable)c.getData()).setIval(variables.get(index));
                }
            }
        }
        
        Script fakeScript = new Script(null, null);
        fakeScript.varList = new IVariableList();
        fakeScript.eval(tree, player);
    }
}
