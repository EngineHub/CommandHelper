package com.laytonsmith.core.arguments;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A signature is a single arrangement of arguments. Multiple
 * (non colliding) signatures may exist in one argument builder.
 * 
 * For the purposes of comparison, two signatures are considered as if
 * ALL arguments are non-optional, and will only return equals, or not
 * equals.
 * @author lsmith
 */
public class Signature implements Comparable<Signature> {
	private List<Argument> args;
	private boolean containsOptional = false;
	private List<Argument> optionals = null;
	
	public Signature(Argument ... args){
		this.args = Arrays.asList(args);
		for(int i = 0; i < args.length; i++){
			Argument a = args[i];
			if(a.isOptional()){
				containsOptional = true;
				break;
			}
			if(i != args.length - 1 && args[i].isVarargs()){
				throw new Error("Only the last argument in a signature may be varargs");
			}
		}
	}
	
	/*package*/ void addOptionals(List<Argument> optionals){
		this.optionals = optionals;
	}

	public boolean containsOptional() {
		return containsOptional;
	}
	
	public List<Argument> getArguments(){
		return new ArrayList<Argument>(args);
	}
	
	/**
	 * Returns the optionals list.
	 * @return 
	 */
	/*package*/ List<Argument> getDefaults(){
		if(optionals == null){
			return new ArrayList<Argument>();
		} else {
			return new ArrayList<Argument>(optionals);
		}
	}


	@Override
	public int hashCode() {
		int hash = 3;
		hash = 37 * hash + (this.args != null ? this.args.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Signature other = (Signature) obj;
		if(other.args.size() != this.args.size()){
			return false;
		}
		for(int i = 0; i < other.args.size(); i++){
			Argument thiz = this.args.get(i);
			Argument that = other.args.get(i);
			//Don't care about the names, just the types.
			if(thiz.getType() != that.getType()){
				return false;
			}
		}
		return true;
	}
	
	public boolean equalsErasure(Signature other){
		if(other.args.size() != this.args.size()){
			return false;
		}
		for(int i = 0; i < other.args.size(); i++){
			Argument thiz = this.args.get(i);
			Argument that = other.args.get(i);
			//Check the erasure on these, not just if they are directly equal
			if(!thiz.getType().isAssignableFrom(that.getType()) || !that.getType().isAssignableFrom(thiz.getType())){
				return false;
			}
		}
		return true;
	}

	public int compareTo(Signature o) {
		if(this.equals(o)){
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public String toString() {
		return StringUtils.Join(args, ", ");
	}
	
}
