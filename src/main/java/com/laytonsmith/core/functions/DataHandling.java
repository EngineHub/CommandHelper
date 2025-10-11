package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.annotations.DocumentLink;
import com.laytonsmith.annotations.OperatorPreferred;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.annotations.nolinking;
import com.laytonsmith.annotations.noprofile;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.annotations.unbreakable;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.natives.interfaces.Callable;
import com.laytonsmith.core.Globals;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.NodeModifiers;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Procedure;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.BranchStatement;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.compiler.SelfStatement;
import com.laytonsmith.core.compiler.VariableScope;
import com.laytonsmith.core.compiler.analysis.BreakableBoundDeclaration;
import com.laytonsmith.core.compiler.analysis.ContinuableBoundDeclaration;
import com.laytonsmith.core.compiler.analysis.Declaration;
import com.laytonsmith.core.compiler.analysis.IVariableAssignDeclaration;
import com.laytonsmith.core.compiler.analysis.IncludeReference;
import com.laytonsmith.core.compiler.analysis.Namespace;
import com.laytonsmith.core.compiler.analysis.ParamDeclaration;
import com.laytonsmith.core.compiler.analysis.ProcDeclaration;
import com.laytonsmith.core.compiler.analysis.ProcRootDeclaration;
import com.laytonsmith.core.compiler.analysis.Reference;
import com.laytonsmith.core.compiler.analysis.ReturnableDeclaration;
import com.laytonsmith.core.compiler.analysis.Scope;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.compiler.signature.SignatureBuilder;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFixedArray;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CIClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CLabel;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CSlice;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.InstanceofUtil;
import com.laytonsmith.core.constructs.ProcedureUsage;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.Environment.EnvironmentImpl;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREIncludeException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CREInvalidProcedureException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREStackOverflowError;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.StackTraceManager;
import com.laytonsmith.core.functions.ArrayHandling.array_get;
import com.laytonsmith.core.functions.ArrayHandling.array_push;
import com.laytonsmith.core.functions.ArrayHandling.array_set;
import com.laytonsmith.core.functions.Compiler.__autoconcat__;
import com.laytonsmith.core.functions.Compiler.__statements__;
import com.laytonsmith.core.functions.Compiler.__type_ref__;
import com.laytonsmith.core.functions.Compiler.centry;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.tools.docgen.templates.ArrayIteration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.SymbolKind;

/**
 *
 */
@core
public class DataHandling {

	public static String docs() {
		return "This class provides various methods to control script data and program flow.";
	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Arrays.class, ArrayIteration.class})
	public static class array extends AbstractFunction implements Optimizable {

		public static final String NAME = "array";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return new CArray(t, args);
		}

		@Override
		public CClassType typecheck(StaticAnalysis analysis,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			return typecheckArray(analysis, ast, env, exceptions);
		}

		protected static CClassType typecheckArray(StaticAnalysis analysis,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			for(ParseTree child : ast.getChildren()) {
				Mixed elem = child.getData();

				// If this is a centry(), ignore the first (CLabel) argument.
				if(elem instanceof CFunction && centry.NAME.equals(elem.val())) {
					if(child.numberOfChildren() == 2) {
						CClassType type = analysis.typecheck(child.getChildAt(1), env, exceptions);
						StaticAnalysis.requireType(type, Mixed.TYPE, child.getChildAt(1).getTarget(), env, exceptions);
					}
				} else {

					// This is normal value, so typecheck it.
					CClassType type = analysis.typecheck(child, env, exceptions);
					StaticAnalysis.requireType(type, Mixed.TYPE, child.getTarget(), env, exceptions);
				}
			}
			return CArray.TYPE;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public String docs() {
			return "array {[var1, [var2...]]} Creates an array of values.";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "array @array = array(1, 2, 3);\nmsg(@array);"),
				new ExampleScript("Associative array creation", "array @array = array(one: 'apple', two: 'banana');\nmsg(@array);")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		FileOptions lastFileOptions = null;

		@Override
		@SuppressWarnings("null")
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			//We need to check here to ensure that
			//we aren't getting a slice in a label, which is used in switch
			//statements, but doesn't make sense here.
			//Also check for dynamic labels
			for(ParseTree child : children) {
				if(child.getData() instanceof CFunction && centry.NAME.equals(child.getData().val())) {
					if(((CLabel) child.getChildAt(0).getData()).cVal() instanceof CSlice) {
						throw new ConfigCompileException("Slices cannot be used as array indices", child.getChildAt(0).getTarget());
					}
					CLabel label = ((CLabel) child.getChildAt(0).getData());
					if(label.cVal() instanceof IVariable || label.cVal() instanceof CFunction) {
						String array = "@a";
						String valueName = "\"key\"";
						if(label.cVal() instanceof IVariable ivar) {
							valueName = ivar.getVariableName();
						}
						Mixed value = child.getChildAt(1).getData();
						String v;
						if(value instanceof IVariable iVariable) {
							v = iVariable.getVariableName();
						} else if(value.isInstanceOf(CString.TYPE)) {
							v = ((CString) value).getQuote();
						} else {
							v = "@value";
						}
						if("@a".equals(valueName)) {
							array = "@myArray";
						}
						throw new ConfigCompileException("Dynamic values cannot be used as indices in array construction."
								+ "\nTo make dynamic indicies, do the following: "
								+ "array " + array + " = array(); " + array + "[" + valueName + "] = " + v + ";", t);
					}
				}
			}
			return null;
		}

	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Arrays.class, ArrayIteration.class})
	public static class associative_array extends AbstractFunction {

		public static final String NAME = "associative_array";

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CArray.GetAssociativeArray(t, args);
		}

		@Override
		public CClassType typecheck(StaticAnalysis analysis,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			return array.typecheckArray(analysis, ast, env, exceptions);
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "array {[args...]} Works exactly like array(), except the array created will be an associative array, even"
					+ " if the array has been created with no elements. This is the only use case where this is necessary, vs"
					+ " using the normal array() function, or in the case where you assign sequential keys anyways, and the same"
					+ " array could have been created using array().";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Usage with an empty array", "assign(@array, associative_array())\nmsg(is_associative(@array))"),
				new ExampleScript("Usage with an array with sequential keys", "assign(@array, array(0: '0', 1: '1'))\nmsg(is_associative(@array))\n"
				+ "assign(@array, associative_array(0: '0', 1: '1'))\nmsg(is_associative(@array))")};
		}

	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Variables.class})
	@OperatorPreferred("=")
	public static class assign extends AbstractFunction implements Optimizable {

		public static final String NAME = "assign";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			IVariableList list = env.getEnv(GlobalEnv.class).GetVarList();
			int offset;
			CClassType type;
			String name;
			if(args.length == 3) {
				offset = 1;
				if(!(args[offset] instanceof IVariable)) {
					throw new CRECastException(getName() + " with 3 arguments only accepts an ivariable as the second argument.", t);
				}
				name = ((IVariable) args[offset]).getVariableName();
				if(list.has(name) && env.getEnv(GlobalEnv.class).GetFlag("no-check-duplicate-assign") == null) {
					if(env.getEnv(GlobalEnv.class).GetFlag("closure-warn-overwrite") != null) {
						MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.WARNING,
								"The variable " + name + " is hiding another value of the"
								+ " same name in the main scope.", t);
					} else if(!StaticAnalysis.enabled() && t != list.get(name, t, true, env).getDefinedTarget()) {
						MSLog.GetLogger().Log(MSLog.Tags.RUNTIME, LogLevel.ERROR, name + " was already defined at "
								+ list.get(name, t, true, env).getDefinedTarget() + " but is being redefined.", t);
					}
				}
				type = ArgumentValidation.getClassType(args[0], t);
				Boolean varArgsAllowed = env.getEnv(GlobalEnv.class).GetFlag("var-args-allowed");
				if(varArgsAllowed == null) {
					varArgsAllowed = false;
				}
				if(type.isVarargs() && !varArgsAllowed) {
					throw new CRECastException("Cannot use varargs type in this context", t);
				}
			} else {
				offset = 0;
				if(!(args[offset] instanceof IVariable)) {
					throw new CRECastException(getName() + " with 2 arguments only accepts an ivariable as the first argument.", t);
				}
				name = ((IVariable) args[offset]).getVariableName();
				IVariable listVar = list.get(name, t, true, env);
				t = listVar.getDefinedTarget();
				type = listVar.getDefinedType();
			}
			Mixed c = args[offset + 1];
			while(c instanceof IVariable) {
				IVariable cur = (IVariable) c;
				c = list.get(cur.getVariableName(), cur.getTarget(), env).ival();
			}
			IVariable v = new IVariable(type, name, c, t, env);
			list.set(v);
			return v;
		}

		@Override
		@SuppressWarnings("checkstyle:FallThrough")
		public CClassType typecheck(StaticAnalysis analysis,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			int ind = 0;
			CClassType declaredType = null;
			switch(ast.numberOfChildren()) {
				case 3:
					// Typecheck declaration type.
					ParseTree typeNode = ast.getChildAt(ind++);
					declaredType = StaticAnalysis.requireClassType(typeNode, exceptions);
					// Intentional fallthrough.
				case 2:
					// Typecheck variable.
					ParseTree varNode = ast.getChildAt(ind++);
					IVariable ivar = StaticAnalysis.requireIVariable(
							varNode.getData(), varNode.getTarget(), exceptions);

					// Get assigned value.
					ParseTree valNode = ast.getChildAt(ind);
					CClassType valType = analysis.typecheck(valNode, env, exceptions);

					// Attempt to get the declared type from this variable's declaration.
					if(declaredType == null && ivar != null) {
						Scope scope = analysis.getTermScope(varNode);
						if(scope != null) {
							Set<Declaration> decls = scope.getDeclarations(Namespace.IVARIABLE, ivar.getVariableName());
							if(decls.size() > 0) {

								// Type check assigned value for all found declaration types.
								for(Declaration decl : decls) {
									StaticAnalysis.requireType(
											valType, decl.getType(), valNode.getTarget(), env, exceptions);
								}
								return valType;
							}
						}
					}

					// If a variable is declared as AUTO or unknown, then its value should actually be any mixed.
					if(declaredType == null || declaredType == CClassType.AUTO) {
						declaredType = Mixed.TYPE;
					}

					// Type check assigned value.
					StaticAnalysis.requireType(valType, declaredType, valNode.getTarget(), env, exceptions);

					// Return the value type.
					return valType;
				default:
					// Invalid number of arguments. Don't generate any further errors.
					return CClassType.AUTO;
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast,
				Environment env, Set<ConfigCompileException> exceptions) {
			if(ast.getChildren().size() == 3) { // Variable declaration + assign: assign(type, var, val).

				// Handle the assigned value.
				Scope valScope = analysis.linkScope(parentScope, ast.getChildAt(2), env, exceptions);

				// Create a new scope and put the variable declaration in that scope.
				Scope declScope = analysis.createNewScope(valScope);
				Mixed rawType = ast.getChildAt(0).getData();
				ParseTree ivarAst = ast.getChildAt(1);
				Mixed rawIVar = ivarAst.getData();
				if(rawType instanceof CClassType && rawIVar instanceof IVariable) {
					CClassType type = (CClassType) rawType;
					IVariable iVar = (IVariable) rawIVar;

					// Add the new variable declaration.
					declScope.addDeclaration(new Declaration(
							Namespace.IVARIABLE, iVar.getVariableName(), type, ast.getNodeModifiers(),
							ast.getTarget()));
					analysis.setTermScope(ivarAst, declScope);
				}

				// Return the declaration scope.
				return declScope;

			} else if(ast.getChildren().size() == 2) { // Variable assign: assign(var, val).

				// Handle the assigned value.
				Scope valScope = analysis.linkScope(parentScope, ast.getChildAt(1), env, exceptions);

				// Create a new scope and put the variable assign declaration in that scope.
				// This declaration will be converted to either a variable reference or declaration later.
				Scope newScope = analysis.createNewScope(valScope);
				ParseTree ivarAst = ast.getChildAt(0);
				Mixed rawIVar = ivarAst.getData();
				if(rawIVar instanceof IVariable) {
					IVariable iVar = (IVariable) rawIVar;

					// Add ivariable assign declaration in a new scope.
					newScope.addDeclaration(new IVariableAssignDeclaration(iVar.getVariableName(),
							ast.getNodeModifiers(),
							iVar.getTarget()));
					analysis.setTermScope(ivarAst, newScope);
				}

				// Return the new scope.
				return newScope;
			}
			return super.linkScope(analysis, parentScope, ast, env, exceptions);
		}

		/**
		 * Handles an {@code assign()} that is used as parameter in for example a procedure or closure.This will declare the parameter in the paramScope scope, using the {@link Namespace#IVARIABLE} namespace.
		 * The default parameter value (assigned value) will be handled in the valScope.
		 * @param analysis
		 * @param paramScope - The scope to which a new scope is linked in which the declaration will be placed.
		 * @param valScope - The scope to which a new scope is linked in which the assigned value will be handled.
		 * @param ast - The AST of the {@code assign()} function.
		 * @param env
		 * @param exceptions
		 * @param params
		 * @return The resulting scopes in format {paramScope, valScope}.
		 */
		public Scope[] linkParamScope(StaticAnalysis analysis, Scope paramScope, Scope valScope,
				ParseTree ast, Environment env, Set<ConfigCompileException> exceptions, List<ParamDeclaration> params) {

			// Typed parameter: assign(type, var, val).
			if(ast.getChildren().size() == 3) {

				// Handle the assigned value.
				valScope = analysis.linkScope(valScope, ast.getChildAt(2), env, exceptions);

				// Put the variable declaration in the param scope.
				Mixed rawType = ast.getChildAt(0).getData();
				ParseTree ivarAst = ast.getChildAt(1);
				Mixed rawIVar = ivarAst.getData();
				if(rawType instanceof CClassType && rawIVar instanceof IVariable) {
					CClassType type = (CClassType) rawType;
					IVariable iVar = (IVariable) rawIVar;

					// Add the new variable declaration.
					paramScope = analysis.createNewScope(paramScope);
					ParamDeclaration pDecl = new ParamDeclaration(iVar.getVariableName(), type, ast.getChildAt(2),
							ast.getNodeModifiers(),
							ast.getTarget());
					params.add(pDecl);
					paramScope.addDeclaration(pDecl);
					analysis.setTermScope(ivarAst, paramScope);
				}

				// Return the scope pair.
				return new Scope[] {paramScope, valScope};

			}

			// Untyped parameter: assign(var, val).
			if(ast.getChildren().size() == 2) {

				// Handle the assigned value.
				valScope = analysis.linkScope(valScope, ast.getChildAt(1), env, exceptions);

				// Put the variable declaration in the param scope.
				ParseTree ivarAst = ast.getChildAt(0);
				Mixed rawIVar = ivarAst.getData();
				if(rawIVar instanceof IVariable) {
					IVariable iVar = (IVariable) rawIVar;

					// Add the new variable declaration.
					paramScope = analysis.createNewScope(paramScope);
					ParamDeclaration pDecl = new ParamDeclaration(
							iVar.getVariableName(), CClassType.AUTO, null, ast.getNodeModifiers(),
							ast.getTarget());
					params.add(pDecl);
					paramScope.addDeclaration(pDecl);
					analysis.setTermScope(ivarAst, paramScope);
				}

				// Return the scope pair.
				return new Scope[] {paramScope, valScope};
			}

			// Invalid parameter. Fall back to handling this function's arguments.
			return new Scope[] {paramScope, super.linkScope(analysis, valScope, ast, env, exceptions)};
		}

		@Override
		public String docs() {
			return "ivariable {[type], ivar, mixed} Accepts an ivariable ivar as a parameter, and puts the specified value mixed in it."
					+ " Returns the variable that was assigned. Operator syntax is also supported: <code>@a = 5;</code>."
					+ " Other forms are supported as well, +=, -=, *=, /=, .=, which do multiple operations at once. Array assigns"
					+ " are also supported: @array[5] = 'new value in index 5';";
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_CONSTANT,
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public Mixed optimize(Target t, Environment env, Mixed... args) throws ConfigCompileException {
			//We can't really optimize, but we can check that we are
			//getting an ivariable.
			int offset = 0;
			if(args.length == 3) {
				offset = 1;
				if(!args[0].isInstanceOf(CClassType.TYPE)
						&& (!(args[0] instanceof CFunction) || !args[0].val().equals(__type_ref__.NAME))) {
					throw new ConfigCompileException("Expecting a ClassType for parameter 1 to assign", t);
				}
			}
			if(args.length > 0 && !(args[offset] instanceof IVariable)) {
				throw new ConfigCompileException("Expecting an ivar for argument 1 to assign", t);
			}
			return null;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			//Check for too few arguments
			if(children.size() < 2) {
				return null;
			}
			if(children.get(0).getData() instanceof IVariable
					&& children.get(1).getData() instanceof IVariable) {
				if(((IVariable) children.get(0).getData()).getVariableName().equals(
						((IVariable) children.get(1).getData()).getVariableName())) {
					String msg = "Assigning a variable to itself";
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
							new CompilerWarning(msg, t, null));
				}
			}
			{
				// Check for declaration of variables named "pass" or "password" and see if it's defined as a
				// secure_string. If not, warn.
				if(children.get(0).getData() instanceof CClassType && children.get(1).getData() instanceof IVariable) {
					boolean isString
							= ((CClassType) children.get(0).getData()).getNativeType() == CString.class;
					String varName = ((IVariable) children.get(1).getData()).getVariableName();
					if((varName.equalsIgnoreCase("pass") || varName.equalsIgnoreCase("password"))
							&& isString) {
						String msg = "";
						env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
								new CompilerWarning(msg, t, FileOptions.SuppressWarning.CodeUpgradeNotices));
					}
				}
			}
			return null;
		}

		@Override
		public ParseTree postParseRewrite(ParseTree ast, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
			List<ParseTree> children = ast.getChildren();

			// Check for too few arguments.
			if(children.size() < 2) {
				return null;
			}

			// Convert "assign(@a[<ind>], val)" to "array_push(@a, val)" or "array_set(@a, ind, val)".
			if(children.get(0).getData() instanceof CFunction && array_get.NAME.equals(children.get(0).getData().val())) {
				if(children.get(0).getChildAt(1).getData() instanceof CSlice) {
					CSlice cs = (CSlice) children.get(0).getChildAt(1).getData();
					if(cs.getStart() == 0 && cs.getFinish() == -1) {
						//Turn this into an array_push
						ParseTree tree = new ParseTree(new CFunction(
								array_push.NAME, ast.getTarget()), children.get(0).getFileOptions());
						tree.addChild(children.get(0).getChildAt(0));
						tree.addChild(children.get(1));
						return tree;
					}
					//else, not really sure what's going on, so we'll just carry on, and probably there
					//will be an error generated elsewhere
				} else {
					//Turn this into an array set instead
					ParseTree tree = new ParseTree(new CFunction(
							array_set.NAME, ast.getTarget()), children.get(0).getFileOptions());
					tree.addChild(children.get(0).getChildAt(0));
					tree.addChild(children.get(0).getChildAt(1));
					tree.addChild(children.get(1));
					return tree;
				}
			}

			// Convert concatenated types such as "concat(concat(ms, lang), int)" to "ms.lang.int".
			if(children.size() == 3) {
				ParseTree typeRefNode = __type_ref__.createFromBareStringOrConcats(children.get(0));
				if(typeRefNode != null) {
					children.set(0, typeRefNode);
					return ast;
				}
			}

			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "assign(@variable, 5);\nmsg(@variable);"),
				new ExampleScript("Array assignment", "assign(@variable, associative_array());\nassign(@variable['associative'], 5);\nmsg(@variable);"),
				new ExampleScript("String assignment with type", "assign(string, @s, 'string');"),
				new ExampleScript("String assignment with invalid type", "assign(int, @i, 'string');", true),
				new ExampleScript("Operator syntax", "@variable = 5;\nmsg(@variable);"),
				new ExampleScript("Operator syntax with type", "string @s = 'string';"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 'string';\n@variable .= ' more string';\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable += 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable -= 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable *= 10;\nmsg(@variable);"),
				new ExampleScript("Operator syntax using combined operators", "@variable = 5;\n@variable /= 10;\nmsg(@variable);")};
		}
	}

	@api
	public static class is_stringable extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_stringable";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is convertable to a string. Everything but arrays can be used as strings.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(!(args[0].isInstanceOf(CArray.TYPE)));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_stringable('yes')"),
				new ExampleScript("True condition", "is_stringable(1) #This can be used as a string, yes"),
				new ExampleScript("False condition", "is_stringable(array(1))")};
		}
	}

	@api
	public static class is_string extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_string";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a string datatype. If you just care if some data can be used as a string,"
					+ " use is_stringable().";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CString.TYPE));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_string('yes')"),
				new ExampleScript("False condition", "is_string(1) #is_stringable() would return true here")};
		}
	}

	@api
	public static class is_bytearray extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_bytearray";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is actually a ByteArray datatype.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CByteArray.TYPE));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_bytearray(string_get_bytes('yay'))"),
				new ExampleScript("False condition", "is_bytearray('Nay')"),
				new ExampleScript("False condition", "is_bytearray(123)")};
		}
	}

	@api
	public static class is_array extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the item is an array";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CArray.TYPE));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_array(array(1))"),
				new ExampleScript("True condition", "is_array(array(one: 1))"),
				new ExampleScript("False condition", "is_array('no')")};
		}
	}

	@api
	public static class is_number extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_number";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is an integer or a double. Note that numeric strings can usually be used as integers and doubles,"
					+ " however this function checks the actual datatype of the item. If you just want to see if an item can be used as a number,"
					+ " use is_integral() or is_numeric() instead.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CInt.TYPE) || args[0].isInstanceOf(CDouble.TYPE));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_number(1)"),
				new ExampleScript("True condition", "is_number(1.0)"),
				new ExampleScript("False condition", "is_number('1')"),
				new ExampleScript("False condition", "is_number('1.0')")};
		}
	}

	@api
	public static class is_double extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_double";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is a double. Note that numeric strings and integers"
					+ " can usually be used as a double, however this function checks the actual datatype of the item. If"
					+ " you just want to see if an item can be used as a number, use is_numeric() instead.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CDouble.TYPE));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_double(1.0)"),
				new ExampleScript("False condition", "is_double(1)")};
		}
	}

	@api
	public static class is_integer extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_integer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is an integer. Note that numeric strings can usually be used as integers,"
					+ " however this function checks the actual datatype of the item. If you just want to see if an item can be used as a number,"
					+ " use is_integral() or is_numeric() instead.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CInt.TYPE));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_integer(1)"),
				new ExampleScript("False condition", "is_integer(1.0)")};
		}
	}

	@api
	public static class is_boolean extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_boolean";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether the given item is of the boolean datatype. Note that all datatypes can be used as booleans, however"
					+ " this function checks the specific datatype of the given item.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CBoolean.TYPE));
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_boolean(false)"),
				new ExampleScript("False condition", "is_boolean(0)")};
		}
	}

	@api
	public static class is_null extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_null";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns whether or not the given item is null.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0] instanceof CNull);
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_null(null)"),
				new ExampleScript("False condition", "is_null(0)")};
		}
	}

	@api
	public static class is_numeric extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_numeric";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns false if the item would fail if it were used as a numeric value."
					+ " If it can be parsed or otherwise converted into a numeric value, true is returned.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean b = true;
			try {
				ArgumentValidation.getNumber(args[0], t);
			} catch (ConfigRuntimeException e) {
				b = false;
			}
			return CBoolean.get(b);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_numeric('1.0')"),
				new ExampleScript("True condition", "is_numeric('1')"),
				new ExampleScript("True condition", "is_numeric(1)"),
				new ExampleScript("True condition", "is_numeric(1.5)"),
				new ExampleScript("False condition", "is_numeric('string')"),
				new ExampleScript("True condition, because null is coerced to 0.0, which is numeric.", "is_numeric(null)")};
		}
	}

	@api
	public static class is_integral extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_integral";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns true if the numeric value represented by "
					+ " a given double or numeric string could be cast to an integer"
					+ " without losing data (or if it's an integer). For instance,"
					+ " is_numeric(4.5) would return true, and integer(4.5) would work,"
					+ " however, equals(4.5, integer(4.5)) returns false, because the"
					+ " value was narrowed to 4.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			double d;
			try {
				d = ArgumentValidation.getDouble(args[0], t);
			} catch (ConfigRuntimeException e) {
				return CBoolean.FALSE;
			}
			return CBoolean.get((long) d == d);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_integral(1.0)"),
				new ExampleScript("True condition", "is_integral(1)"),
				new ExampleScript("True condition", "is_integral('5.0')"),
				new ExampleScript("True condition", "is_integral('6')"),
				new ExampleScript("False condition", "is_integral(1.5)"),
				new ExampleScript("True condition, because null is coerced to 0, which is integral", "is_integral(null)")};
		}
	}

	@api
	@unbreakable
	@SelfStatement
	public static class proc extends AbstractFunction implements BranchStatement, VariableScope, DocumentSymbolProvider {

		public static final String NAME = "proc";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {procName, [params...], procCode} Creates a new user defined procedure (also known as"
					+ " \"function\"), with the given name and parameters, that can be called later in code."
					+ " The name of the procedure must be a constant and its parameters must be variables."
					+ " Please see the more detailed documentation on procedures for more information."
					+ " In general, brace syntax and keyword usage is preferred:"
					+ " proc _myProc(@a, @b){ procCode(@a, @b); }";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_3;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			Procedure myProc = getProcedure(t, env, parent, nodes);
			env.getEnv(GlobalEnv.class).GetProcs().put(myProc.getName(), myProc);
			return CVoid.VOID;
		}

		public static Procedure getProcedure(Target t, Environment env, Script parent, ParseTree... nodes) {
			String name = "";
			List<IVariable> vars = new ArrayList<>();
			ParseTree tree = null;
			List<String> varNames = new ArrayList<>();
			boolean usesAssign = false;
			CClassType returnType = Auto.TYPE;
			NodeModifiers modifiers = null;
			if(nodes[0].getData().equals(CVoid.VOID) || nodes[0].getData().isInstanceOf(CClassType.TYPE)) {
				if(nodes[0].getData().equals(CVoid.VOID)) {
					returnType = CVoid.TYPE;
				} else {
					returnType = (CClassType) nodes[0].getData();
				}
				ParseTree[] newNodes = new ParseTree[nodes.length - 1];
				for(int i = 1; i < nodes.length; i++) {
					newNodes[i - 1] = nodes[i];
				}
				modifiers = nodes[0].getNodeModifiers();
				nodes = newNodes;
			}
			nodes[0].getNodeModifiers().merge(modifiers);
			// We have to restore the variable list once we're done
			IVariableList originalList = env.getEnv(GlobalEnv.class).GetVarList().clone();
			for(int i = 0; i < nodes.length; i++) {
				if(i == nodes.length - 1) {
					tree = nodes[i];
				} else {
					boolean thisNodeIsAssign = false;
					if(nodes[i].getData() instanceof CFunction) {
						if((nodes[i].getData()).val().equals(assign.NAME)) {
							thisNodeIsAssign = true;
							if((nodes[i].getChildren().size() == 3 && Construct.IsDynamicHelper(nodes[i].getChildAt(0).getData()))
									|| Construct.IsDynamicHelper(nodes[i].getChildAt(1).getData())) {
								usesAssign = true;
							}
						} else if((nodes[i].getData()).val().equals(__autoconcat__.NAME)) {
							throw new CREInvalidProcedureException("Invalid arguments defined for procedure", t);
						}
					}
					env.getEnv(GlobalEnv.class).SetFlag("no-check-duplicate-assign", true);
					env.getEnv(GlobalEnv.class).SetFlag("var-args-allowed", true);
					Mixed cons = parent.eval(nodes[i], env);
					env.getEnv(GlobalEnv.class).ClearFlag("var-args-allowed");
					env.getEnv(GlobalEnv.class).ClearFlag("no-check-duplicate-assign");
					if(i == 0) {
						if(cons instanceof IVariable) {
							throw new CREInvalidProcedureException("Anonymous Procedures are not allowed", t);
						}
						name = cons.val();
					} else {
						if(!(cons instanceof IVariable)) {
							throw new CREInvalidProcedureException("You must use IVariables as the arguments", t);
						}
						IVariable ivar = null;
						try {
							Mixed c = cons;
							String varName = ((IVariable) c).getVariableName();
							if(varNames.contains(varName)) {
								throw new CREInvalidProcedureException("Same variable name defined twice in " + name, t);
							}
							varNames.add(varName);
							while(c instanceof IVariable) {
								c = env.getEnv(GlobalEnv.class).GetVarList().get(((IVariable) c).getVariableName(), t,
										true, env).ival();
							}
							if(!thisNodeIsAssign) {
								//This is required because otherwise a default value that's already in the environment
								//would end up getting set to the existing value, thereby leaking in the global env
								//into this proc, if the call to the proc didn't have a value in this slot.
								c = new CString("", t);
							}
							ivar = new IVariable(((IVariable) cons).getDefinedType(),
									((IVariable) cons).getVariableName(), c.clone(), t, env);
						} catch (CloneNotSupportedException ex) {
							//
						}
						vars.add(ivar);
					}
				}
			}
			env.getEnv(GlobalEnv.class).SetVarList(originalList);
			Procedure myProc = new Procedure(name, returnType, vars, nodes[0].getNodeModifiers().getComment(), tree, t);
			if(usesAssign) {
				myProc.definitelyNotConstant();
			}
			return myProc;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast,
				Environment env, Set<ConfigCompileException> exceptions) {

			// Handle not enough arguments.
			if(ast.numberOfChildren() < 2) {
				return parentScope;
			}

			// Handle optional return type argument (CClassType or CVoid, default to AUTO).
			int ind = 0;
			CClassType retType;
			if(ast.getChildAt(ind).getData() instanceof CClassType) {
				retType = (CClassType) ast.getChildAt(ind++).getData();
			} else if(ast.getChildAt(ind).getData().equals(CVoid.VOID)) {
				ind++;
				retType = CVoid.TYPE;
			} else {
				retType = CClassType.AUTO;
			}

			// Get proc name.
			ParseTree procNameNode = ast.getChildAt(ind++);
			String procName = procNameNode.getData().val();

			// Create parameter scope.
			Scope paramScope = analysis.createNewScope();

			// Insert @arguments parameter.
			paramScope.addDeclaration(new ParamDeclaration("@arguments", CArray.TYPE, null, ast.getNodeModifiers(),
					ast.getTarget()));

			// Handle procedure parameters from left to right.
			Scope valScope = parentScope;
			List<ParamDeclaration> params = new ArrayList<>();
			while(ind < ast.numberOfChildren() - 1) {
				ParseTree param = ast.getChildAt(ind++);
				Scope[] scopes = analysis.linkParamScope(paramScope, valScope, param, env, exceptions, params);
				valScope = scopes[1];
				paramScope = scopes[0];
			}

			// Handle procedure code.
			ParseTree code = ast.getChildAt(ast.numberOfChildren() - 1);
			analysis.linkScope(paramScope, code, env, exceptions);

			// Create proc declaration in a new scope.
			// TODO - Include proc signature (argument types and number of arguments) in declaration.
			Scope declScope = analysis.createNewScope(parentScope);
			ProcDeclaration procDecl = new ProcDeclaration(procName, retType, params,
					ast.getNodeModifiers(), ast.getTarget());
			declScope.addDeclaration(procDecl);
			analysis.setTermScope(ast, declScope);

			// Create proc root declaration in the inner root scope.
			paramScope.addDeclaration(new ProcRootDeclaration(procDecl, ast.getNodeModifiers()));

			// Allow procedures to perform lookups in the decl scope.
			paramScope.addSpecificParent(declScope, Namespace.PROCEDURE);

			// Create returnable declaration in the inner root scope.
			paramScope.addDeclaration(new ReturnableDeclaration(retType, ast.getNodeModifiers(), ast.getTarget()));

			// Return the declaration scope. Parameters and their default values are not accessible after the procedure.
			return declScope;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		/**
		 * Returns either null to indicate that the procedure is not const, or returns a single Mixed, which should
		 * replace the call to the procedure.
		 *
		 * @param t
		 * @param myProc
		 * @param children
		 * @return
		 * @throws ConfigRuntimeException
		 */
		public static Mixed optimizeProcedure(Target t, Procedure myProc, List<ParseTree> children) throws ConfigRuntimeException {
			if(myProc.isPossiblyConstant()) {
				//Oooh, it's possibly constant. So, let's run it with our children.
				try {
					FileOptions options;
					if(!children.isEmpty()) {
						options = children.get(0).getFileOptions();
					} else {
						options = new FileOptions(new HashMap<>());
					}
					ParseTree root = new ParseTree(new CFunction(__autoconcat__.NAME, Target.UNKNOWN), options);
					Script fakeScript = Script.GenerateScript(root, Static.GLOBAL_PERMISSION, null);
					Environment env = Static.GenerateStandaloneEnvironment();
					env.getEnv(GlobalEnv.class).SetScript(fakeScript);
					Mixed c = myProc.cexecute(children, env, t);
					//Yup! It worked. It's a const proc.
					return c;
				} catch (ConfigRuntimeException e) {
					if(e instanceof CREThrowable
							&& ((CREThrowable) e).isInstanceOf(CREInvalidProcedureException.TYPE)) {
						//This is the only valid exception that doesn't strictly mean it's a bad
						//call.
						return null;
					}
					throw e; //Rethrow it. Since the functions are all static, and we actually are
					//running it with a mostly legit environment, this is a real runtime error,
					//and we can safely convert it to a compile error upstream
				} catch (Exception e) {
					//Nope. Something is preventing us from running it statically.
					//We don't really care. We just know it can't be optimized.
					return null;
				}
			} else {
				//Oh. Well, we tried.
				return null;
			}
		}

//		@Override
//		public boolean canOptimizeDynamic() {
//			return true;
//		}
//
//		@Override
//		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
//			//We seriously lose out on the ability to optimize this procedure
//			//if we are assigning a dynamic value as a default, but we have to check
//			//that here. If we don't, we lose the information
//			return ;
//		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(int i = 0; i < children.size() - 1; i++) {
				ret.add(false);
			}
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			for(ParseTree child : children) {
				ret.add(true);
			}
			return ret;
		}

		@Override
		public String symbolDisplayName(List<ParseTree> children) {
			StringBuilder builder = new StringBuilder();
			int offset = 0;
			if(children.get(0).getData() instanceof CClassType type) {
				offset = 1;
				builder.append(type.getSimpleName());
			} else {
				builder.append("auto");
			}
			builder.append(" proc ");
			builder.append(ArgumentValidation.getString(children.get(offset).getData(), Target.UNKNOWN));
			builder.append("(");
			boolean first = true;
			for(int i = 1 + offset; i < children.size() - 1; i++) {
				if(!first) {
					builder.append(", ");
				}
				first = false;
				ParseTree child = children.get(i);
				Mixed parameter = child.getData();
				if(parameter instanceof IVariable ivar) {
					builder.append(ivar.getVariableName());
				} else if(parameter instanceof CFunction f) {
					try {
						if(f.getFunction() instanceof assign) {
							CClassType type = Auto.TYPE;
							Mixed variable = child.getChildAt(0).getData();
							Mixed value = child.getChildAt(1).getData();
							if(variable instanceof CClassType cct) {
								type = cct;
								variable = child.getChildAt(1).getData();
								value = child.getChildAt(2).getData();
							}
							builder.append(type.getSimpleName()).append(" ");
							if(variable instanceof IVariable ivar) {
								builder.append(ivar.getVariableName());
								if(value != CNull.UNDEFINED) {
									builder.append(" = ");
									if(value instanceof CString) {
										builder.append("'")
												.append(value.val().replace("\\", "\\\\")
														.replace("\t", "\\t").replace("\n", "\\n")
														.replace("'", "\\'"))
												.append("'");
									} else {
										builder.append(value.val());
									}
								}
							}
						}
					} catch (ConfigCompileException ex) {
						builder.append("_");
					}
				}
			}
			builder.append(")");
			return builder.toString();
		}

		@Override
		public SymbolKind getSymbolKind() {
			return SymbolKind.Function;
		}
	}

	@api
	public static class get_proc extends AbstractFunction implements Optimizable {

		public static final String NAME = "get_proc";

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			String procName = args[0].val();
			Procedure proc = env.getEnv(GlobalEnv.class).GetProcs().get(procName);
			if(proc == null) {
				throw new CREIllegalArgumentException("Could not find proc named \"" + procName + "\" in this scope.", t);
			}
			return new ProcedureUsage(proc, env, t);
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			ParseTree procName = ast.getChildren().get(0);
			Target t = procName.getTarget();
			if(!procName.isConst()) {
				exceptions.add(new ConfigCompileException("get_proc (or proc keyword usage) must contain a"
						+ " hardcoded procedure name.", t));
				return parentScope;
			}
			if(procName.getData() instanceof CNull) {
				exceptions.add(new ConfigCompileException("get_proc cannot accept null.", t));
				return parentScope;
			}

			Scope refScope = analysis.createNewScope(parentScope);
			refScope.addReference(new Reference(Namespace.PROCEDURE, procName.getData().val(), procName.getTarget()));
			analysis.setTermScope(procName, refScope);
			return refScope;
		}

		@Override
		public CClassType typecheck(StaticAnalysis sa, ParseTree ast, Environment env, Set<ConfigCompileException> exceptions) {
			ParseTree procName = ast.getChildren().get(0);
			Target t = procName.getTarget();
			boolean found;
			if(sa != null && sa.isLocalEnabled()) {
				found = !sa.getTermScope(procName)
						.getDeclarations(Namespace.PROCEDURE, procName.getData().val()).isEmpty();
			} else {
				found = true;
			}
			if(!found) {
				exceptions.add(new ConfigCompileException("Could not find proc \"" + procName + "\"",
						t));
			}
			return ProcedureUsage.TYPE;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, Set<Class<? extends EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException,
				ConfigRuntimeException, ConfigCompileGroupException {
			// TODO: Once typecheck is always called, this whole function can be removed.
			if(!children.get(0).isConst()) {
				throw new ConfigCompileException("get_proc (or proc keyword usage) must contain a"
						+ " hardcoded procedure name.", t);
			}
			if(children.get(0).getData() instanceof CNull) {
				throw new ConfigCompileException("get_proc cannot accept null.", t);
			}
			return null;
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "Procedure {reference} Returns a first class reference to the currently in scope procedure."
					+ " This can be stored in variables and generally passed around, though it cannot be"
					+ " serialized. Keyword usage is preferred, such as <code>proc _asdf;</code> instead of"
					+ " <code>get_proc('_asdf')</code>. Note that this is a special compiler function, and"
					+ " must contain a hardcoded procedure name.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}

	@api
	@DocumentLink(0)
	public static class include extends AbstractFunction implements Optimizable, DocumentLinkProvider {

		public static final String NAME = "include";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {path} Includes external code at the specified path.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIncludeException.class, CREIOException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public CVoid exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public CVoid execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			ParseTree tree = nodes[0];
			Mixed arg = parent.seval(tree, env);
			String location = arg.val();
			File file = Static.GetFileFromArgument(location, env, t, null);
			try {
				file = file.getCanonicalFile();
			} catch (IOException ex) {
				throw new CREIOException(ex.getMessage(), t);
			}
			// Create new static analysis for dynamic includes that have not yet been cached.
			StaticAnalysis analysis;
			IncludeCache includeCache = env.getEnv(StaticRuntimeEnv.class).getIncludeCache();
			boolean isFirstCompile = false;
			Scope parentScope = includeCache.getDynamicAnalysisParentScopeCache().get(t);
			if(parentScope != null) {
				analysis = includeCache.getStaticAnalysis(file);
				if(analysis == null) {
					analysis = new StaticAnalysis(true);
					analysis.getStartScope().addParent(parentScope);
					isFirstCompile = true;
				}
			} else {
				analysis = new StaticAnalysis(true); // It's a static include.
			}

			// Get or load the include.
			ParseTree include = IncludeCache.get(file, env, env.getEnvClasses(), analysis, t);

			// Perform static analysis for dynamic includes.
			// This should not run if this is the first compile for this include, as IncludeCache.get() checks it then.
			/*
			 *  TODO - This analysis runs on an optimized AST.
			 *  Cloning, caching and using the non-optimized AST would be nice.
			 *  This solution is acceptable in the meantime, as the first analysis of a dynamic include runs
			 *  on the non-optimized AST through IncludeCache.get(), and otherwise-runtime errors should still be
			 *  caught when analyzing the optimized AST.
			 */
			if(isFirstCompile) {

				// Remove this parent scope since it should not end up in the cached analysis.
				analysis.getStartScope().removeParent(parentScope);
			} else {

				// Set up analysis. Cloning is required to not mess up the cached analysis.
				analysis = analysis.clone();
				analysis.getStartScope().addParent(parentScope);
				Set<ConfigCompileException> exceptions = new HashSet<>();
				analysis.analyzeFinalScopeGraph(env, exceptions);

				// Handle compile exceptions.
				if(exceptions.size() == 1) {
					ConfigCompileException ex = exceptions.iterator().next();
					String fileName = (ex.getFile() == null ? "Unknown Source" : ex.getFile().getName());
					throw new CREIncludeException(
							"There was a compile error when trying to include the script at " + file
							+ "\n" + ex.getMessage() + " :: " + fileName + ":" + ex.getLineNum(), t);
				} else if(exceptions.size() > 1) {
					StringBuilder b = new StringBuilder();
					b.append("There were compile errors when trying to include the script at ")
							.append(file).append("\n");
					for(ConfigCompileException ex : exceptions) {
						String fileName = (ex.getFile() == null ? "Unknown Source" : ex.getFile().getName());
						b.append(ex.getMessage()).append(" :: ").append(fileName).append(":")
								.append(ex.getLineNum()).append("\n");
					}
					throw new CREIncludeException(b.toString(), t);
				}
			}

			if(include != null) {
				// It could be an empty file
				StackTraceManager stManager = env.getEnv(GlobalEnv.class).GetStackTraceManager();
				stManager.addStackTraceElement(
						new ConfigRuntimeException.StackTraceElement("<<include " + arg.val() + ">>", t));
				try {
					parent.eval(include.getChildAt(0), env);
				} catch (AbstractCREException e) {
					e.freezeStackTraceElements(stManager);
					throw e;
				} catch (StackOverflowError e) {
					throw new CREStackOverflowError(null, t, e);
				} finally {
					stManager.popStackTraceElement();
				}
			}
			return CVoid.VOID;
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast,
				Environment env, Set<ConfigCompileException> exceptions) {

			// Store references for static includes and a static analysis for dynamic includes.
			if(ast.numberOfChildren() == 1) {
				Mixed includePathNode = ast.getChildAt(0).getData();
				if(includePathNode instanceof CString) {

					// Create a new unlinked scope to leave a gap for the include scopes.
					// Create a reference with these unlinked scopes to be able to perform linkage at a later stage.
					Scope outScope = analysis.createNewScope();
					parentScope.addReference(new IncludeReference(
							includePathNode.val(), parentScope, outScope, ast.getTarget()));
					return outScope;
				} else {

					// The include is dynamic, so it cannot be checked in compile time.
					// Store the parent scope for static analysis to check the file as soon as it is loaded in runtime.
					env.getEnv(StaticRuntimeEnv.class).getIncludeCache().getDynamicAnalysisParentScopeCache()
							.put(ast.getTarget(), parentScope);
					return super.linkScope(analysis, parentScope, ast, env, exceptions);
				}
			}

			// Fall back to default behavior for invalid syntax.
			return super.linkScope(analysis, parentScope, ast, env, exceptions);
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.OPTIMIZE_DYNAMIC
			);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.isEmpty()) {
				throw new ConfigCompileException("include() expects 1 argument.", t);
			}
			//We can't optimize per se, but if the path is constant, and the code is uncompilable, we
			//can give a warning, and go ahead and cache the tree.
			if(children.get(0).isConst()) {
				String path = children.get(0).getData().val();
				File file = Static.GetFileFromArgument(path, env, t, null);
				if(!file.exists()) {
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
							new CompilerWarning("File doesn't exist, this will be an error at runtime.",
									children.get(0).getTarget(), FileOptions.SuppressWarning.IncludedFileNotFound));
				}
				try {
					if(!Static.InCmdLine(env, true) && !Security.CheckSecurity(file)) {
						throw new ConfigCompileException("Included file is inaccessible due to the base-dir setting",
								children.get(0).getTarget());
					}
				} catch (IOException ex) {
					// Just ignore it. This is not something we can deal with anyways, and if it's still a problem
					// at runtime, it will be reported through existing means.
				}
				// Some users have dynamic inclusion solutions, because for larger codebases, compilation is a non
				// trival amount of time, and currently this happens on the main thread. Once compilation happens on
				// a background thread, (or at least recompiles on a background thread) this code can be revisted, and
				// re-added if needed. Having said that, a code ecosystem that determines inter-script dependencies
				// would likely obsolete the need for this anyways.
//				try {
//					IncludeCache.get(file, env, t);
//				} catch (CREIOException ex) {
//					// This is thrown if a file doesn't exist. When it actually runs, this is definitely an error,
//					// for now we just want it to be a warning.
//					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
//							new CompilerWarning(ex.getMessage(), children.get(0).getTarget(), null));
//				}
			}
			return null;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.ERROR;
		}

		@Override
		public String profileMessageS(List<ParseTree> args) {
			String m = "Executing function: include(";
			if(args.get(0).isConst()) {
				m += args.get(0).getData().val();
			} else {
				m += "<dynamic input>";
			}
			return m + ")";
		}

	}

	@api
	@DocumentLink(0)
	public static class include_dir extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			// This function is rewritten to include() calls in compile time, so this doesn't exist in runtime.
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return "include_dir";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "nothing {directory, [recursive]} Works like include, but takes a directory, and includes all"
					+ " files within the directory. Recursive defaults to false, but if true, recurses down into"
					+ " all subdirectories as well. As an implementation note, this function is fully resolved"
					+ " at compile time, thus the inputs must be hardcoded. The directories are scanned at compile"
					+ " time, and replaced with individual includes for each .ms file found.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public ParseTree postParseRewrite(ParseTree ast, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, Set<ConfigCompileException> exceptions) {
			Target t = ast.getTarget();

			// Check arguments size.
			if(ast.numberOfChildren() < 1 || ast.numberOfChildren() > 2) {
				exceptions.add(new ConfigCompileException(
						"Incorrect number of arguments passed to " + this.getName(), t));
				return null;
			}

			// Require and get hard-coded arguments.
			Mixed dirNode = ast.getChildAt(0).getData();
			boolean recurse = false;
			boolean exception = false;
			if(!(dirNode instanceof CString)) {
				exceptions.add(new ConfigCompileException(
						"Directory argument passed to " + this.getName() + " must be a hard-coded string.", t));
				exception = true;
			}
			if(ast.numberOfChildren() >= 2) {
				Mixed recurseNode = ast.getChildAt(1).getData();
				if(!(recurseNode instanceof CBoolean)) {
					exceptions.add(new ConfigCompileException(
							"Recurse argument passed to " + this.getName() + " must be a hard-coded boolean.", t));
					exception = true;
				} else {
					recurse = ((CBoolean) recurseNode).getBoolean();
				}
			}
			if(exception) {
				return null;
			}

			// Require directory to resolve to an actual directory.
			String dir = dirNode.val();
			File file = Static.GetFileFromArgument(dir, env, t, null);
			if(!file.isDirectory()) {
				exceptions.add(new ConfigCompileException(
						"Directory path passed to " + this.getName() + " must be a directory which exists.", t));
				return null;
			}

			// Collect all .ms files that should be included.
			List<File> msFiles = new ArrayList<>();
			if(recurse) {
				try {
					FileUtil.recursiveFind(file, ((f) -> {
						if(f.isFile() && f.getAbsolutePath().endsWith(".ms")) {
							msFiles.add(f);
						}
					}));
				} catch (IOException ex) {
					exceptions.add(new ConfigCompileException(ex.getMessage(), t, ex));
					exception = true;
				}
			} else {
				for(File f : file.listFiles()) {
					if(f.isFile() && f.getAbsolutePath().endsWith(".ms")) {
						msFiles.add(f);
					}
				}
			}
			if(exception) {
				return null;
			}

			// Convert this function to g(include(...), include(...), ...).
			ParseTree gNode = new ParseTree(new CFunction(g.NAME, t), ast.getFileOptions());
			for(File f : msFiles) {
				ParseTree includeNode = new ParseTree(new CFunction(include.NAME, t), ast.getFileOptions());
				includeNode.addChild(new ParseTree(new CString(f.getAbsolutePath(), t), ast.getFileOptions()));
				gNode.addChild(includeNode);
			}
			return gNode;
		}
	}

	@api
	public static class is_proc extends AbstractFunction {

		@Override
		public String getName() {
			return "is_proc";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {procName} Returns whether or not the given procName is currently defined, i.e. if calling this proc wouldn't"
					+ " throw an exception.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_2_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) {
			return CBoolean.get(env.getEnv(GlobalEnv.class).GetProcs().get(args[0].val()) != null);
		}
	}

	@api
	public static class is_associative extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "is_associative";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {array} Returns whether or not the array is associative. If the parameter is not an array, throws a CastException.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CArray.TYPE)) {
				return CBoolean.get(((CArray) args[0]).inAssociativeMode());
			} else {
				throw new CRECastException(this.getName() + " expects argument 1 to be an array", t);
			}
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_associative(array(one: 1, two: 2))"),
				new ExampleScript("False condition", "is_associative(array(1, 2, 3))")};
		}
	}

	@api
	public static class is_closure extends AbstractFunction {

		@Override
		public String getName() {
			return "is_closure";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {arg} Returns true if the argument is a closure (could be executed)"
					+ " or false otherwise";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(args[0].isInstanceOf(CClosure.TYPE));
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("True condition", "is_closure(closure(msg('code')))"),
				new ExampleScript("False condition", "is_closure('a string')")};
		}
	}

	@api
	@seealso({_export.class})
	public static class _import extends AbstractFunction {

		@Override
		public String getName() {
			return "import";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "mixed {key, [default]} This function imports a value from the global value register. It looks for a"
					+ " value stored with the specified key (using the export function), and returns that value."
					+ " If specified key doesn't exist, it will return either null or the default value if specified."
					+ " An array may be used as a key. It is converted into a string with the array values separated by"
					+ " dots. import() is threadsafe.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREIndexOverflowException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String key;
			if(args[0].isInstanceOf(CString.TYPE)) {
				key = args[0].val();
			} else if(args[0].isInstanceOf(CArray.TYPE)) {
				if(((CArray) args[0]).isAssociative()) {
					throw new CREIllegalArgumentException("Associative arrays may not be used as keys in " + getName(), t);
				}
				key = GetNamespace((CArray) args[0], t);
			} else {
				throw new CREIllegalArgumentException("Argument 1 in " + this.getName() + " must be a string or array.", t);
			}
			Mixed c = Globals.GetGlobalConstruct(key);
			if(args.length == 2 && c instanceof CNull) {
				c = args[1];
			}
			return c;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new _export().examples();
		}
	}

	@api
	@seealso({_import.class})
	public static class _export extends AbstractFunction {

		@Override
		public String getName() {
			return "export";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "void {key, value} Stores a value in the global storage register."
					+ " An arbitrary value is stored with the given key, and can be retrieved using import."
					+ " If the value is already stored, it is overwritten. See {{function|import}}."
					+ " The reference to the value is stored, not a copy of the value, so in the case of"
					+ " arrays, manipulating the contents of the array will manipulate the stored value. An array may"
					+ " be used as a key. It is converted into a string with the array values separated by dots."
					+ " export() is threadsafe.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class, CREIndexOverflowException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String key;
			if(args[0].isInstanceOf(CString.TYPE)) {
				key = args[0].val();
			} else if(args[0].isInstanceOf(CArray.TYPE)) {
				if(((CArray) args[0]).isAssociative()) {
					throw new CREIllegalArgumentException("Associative arrays may not be used as keys in " + getName(), t);
				}
				key = GetNamespace((CArray) args[0], t);
			} else {
				throw new CREIllegalArgumentException("Argument 1 in " + this.getName() + " must be a string or array.", t);
			}
			Mixed c = args[1];
			Globals.SetGlobal(key, c);
			return CVoid.VOID;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@var = 2;\n"
				+ "export('custom.name', @var);\n"
				+ "@var2 = import('custom.name');\n"
				+ "msg(@var2);"),
				new ExampleScript("Storage of references", "@array = array(1, 2, 3);\n"
				+ "export('array', @array);\n"
				+ "@array[0] = 4;\n"
				+ "@array2 = import('array');\n"
				+ "msg(@array2);"),
				new ExampleScript("Array key usage", "@key = array('custom', 'name');\n"
				+ "export(@key, 'value');\n"
				+ "@value = import(@key);\n"
				+ "msg(@value);"),
				new ExampleScript("Default value usage", "export('custom.name', null);\n"
				+ "@value = import('custom.name', 'default value');\n"
				+ "msg(@value);")
			};
		}
	}

	@api
	@unbreakable
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class})
	public static class closure extends AbstractFunction implements BranchStatement, VariableScope {

		@Override
		public String getName() {
			return "closure";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "closure {[params...], code} Returns a closure on the provided code. A closure is"
					+ " a datatype that represents some code as code, not the results of some"
					+ " code after it is run. Code placed in a closure can be used as"
					+ " a string, or executed by other functions using the execute() function."
					+ " If a closure is \"to string'd\" it will not necessarily look like"
					+ " the original code, but will be functionally equivalent. The current environment"
					+ " is \"snapshotted\" and stored with the closure, however, this information is"
					+ " only stored in memory, it isn't retained during a serialization operation."
					+ " Also, the special variable @arguments is automatically created for you, and contains"
					+ " an array of all the arguments passed to the closure, much like procedures."
					+ " See the wiki article on [[Closures|closures]] for more details"
					+ " and examples.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {

			// Use first child as closure return type if it is a type.
			CClassType returnType = Auto.TYPE;
			int nodeOffset = 0;
			if(nodes.length > 0 && nodes[0].getData().isInstanceOf(CClassType.TYPE)) {
				returnType = (CClassType) nodes[0].getData();
				nodeOffset = 1;
			}

			// Return an empty (possibly statically typed) closure when it is empty and does not have any parameters.
			if(nodes.length - nodeOffset == 0) {
				return new CClosure(null, env, returnType, new String[0], new Mixed[0], new CClassType[0], t);
			}

			// Clone the environment to prevent parameter and variable assigns overwriting variables in the outer scope.
			Environment myEnv;
			try {
				myEnv = env.clone();
			} catch (CloneNotSupportedException ex) {
				throw new RuntimeException(ex);
			}

			// Get closure parameter names, default values and types.
			int numParams = nodes.length - nodeOffset - 1;
			String[] names = new String[numParams];
			Mixed[] defaults = new Mixed[numParams];
			CClassType[] types = new CClassType[numParams];
			for(int i = 0; i < numParams; i++) {
				ParseTree node = nodes[i + nodeOffset];
				ParseTree newNode = new ParseTree(new CFunction(g.NAME, t), node.getFileOptions());
				List<ParseTree> children = new ArrayList<>();
				children.add(node);
				newNode.setChildren(children);
				Script fakeScript = Script.GenerateScript(newNode, myEnv.getEnv(GlobalEnv.class).GetLabel(), null);
				myEnv.getEnv(GlobalEnv.class).SetFlag("closure-warn-overwrite", true);
				myEnv.getEnv(GlobalEnv.class).SetFlag("var-args-allowed", true);
				Mixed ret = MethodScriptCompiler.execute(newNode, myEnv, null, fakeScript);
				myEnv.getEnv(GlobalEnv.class).ClearFlag("var-args-allowed");
				myEnv.getEnv(GlobalEnv.class).ClearFlag("closure-warn-overwrite");
				if(!(ret instanceof IVariable)) {
					throw new CRECastException("Arguments sent to " + getName() + " barring the last) must be ivariables", t);
				}
				names[i] = ((IVariable) ret).getVariableName();
				try {
					defaults[i] = ((IVariable) ret).ival().clone();
					types[i] = ((IVariable) ret).getDefinedType();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(DataHandling.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			// Create and return the closure, using the last argument as the closure body.
			return new CClosure(nodes[nodes.length - 1], myEnv, returnType, names, defaults, types, t);
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast,
				Environment env, Set<ConfigCompileException> exceptions) {
			return this.linkScope(analysis, parentScope, ast, env, exceptions, true);
		}

		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast,
				Environment env, Set<ConfigCompileException> exceptions, boolean codeInheritsParentScope) {

			// Handle empty closure.
			if(ast.numberOfChildren() == 0) {
				return parentScope;
			}

			// Handle optional return type argument (CClassType or CVoid, default to AUTO).
			int ind = 0;
			CClassType retType;
			if(ast.getChildAt(ind).getData() instanceof CClassType) {
				retType = (CClassType) ast.getChildAt(ind++).getData();
			} else if(ast.getChildAt(ind).getData().equals(CVoid.VOID)) {
				ind++;
				retType = CVoid.TYPE;
			} else {
				retType = CClassType.AUTO;
			}

			// Create parameter scope. Set parent scope if this closure type is allowed to resolve in the parent scope.
			// Procedures can always look up in the parent scope.
			Scope paramScope = analysis.createNewScope();
			if(codeInheritsParentScope) {
				paramScope.addParent(parentScope);
			} else {
				paramScope.addSpecificParent(parentScope, Namespace.PROCEDURE);
			}

			// Insert @arguments parameter.
			paramScope.addDeclaration(new ParamDeclaration("@arguments", CArray.TYPE, null, ast.getNodeModifiers(),
					ast.getTarget()));

			// Handle closure parameters from left to right.
			Scope valScope = parentScope;
			List<ParamDeclaration> params = new ArrayList<>();
			while(ind < ast.numberOfChildren() - 1) {
				ParseTree param = ast.getChildAt(ind++);
				Scope[] scopes = analysis.linkParamScope(paramScope, valScope, param, env, exceptions, params);
				valScope = scopes[1];
				paramScope = scopes[0];
			}

			// Create returnable declaration in the inner root scope.
			paramScope.addDeclaration(new ReturnableDeclaration(retType, ast.getNodeModifiers(), ast.getTarget()));

			// Create breakable and continuable bound declarations to prevent resolving to parent scope.
			Scope codeParentScope = analysis.createNewScope(paramScope);
			codeParentScope.addDeclaration(new BreakableBoundDeclaration(ast.getNodeModifiers(), ast.getTarget()));
			codeParentScope.addDeclaration(new ContinuableBoundDeclaration(ast.getNodeModifiers(), ast.getTarget()));

			// Handle closure code.
			ParseTree code = ast.getChildAt(ast.numberOfChildren() - 1);
			analysis.linkScope(codeParentScope, code, env, exceptions);

			// Return the parent scope, as parameters and their default values are not accessible after the closure.
			return parentScope;
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Creates a closure", "closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "};"),
				new ExampleScript("Executes a closure", "execute(closure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});")
			};
		}

		@Override
		public List<Boolean> isBranch(List<ParseTree> children) {
			List<Boolean> ret = new ArrayList<>(children.size());
			if(children.isEmpty()) {
				// This is the "closure()" usage, and does not have any branches.
				return ret;
			}
			for(int i = 0; i < children.size() - 1; i++) {
				ret.add(false);
			}
			ret.add(true);
			return ret;
		}

		@Override
		public List<Boolean> isScope(List<ParseTree> children) {
			return isBranch(children);
		}

	}

	@api
	@unbreakable
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class})
	public static class iclosure extends closure {

		@Override
		public String getName() {
			return "iclosure";
		}

		@Override
		public String docs() {
			return "iclosure {[params...], code} Returns a scope isolated closure on the provided code. An iclosure is"
					+ " a datatype that represents some code as code, not the results of some"
					+ " code after it is run. Code placed in an iclosure can be used as"
					+ " a string, or executed by other functions using the execute() function."
					+ " If a closure is \"to string'd\" it will not necessarily look like"
					+ " the original code, but will be functionally equivalent. The current environment"
					+ " is \"snapshotted\" and stored with the closure, however, this information is"
					+ " only stored in memory, it isn't retained during a serialization operation. However,"
					+ " the variable table of the parent scope is not retained, thus making this closure \"isolated\""
					+ " from the parent code."
					+ " The special variable @arguments is automatically created for you, and contains"
					+ " an array of all the arguments passed to the closure, much like procedures."
					+ " See the wiki article on [[Closures|closures]] for more details"
					+ " and examples.";
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(nodes.length == 0) {
				//Empty closure, do nothing.
				return new CIClosure(null, env, Auto.TYPE, new String[]{}, new Mixed[]{}, new CClassType[]{}, t);
			}
			// Handle the closure type first thing
			CClassType returnType = Auto.TYPE;
			if(nodes[0].getData().isInstanceOf(CClassType.TYPE)) {
				returnType = (CClassType) nodes[0].getData();
				ParseTree[] newNodes = new ParseTree[nodes.length - 1];
				for(int i = 1; i < nodes.length; i++) {
					newNodes[i - 1] = nodes[i];
				}
				nodes = newNodes;
			}
			String[] names = new String[nodes.length - 1];
			Mixed[] defaults = new Mixed[nodes.length - 1];
			CClassType[] types = new CClassType[nodes.length - 1];
			// We clone the enviornment at this point, because we don't want the values
			// that are assigned here to overwrite values in the main scope.
			Environment myEnv;
			try {
				myEnv = env.clone();
			} catch (CloneNotSupportedException ex) {
				throw new RuntimeException(ex);
			}
			for(int i = 0; i < nodes.length - 1; i++) {
				ParseTree node = nodes[i];
				ParseTree newNode = new ParseTree(new CFunction(g.NAME, t), node.getFileOptions());
				List<ParseTree> children = new ArrayList<>();
				children.add(node);
				newNode.setChildren(children);
				Script fakeScript = Script.GenerateScript(newNode, myEnv.getEnv(GlobalEnv.class).GetLabel(), null);
				myEnv.getEnv(GlobalEnv.class).SetFlag("closure-warn-overwrite", true);
				myEnv.getEnv(GlobalEnv.class).SetFlag("var-args-allowed", true);
				Mixed ret = MethodScriptCompiler.execute(newNode, myEnv, null, fakeScript);
				myEnv.getEnv(GlobalEnv.class).ClearFlag("var-args-allowed");
				myEnv.getEnv(GlobalEnv.class).ClearFlag("closure-warn-overwrite");
				if(!(ret instanceof IVariable)) {
					throw new CRECastException("Arguments sent to " + getName() + " barring the last) must be ivariables", t);
				}
				names[i] = ((IVariable) ret).getVariableName();
				try {
					defaults[i] = ((IVariable) ret).ival().clone();
					types[i] = ((IVariable) ret).getDefinedType();
				} catch (CloneNotSupportedException ex) {
					Logger.getLogger(DataHandling.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			// Now that iclosure is done with the current variable list, it can be removed from the cloned environment.
			// This ensures it's not unintentionally retaining values in memory cloned from the original scope.
			myEnv.getEnv(GlobalEnv.class).SetVarList(null);
			return new CIClosure(nodes[nodes.length - 1], myEnv, returnType, names, defaults, types, t);
		}

		@Override
		public Scope linkScope(StaticAnalysis analysis, Scope parentScope, ParseTree ast,
				Environment env, Set<ConfigCompileException> exceptions) {
			return this.linkScope(analysis, parentScope, ast, env, exceptions, false);
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Creates an iclosure", "iclosure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "};"),
				new ExampleScript("Executes an iclosure", "execute(iclosure(){\n"
				+ "\tmsg('Hello World!');\n"
				+ "});"),
				new ExampleScript("Shows scoping", "@a = \'variable\';\n"
				+ "msg('Outside of iclosure: '.reflect_pull('varlist'));\n"
				+ "// Note that this is an iclosure\n"
				+ "execute('val1', iclosure(@b){\n"
				+ "\tmsg('Inside of iclosure: '.reflect_pull('varlist'));\n"
				+ "});\n"
				+ "// Note that this is a regular closure\n"
				+ "execute('val2', closure(@c){\n"
				+ "\tmsg('Insider of closure: '.reflect_pull('varlist'));\n"
				+ "});")
			};
		}

	}

	@api
	@hide("Until the Federation system is finished, this is hidden")
	@unbreakable
	@nolinking
	public static class rclosure extends closure {

		@Override
		public String getName() {
			return "rclosure";
		}

		@Override
		public String docs() {
			return "closure {[params...], code} Returns a non-linking closure on the provided code. The same rules apply"
					+ " for closures, except the top level internal code does not check for proper linking at compile time,"
					+ " and instead links at runtime. Lexer errors and some other compile time checks ARE done however, but"
					+ " functions are not optimized or linked. This is used for remote code execution, since the remote platform"
					+ " may have some functionality unavailable on this current platform.";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class, execute_array.class, executeas.class})
	public static class execute extends AbstractFunction {

		public static final String NAME = "execute";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {[values...], callable} Executes the given closure or other Callable. You can also"
					+ " send arguments"
					+ " to the Callable, which it may or may not use, depending on the particular Callable's"
					+ " definition. If the Callable returns a value, then that value will"
					+ " be returned with execute. Otherwise, void is returned. Note that Callables are in"
					+ " general first class language features, which can be invoked with parenthesis, for"
					+ " instance <code>Callable @c = ...; @c();</code> will execute @c, and is the preferred"
					+ " syntax.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[args.length - 1] instanceof Callable callable) {
				Mixed[] vals = new Mixed[args.length - 1];
				System.arraycopy(args, 0, vals, 0, args.length - 1);
				return callable.executeCallable(environment, t, vals);
			} else {
				throw new CRECastException("Only a Callable (created for instance from the closure function) can be"
						+ " sent to execute(), or executed directly, such as @c().", t);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public FunctionSignatures getSignatures() {
			return new SignatureBuilder(Auto.TYPE)
					.varParam(Auto.TYPE, "parameters", "The parameters to be sent to the Callable.")
					.param(Callable.TYPE, "callable", "The executable Callable object")
					.build();
		}


	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class, execute.class})
	public static class execute_array extends AbstractFunction {
		@Override
		public String getName() {
			return "execute_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {valueArray, closure} Executes the given closure, expanding the value"
					+ " array as individual arguments to the closure. If there are no arguments to be"
					+ " sent to the closure, an empty array can be sent."
					+ " If the closure returns a value with return(), then that value will"
					+ " be returned with execute. Otherwise, void is returned.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Mixed[] vals = ArgumentValidation.getArray(args[0], t).asList().toArray(new Mixed[0]);
			CClosure closure = ArgumentValidation.getObject(args[1], t, CClosure.class);
			return closure.executeCallable(vals);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_4;
		}
	}

	@api
	@seealso({com.laytonsmith.tools.docgen.templates.Closures.class})
	public static class executeas extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "executeas";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {sender, label, [values...], closure} Executes the given closure in the context of a given"
					+ " player or " + Static.getConsoleName() + ". A closure that runs player(), for instance,"
					+ " would return the specified player's name."
					+ " If null is given, it will execute with the current sender context instead of the closure's."
					+ " The label argument sets the permission label that this closure will use. If null is given,"
					+ " the closure's label will be used, like with execute().";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(!(args[args.length - 1].isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("Only a closure (created from the closure function) can be sent to executeas()", t);
			}
			Mixed[] vals = new Mixed[args.length - 3];
			System.arraycopy(args, 2, vals, 0, args.length - 3);
			CClosure closure = (CClosure) args[args.length - 1];
			CommandHelperEnvironment cEnv = closure.getEnv().getEnv(CommandHelperEnvironment.class);
			GlobalEnv gEnv = closure.getEnv().getEnv(GlobalEnv.class);

			MCCommandSender originalSender = cEnv.GetCommandSender();
			MCCommandSender sender;
			if(args[0] instanceof CNull) {
				sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			} else {
				sender = Static.GetCommandSender(args[0].val(), t);
			}
			cEnv.SetCommandSender(sender);

			String originalLabel = gEnv.GetLabel();
			if(!(args[1] instanceof CNull)) {
				gEnv.SetLabel(args[1].val());
			}

			try {
				return closure.executeCallable(vals);
			} finally {
				cEnv.SetCommandSender(originalSender);
				gEnv.SetLabel(originalLabel);
			}
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 3) {
				throw new ConfigCompileException(getName() + " must have 3 or more arguments", t);
			}
			return null;
		}
	}

	@api
	public static class _boolean extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "boolean";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "boolean {item} Returns a new construct that has been cast to a boolean. The item is cast according to"
					+ " the boolean conversion rules. Since all data types can be cast to a"
					+ " a boolean, this function will never throw an exception.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return CBoolean.get(ArgumentValidation.getBoolean(args[0], t));
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "boolean(1)"),
				new ExampleScript("Basic usage", "boolean(0)"),
				new ExampleScript("Basic usage", "boolean(array(1))"),
				new ExampleScript("Basic usage", "boolean(array())"),
				new ExampleScript("Basic usage", "boolean(null)"),
				new ExampleScript("Basic usage", "boolean('string')"),
				new ExampleScript("Basic usage", "boolean('')")};
		}
	}

	@api
	public static class _integer extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "integer";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "integer {item} Returns a new construct that has been cast to an integer."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly. Data"
					+ " may be lost in this conversion. For instance, 4.5 will be converted"
					+ " to 4, by using integer truncation. You can use is_integral to see"
					+ " if this data loss would occur.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Mixed arg = args[0];
			if(arg instanceof CMutablePrimitive) {
				arg = ((CMutablePrimitive) arg).get();
			}
			if(arg instanceof CInt) {
				return arg;
			}
			long i;
			if(arg instanceof CDouble) {
				i = (long) ((CDouble) arg).getDouble();
			} else if(arg instanceof CString) {
				try {
					if(arg.val().indexOf('.') > -1) {
						i = (long) Double.parseDouble(arg.val());
					} else {
						i = Long.parseLong(arg.val());
					}
				} catch (NumberFormatException e) {
					throw new CRECastException("Expecting a number, but received \"" + arg.val() + "\" instead", t);
				}
			} else if(arg instanceof CBoolean) {
				if(((CBoolean) arg).getBoolean()) {
					i = 1;
				} else {
					i = 0;
				}
			} else if(arg instanceof CNull) {
				i = 0;
			} else {
				throw new CRECastException("Expecting a number, but received type " + arg.getName() + " instead", t);
			}
			return new CInt(i, t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "integer(1.0)"),
				new ExampleScript("Basic usage", "integer(1.5)"),
				new ExampleScript("Failure", "assign(@var, 'string')\ninteger(@var)")};
		}
	}

	@api
	public static class _double extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "double";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "double {item} Returns a new construct that has been cast to an double."
					+ " This function will throw a CastException if is_numeric would return"
					+ " false for this item, but otherwise, it will be cast properly.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			return new CDouble(ArgumentValidation.getDouble(args[0], t), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "double(1)"),
				new ExampleScript("Failure", "@var = 'string';\ndouble(@var);")};
		}
	}

	@api
	public static class _string extends AbstractFunction implements Optimizable {

		public static final String NAME = "string";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {item} Creates a new construct that is the \"toString\" of an item."
					+ " For arrays, an human readable version is returned; this should not be"
					+ " used directly, as the format is not guaranteed to remain consistent. Booleans return \"true\""
					+ " or \"false\" and null returns \"null\". Strings (and subclasses of strings) are simply returned"
					+ " as is.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CString.TYPE)) {
				return args[0];
			}
			return new CString(args[0].val(), t);
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(
					OptimizationOption.CONSTANT_OFFLINE,
					OptimizationOption.CACHE_RETURN
			);
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "string(1)"),
				new ExampleScript("Basic usage", "string(true)"),
				new ExampleScript("Basic usage", "string(false)"),
				new ExampleScript("Basic usage", "string(null)"),
				new ExampleScript("Basic usage", "string(array(1, 2))"),
				new ExampleScript("Basic usage", "string(array(one: 'one', two: 'two'))")};
		}
	}

	@api
	@seealso(parse_int.class)
	public static class to_radix extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			int radix = ArgumentValidation.getInt32(args[1], t);
			if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
				throw new CRERangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			return new CString(Long.toString(ArgumentValidation.getInt(args[0], t), radix), t);
		}

		@Override
		public String getName() {
			return "to_radix";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "string {value, radix} Given an int and a radix, returns a string representation of the integer value"
					+ " in the given base. A common use would be to output a hex or binary representation of a number, for"
					+ " instance. ---- It is useful to note that all integers are stored internally by the computer as binary,"
					+ " but since we usually represent numbers in text as base 10 numbers, we often times forget that both"
					+ " base 16 'F' and base 10 '15' and base 2 '1111' are actually the same number, just represented differently"
					+ " as strings in different bases. This doesn't change how the program behaves, since the base is just a way to represent"
					+ " the number on paper. The 'radix' is the base. So, given to_radix(10, 10), that would return '10', because"
					+ " in code, we wrote out our value '10' in base 10, and we convert it to base 10, so nothing changes. However,"
					+ " if we write to_radix(15, 16) we are saying \"convert the base 10 value 15 to base 16\", so it returns 'F'."
					+ " See {{function|parse_int}} for the opposite operation. The radix must be between " + Character.MIN_RADIX + " and "
					+ Character.MAX_RADIX + ", inclusive, or a range exception is thrown. This is because there are only " + Character.MAX_RADIX
					+ " characters that are normally used to represent different base numbers (that is, 0-9, a-z). The minimum radix is "
					+ Character.MIN_RADIX + ", because it is impossible to represent any numbers with out at least a binary base.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("To a hex string", "to_radix(15, 16)"),
				new ExampleScript("To a binary string", "to_radix(15, 2)"),
				new ExampleScript("Using hex value in source", "to_radix(0xff, 16)"),
				new ExampleScript("Using binary value in source", "to_radix(0b10101010, 2)")
			};
		}

	}

	@api
	@seealso(to_radix.class)
	public static class parse_int extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRERangeException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String value = args[0].val();
			int radix = ArgumentValidation.getInt32(args[1], t);
			if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
				throw new CRERangeException("The radix must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.", t);
			}
			long ret;
			try {
				ret = Long.parseLong(value, radix);
			} catch (NumberFormatException ex) {
				throw new CREFormatException("The input string: \"" + value + "\" is improperly formatted. (Perhaps you're using a character greater than"
						+ " the radix specified?)", t);
			}
			return new CInt(ret, t);
		}

		@Override
		public String getName() {
			return "parse_int";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "int {value, radix} Converts a string representation of an integer to a real integer, given the value's"
					+ " radix (base). See {{function|to_radix}} for a more detailed explanation of number theory. Radix must be"
					+ " between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX + ", inclusive.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("From hex string", "parse_int('F', 16)"),
				new ExampleScript("From binary string", "parse_int('1111', 2)")
			};
		}
	}

	/**
	 * Generates the namespace for this value, given an array.
	 *
	 * @param array
	 * @return
	 */
	private static String GetNamespace(CArray array, Target t) {
		boolean first = true;
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < array.size(); i++) {
			if(!first) {
				b.append(".");
			}
			first = false;
			b.append(array.get(i, t).val());
		}
		return b.toString();
	}

	@api
	public static class typeof extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				return args[0].typeof();
			} catch (IllegalArgumentException ex) {
				throw new Error("Class " + args[0].getClass().getName() + " is not annotated with @typeof. Please report this"
						+ " error to the developers.");
			}
		}

		@Override
		public String getName() {
			return "typeof";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "ClassType {arg} Returns a ClassType value of the typeof a value. For instance 'array' is returned"
					+ " for typeof(array()). This is a generic replacement for the is_* series of functions.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage, typeof string", "typeof('value')"),
				new ExampleScript("Basic usage, typeof int", "typeof(1)"),
				new ExampleScript("Basic usage, typeof double", "typeof(1.0)"),
				new ExampleScript("Basic usage, typeof closure", "typeof(closure(){ msg('test') })")};
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.CONSTANT_OFFLINE);
		}

	}

	@api
	public static class eval extends AbstractFunction implements Optimizable {

		@Override
		public String getName() {
			return "eval";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "string {script_string} Executes arbitrary MethodScript. Note that this function is very experimental, and is subject to changing or "
					+ "removal. To globally disable use of eval, set the runtime setting \"function.eval.disable\" to"
					+ " true, which will cause use of the function to throw an exception.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREInsufficientPermissionException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_0;
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			if(ArgumentValidation.getBooleanish(env.getEnv(GlobalEnv.class).GetRuntimeSetting("function.eval.disable",
					CBoolean.FALSE), t)) {
				throw new CREInsufficientPermissionException("eval is disabled", t);
			}
			boolean oldDynamicScriptMode = env.getEnv(GlobalEnv.class).GetDynamicScriptingMode();
			ParseTree node = nodes[0];
			try {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(true);
				Mixed script = parent.seval(node, env);
				if(script.isInstanceOf(CClosure.TYPE)) {
					throw new CRECastException("Closures cannot be eval'd directly. Use execute() instead.", t);
				}
				ParseTree root = MethodScriptCompiler.compile(MethodScriptCompiler.lex(script.val(), env, t.file(), true),
						env, env.getEnvClasses());
				if(root == null) {
					return new CString("", t);
				}

				// Unwrap single value in __statements__() and return its string value.
				if(root.getChildren().size() == 1 && root.getChildAt(0).getData() instanceof CFunction
						&& ((CFunction) root.getChildAt(0).getData()).getFunction().getName().equals(__statements__.NAME)
						&& root.getChildAt(0).getChildren().size() == 1) {
					return new CString(parent.seval(root.getChildAt(0).getChildAt(0), env).val(), t);
				}

				// Concat string values of all children and return the result.
				StringBuilder b = new StringBuilder();
				int count = 0;
				for(ParseTree child : root.getChildren()) {
					Mixed s = parent.seval(child, env);
					if(!s.val().trim().isEmpty()) {
						if(count > 0) {
							b.append(" ");
						}
						b.append(s.val());
					}
					count++;
				}
				return new CString(b.toString(), t);
			} catch (ConfigCompileException e) {
				throw new CREFormatException("Could not compile eval'd code: " + e.getMessage(), t);
			} catch (ConfigCompileGroupException ex) {
				StringBuilder b = new StringBuilder();
				b.append("Could not compile eval'd code: ");
				for(ConfigCompileException e : ex.getList()) {
					b.append(e.getMessage()).append("\n");
				}
				throw new CREFormatException(b.toString(), t);
			} finally {
				env.getEnv(GlobalEnv.class).SetDynamicScriptingMode(oldDynamicScriptMode);
			}
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}
		//Doesn't matter, run out of state anyways

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Set<Optimizable.OptimizationOption> optimizationOptions() {
			return EnumSet.of(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1) {
				throw new ConfigCompileException(getName() + " expects only one argument", t);
			}
			if(children.get(0).isConst()) {
				String msg = "Eval'd code is hardcoded, consider simply using the code directly, as wrapping"
						+ " hardcoded code in " + getName() + " is much less efficient.";
				env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
						new CompilerWarning(msg, t, FileOptions.SuppressWarning.HardcodedDynamicParameter));
			}
			return null;
		}

	}

	@api
	@noprofile
	@hide("This will eventually be replaced by ; statements.")
	public static class g extends AbstractFunction {

		public static final String NAME = "g";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			return CVoid.VOID;
		}

		@Override
		public String docs() {
			return "string {func1, [func2...]} Groups any number of functions together, and returns void. ";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_1;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}
	}

	/**
	 * For now, this feature works as is. However, I'm debating on whether or not I should just override assign()
	 * instead. The only issue with this is that if assign is overwritten, then a mutable_primitive will be "stuck" in
	 * the variable. So if later you wanted to make a value not a mutable primitive, there would be no way to do so.
	 * Another method could be introduced to "clear" the value out, but then there would be no way to tell if the value
	 * were actually mutable or not, so a third function would have to be added. The other point of concern is how to
	 * handle typeof() for a CMutablePrimitive. Should it return the underlying type, or mutable_primitive? If
	 * assignments are "sticky", then it would make sense to have it return the underlying type, but there's an issue
	 * with that, because then typeof wouldn't be useable for debug type situations. Given all these potential issues,
	 * it is still hidden, but available for experimental cases.
	 */
	@api
	@hide("This is still experimental")
	public static class mutable_primitive extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Mixed val = CNull.NULL;
			if(args.length > 0) {
				val = args[0];
			}
			return new CMutablePrimitive(val, t);
		}

		@Override
		public String getName() {
			return "mutable_primitive";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		@Override
		public String docs() {
			return "mutable_primitive {[primitive_value]} Creates a mutable primitive object, initially setting the value of the object to"
					+ " null, or the specified value. The value must be a primitive value, and cannot be an array or object. ----"
					+ " The underlying primitive value is used in all cases where a value can be inferred. In all other cases, you must convert"
					+ " the primitive to the desired type, e.g. double(@mutable_primitive). Mutable primitives work like an array as well,"
					+ " in some cases, but not others. In general, setting of the underlying values may be done with array_push(). Assigning"
					+ " a new value to the variable works the same as assigning a new value to any other value, it overwrites the value with"
					+ " the new type. Most array functions will work with the mutable primitive, however, they will return useless data, for"
					+ " instance, array_resize() will simply set the value to the default value shown. array_size() is an exception to this"
					+ " rule, it will not work, and will throw an exception. See the examples for more use cases. In general, this is meant"
					+ " as a convenience feature for values that are passed to closures or procs, but should be passed by reference. Cloning the"
					+ " mutable primitive with the array clone operation creates a distinct copy.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "@val = mutable_primitive(0);\n"
				+ "msg('typeof: ' . typeof(@val));\n"
				+ "msg('value: ' . @val);\n"
				+ "msg('@val + 5: ' . (@val + 5)); // Works as if it were a primitive with most functions\n"
				+ "(++@val); // As a special exception to how assignments work, increment/decrement works as well\n"
				+ "msg(@val); // 1\n"),
				new ExampleScript("Basic usage with procs", "proc _testWithMutable(@a){\n"
				+ "\t@a[] = 5;\n"
				+ "}\n\n"
				+ ""
				+ "proc _testWithoutMutable(@a){\n"
				+ "\t@a = 10;\n"
				+ "}\n\n"
				+ ""
				+ "@a = mutable_primitive(0);\n"
				+ "msg(@a); // The value starts out as 0\n"
				+ "_testWithMutable(@a); // This will actually change the value\n"
				+ "msg(@a); // Here, the value is 5\n"
				+ "_testWithoutMutable(@a); // This will not change the value\n"
				+ "msg(@a); // Still the value is 5\n"),
				new ExampleScript("Basic usage with closure", "@a = mutable_primitive(0);\n"
				+ "execute(closure(){\n"
				+ "\t@a++;\n"
				+ "});\n"
				+ "msg(@a); // 1\n"),
				new ExampleScript("Cloning the value", "@a = mutable_primitive(0);\n"
				+ "@b = @a[];\n"
				+ "@a[] = 5;\n"
				+ "msg(@a);\n"
				+ "msg(@b);\n")
			};
		}

	}

	@api
	public static class _instanceof extends AbstractFunction implements Optimizable {

		public static final String NAME = "instanceof";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0] instanceof CNull) {
				return CBoolean.FALSE;
			}
			CClassType type;
			if(args[1].isInstanceOf(CClassType.TYPE)) {
				type = (CClassType) args[1];
			} else {
				throw new RuntimeException("This should have been optimized out, this is a bug in instanceof,"
						+ " please report it");
			}
			boolean b = InstanceofUtil.isInstanceof(args[0], type, environment);
			return CBoolean.get(b);
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "boolean {value, type} Checks to see if the value is, extends, or implements the given type. Keyword usage is preferred:"
					+ " <code>@value instanceof int</code>. The opposite operation is <code>@value notinstanceof int</code>. ---- Null is a special value, while any type may be assigned null, it does not extend"
					+ " any type, and therefore \"null instanceof AnyType\" will always return false. Likewise, other than null, all"
					+ " values extend \"mixed\", and therefore \"anyNonNullValue instanceof mixed\" will always return true. There is no"
					+ " (single) functional equivalent to the notinstanceof keyword. <code>@value notinstanceof int</code> simply compiles to not(instanceof(@value, int)).";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs, List<ParseTree> children,
				FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			// There are two specific cases here where we will give more precise error messages.
			// If it's a string, yell at them (Note CKeyword extends CString, for better or worse)
			if(children.get(1).getData().isInstanceOf(CString.TYPE)) {
				throw new ConfigCompileException("Unexpected string type passed to \"instanceof\"", t);
			}
			// If it's a variable, also yell at them
			if(children.get(1).getData() instanceof IVariable) {
				throw new ConfigCompileException("Variable types are not allowed in \"instanceof\"", t);
			}
			// Unknown error, but this is still never valid.
			if(!(children.get(1).getData().isInstanceOf(CClassType.TYPE))) {
				throw new ConfigCompileException("Unexpected type for \"instanceof\": " + children.get(1).getData(), t);
			}
			// null is technically a type, but instanceof shouldn't work with that
			if(children.get(1).getData().val().equals("null")) {
				throw new ConfigCompileException("\"null\" cannot be compared against with instanceof. Use <value> === null.", t);
			}
			// It's hardcoded, allow it, but optimize it out.
			if(children.get(0).isConst()) {
				return new ParseTree(exec(t, null, children.get(0).getData(), children.get(1).getData()), fileOptions);
			}
			return null;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Basic usage", "mixed @a = 5; // Actually an int\n"
				+ "msg(@a instanceof int); // true\n"
				+ "msg(@a instanceof string); // false\n"),
				new ExampleScript("Functional usage", "instanceof(5, int)"),
				new ExampleScript("Inverted usage", "mixed @a = 5;\n"
				+ "msg(@a notinstanceof int); // false\n"
				+ "msg(@a notinstanceof string); // true\n"),
				new ExampleScript("Inverted functional usage", "!instanceof(5, int)")
			};
		}

	}

//	@api
	public static class free extends AbstractFunction implements Optimizable {

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			IVariableList list = env.getEnv(GlobalEnv.class).GetVarList();
			Mixed value = list.get(((IVariable) args[0]).getVariableName(), t, env).ival();
			list.freeValue(value);
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "free";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public boolean preResolveVariables() {
			return false;
		}

		@Override
		public String docs() {
			return "void {ivar} Frees the memory of the underlying value.";
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env, Set<Class<? extends EnvironmentImpl>> envs, List<ParseTree> children, FileOptions fileOptions) throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() != 1 || !(children.get(0).getData() instanceof IVariable)) {
				throw new ConfigCompileException(getName() + " can only accept an IVariable as the argument.", t);
			}
			return null;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}
	}

	@api
	@seealso(array.class)
	public static class fixed_array extends AbstractFunction {

		@Override
		public Version since() {
			return MSVersion.V3_3_5;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRERangeException.class, CRECastException.class};
		}

		@Override
		public boolean isRestricted() {
			return false;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			CClassType type = ArgumentValidation.getClassType(args[0], t);
			int size = ArgumentValidation.getInt32(args[1], t);
			if(size < 0) {
				throw new CRERangeException("Array size must be zero or greater. Received: " + size, t);
			}
			// nullOut is intentionally ignored here, as it's irrelevant in the case of the interpreter
			return new CFixedArray(t, type, size);
		}

		@Override
		public String getName() {
			return "fixed_array";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		@Override
		public String docs() {
			return "fixed_array {ClassType type, int size, [boolean nullOut]} Creates an array that can hold values of"
					+ " the given type, and is of the given size."
					+ " The array cannot be resized or retyped later."
					+ " The array cannot be associative. In general, this isn't"
					+ " meant for normal use, and unless you have specific need for a fixed_size array, array() should"
					+ " be used instead. This is instead meant for writing low level system code. On the other hand,"
					+ " for performance sensitive needs, this may be used instead, though note that most of the API"
					+ " does not accept fixed_arrays (though it does implement ArrayAccess, and so can be used in most"
					+ " read only array based functions), nor will it in the future. This does however map to the"
					+ " underlying system array closely, and so can in particular be used to integrate more directly"
					+ " with the system. fixed_array isn't a particularly flexible type, but it isn't meant to be,"
					+ " it's meant to more directly map to the lower level part of the system. nullOut defaults to"
					+ " true, and in the interpreter is irrelevant, but for native code, if set to true, this loops"
					+ " through the array and sets each value to null (or equivalent for primitive types). This"
					+ " takes additional work, but sets the value to a known state. This can be bypassed if the array"
					+ " is about to be filled from 0 to length, but should otherwise always be set to true."
					+ " A RangeException is thrown if size is negative, or larger than a 32 bits signed integer."
					+ " A CastException is thrown when size cannot be cast to an int.";
		}
	}

}
