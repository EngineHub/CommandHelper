package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopManipulationException;
import com.laytonsmith.core.functions.DataHandling;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A procedure is a user defined function, essentially. Unlike a closure, however, it does not
 * clone a reference to the environment when it is defined. It takes on the environment characteristics
 * of the executing environment, not the defining environment.
 */
public class Procedure implements Cloneable {

    private final String name;
    private Map<String, IVariable> varList;
    private final Map<String, Construct> originals = new HashMap<>();
    private final List<IVariable> varIndex = new ArrayList<>();
    private ParseTree tree;
	private CClassType returnType;
    private boolean possiblyConstant = false;
	/**
	 * The line the procedure is defined at (for stacktraces)
	 */
	private final Target definedAt;

    public Procedure(String name, CClassType returnType, List<IVariable> varList, ParseTree tree, Target t) {
        this.name = name;
		this.definedAt = t;
        this.varList = new HashMap<>();
        for (IVariable var : varList) {
            try {
                this.varList.put(var.getName(), var.clone());
            }
            catch (CloneNotSupportedException e) {
                this.varList.put(var.getName(), var);
            }
            this.varIndex.add(var);
            this.originals.put(var.getName(), var.ival());
        }
        this.tree = tree;
        if (!this.name.matches("^_[a-zA-Z0-9]+[a-zA-Z_0-9]*")) {
            throw new ConfigRuntimeException("Procedure names must start with an underscore, and may only contain letters, underscores, and digits. (Found " + this.name + ")", ExceptionType.FormatException, t);
        }
        //Let's look through the tree now, and see if this is possibly constant or not.
        //If it is, it may or may not help us during compilation, but if it's not,
        //we can be sure that we cannot inline this in any way.
        this.possiblyConstant = checkPossiblyConstant(tree);
		this.returnType = returnType;
    }

    private boolean checkPossiblyConstant(ParseTree tree) {
		//TODO: This whole thing is a mess. Instead of doing it this way,
		//individual procs need to be inlined as deemed appropriate.
		if(true){
			return false;
		}
        if (!tree.getData().isDynamic()) {
            //If it isn't dynamic, it certainly could be constant
            return true;
        } else if (tree.getData() instanceof IVariable) {
            //Variables will return true for isDynamic, but they are technically constant, because
            //they are being declared in this scope, or passed in. An import() would break this
            //contract, but import() itself is dynamic, so this is not an issue.
            return true;
        } else if (tree.getData() instanceof CFunction) {
            //If the function itself is not optimizable, we needn't recurse.
            try {
                FunctionBase fb = FunctionList.getFunction(tree.getData());
                if (fb instanceof Function) {
                    Function f = (Function) fb;
                    if (f instanceof DataHandling._return) {
                        //This is a special exception. Return itself is not optimizable,
                        //but if the contents are optimizable, it is still considered constant.
                        if (!tree.hasChildren()) {
                            return true;
                        } else {
                            return checkPossiblyConstant(tree.getChildAt(0));
                        }
                    }
                    //If it's optimizable, it's possible. If it's restricted, it doesn't matter, because
                    //we can't optimize it out anyways, because we need to do the permission check
					Set<Optimizable.OptimizationOption> o = EnumSet.noneOf(Optimizable.OptimizationOption.class);
					if(f instanceof Optimizable){
						o = ((Optimizable)f).optimizationOptions();
					}
                    if (!( ( o != null && (o.contains(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC)
							|| o.contains(Optimizable.OptimizationOption.OPTIMIZE_CONSTANT))) && !f.isRestricted() )) {
                        return false; //Nope. Doesn't matter if the children are or not
                    }
                } else {
                    return false;
                }
            }
            catch (ConfigCompileException e) {
                //It's a proc. We will treat this just like any other function call,
            }
            //Ok, well, we have to check the children first.
            for (ParseTree child : tree.getChildren()) {
                if (!checkPossiblyConstant(child)) {
                    return false; //Nope, since our child can't be constant, neither can we
                }
            }
            //They all check out, so, yep, we could possibly be constant
            return true;
        } else {
            //Uh. Ok, well, nope.
            return false;
        }
    }

    public boolean isPossiblyConstant() {
        return this.possiblyConstant;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
		return name + "(" + StringUtils.Join(varList.keySet(), ", ") + ")";
	}

    /**
     * Convenience wrapper around executing a procedure if the parameters are in
     * tree mode still.
     *
     * @param args
     * @param env
	 * @param t
     * @return
     */
    public Construct cexecute(List<ParseTree> args, Environment env, Target t) {
        List<Construct> list = new ArrayList<>();
        for (ParseTree arg : args) {
            list.add(env.getEnv(GlobalEnv.class).GetScript().seval(arg, env));
        }
        return execute(list, env, t);
    }

    /**
     * Executes this procedure, with the arguments that were passed in
     *
     * @param args
     * @param env
	 * @param t
     * @return
     */
    public Construct execute(List<Construct> args, Environment env, Target t) {
        env.getEnv(GlobalEnv.class).SetVarList(new IVariableList());
		//This is what will become our @arguments var
        CArray arguments = new CArray(Target.UNKNOWN);
        for (String key : originals.keySet()) {
            Construct c = originals.get(key);
            env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(CClassType.AUTO, key, c, Target.UNKNOWN));
            arguments.push(c);
        }
        Script fakeScript = Script.GenerateScript(tree, env.getEnv(GlobalEnv.class).GetLabel());//new Script(null, null);
        for (int i = 0; i < args.size(); i++) {
            Construct c = args.get(i);
            arguments.set(i, c, t);
            if (varIndex.size() > i) {
                String varname = varIndex.get(i).getName();
				if(c instanceof CNull || InstanceofUtil.isInstanceof(c, varIndex.get(i).getDefinedType()) || varIndex.get(i).getDefinedType().equals(CClassType.AUTO)){
					env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(varIndex.get(i).getDefinedType(), varname, c, c.getTarget()));
				} else {
					throw new Exceptions.CastException("Procedure \"" + name + "\" expects a value of type "
							+ varIndex.get(i).getDefinedType().val() + " in argument " + (i + 1) + ", but"
							+ " a value of type " + c.typeof() + " was found instead.", c.getTarget());
				}
            }
        }
        env.getEnv(GlobalEnv.class).GetVarList().set(new IVariable(new CClassType("array", Target.UNKNOWN), "@arguments", arguments, Target.UNKNOWN));

        try {
			if(tree.getData() instanceof CFunction
					&& "sconcat".equals(tree.getData().val())){
				//If the inner tree is just an sconcat, we can optimize by
				//simply running the arguments to the sconcat. We're not going
				//to use the results, after all, and this is a common occurance,
				//because the compiler will often put it there automatically.
				//We *could* optimize this by removing it from the compiled code,
				//and we still should do that, but this check is quick enough,
				//and so can remain even once we do add the optimization to the
				//compiler proper.
				for(ParseTree child : tree.getChildren()){
					fakeScript.eval(child, env);
				}
			} else {
				fakeScript.eval(tree, env);
			}
        } catch (FunctionReturnException e) {
            Construct ret = e.getReturn();
			if(!InstanceofUtil.isInstanceof(ret, returnType)){
				throw new Exceptions.CastException("Expected procedure \"" + name + "\" to return a value of type " + returnType.val()
						 + " but a value of type " + ret.typeof() + " was returned instead", ret.getTarget());
			}
			return ret;
		} catch(LoopManipulationException ex){
			// These cannot bubble up past procedure calls. This will eventually be
			// a compile error.
			throw ConfigRuntimeException.CreateUncatchableException("Loop manipulation operations (e.g. break() or continue()) cannot"
					+ " bubble up past procedures.", t);
        } catch(ConfigRuntimeException e){
			e.addStackTraceTrail(new ConfigRuntimeException.StackTraceElement("proc " + name, e.getTarget()), t);
			throw e;
		}
		// If we got here, then there was no return value. This is fine, but only for returnType void or auto.
		if(!(returnType.equals(CClassType.AUTO) || returnType.equals(CClassType.VOID))){
			throw new Exceptions.CastException("Expecting procedure \"" + name + "\" to return a value of type " + returnType.val() + ","
					+ " but no value was returned.", tree.getTarget());
		}
        return CVoid.VOID;
    }

	public Target getTarget(){
		return definedAt;
	}

    @Override
    public Procedure clone() throws CloneNotSupportedException {
        Procedure clone = (Procedure) super.clone();
        if (this.varList != null) {
            clone.varList = new HashMap<>(this.varList);
        }
        if (this.tree != null) {
            clone.tree = this.tree.clone();
        }
        return clone;
    }

	public void definitelyNotConstant() {
		possiblyConstant = false;
	}
}
