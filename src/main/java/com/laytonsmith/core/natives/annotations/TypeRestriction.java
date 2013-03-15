package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.natives.interfaces.Mixed;

/**
 *
 */
@typename("TypeRestriction")
public class TypeRestriction extends MAnnotation {
	public Class<? extends Mixed>[] value;

	public TypeRestriction(Class<? extends Mixed> ... value){
		this.value = value;
	}
	
	@Override
	public MAnnotation[] getMetaAnnotations() {
		return new MAnnotation[]{
			new TargetRestriction(ElementType.ANNOTATION_TYPE)
		};
	}
	
	public String docs() {
		return "If an annotation can only be tagged onto some type (or its subclasses), then this annotation can"
				+ " be used to provide that restriction. This is a meta annotation. It may only be tagged onto annotations"
				+ " that also have the @{TargetRestriction} annotation, and includes only the ASSIGNABLE value.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
}
