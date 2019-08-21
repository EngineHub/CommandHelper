package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.Callable;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.UnqualifiedClassName;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREClassDefinitionError;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.MAnnotation;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.DuplicateObjectDefintionException;
import com.laytonsmith.core.objects.ElementDefinition;
import com.laytonsmith.core.objects.ObjectDefinition;
import com.laytonsmith.core.objects.ObjectDefinitionNotFoundException;
import com.laytonsmith.core.objects.ObjectDefinitionTable;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.core.objects.UserObject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class ObjectManagement {
	public static String docs() {
		return "Provides functions for creating and using objects. None of these methods should normally be used,"
				+ " all of them provide easier to use compiler support.";
	}

	@api
	@hide("Not ready for consumption by mortals yet.")
	public static class dereference extends AbstractFunction {

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
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

		@Override
		public String getName() {
			return "dereference";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		@Override
		public String docs() {
			return "mixed {object, element} Dereferences a property on a value.";
		}

		@Override
		public Version since() {
			return MSVersion.V0_0_0;
		}

	}

	@api
	@hide("Not meant for normal use")
	public static class define_object extends AbstractFunction implements Optimizable {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREClassDefinitionError.class};
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
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new Error();
		}

		/**
		 * We can't use parent in the execs function, because we are expected to work at compile time. But
		 * that also means we can't use dynamic elements, like array. Though that is ok, so long as the
		 * array is hardcoded. But it also means we have to manually compile the array.
		 * @param data
		 * @param t
		 * @return
		 */
		private Mixed evaluateArray(ParseTree data, Target t) {
			if(data.getData() instanceof CNull) {
				return CNull.NULL;
			}
			CArray n = new CArray(t);
			if(!(data.getData() instanceof CFunction) || !data.getData().val().equals("array")) {
				throw new CREClassDefinitionError("Expected array, but found " + data.getData() + " instead", t);
			}
			for(ParseTree child : data.getChildren()) {
				if(child.isDynamic()) {
					throw new CREClassDefinitionError("Dynamic elements may not be used in a class definition", t);
				}
				n.push(child.getData(), t);
			}
			return n;
		}

		private CArray evaluateArrayNoNull(ParseTree data, Target t) {
			Mixed d = evaluateArray(data, t);
			if(d instanceof CNull) {
				throw new CREClassDefinitionError("Unexpected null value, expected an array", t);
			}
			return (CArray) d;
		}

		private Mixed evaluateString(ParseTree data, Target t) {
			if(data.getData() instanceof CNull) {
				return CNull.NULL;
			}
			if(!(data.getData().isInstanceOf(CString.TYPE))) {
				throw new CREClassDefinitionError("Expected a string, but found " + data.getData() + " instead", t);
			}
			return data.getData();
		}
		private CString evaluateStringNoNull(ParseTree data, Target t) {
			Mixed d = evaluateString(data, t);
			if(d instanceof CNull) {
				throw new CREClassDefinitionError("Expected a string, but found null instead", t);
			}
			return (CString) d;
		}

		private Mixed evaluateMixed(ParseTree data, Target t) {
			if(data.isDynamic()) {
				throw new CREClassDefinitionError("Expected a non-dynamic value, but " + data.getData()
						+ " was found.", t);
			}
			return data.getData();
		}

		@Override
		public Mixed execs(Target t, Environment env, Script parent, ParseTree... nodes) {
			// 0 - Access Modifier
			AccessModifier accessModifier = ArgumentValidation.getEnum(evaluateStringNoNull(nodes[0], t),
					AccessModifier.class, t);

			// 1 - Object Modifiers
			Set<ObjectModifier> objectModifiers = evaluateArrayNoNull(nodes[1], t).asList().stream()
					.map((item) -> ArgumentValidation.getEnum(item, ObjectModifier.class, t))
					.collect(Collectors.toSet());

			// 2 - Object Type
			ObjectType type = ArgumentValidation.getEnum(evaluateStringNoNull(nodes[2], t), ObjectType.class, t);

			// 3 - Object Name
			FullyQualifiedClassName name
					= FullyQualifiedClassName.forFullyQualifiedClass(evaluateStringNoNull(nodes[3], t).val());

			// 4 - Superclasses
			Set<UnqualifiedClassName> superclasses = new HashSet<>();
			{
				CArray su = evaluateArrayNoNull(nodes[4], t);
				if(!type.canUseExtends() && !su.isEmpty()) {
					throw new CREClassDefinitionError("An object definition of type " + type.name().toLowerCase()
							+ " may not extend"
							+ " another object type" + (type.canUseImplements() ? " (though it can implement"
							+ " other types)" : "") + ".", t);
				}
				for(Mixed m : su) {
					if(m instanceof CClassType) {
						superclasses.add(new UnqualifiedClassName(((CClassType) m).getFQCN()));
					} else {
						superclasses.add(new UnqualifiedClassName(m.val(), t));
					}
				}
			}

			if(type.extendsMixed() && superclasses.isEmpty()) {
				superclasses.add(Mixed.TYPE.getFQCN().asUCN());
			}

			// 5 - Interfaces
			Set<UnqualifiedClassName> interfaces = new HashSet<>();
			{
				CArray su = evaluateArrayNoNull(nodes[5], t);
				for(Mixed m : su) {
					if(m instanceof CClassType) {
						interfaces.add(new UnqualifiedClassName(((CClassType) m).getFQCN()));
					} else {
						interfaces.add(new UnqualifiedClassName(m.val(), t));
					}
				}

			}

			// 6- Enum list
			Mixed el = evaluateArray(nodes[6], t);
			if(type != ObjectType.ENUM && el != CNull.NULL) {
				throw new CREClassDefinitionError("Only enum types may define an enum list", t);
			} else if(type == ObjectType.ENUM && el == CNull.NULL) {
				throw new CREClassDefinitionError("Enum type was defined, but sent null as enum list. It may be an"
						+ " empty array, but cannot be null.", t);
			} else {
				// TODO
			}

			// 7 - map<property->element>
			Map<String, List<ElementDefinition>> elementDefinitions = new HashMap<>();
			// TODO

			// 8 - array<annotations>
			List<MAnnotation> annotations = new ArrayList<>();
			// TODO

			// 9 - containing class
			// The containing class must have been created first, so we can just jump to using CClassType already.
			CClassType containingClass;
			if(nodes[9].getData() instanceof CNull) {
				containingClass = null;
			} else {
				containingClass = ArgumentValidation.getClassType(evaluateMixed(nodes[9], t), t);
			}

			// 10 - Class Comment
			SmartComment classComment = null;

			Class<? extends Mixed> nativeClass = null;
			if(objectModifiers.contains(ObjectModifier.NATIVE)) {
				try {
					// It must exist in the native type list
					nativeClass = NativeTypeList.getNativeClass(name);
				} catch (ClassNotFoundException ex) {
					throw new CREClassDefinitionError(name + " was defined as a native class, but could not find"
						+ " the native class associated with it.", t);
				}
			}

			// 11 - Generic Parameter declarations
			// TODO This should of course not be Object, but I need
			// to create a new class first.
			List<Object> genericDeclarations = new ArrayList<>();

			// TODO Populate the native elements in the ElementDefinition

			// Native classes MUST define a constructor, they are not allowed to use the default
			// constructor. They *may* define a native constructor, but they must explicitly do
			// so. They also must directly call one of the native constructors in the non-native
			// constructors, so we know that the native class will be properly defined. If a native
			// class is unconstructable (i.e. static utility class) it must define a private constructor,
			// and take care never to call it internally.
			if(objectModifiers.contains(ObjectModifier.NATIVE)) {
				if(elementDefinitions.get("<constructor>").isEmpty()) {
					throw new CREClassDefinitionError(name + " was defined as a native class, but did not define"
							+ " any constructors. Native classes do not get a default constructor, and so must"
							+ " explicitely define at least one. (It may have no arguments and point to an"
							+ " @ExposedProperty constructor in the native code, however.) At least one"
							+ " native constructor must be defined, and called during construction.", t);
				}
				// TODO check if the non-native constructors actually call one of the native ones
			}

			ObjectDefinition def = new ObjectDefinition(
					accessModifier,
					objectModifiers,
					type,
					CClassType.defineClass(name),
					superclasses,
					interfaces,
					containingClass,
					t,
					elementDefinitions,
					annotations,
					classComment,
					genericDeclarations,
					nativeClass);
			if(env == null) {
				throw new Error("Environment may not be null");
			}
			ObjectDefinitionTable odt = env.getEnv(CompilerEnvironment.class).getObjectDefinitionTable();
			try {
				odt.add(def, t);
			} catch (DuplicateObjectDefintionException ex) {
				throw new CREClassDefinitionError("Class " + name + " already defined, cannot redefine!", t);
			}

			if(env.getEnv(GlobalEnv.class).GetCustom("define_object.noQualifyClasses") == null) {
				try {
					// During the course of initial compilation, we do not qualify classes, because the class library
					// does its initial pass, irregardless of what order the classes are defined, and then, after
					// all the class libraries are loaded, the loaded classes are looped through and qualified in bulk.
					// However, during the course of normal runtime, new classes are allowed to be defined, but they
					// are defined and qualified at the same time, so they are ready for immediate use. The bulk compilation
					// option is set only at first load, and then unset, so normal runtime will not have this flag set.
					def.qualifyClasses(env);
				} catch (ConfigCompileGroupException ex) {
					List<String> msgs = new ArrayList<>();
					for(ConfigCompileException e : ex.getList()) {
						msgs.add(e.getMessage() + " - " + e.getTarget());
					}
					throw new CREClassDefinitionError("One or more compile errors occured while trying to compile "
							+ def.getName() + ":\n" + StringUtils.Join(msgs, "\n"), t);
				}
			}

			return CVoid.VOID;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			// Do the same thing as execs, but remove this call
			execs(t, env, null, children.toArray(new ParseTree[children.size()]));
			return REMOVE_ME;
		}

		@Override
		public String getName() {
			return "define_object";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{12};
		}

		@Override
		public String docs() {
			return "void {AccessModifier accessModifier,"
					+ " array<ObjectModifier> objectModifiers,"
					+ " ObjectType objectType,"
					+ " string objectName,"
					+ " array<string> superclasses,"
					+ " array<string> interfaces,"
					+ " ? enumList,"
					+ " map<string, element> elementList,"
					+ " array<? extends annotation> annotations,"
					+ " ClassType containingClass,"
					+ " string classComment,"
					+ " array<?> genericParameters"
					+ "} Defines a new object. Not meant for normal use.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}

	@api
	@hide("Normally one should use the new keyword")
	public static class new_object extends AbstractFunction implements Optimizable {

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
		public boolean useSpecialExec() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			throw new Error();
		}


		// To speed up constructor identification, we modify the code at compile time to contain
		// a reference to the constructor in the second parameter. However, there are some
		// special codes we must support, those are defined here.
		private static final int DEFAULT = -1;
		private static final int UNDECIDEABLE = -2;

		@Override
		public Mixed execs(final Target t, final Environment env, Script parent, ParseTree... args)
				throws ConfigRuntimeException {
			ObjectDefinitionTable odt = env.getEnv(CompilerEnvironment.class).getObjectDefinitionTable();
			CClassType clazz = ((CClassType) args[0].getData());
			ObjectDefinition od;
			try {
				od = odt.get(clazz.getFQCN());
			} catch (ObjectDefinitionNotFoundException ex) {
				throw new CREClassDefinitionError(ex.getMessage(), t, ex);
			}
			int constructorId = (int) ((CInt) args[1].getData()).getInt();
			Callable constructor;
			switch(constructorId) {
				case DEFAULT:
					// Guaranteed not to be a native class
					constructor = null;
					break;
				case UNDECIDEABLE:
					for(ElementDefinition ed : od.getElements().get("<constructor>")) {
						// TODO
					}
					constructor = null; // TODO REMOVE ME
					break;
				default:
					// TODO
					constructor = null; // TODO REMOVE ME
					break;
			}
			// Construct the object!
			// This is the native construction.
			Mixed nativeObject = null;
			if(od.isNative()) {
				// TODO If this is a native object, we need to intercept the call to the native constructor,
				// and grab the object generated there.
			}
			Mixed obj = new UserObject(t, parent, env, od, null);
			// This is the MethodScript construction.
			if(constructor != null) {
				Mixed[] values = new Mixed[args.length - 1];
				values[0] = obj;
				for(int i = 2; i < args.length; i++) {
					values[i + 1] = parent.eval(args[i], env);
				}
				constructor.executeCallable(env, t, values);
			}
			return obj;
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			ObjectDefinitionTable odt = env.getEnv(CompilerEnvironment.class).getObjectDefinitionTable();
			// The first parameter must be hardcoded, and be a class in the compiler environment. The rest of the
			// parameters must match a constructor, but if most types are auto, we may only be able to count the
			// parameters, rather than compare types.
			if(children.get(0).isDynamic()) {
				throw new ConfigCompileException("The first parameter to new_object must be hardcoded.", t);
			}
			FullyQualifiedClassName fqcn = FullyQualifiedClassName.forName(children.get(0).getData().val(), t, env);
			ObjectDefinition od;
			try {
				od = odt.get(fqcn);
			} catch (ObjectDefinitionNotFoundException ex) {
				throw new ConfigCompileException("Could not find class with name " + fqcn + ". Are you missing"
						+ " a \"use\" statement?", t);
			}
			try {
				children.set(0, new ParseTree(CClassType.get(fqcn), fileOptions));
			} catch (ClassNotFoundException ex) {
				// This shouldn't happen, as we would have already thrown a CCE above if the class
				// really didn't exist.
				throw new Error(ex);
			}
			List<ElementDefinition> constructors = od.getElements().get("<constructor>");
			int id;
			if(constructors == null || constructors.isEmpty()) {
				// Default constructor
				if(children.size() > 1) {
					throw new ConfigCompileException("No suitable constructor found for " + fqcn + " only the default"
							+ " constructor is available.", t);
				}
				id = DEFAULT;
			} else {
				// Need to find the correct constructor from the list
				int parameterCount = children.size() - 1;
				for(ElementDefinition d : constructors) {
					// TODO
				}
				id = UNDECIDEABLE;
			}
			children.add(1, new ParseTree(new CInt(id, t), fileOptions));
			return null;
		}



		@Override
		public String getName() {
			return "new_object";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "<T> T {ClassType<T> type, params...} Constructs a new object of the specified type. The type must"
					+ " be hardcoded.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_4;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}
}
