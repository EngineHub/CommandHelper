package com.laytonsmith.core.arguments;

import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 * @author lsmith
 */
public class Argument implements Documentation {
	private final String docs;
	private final Class clazz;
	private final String name;
	private boolean optional;
	private boolean varargs;
	private CHVersion since;
	private Mixed def;
	
	public Argument(String docs, Class clazz, String name){
		this.docs = docs;
		this.clazz = clazz;
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
		return setVarargs(true);
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
	 * this, for chaining.
	 * @param varargs
	 * @return 
	 */
	public Argument setVarargs(boolean varargs){
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
		if (this.clazz != other.clazz && (this.clazz == null || !this.clazz.equals(other.clazz))) {
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
		hash = 67 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
		hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 67 * hash + (this.optional ? 1 : 0);
		hash = 67 * hash + (this.varargs ? 1 : 0);
		return hash;
	}

	/**
	 * Returns the underlying class type of this argument.
	 * @return 
	 */
	public Class getType() {
		return clazz;
	}

	@Override
	public String toString() {
		return (optional?"[":"") + clazz.getSimpleName() + " " + name + (optional?"]":"");
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
}
