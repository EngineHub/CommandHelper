package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.PureUtilities.SmartComment;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.EarlyBindingKeyword;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.natives.interfaces.MAnnotation;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An ObjectDefinitionKeyword is a keyword which defines an object type, for instance "class" or
 * "interface". The particulars of the contents of the class may vary, and it is up to the subclass
 * to parse the contents correctly.
 */
public abstract class ObjectDefinitionKeyword extends EarlyBindingKeyword {

	@Override
	public int process(TokenStream stream, Environment env, int keywordPosition) throws ConfigCompileException {
		// There are a couple of rules for all object definition keywords, particularly that the
		// object definition name must come exactly after, the optional modifiers may come before (in any
		// order), optionally followed by implements or extends, and after those, a comma separated list
		// of 1 or more classes. Finally, an open brace, any internals, and a closing brace. The internals
		// are left completely up to the subclass to manage, but the above pieces are handled by this
		// class.
		// Do a lookbehind until we reach 0, or we find something other than an access modifier or
		// object modifier.
		int startRemovalFrom = keywordPosition;
		AccessModifier accessModifier = null;
		Set<ObjectModifier> objectModifiers = new HashSet<>();
		SmartComment smartComment = null;
		for(int i = keywordPosition; i >= 0; i--) {
			Token keyword = stream.get(i);
			if(keyword.type == Token.TType.COMMENT) {
				continue;
			}
			if(keyword.type == Token.TType.SMART_COMMENT && smartComment == null) {
				smartComment = new SmartComment(keyword.value);
			}
			if(keyword.type == Token.TType.KEYWORD) {
				AccessModifier possibleAccessModifier = AccessModifier.valueOf(keyword.value.toUpperCase());
				if(possibleAccessModifier != null) {
					if(accessModifier != null) {
						throw new ConfigCompileException("Unexpected access modifier", keyword.target);
					}
					accessModifier = possibleAccessModifier;
				}

				ObjectModifier possibleObjectModifier = ObjectModifier.valueOf(keyword.value.toUpperCase());
				if(possibleObjectModifier != null) {
					if(objectModifiers.contains(possibleObjectModifier)) {
						throw new ConfigCompileException("Duplicated object modifier", keyword.target);
					}
					objectModifiers.add(possibleObjectModifier);
				}
			} else {
				break;
			}
		}
		// Now we have the keyword itself (i.e. "class") which we can ignore.
		// After that, we have the object name. However, note that this should be the
		// fully qualified class name, which in most cases should have multiple parts, i.e. "ms.lang.string"
		// This would be 5 different nodes. So we start with a bare string, then can be followed by one of:
		// 1. a concat operator, 2. the implements keyword, 3. the extends keyword, 4. the __cbrace__ function.
		// All other nodes are an error case. After we determine which case, we follow a different code path.
		StringBuilder className = new StringBuilder();
		int advance = getClassName(stream, keywordPosition + 1, className);
		List<ParseTree> implementsTypes = new ArrayList<>();
		// Concat operator
			// Append the ., look for another bare string, start loop over
			// Anything other than a bare string after the . is a compiler error

		{
			// Implements keyword
			boolean inImplementsKeyword = false;
			for(int i = advance; i < stream.size(); i++) {

			}
//			for(int i = keywordPosition + 2; i < list.size(); i++) {
//				ParseTree l = list.get(i);
//				Keyword k = null;
//				if(l.getData() instanceof CKeyword) {
//					k = ((CKeyword) l.getData()).getKeyword();
//				}
//				if(inImplementsKeyword) {
//					if(l.getData() instanceof CSymbol && ((CSymbol) l.getData()).convert().equals(",")) {
//						continue;
//					} else if(k != null) {
//						break;
//					} else if(CFunction.IsFunction(l.getData(),
//							com.laytonsmith.core.functions.Compiler.__cbrace__.class)) {
//						break;
//					} else if(l.getData() instanceof CBareString) {
//						implementsTypes.add(getClassName(list, i));
//					} else {
//						throw new ConfigCompileException("Unexpected value: " + l.getData(), l.getTarget());
//					}
//				} else {
//					if(k != null && k instanceof ImplementsKeyword) {
//						inImplementsKeyword = true;
//					}
//				}
//			}
		}
//		Target objectDeclaration = list.get(keywordPosition).getTarget();
//
//		ParseTree cbrace = null;
//		for(int i = startRemovalFrom; i < list.size(); i++) {
//			if(CFunction.IsFunction(list.get(i).getData(), com.laytonsmith.core.functions.Compiler.__cbrace__.class)) {
//				cbrace = list.get(i);
//				break;
//			}
//			i--;
//			list.remove(i);
//		}
//
//		if(cbrace == null) {
//			throw new ConfigCompileException("Invalid object declaration", objectDeclaration);
//		}

		// TODO: All the object parameters should be here now, need to further parse the cbrace for relevant data
		// (some of which needs to go to the subclass, for instance, for enums), and rewrite them to a define_object
		// functional format.
		return keywordPosition;
	}

	private static int getClassName(TokenStream list, int start, StringBuilder builder) throws ConfigCompileException {
		Token lastToken = null;
		for(int i = start; i < list.size(); i++) {
			Token t = list.get(i);
			lastToken = t;
			switch(t.type) {
				case STRING -> builder.append(t.toString());
				case CONCAT -> builder.append(t.toString());
				case LCURLY_BRACKET, KEYWORD -> {
					return i;
				}
			}
		}

		throw new ConfigCompileException("Unexpected end of class definition",
				lastToken == null ? Target.UNKNOWN : lastToken.target);
	}

	/**
	 * For most accounts, there is nothing special to do, other than to rewrite this into a
	 * define_object function.
	 * @param annotations
	 * @param accessModifier
	 * @param objectModifiers
	 * @param objectName
	 * @param superclasses
	 * @param interfaces
	 * @param contents
	 * @return
	 */
	protected List<ParseTree> processInternals(List<MAnnotation> annotations, AccessModifier accessModifier,
			Set<ObjectModifier> objectModifiers, CClassType objectName, CClassType[] superclasses,
			CClassType[] interfaces, ParseTree contents) {
		return null;
	}

	/**
	 * The object type that this keyword represents.
	 * @param modifiers Since the modifiers may tweak the type of class, these are sent along to assist in
	 * differentiating. (For instance, class vs abstract class).
	 * @return
	 */
	protected abstract ObjectType getObjectType(Set<ObjectModifier> modifiers);

	/**
	 * Some object definitions do not allow certain modifiers. Those should be specified here.
	 * This may be EnumSet.noneOf(ObjectModifier.class), but should not be null.
	 * @return
	 */
	protected abstract Set<ObjectModifier> illegalModifiers();

}
