package com.laytonsmith.core.compiler.keywords;

import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.compiler.Keyword;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.natives.interfaces.MAnnotation;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import java.util.List;
import java.util.Set;

/**
 * An ObjectDefinitionKeyword is a keyword which defines an object type, for instance "class" or
 * "interface". The particulars of the contents of the class may vary, and it is up to the subclass
 * to parse the contents correctly.
 */
public abstract class ObjectDefinitionKeyword extends Keyword {

	@Override
	public int process(List<ParseTree> list, int keywordPosition) throws ConfigCompileException {
		// There are a couple of rules for all object definition keywords, particularly that the
		// object definition name must come exactly after, the optional modifiers may come before (in any
		// order), optionally followed by implements or extends, and after those, a comma separated list
		// of 1 or more classes. Finally, an open brace, any internals, and a closing brace. The internals
		// are left completely up to the subclass to manage, but the above pieces are handled by this
		// class.
		// Do a lookbehind until we reach 0, or we find something other than an access modifier or
		// object modifier.

		// Now we have the keyword itself (i.e. "class") which we can ignore.
		// After that, we have the object name. However, note that this should be the
		// fully qualified class name, which in most cases should have multiple parts, i.e. "ms.lang.string"
		// This would be 5 different nodes. So we start with a bare string, then can be followed by one of:
		// 1. a concat operator, 2. the implements keyword, 3. the extends keyword, 4. the __cbrace__ function.
		// All other nodes are an error case. After we determine which case, we follow a different code path.
		String className = list.get(keywordPosition + 1).getData().val();

		// Concat operator
			// Append the ., look for another bare string, start loop over
			// Anything other than a bare string after the . is a compiler error

		// Implements keyword
			// Loop through finding the class name

		return keywordPosition;
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
