/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.core.constructs;

import com.laytonsmith.puls3.core.GenericTreeNode;
import java.io.File;

/**
 *
 * @author Layton
 */
public class CClosure extends Construct {
    
    public static final long serialVersionUID = 1L;

    GenericTreeNode<Construct> node;

    public CClosure(String name, GenericTreeNode<Construct> node, int line_num, File file) {
        super(node!=null?node.toString():"", ConstructType.CLOSURE, line_num, file);
        this.node = node;
    }
    
    @Override
    public String val(){
        throw new UnsupportedOperationException(".val() cannot be used in CClosure");
    }

    public GenericTreeNode<Construct> getNode() {
        return node;
    }        
    
    @Override
    public CClosure clone() throws CloneNotSupportedException{
        CClosure clone = (CClosure) super.clone();
        if(this.node != null) clone.node = this.node.clone();
        return clone;
    }
}
