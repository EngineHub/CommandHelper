package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.XMLDocument;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CResource;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.xml.sax.SAXException;

/**
 *
 */
@core
public class ResourceManager {

	public static String docs() {
		return "This class contains functions for resource management. This entire class of functions WILL"
				+ " be deprecated at some point in the future, so don't rely too heavily on it.";
	}

	public static enum ResourceTypes {
		XML_DOCUMENT(XMLDocument.class),
		STRING_BUILDER(StringBuffer.class),
		RANDOM(Random.class);
		private final Class<?> type;

		private ResourceTypes(Class<?> type) {
			this.type = type;
		}

		public Class<?> getType() {
			return type;
		}

		public static ResourceTypes getResourceByType(Class<?> type) {
			for(ResourceTypes c : values()) {
				if(c.getType() == type) {
					return c;
				}
			}
			throw new IllegalArgumentException();
		}
	}

	private static final Map<Long, CResource<?>> RESOURCES = new HashMap<>();

	static {
		StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

			@Override
			public void run() {
				RESOURCES.clear();
			}
		});
	}

	/**
	 * This is used to get the appropriately cast resource from a CResource. If the types aren't matched up, an
	 * appropriate exception is thrown.
	 *
	 * @param <T>
	 * @param resource
	 * @param type
	 * @param t
	 * @return
	 */
	public static <T> T GetResource(CResource<?> resource, Class<T> type, Target t) {
		if(type.isAssignableFrom(resource.getResource().getClass())) {
			return (T) resource.getResource();
		} else {
			throw new CRECastException("Unexpected resource type. Expected resource of type "
					+ ResourceTypes.getResourceByType(type).name() + " but found "
					+ ResourceTypes.getResourceByType(resource.getResource().getClass()).name() + " instead.", t);
		}
	}

	@api
	public static class res_create_resource extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			ResourceTypes type;
			Mixed data = null;
			try {
				type = ResourceTypes.valueOf(args[0].val());
			} catch (IllegalArgumentException e) {
				throw new CREFormatException(e.getMessage(), t);
			}
			if(args.length > 1) {
				data = args[1];
			}
			CResource<?> resource;
			switch(type) {
				case XML_DOCUMENT:
					try {
						if(data == null) {
							throw new CRENullPointerException("data cannot be empty", t);
						}
						resource = new CResource<XMLDocument>(new XMLDocument(data.val()), t);
					} catch (SAXException ex) {
						throw new CREFormatException(ex.getMessage(), t);
					}
					break;
				case STRING_BUILDER:
					resource = new CResource<StringBuffer>(new StringBuffer(), new CResource.ResourceToString() {

						@Override
						public String getString(CResource res) {
							return res.getResource().toString();
						}
					}, t);
					break;
				case RANDOM:
					resource = new CResource<>(new Random(Static.getInt(data, t)), new CResource.ResourceToString() {
						@Override
						public String getString(CResource res) {
							return res.getResource().toString();
						}
					}, t);
					break;
				default:
					throw new Error("Unhandled case in switch statement");
			}
			RESOURCES.put(resource.getId(), resource);
			return resource;
		}

		@Override
		public String getName() {
			return "res_create_resource";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		@Override
		public String docs() {
			return "resource {type, [data]} Creates a new resource, which is stored in memory. Various"
					+ " functions require resources of certain types, which are created with this function."
					+ " Barring resources that you intend on keeping around indefinitely, each call"
					+ " to res_create_resource should be paired with a res_free_resource, being careful"
					+ " to catch any exceptions and still calling res_free_resource anyways. Each resource"
					+ " has its own data to create the resource. Type may be one of: "
					+ StringUtils.Join(ResourceTypes.values(), ", ", ", or ");
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}

	@api
	public static class res_free_resource extends AbstractFunction {

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CRENotFoundException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(args[0].isInstanceOf(CResource.TYPE)) {
				CResource<?> resource = (CResource<?>) args[0];
				if(RESOURCES.containsKey(resource.getId())) {
					RESOURCES.remove(resource.getId());
					return CVoid.VOID;
				} else {
					throw new CRENotFoundException("That resource is not a valid resource.", t);
				}
			} else {
				throw new CRECastException("Expected a resource", t);
			}
		}

		@Override
		public String getName() {
			return "res_free_resource";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		@Override
		public String docs() {
			return "void {resource} Frees the given resource. This should ALWAYS be called at some point after creating a resource"
					+ " with res_create_resource, once you are done with the resource.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
}
