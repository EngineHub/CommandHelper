package com.laytonsmith.core.arguments;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An argument builder returns a signature matcher, which allows for the
 * compiler and runtime to more easily parse arguments. There are two
 * steps in using an ArgumentBuilder. The first step is creation, and the
 * second step is usage.
 * 
 * <p>Creation:</p>
 * <p>To create an ArgumentBuilder, use the static Build methods. A signature
 * consists of some number of arguments, and each of those arguments can have
 * some options. See the methods in {@link Argument} for more details on that.</p>
 * <p>Usage:</p>
 * <p>Pass the arguments to the parse method, and it will parse the arguments for you,
 * into an ArgList, from which you can pull them based on argument name.</p>
 *
 * @author lsmith
 */
public class ArgumentBuilder {

	private final List<Signature> signatures;
	private final String originalSignature;

	private ArgumentBuilder(List<Signature> signatures, String originalSignature) {
		this.signatures = signatures;
		this.originalSignature = originalSignature;
	}

	public int signatureCount() {
		return signatures.size();
	}
	
	/**
	 * Convenience method, if you are building the signature inline.
	 */
	public static ArgumentBuilder Build(Argument ... args){
		Signature s = new Signature(args);
		return Build(s);
	}

	public static ArgumentBuilder Build(Signature signature) {
		//Expand optional Arguments, so that no arguments that get put in this list have optional
		//arguments. This simplifies processing down the line.
		return new ArgumentBuilder(permutations(signature), signature.toString());
	}

	private static List<Signature> permutations(Signature s) {
		List<Signature> list = new LinkedList<Signature>();
		//Count the permutations needed
		int optionalCount = 0;
		for (Argument a : s.getArguments()) {
			if (a.isOptional()) {
				optionalCount++;
			}
		}
		// Say we have 3 optional arguments, in a signature that has 4 arguments
		// [S], L, [S], [S]
		// We have 8 possible permutations here.
		// 0 0 0 - L
		// 0 0 1 - L S
		// 0 1 0 - L S (Duplicate)
		// 0 1 1 - L S S
		// 1 0 0 - S L
		// 1 0 1 - S L S
		// 1 1 0 - S L S (Duplicate)
		// 1 1 1 - S L S S
		// Note however, that a few of these are duplicates, so in reality, we only have
		// 6 permutations. The duplicates are invalid though.
		// For instance, if our signature is
		// [CString a], [CString b], if only one string is provided,
		// it is assigned to a, not b. However, this has two concrete permutations, which
		// have the same argument count:
		// CString a AND CString b. Only the first one is valid though. So long as we
		// parse in the right order, this will settle itself, but we do have to be sure
		// to do it correctly. We do this by iterating in the order we do, so that the bit
		// shifts happen in the appropriate order.
		outer: for (int i = 0; i < Math.pow(2, optionalCount); i++) {
			List<Argument> args = new ArrayList<Argument>();
			List<Argument> optionals = new ArrayList<Argument>();
			int optIndex = 0;
			for (int j = 0; j < s.getArguments().size(); j++) {
				if (s.getArguments().get(j).isOptional()) {
					if (((int) Math.pow(2, optIndex) & i) > 0) {
						//Included in this permutation
						args.add(s.getArguments().get(j));
					} else {
						//Otherwise, we still need to add it to the optionals list
						optionals.add(s.getArguments().get(j));
					}
					optIndex++;
				} else {
					//Not optional, just pop in
					args.add(s.getArguments().get(j));
				}
			}
			Signature toAdd = new Signature(args.toArray(new Argument[args.size()]));
			toAdd.addOptionals(optionals);
			//Check to see if it's already added. If this EXACT one is added, that's fine,
			//we can just skip it, otherwise we need to see if it's "fuzzy" equals, we
			//need to throw an error.
			if(!list.contains(toAdd)){
				for(Signature ss : list){
					if(toAdd.equals(ss)){
						//Skip this one
						continue outer;
					}
				}
				list.add(toAdd);
			}
		}
		return new ArrayList<Signature>(list);
	}
	
	public ArgList parse(Construct [] args, Target t){
		signature: for(Signature s : signatures){
			if(s.getArguments().size() == args.length){
				for(int i = 0; i < args.length; i++){
					Class<? extends Mixed> c1 = args[i].getClass();
					Class<? extends Mixed> c2 = s.getArguments().get(i).getType();
					if(!c2.isAssignableFrom(c1)){
						//Not a match, moving on
						continue signature;
					}
				}
				//If we made it this far, it's a match, so carry on from here
				Map<String, Mixed> ret = new HashMap<String, Mixed>();
				//Fill in the optionals
				for(Argument a : s.getDefaults()){
					ret.put(a.getName(), a.getDefault());
				}
				//Now fill in the actual values
				for(int i = 0; i < args.length; i++){
					ret.put(s.getArguments().get(i).getName(), args[i]);
				}
				return new ArgList(ret);
			}
		}
		//No matches were found. Throw an exception.
		List<String> sent = new ArrayList<String>();
		for(Construct arg : args){
			sent.add(arg.typeName());
		}
		throw new ConfigRuntimeException("The arguments provided: " + StringUtils.Join(sent, ", ")
				 + " did not match the function signature: " + originalSignature + ". (Check your arguments, and try again)", 
				Exceptions.ExceptionType.CastException, t);
	}

	@Override
	public String toString() {
		return StringUtils.Join(signatures, "\n");
	}
	
	public static class ErasureError extends Error{

		public ErasureError(String message) {
			super(message);
		}
		
	}
}
