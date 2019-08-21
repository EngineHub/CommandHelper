package com.laytonsmith.core.objects;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Method;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A UserObject represents an instance of an object that was defined in MethodScript, i.e. we have no native
 * class reference here.
 */
public class UserObject implements Mixed {

	private static int objectIdCounter = 0;

	private final Environment env;
	private final Target t;
	private final ObjectDefinition objectDefinition;

	private final Map<String, Mixed> fieldTable;

	private final Mixed nativeObject;

	private final int objectId;

	/**
	 * Creates a new UserObject.
	 * @param t The code target where this element is being created
	 * @param parent The parent script. This may be null, but ONLY if this is intended on being used as
	 * an invalid object. The default values will not be properly populated if this is null.
	 * @param env The environment
	 * @param objectDefinition The object definition this object is based on.
	 * @param nativeObject If there is an underlying native object, this should be sent along with this object. However,
	 * if this is not a native object, this should be null.
	 */
	public UserObject(Target t, Script parent, Environment env, ObjectDefinition objectDefinition, Mixed nativeObject) {
		this.t = t;
		this.env = env;
		this.objectDefinition = objectDefinition;
		this.nativeObject = nativeObject;
		this.objectId = objectIdCounter++;
		this.fieldTable = new HashMap<>();
		for(Map.Entry<String, List<ElementDefinition>> e : objectDefinition.getElements().entrySet()) {
			// Fields can only have one element definition, so if the list contains more than one, it is
			// certainly a method.
			// TODO
			if(e.getValue().size() > 1) {
				continue;
			}
			ElementDefinition ed = e.getValue().get(0);
			if(ed.getMethod() != null) {
				continue;
			}
			if(parent == null) {
				fieldTable.put(e.getKey(), CNull.UNDEFINED);
			} else {
				Mixed value = parent.eval(ed.getDefaultValue(), env);
				fieldTable.put(e.getKey(), value);
			}
		}
	}

	@Override
	public String val() {
		List<ElementDefinition> toStrings = this.objectDefinition.getElements().get("toString");
		if(toStrings != null) {
			for(ElementDefinition ed : toStrings) {
				Method m = ed.getMethod();
				if(m != null) {
					if(m.getParameters().length == 0) {
						return m.executeCallable(env, t).val();
					}
				}
			}
		}
		// Use the default toString
		return objectDefinition.getClassName() + "@" + String.format("0x%X", objectId);
	}

	@Override
	public void setTarget(Target target) {
		//
	}

	@Override
	public Target getTarget() {
		return t;
	}

	@Override
	public Mixed clone() throws CloneNotSupportedException {
		throw new UnsupportedOperationException("UserObject clone");
	}

	@Override
	public String getName() {
		return objectDefinition.getName();
	}

	@Override
	public String docs() {
		return "TODO";
	}

	@Override
	public Version since() {
		return MSVersion.V0_0_0;
	}

	@Override
	public CClassType[] getSuperclasses() {
		return objectDefinition.getSuperclasses().toArray(new CClassType[objectDefinition.getSuperclasses().size()]);
	}

	@Override
	public CClassType[] getInterfaces() {
		return objectDefinition.getInterfaces().toArray(new CClassType[objectDefinition.getInterfaces().size()]);
	}

	@Override
	public ObjectType getObjectType() {
		return objectDefinition.getObjectType();
	}

	@Override
	public Set<ObjectModifier> getObjectModifiers() {
		return objectDefinition.getObjectModifiers();
	}

	@Override
	public AccessModifier getAccessModifier() {
		return objectDefinition.getAccessModifier();
	}

	@Override
	public CClassType getContainingClass() {
		return objectDefinition.getContainingClass();
	}

	@Override
	public boolean isInstanceOf(CClassType type) {
		return Construct.isInstanceof(this, type);
	}

	@Override
	public boolean isInstanceOf(Class<? extends Mixed> type) {
		return Construct.isInstanceof(this, type);
	}

	@Override
	public CClassType typeof() {
		return objectDefinition.getType();
	}

	@Override
	public URL getSourceJar() {
		return null;
	}

	@Override
	public Class<? extends Documentation>[] seeAlso() {
		return new Class[0];
	}

	public <T extends Mixed> T getNativeObject(Class<T> type) {
		return (T) nativeObject;
	}

}
