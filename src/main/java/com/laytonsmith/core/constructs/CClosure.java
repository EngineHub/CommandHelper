/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import java.io.File;

/**
 * A closure is just an anonymous procedure. 
 * @author Layton
 */
public class CClosure extends Construct {
    
    public static final long serialVersionUID = 1L;

    GenericTreeNode<Construct> node;
    Env env;

    public CClosure(String name, GenericTreeNode<Construct> node, Env env, int line_num, File file) {
        super(node!=null?node.toString():"", ConstructType.CLOSURE, line_num, file);
        this.node = node;
        this.env = env;
    }
    
    @Override
    public String val(){
        StringBuilder b = new StringBuilder();
        condense(getNode(), b);
        return b.toString();
    }
    
    private void condense(GenericTreeNode<Construct> node, StringBuilder b){
        if(node.data instanceof CFunction){            
            b.append(((CFunction)node.data).val()).append("(");
            for(int i = 0; i < node.children.size(); i++){
                condense(node.children.get(i), b);
                if(i > 0 && !((CFunction)node.data).val().equals("sconcat")){
                    //sconcat handles itself in the reversal
                    b.append(",");
                }
                //TODO: optimize concat and sconcat
            }
            b.append(")");
        } else if(node.data instanceof CString){
            CString data = (CString)node.data;
            // Convert: \ -> \\ and ' -> \'
            b.append("'").append(data.val().replaceAll("\t", "\\t").replaceAll("\n", "\\n").replace("\\", "\\\\").replaceAll("'", "\\'")).append("'");
        } else {
            b.append(node.data.val());
        }
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
    
    public void execute(){
        //TODO
    }
}
