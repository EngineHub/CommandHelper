/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.GenericTreeNode;
import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.aliasengine.ConfigRuntimeException;
import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CBoolean;
import com.laytonsmith.aliasengine.Constructs.CVoid;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Constructs.IVariable;
import com.laytonsmith.aliasengine.RunnableAlias;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class DataHandling {
    public static class array implements Function{

        public String getName() {
            return "array";
        }

        public Integer[] numArgs() {
            return new Integer[]{Integer.MAX_VALUE};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return new CArray(line_num, args);
        }

        public String docs() {
            return "Returns an array of objects";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {}

        public boolean preResolveVariables() {
            return false;
        }
    }
    
    public static class assign implements Function{
        IVariableList varList;
        public String getName() {
            return "assign";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            if(args[0] instanceof IVariable){
                IVariable v = (IVariable)args[0];
                Construct c = args[1];
                v.def = c.val();
                varList.set(v);
                return v;
            }
            throw new ConfigRuntimeException("assign only accepts an ivariable as the first argument");
        }

        public String docs() {
            return "Accepts an ivariable as a parameter, and puts the specified value in it. Returns the variable that was assigned.";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            this.varList = varList;
        }

        public boolean preResolveVariables() {
            return false;
        }
        
    }
    
    public static class _for implements Function{
        IVariableList varList;
        public String getName() {
            return "for";
        }

        public Integer[] numArgs() {
            return new Integer[]{4};
        }
        public Construct execs(int line_num, Player p, RunnableAlias parent, GenericTreeNode<Construct> assign, 
                GenericTreeNode<Construct> condition, GenericTreeNode<Construct> expression, 
                GenericTreeNode<Construct> runnable) throws CancelCommandException{
            Construct counter = parent.eval(assign);
            if(!(counter instanceof IVariable)){
                throw new ConfigRuntimeException("First parameter of for must be an ivariable");
            }
            while(true){
                Construct cond = parent.eval(condition);
                if(!(cond instanceof CBoolean)){
                    throw new ConfigRuntimeException("Second parameter of for must return a boolean");
                }
                CBoolean bcond = ((CBoolean) cond);
                if(bcond.getBoolean() == false){
                    break;
                }
                parent.eval(runnable);
                parent.eval(expression);
            }
            return new CVoid(line_num);
        }
        public Construct exec(int line_num, Player p, Construct... args) throws CancelCommandException, ConfigRuntimeException {
            return null;
        }

        public String docs() {
            return "Acts as a typical for loop";
        }

        public boolean isRestricted() {
            return false;
        }

        public void varList(IVariableList varList) {
            this.varList = varList;
        }

        public boolean preResolveVariables() {
            return false;
        }
        
    }
}
