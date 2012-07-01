/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Env;
import com.laytonsmith.core.GenericTreeNode;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A closure is just an anonymous procedure. 
 * @author Layton
 */
public class CClosure extends Construct {
    
    public static final long serialVersionUID = 1L;

    Construct[] defaults;
    Env env;
    String[] names;
    GenericTreeNode<Construct> node;

    public CClosure(GenericTreeNode<Construct> node, Env env, String[] names, Construct[] defaults, Target t) {
        super(node!=null?node.toString():"", ConstructType.CLOSURE, t);
        this.node = node;
        try {
            this.env = env.clone();
        } catch (CloneNotSupportedException ex) {
            throw new ConfigRuntimeException("A failure occured while trying to clone the environment.", t);
        }
        this.names = names;
        this.defaults = defaults;
    }
    
    @Override
    public CClosure clone() throws CloneNotSupportedException{
        CClosure clone = (CClosure) super.clone();
        if(this.node != null) clone.node = this.node.clone();
        return clone;
    }
    
    private void condense(GenericTreeNode<Construct> node, StringBuilder b){
        if(node.data instanceof CFunction){            
            b.append(((CFunction)node.data).val()).append("(");
            for(int i = 0; i < node.children.size(); i++){
                condense(node.children.get(i), b);
                if(i > 0 && !((CFunction)node.data).val().equals("__autoconcat__")){
                    b.append(",");
                }
            }
            b.append(")");
        } else if(node.data instanceof CString){
            CString data = (CString)node.data;
            // Convert: \ -> \\ and ' -> \'
            b.append("'").append(data.val().replaceAll("\t", "\\t").replaceAll("\n", "\\n").replace("\\", "\\\\").replace("'", "\\'")).append("'");
        } else {
            b.append(node.data.val());
        }
    }

    /**
     * Executes the closure, giving it the supplied arguments. {@code values} may be null, which means that
     * no arguments are being sent.
     * @param values 
     */
    public void execute(Construct[] values){
        try {
            Env environment;
            synchronized(this){
                environment = env.clone();
            }
            if(values != null){
                for(int i = 0; i < names.length; i++){
                    String name = names[i];
                    Construct value;
                    try{
                        value = values[i];
                    } catch(Exception e) {
                        value = defaults[i].clone();
                    }
                    environment.GetVarList().set(new IVariable(name, value, getTarget()));
                }
            }
            CArray arguments = new CArray(node.data.getTarget());
            if(values != null){
                for(Construct value : values){
                    arguments.push(value);
                }
            }
            environment.GetVarList().set(new IVariable("@arguments", arguments, node.data.getTarget()));
            GenericTreeNode<Construct> newNode = new GenericTreeNode<Construct>(new CFunction("g", getTarget()));
            List<GenericTreeNode<Construct>> children = new ArrayList<GenericTreeNode<Construct>>();
            children.add(node);
            newNode.setChildren(children);
            try{
                MethodScriptCompiler.execute(newNode, environment, null, environment.GetScript());                
            } catch(ConfigRuntimeException e){
                ConfigRuntimeException.React(e);
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(CClosure.class.getName()).log(Level.SEVERE, null, ex);
        }
    }        
    
    /**
     * If meta code needs to affect this closure's environment, it can
     * access it with this function. Note that changing this will only
     * affect future runs of the closure, it will not affect the currently
     * running closure, (if any) due to the environment being cloned right
     * before running.
     * @return 
     */
    public synchronized Env getEnv(){        
        return env;
    }
    
    public GenericTreeNode<Construct> getNode() {
        return node;
    }
    
    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public String val(){
        StringBuilder b = new StringBuilder();
        condense(getNode(), b);
        return b.toString();
    }
}
