
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.typename;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.arguments.Argument;
import com.laytonsmith.core.arguments.ArgumentBuilder;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.CResource;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.natives.MEnum;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;

/**
 *
 */
public class ResourceManager {
	public static String docs(){
		return "This class contains functions for resource management. This entire class of functions WILL"
				+ " be deprecated at some point in the future, so don't rely too heavily on it.";
	}
	
	@typename("ResourceType")
	public static enum ResourceType implements MEnum {
		XML_DOCUMENT(XMLDocument.class),
		STRING_BUILDER(StringBuffer.class);
		private final Class<?> type;
		private ResourceType(Class<?> type){
			this.type = type;
		}
		
		public Class<?> getType(){
			return type;
		}
		
		public static ResourceType getResourceByType(Class<?> type){
			for(ResourceType c : values()){
				if(c.getType() == type){
					return c;
				}
			}
			throw new IllegalArgumentException();
		}

		public Object value() {
			return this;
		}

		public String val() {
			return this.name();
		}

		public String typeName() {
			return this.getClass().getAnnotation(typename.class).value();
		}

		public CPrimitive primitive(Target t) throws ConfigRuntimeException {
			throw new Error();
		}

		public boolean isImmutable() {
			return true;
		}

		public boolean isDynamic() {
			return false;
		}

		public void destructor() {
			
		}

		public Mixed doClone() {
			return this;
		}

		public Target getTarget() {
			return Target.UNKNOWN;
		}
	}
	
	private static final Map<Long, CResource<?>> resources = new HashMap<Long, CResource<?>>();
	static {
		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			public void run() {
				resources.clear();
			}
		});
	}
	
	/**
	 * This is used to get the appropriately cast resource from a CResource. If the
	 * types aren't matched up, an appropriate exception is thrown.
	 * @param <T>
	 * @param resource
	 * @param type
	 * @param t
	 * @return 
	 */
	public static <T> T GetResource(CResource<?> resource, Class<T> type, Target t){
		if(type.isAssignableFrom(resource.getResource().getClass())){
			return (T) resource.getResource();
		} else {
			throw new Exceptions.CastException("Unexpected resource type. Expected resource of type "
					+ ResourceType.getResourceByType(type).name() + " but found "
					+ ResourceType.getResourceByType(resource.getResource().getClass()).name() + " instead.", t);
		}
	}
	
	@api
	public static class res_create_resource extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ResourceType type;
			Mixed data = null;
			try{
				type = ResourceType.valueOf(args[0].val());
			} catch(IllegalArgumentException e){
				throw new Exceptions.FormatException(e.getMessage(), t);
			}
			if(args.length > 1){
				data = args[1];
			}
			CResource<?> resource;
			switch(type){
				case XML_DOCUMENT:
					try {
						if(data == null){
							throw new ConfigRuntimeException("data cannot be empty", ExceptionType.NullPointerException, t);
						}
						resource = new CResource<XMLDocument>(new XMLDocument(data.val()), t);
					} catch (SAXException ex) {
						throw new Exceptions.FormatException(ex.getMessage(), t);
					}
					break;
				case STRING_BUILDER:
					resource = new CResource<StringBuffer>(new StringBuffer(), new CResource.ResourceToString() {

						public String getString(CResource res) {
							return res.getResource().toString();
						}
					}, t);
					break;
				default:
					throw new Error("Unhandled case in switch statement");
			}
			resources.put(resource.getId(), resource);
			return resource;
		}

		public String getName() {
			return "res_create_resource";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "Creates a new resource, which is stored in memory. Various"
					+ " functions require resources of certain types, which are created with this function."
					+ " Barring resources that you intend on keeping around indefinitely, each call"
					+ " to res_create_resource should be paired with a res_free_resource, being careful"
					+ " to catch any exceptions and still calling res_free_resource anyways. Each resource"
					+ " has its own data to create the resource. Type may be one of: " 
					+ StringUtils.Join(ResourceType.values(), ", ", ", or ");
		}

		public Argument returnType() {
			return new Argument("", CResource.class);
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", ResourceType.class, "type"),
						new Argument("", Mixed.class, "data").setVarargs().setOptionalDefaultNull()
					);
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api
	public static class res_free_resource extends AbstractFunction {
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException, ExceptionType.NotFoundException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0] instanceof CResource){
				CResource<?> resource = (CResource<?>) args[0];
				if(resources.containsKey(resource.getId())){
					resources.remove(resource.getId());
					return new CVoid(t);
				} else {
					throw new ConfigRuntimeException("That resource is not a valid resource.", ExceptionType.NotFoundException, t);
				}
			} else {
				throw new Exceptions.CastException("Expected a resource", t);
			}
		}

		public String getName() {
			return "res_free_resource";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "Frees the given resource. This should ALWAYS be called at some point after creating a resource"
					+ " with res_create_resource, once you are done with the resource.";
		}

		public Argument returnType() {
			return Argument.VOID;
		}

		public ArgumentBuilder arguments() {
			return ArgumentBuilder.Build(
						new Argument("", CResource.class, "resource")
					);
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}
}
