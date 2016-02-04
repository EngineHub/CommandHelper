package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopManipulationException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A closure is just an anonymous procedure.
 *
 *
 */
@typeof("closure")
public class CClosure extends Construct {

    public static final long serialVersionUID = 1L;
    protected ParseTree node;
    protected final Environment env;
    protected final String[] names;
    protected final Construct[] defaults;
	protected final CClassType[] types;
	protected final CClassType returnType;

    public CClosure(ParseTree node, Environment env, CClassType returnType, String[] names, Construct[] defaults, CClassType[] types, Target t) {
        super(node != null ? node.toString() : "", ConstructType.CLOSURE, t);
        this.node = node;
		this.env = env;
        this.names = names;
        this.defaults = defaults;
		this.types = types;
		this.returnType = returnType;
		for(String pName : names){
			if(pName.equals("@arguments")){
				CHLog.GetLogger().w(CHLog.Tags.COMPILER, "This closure overrides the builtin @arguments parameter", t);
				break;
			}
		}
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
			b.append(((IVariable)node.getData()).getVariableName());
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
	 * LoopManipulationExceptions will never bubble up past this point, because they are
	 * never allowed, so they are handled automatically, but
	 * other ProgramFlowManipulationExceptions will, . ConfigRuntimeExceptions will
	 * also bubble up past this, since an execution mechanism may need to do custom
	 * handling.
	 *
	 * A typical execution will include the following code:
	 * <pre>
	 * try {
	 *	closure.execute();
	 * } catch(ConfigRuntimeException e){
	 *	ConfigRuntimeException.HandleUncaughtException(e);
	 * } catch(ProgramFlowManipulationException e){
	 *	// Ignored
	 * }
     * </pre>
     * @param values The values to be passed to the closure
	 * @throws ConfigRuntimeException If any call inside the closure causes a CRE
	 * @throws ProgramFlowManipulationException If any ProgramFlowManipulationException is thrown
	 * (other than a LoopManipulationException) within the closure
	 * @throws FunctionReturnException If the closure has a return() call in it.
     */
    public void execute(Construct... values) throws ConfigRuntimeException, ProgramFlowManipulationException, FunctionReturnException, CancelCommandException {
		if(node == null){
			return;
		}
		StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
		stManager.addStackTraceElement(new ConfigRuntimeException.StackTraceElement("<<closure>>", getTarget()));
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
                    environment.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(types[i], name, value, getTarget()));
                }
            }
			boolean hasArgumentsParam = false;
			for(String pName : this.names){
				if(pName.equals("@arguments")){
					hasArgumentsParam = true;
					break;
				}
			}
			
			if(!hasArgumentsParam){
				CArray arguments = new CArray(node.getData().getTarget());
				if (values != null) {
					for (Construct value : values) {
						arguments.push(value, node.getData().getTarget());
					}
				}
				environment.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(new CClassType("array", Target.UNKNOWN), "@arguments", arguments, node.getData().getTarget()));
			}
			
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
				ConfigRuntimeException.HandleUncaughtException(ConfigRuntimeException.CreateUncatchableException("A " + lme.getName() + "() bubbled up to the top of"
						+ " a closure, which is unexpected behavior.", t), environment);
			} catch (FunctionReturnException ex){
				// Check the return type of the closure to see if it matches the defined type
				// Normal execution.
				Construct ret = ex.getReturn();
				if(!InstanceofUtil.isInstanceof(ret, returnType)){
					throw new CRECastException("Expected closure to return a value of type " + returnType.val()
							 + " but a value of type " + ret.typeof() + " was returned instead", ret.getTarget());
				}
				// Now rethrow it
				throw ex;
			} catch (CancelCommandException e){
				// die()
			} catch(ConfigRuntimeException ex){
				if(ex instanceof AbstractCREException){
					((AbstractCREException)ex).freezeStackTraceElements(stManager);
				}
				throw ex;
			} catch(Throwable t){
				// Not sure. Pop and re-throw.
				throw t;
			} finally {
				stManager.popStackTraceElement();
			}
			// If we got here, then there was no return type. This is fine, but only for returnType void or auto.
			if(!(returnType.equals(CClassType.AUTO) || returnType.equals(CClassType.VOID))){
				throw new CRECastException("Expecting closure to return a value of type " + returnType.val() + ","
						+ " but no value was returned.", node.getTarget());
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

	@Override
	public String docs() {
		return "A closure is a data type that contains executable code. This is similar to a procedure, but the value is first class,"
				+ " and can be stored in variables, and executed.";
	}

	@Override
	public Version since() {
		return CHVersion.V3_3_1;
	}
}
