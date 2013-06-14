package com.laytonsmith.core.arguments;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.NonScriptError;
import com.laytonsmith.core.natives.MEnum;
import com.laytonsmith.core.natives.annotations.MAnnotation;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Argument implements Documentation {
	/**
	 * The documentation for this argument.
	 */
	private final String docs;
	
	/**
	 * The types of this argument. If there are more than one classes in this list,
	 * it is a disjoint type.
	 */
	private final List<Class<? extends Mixed>> types = new ArrayList<Class<? extends Mixed>>();
	
	/**
	 * The name of this argument, though this is null in the case of a return type
	 */
	private final String name;
	
	/**
	 * True if this argument is optional
	 */
	private boolean optional;
	
	/**
	 * True if this argument is varargs
	 */
	private boolean varargs;
	
	/**
	 * By default, this is null, but if specified, it means the argument was added
	 * later than the containing function.
	 */
	private CHVersion since;
	
	/**
	 * If the argument has a default value, it is stored here.
	 */
	private Mixed def;
	
	/**
	 * The generics attached to this argument. It is a map, because each generic is attached to 
	 * the individual disjoint type.
	 */
	private Map<Class<? extends Mixed>, List<Generic>> generics = new HashMap<Class<? extends Mixed>, List<Generic>>();
	
	/**
	 * The annotations associated with this field.
	 */
	private List<MAnnotation> annotations = new ArrayList<MAnnotation>();
	
	/**
	 * Void return types don't need any documentation, so can
	 * all use the same Argument object. Note that VOID isn't a
	 * real type, despite the fact that CVoid is. This is because a function
	 * should never return void as part of a disjoint type, since scripts
	 * cannot set that as a valid type. Therefore, if a script returns
	 * ANYTHING other than void, it must NOT return void, and vice versa.
	 */
	public static final Argument VOID = new Argument("", CVoid.class){

		@Override
		public String toString() {
			return "<void>";
		}
	};
	
	/**
	 * Very few functions need this, but this is provided for functions that have
	 * no return type, because they have abnormal exit conditions. Usually,
	 * methods that return this have the {@link com.laytonsmith.core.compiler.Optimizable.OptimizationOption#TERMINAL}
	 * optimization.
	 */
	public static final Argument NONE = new Argument("", new Class[]{}){

		@Override
		public String toString() {
			return "<none>";
		}
		
	};
	
	/**
	 * This is the default value for a few core language features, but should
	 * not be used by general purpose functions.
	 */
	public static final Argument AUTO = new Argument("", new Class[]{}){

		@Override
		public String toString() {
			return "<auto>";
		}
		
	};
	
	/**
	 * Shorthand for creating a non-named argument, for use in
	 * return types only.
	 * @param docs
	 * @param clazz 
	 */
	public Argument(String docs, Class<? extends Mixed> clazz){
		this(docs, new Class[]{clazz}, null);
	}
	
	/**
	 * Shorthand for creating a non-named argument with exactly two
	 * disjoint types, for use in return types only.
	 * @param docs
	 * @param clazz1
	 * @param clazz2 
	 */
	public Argument(String docs, Class<? extends Mixed> clazz1, Class<? extends Mixed> clazz2){
		this(docs, new Class[]{clazz1, clazz2}, null);
	}
	
	/**
	 * Shorthand for creating a non-named argument with disjoint
	 * types, for use in return types only.
	 * @param docs
	 * @param classes 
	 */
	public Argument(String docs, Class<? extends Mixed>[] classes){
		this(docs, classes, null);
	}
	
	/**
	 * Creates a new Argument that represents a singly typed
	 * Argument.
	 * @param docs
	 * @param clazz
	 * @param name 
	 */
	public Argument(String docs, Class<? extends Mixed> clazz, String name){
		this(docs, new Class[]{clazz}, name);
	}
	
	
	/**
	 * Overload for easier syntax for a disjoint type with exactly two types.
	 * @param docs
	 * @param clazz1
	 * @param clazz2
	 * @param name 
	 */
	public Argument(String docs, Class<? extends Mixed> clazz1, Class<? extends Mixed> clazz2, String name){
		this(docs, new Class[]{clazz1, clazz2}, name);
	}
	
	/**
	 * Overload for easier syntax for a disjoint type with exactly three types.
	 * @param docs
	 * @param clazz1
	 * @param clazz2
	 * @param clazz3
	 * @param name 
	 */
	public Argument(String docs, Class<? extends Mixed> clazz1, Class<? extends Mixed> clazz2, Class<? extends Mixed> clazz3, String name){
		this(docs, new Class[]{clazz1, clazz2, clazz3}, name);
	}
	
	/**
	 * Create a new Argument that has disjoint types. For instance,
	 * if an argument can take either a string or an array, (denoted as
	 * string|array in mscript) then this constructor is appropriate to use.
	 * @param docs
	 * @param disjointTypes
	 * @param name 
	 */
	public Argument(String docs, Class<? extends Mixed>[] disjointTypes, String name){
		this.docs = docs;
		this.types.addAll(Arrays.asList(disjointTypes));
		this.name = name;
	}
	
	/**
	 * Sets this to optional, then returns it, for chaining.
	 * @return 
	 */
	public Argument setOptional(){
		return setOptional(true);
	}
	
	/**
	 * Sets the optional value as specified, then returns it, for chaining.
	 * @param optional
	 * @return 
	 */
	public Argument setOptional(boolean optional){
		this.optional = optional;
		return this;
	}

	/**
	 * Returns true if this argument is optional.
	 * @return 
	 */
	public boolean isOptional() {
		return optional;
	}
	
	/**
	 * Sets the varargs flag to true, then returns
	 * this, for chaining.
	 * @return 
	 */
	public Argument setVarargs(){
		setVarargs(true).setOptional(true);
		if(def == null){
			def = new CArray(Target.UNKNOWN);
		}
		return this;
	}
	
	/**
	 * Sets the generic parameters to this argument type. Multiple generic types
	 * can be added if this is an argument attached to a declaration. Using this version
	 * of the method assumes that there is only one argument type. If this is a disjoint
	 * argument, then this method may not be used, and you must specify which type this
	 * is being added for with the {@link #setGenerics(java.lang.Class, com.laytonsmith.core.arguments.Generic[])}
	 * version.
	 * @param generics
	 * @return 
	 */
	public Argument setGenerics(Generic...generics){
		if(types.size() > 1){
			throw new NonScriptError("For disjoint types, you must use the setGenerics(Class, Generic...) method instead of this one.");
		}
		Class<? extends Mixed> type = types.get(0);
		this.generics.put(type, Arrays.asList(generics));
		return this;
	}
	
	/**
	 * Sets the generic parameters to this argument type, for the disjoint type
	 * specified. If this is not a disjoint type, it is faster to use the
	 * {@link #setGenerics(com.laytonsmith.core.arguments.Generic[])} version of this
	 * method.
	 * @param forClass
	 * @param generics
	 * @return 
	 */
	public Argument setGenerics(Class<? extends Mixed> forClass, Generic...generics){
		
		return this;
	}
	
	/**
	 * Sets the default argument (which itself defaults to null),
	 * which is returned by {@link #getDefault(Class)}.
	 * @param def
	 * @return 
	 */
	public Argument setDefault(Mixed def){
		this.def = def;
		return this;
	}
	
	/**
	 * Sets the argument to optional, and sets the default, in one step.
	 * @param def
	 * @return 
	 */
	public Argument setOptionalDefault(Mixed def){
		return this.setOptional().setDefault(def);
	}
	
	/**
	 * Overload for POJO String.
	 * @param def
	 * @return 
	 */
	public Argument setOptionalDefault(String def){
		return this.setOptionalDefault(new CString(def, Target.UNKNOWN));
	}
	
	/**
	 * Overload for POJO long.
	 * @param def
	 * @return 
	 */
	public Argument setOptionalDefault(long def){
		return this.setOptionalDefault(new CInt(def, Target.UNKNOWN));
	}
	
	/**
	 * Overload for POJO double.
	 * @param def
	 * @return 
	 */
	public Argument setOptionalDefault(double def){
		return this.setOptionalDefault(new CDouble(def, Target.UNKNOWN));
	}
	
	/**
	 * Overload for POJO boolean.
	 * @param def
	 * @return 
	 */
	public Argument setOptionalDefault(boolean def){
		return this.setOptionalDefault(new CBoolean(def, Target.UNKNOWN));
	}
	
	/**
	 * Overload to set the default to a null value.
	 * @return 
	 */
	public Argument setOptionalDefaultNull(){
		return this.setOptionalDefault((Mixed)null);
	}
	
	/**
	 * Adds an argument annotation to this argument. TODO: This is only allowed if the
	 * annotation is allowed to be added to a field.
	 * @param annotation
	 * @return 
	 */
	public Argument addAnnotation(MAnnotation annotation){
		annotations.add(annotation);
		return this;
	}
	
	public <T extends Mixed> T getDefault(){
		try{
			return (T)def;
		} catch(ClassCastException e){
			//Actually an error
			throw new Error("ClassCastException thrown", e);
		}
	}
	
	/**
	 * Sets the varargs flag as specified, then return
	 * this, for chaining. If an argument is varargs, it is
	 * also optional, by definition, and defaults to an empty array.
	 * @param varargs
	 * @return 
	 */
	public Argument setVarargs(boolean varargs){
		if(types.size() != 1 || types.get(0) != CArray.class){
			throw new Error("Vararg status can only be set on an Argument that is a non-disjoint CArray type.");
		}
		this.varargs = varargs;
		return this;
	}
	
	/**
	 * Returns true if this is a vararg
	 * @return 
	 */
	public boolean isVarargs() {
		return varargs;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Argument other = (Argument) obj;
		if (this.types != other.types && (this.types == null || !this.types.equals(other.types))) {
			return false;
		}
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		if (this.optional != other.optional) {
			return false;
		}
		if (this.varargs != other.varargs) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + (this.types != null ? this.types.hashCode() : 0);
		hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 67 * hash + (this.optional ? 1 : 0);
		hash = 67 * hash + (this.varargs ? 1 : 0);
		return hash;
	}

	/**
	 * Returns the underlying class type(s) of this argument.
	 * @return 
	 */
	public List<Class<? extends Mixed>> getType() {
		return new ArrayList<Class<? extends Mixed>>(types);
	}

	@Override
	public String toString() {
		List<String> stypes = new ArrayList<String>();
		for(Class<? extends Mixed> c : types){
			String type = c.getSimpleName();
			if(c.getAnnotation(typename.class) != null){
				typename t = c.getAnnotation(typename.class);
				String tt = t.value();
				if(!"".equals(tt)){
					type = tt;
				}
				//Otherwise, it's dynamic, and we can't get that anyways right now, because
				//we don't have an instance.
			}
			String generic = "";
			if(generics.containsKey(c)){
				generic = "<" + StringUtils.Join(generics.get(c), ", ") + ">";
			}
			stypes.add(type + generic);
		}
		return (StringUtils.Join(annotations, " ") + " " + (optional?"[":"") + StringUtils.Join(stypes, "|") + (varargs?"...":"") + " " + name + (optional?"]":"")).trim();
	}	

	public String getName() {
		return name;
	}

	public String docs() {
		return docs;
	}
	
	/**
	 * Sets the since for this parameter. Normally, {@link #since()} returns
	 * null, which indicates that it should inherit that value from its parent
	 * function, but if this parameter was added later, it should be noted here.
	 * @param since
	 * @return this, for chaining.
	 */
	public Argument setSince(CHVersion since){
		this.since = since;
		return this;
	}

	public CHVersion since() {
		return since;
	}
	
	/**
	 * Returns the generics associated with this argument. If this is a disjoint
	 * type, you must use {@link #getGenerics(java.lang.Class)} instead.
	 * @return 
	 */
	public List<Generic> getGenerics(){
		if(types.size() == 1){
			return generics.get(types.get(0));
		} else {
			throw new NonScriptError("This method can only be used for non-disjoint types.");
		}
	}
	
	/**
	 * Returns the generics associated with the type of the specified disjoint
	 * type in this argument. If only one type is provided, then the {@link #getGenerics()}
	 * method may be used instead.
	 * @return 
	 */
	public List<Generic> getGenerics(Class<? extends Mixed> forClass){
		if(generics.containsKey(forClass)){
			return generics.get(forClass);
		} else {
			throw new NonScriptError("This argument does not have " + forClass + " as a valid type.");
		}
	}
}
