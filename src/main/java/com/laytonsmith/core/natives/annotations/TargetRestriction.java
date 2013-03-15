package com.laytonsmith.core.natives.annotations;

import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;

/**
 *
 */
@typename("TargetRestriction")
public class TargetRestriction extends MAnnotation {
	
	public ElementType[] value;
	
	public TargetRestriction(ElementType...value){
		this.value = value;
	}

	@Override
	public MAnnotation[] getMetaAnnotations() {
		return new MAnnotation[]{
			new TargetRestriction(ElementType.ANNOTATION_TYPE)
		};
	}

	public String docs() {
		return "This meta annotation ensures that other annotations are tagged onto the proper taggable elements.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}
	
	
	
}
