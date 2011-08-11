/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.Constructs;

import com.laytonsmith.aliasengine.GenericTreeNode;
import java.io.File;

/**
 *
 * @author Layton
 */
public class CClosure extends Construct {

    GenericTreeNode<Construct> node;

    public CClosure(String name, GenericTreeNode<Construct> node, int line_num, File file) {
        super(node.toString(), ConstructType.CLOSURE, line_num, file);
        this.node = node;
    }
    
    @Override
    public String val(){
        throw new UnsupportedOperationException(".val() cannot be used in CClosure");
    }

    public GenericTreeNode<Construct> getNode() {
        return node;
    }        
}
