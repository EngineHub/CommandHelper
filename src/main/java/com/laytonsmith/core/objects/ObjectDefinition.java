package com.laytonsmith.core.objects;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.natives.interfaces.MAnnotation;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains the definition of an object. Within certain limits, this information is available to the runtime, and in
 * limited cases, can even be modified at runtime. For the large part, however, the data in an ObjectDefinition is
 * read only.
 *
 * Everything is an object at the core, but there are subtypes of Object that have special handling, such as enums or
 * annotations, but those are non-the-less represented in this class.
 */
public class ObjectDefinition {
	private final AccessModifier accessModifier;
	private final Set<ObjectModifier> objectModifiers;
	private final ObjectType objectType;
	private final CClassType name;
	private final CClassType[] superclasses;
	private final CClassType[] interfaces;
	private final CClassType containingClass;
	private final Target definitionTarget;
	private final Map<String, ElementDefinition> properties;
	private final List<MAnnotation> annotations;

	public ObjectDefinition(AccessModifier accessModifier, Set<ObjectModifier> objectModifiers, ObjectType objectType,
			CClassType name,
			CClassType[] superclasses, CClassType[] interfaces, CClassType containingClass, Target t,
			Map<String, ElementDefinition> properties, List<MAnnotation> annotations) {
		this.accessModifier = accessModifier;
		this.objectModifiers = objectModifiers;
		this.objectType = objectType;
		this.name = name;
		this.superclasses = superclasses;
		this.interfaces = interfaces;
		this.containingClass = containingClass;
		this.definitionTarget = t;
		this.properties = properties;
		this.annotations = annotations;
	}

	@SuppressWarnings("LocalVariableHidesMemberVariable")
	public ObjectDefinition(Class<? extends Mixed> clazz) {
		Mixed m = NativeTypeList.getNativeInvalidInstanceForUse(clazz);
		AccessModifier accessModifier = m.getAccessModifier();
		Set<ObjectModifier> objectModifiers = m.getObjectModifiers();
		ObjectType objectType = m.getObjectType();
		CClassType name = m.typeof();
		CClassType[] superclasses = m.getSuperclasses();
		CClassType[] interfaces = m.getInterfaces();
		CClassType containingClass = m.getContainingClass();
		Target t = new Target(0, new File("/Natives:/" + name + ".ms"), 0);
		List<MAnnotation> annotations = null;//m.getAnnotations();
		this.accessModifier = accessModifier;
		this.objectModifiers = objectModifiers;
		this.objectType = objectType;
		this.name = name;
		this.superclasses = superclasses;
		this.interfaces = interfaces;
		this.containingClass = containingClass;
		this.definitionTarget = t;
		// TODO: define this
		this.properties = new HashMap<>();
		this.annotations = annotations;
	}

	public String getClassName() {
		return this.name.getName();
	}
}
