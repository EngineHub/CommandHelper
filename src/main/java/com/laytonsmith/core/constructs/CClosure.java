package com.laytonsmith.core.constructs;

import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.LoopManipulationException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A closure is just an anonymous procedure.
 *
 * @author Layton
 */
public class CClosure extends Construct {

    public static final long serialVersionUID = 1L;
    ParseTree node;
    Environment env;
    String[] names;
    Construct[] defaults;
	
    public CClosure(ParseTree node, Environment env, String[] names, Construct[] defaults, Target t) {
        super(node != null ? node.toString() : "", ConstructType.CLOSURE, t);
        this.node = node;
        try {
            this.env = env.clone();
        }
        catch (CloneNotSupportedException ex) {
            throw ConfigRuntimeException.CreateUncatchableException("A failure occured while trying to clone the environment. " + ex.getMessage(), t);
        }
        this.names = names;
        this.defaults = defaults;
    }

    @Override
    public String val() {
        StringBuilder b = new StringBuilder();
        condense(getNode(), b);
        return b.toString();
    }

    private void condense(ParseTree node, StringBuilder b) {
		if(node == null){
			return;
		}
        if (node.getData() instanceof CFunction) {
            b.append(( (CFunction) node.getData() ).val()).append("(");
            for (int i = 0; i < node.numberOfChildren(); i++) {
                condense(node.getChildAt(i), b);
                if (i != node.numberOfChildren() - 1 && !( (CFunction) node.getData() ).val().equals("__autoconcat__")) {
                    b.append(",");
                }
            }
            b.append(")");
        } else if (node.getData() instanceof CString) {
            CString data = (CString) node.getData();
            // Convert: \ -> \\ and ' -> \'
            b.append("'").append(data.val().replace("\\", "\\\\").replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n").replace("'", "\\'")).append("'");
		} else if(node.getData() instanceof IVariable){
			b.append(((IVariable)node.getData()).getName());
        } else {
            b.append(node.getData().val());
        }
    }

    public ParseTree getNode() {
        return node;
    }

    @Override
    public CClosure clone() throws CloneNotSupportedException {
        CClosure clone = (CClosure) super.clone();
        if (this.node != null) {
            clone.node = this.node.clone();
        }
        return clone;
    }

    /**
     * If meta code needs to affect this closure's environment, it can access it
     * with this function. Note that changing this will only affect future runs
     * of the closure, it will not affect the currently running closure, (if
     * any) due to the environment being cloned right before running.
     *
     * @return
     */
    public synchronized Environment getEnv() {
        return env;
    }

    /**
     * Executes the closure, giving it the supplied arguments. {@code values}
     * may be null, which means that no arguments are being sent.
	 * 
	 * LoopManipulationExceptions will never bubble up past this point, but
	 * other ProgramFlowManipulationExceptions will. ConfigRuntimeExceptions will
	 * also bubble up past this, since an execution mechanism may need to do custom
	 * handling.
     *
     * @param values
     */
    public void execute(Construct... values) throws ConfigRuntimeException, ProgramFlowManipulationException {
		if(node == null){
			return;
		}
        try {
            Environment environment;
            synchronized (this) {
                environment = env.clone();
            }
            if (values != null) {
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    Construct value;
                    try {
                        value = values[i];
                    }
                    catch (Exception e) {
                        value = defaults[i].clone();
                    }
                    environment.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(name, value, getTarget()));
                }
            }
            CArray arguments = new CArray(node.getData().getTarget());
            if (values != null) {
                for (Construct value : values) {
                    arguments.push(value);
                }
            }
            environment.getEnv(GlobalEnv.class).GetVarList().set(new IVariable("@arguments", arguments, node.getData().getTarget()));
            ParseTree newNode = new ParseTree(new CFunction("g", getTarget()), node.getFileOptions());
            List<ParseTree> children = new ArrayList<ParseTree>();
            children.add(node);
            newNode.setChildren(children);
            try {
                MethodScriptCompiler.execute(newNode, environment, null, environment.getEnv(GlobalEnv.class).GetScript());
            } catch (LoopManipulationException e){
				//This shouldn't ever happen.
				LoopManipulationException lme = ((LoopManipulationException)e);
				Target t = lme.getTarget();
				ConfigRuntimeException.React(ConfigRuntimeException.CreateUncatchableException("A " + lme.getName() + "() bubbled up to the top of"
						+ " a closure, which is unexpected behavior.", t), environment);
			}
        }
        catch (CloneNotSupportedException ex) {
            Logger.getLogger(CClosure.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
