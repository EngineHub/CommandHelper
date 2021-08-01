package com.laytonsmith.core.asm.metadata;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Triplet;
import com.laytonsmith.core.asm.LLVMEnvironment;
import com.laytonsmith.core.environments.Environment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * LLVM IR allows metadata to be attached to instructions and global objects in the program that can convey extra
 * information about the code to the optimizers and code generator. One example application of metadata is source-level
 * debug information.
 * <p>
 * This class wraps and represents a single metadata reference. Note that in general, this class doesn't store anything
 * in the environment other than the generic reference from id to this class. In order to further reference the data,
 * an additional mapping from native concept to metadata id must be created in the environment. First class subclasses
 * do this, but then you should use those subclasses, rather than this class directly, then in the future, reference
 * this metadata instance through the environment, rather than trying to keep a reference around some other way, (or
 * worse, recreating a new instance).
 */
public class IRMetadata {
	/**
	 * Represents the type of the value, which is used to properly escape the value.
	 * Note that in all cases, references to other metadata can instead be provided,
	 * which has different escaping rules.
	 */
	public static enum DataType {
		/**
		 * The value is a string. Quotes are escaped.
		 */
		STRING,
		/**
		 * The value is a number. No escaping is done.
		 */
		NUMBER,
		/**
		 * The value is a constant.
		 */
		CONST,
		/**
		 * The value is an array.
		 */
		TUPLE,
		/**
		 * The value is a required to be a reference to another metadata object, i.e. !42
		 */
		REFERENCE,
		/**
		 * A boolean.
		 */
		BOOLEAN,
	}

	/**
	 * Creates a tuple
	 * @param env
	 * @param values
	 * @return
	 */
	public static IRMetadata AsTuple(Environment env, String ... values) {
		return new IRMetadata(env, Arrays.asList(values), false);
	}

	/**
	 * Creates an anonymous tuple. This means that the tuple will not be added to the registry as numbered metadata
	 * entry, and should be used in combination with named metadata references, such as "!llvm.dbg.cu" and others.
	 * <p>
	 * Note that calling getReference on this object is invalid, and will
	 * result in an exception. Instead, use getDefinition, which will be missing the reference id assigment.
	 * @param env
	 * @param values
	 * @return
	 */
	public static IRMetadata AsAnonymousTuple(Environment env, String ... values) {
		return new IRMetadata(env, Arrays.asList(values), true);
	}

	/**
	 * Creates an empty tuple. Note that tuples are immutable, and you should likely be using
	 * {@link LLVMMetadataRegistry#getEmptyTuple(com.laytonsmith.core.environments.Environment)} instead.
	 * @param env
	 * @return
	 */
	public static IRMetadata AsEmptyTuple(Environment env) {
		return new IRMetadata(env, new ArrayList<>(), false);
	}

	private final Environment env;
	private final Map<String, DataType> prototype = new HashMap<>();
	private final Map<String, String> attributes = new HashMap<>();
	private final Map<String, IRMetadata> metadataReference = new HashMap<>();
	private final int metadataId;
	private final String metadataTypeName;
	private boolean isDistinct = false;
	private final boolean isTuple;
	private final List<String> tupleList;

	/**
	 * Tuple constructor
	 */
	@SuppressWarnings("LeakingThisInConstructor")
	private IRMetadata(Environment env, List<String> tupleList, boolean anonymous) {
		Objects.nonNull(env);
		this.env = env;
		this.isTuple = true;
		if(anonymous) {
			this.metadataId = -1;
		} else {
			this.metadataId = newMetadataId();
		}
		this.metadataTypeName = null;
		this.tupleList = tupleList;
		if(!anonymous) {
			env.getEnv(LLVMEnvironment.class).getMetadataRegistry().addMetadata(this);
		}
	}

	/**
	 * Constructs a new metadata object. This can be used generally for generic data, but most likely you want
	 * a subclass of it, rather than to directly use it.
	 * @param env The environment
	 * @param prototype The prototype. Usually this should be constructed inline, such as <code>new IRMetadata.PrototypeBuilder().put("key1", IRMetadata.DataType.STRING).put("key2", IRMetadata.DataType.CONST).put("key3", IRMetadata.DataType.NUMBER).build()</code>
	 * @param metadataTypeName The metadata type name. May not be null, but may be empty string.
	 */
	@SuppressWarnings("LeakingThisInConstructor")
	public IRMetadata(Environment env, Map<String, DataType> prototype, String metadataTypeName) {
		Objects.nonNull(env);
		Objects.nonNull(prototype);
		Objects.nonNull(metadataTypeName);
		this.isTuple = false;
		this.tupleList = null;
		this.env = env;
		this.prototype.putAll(prototype);
		this.metadataId = newMetadataId();
		this.metadataTypeName = metadataTypeName;
		env.getEnv(LLVMEnvironment.class).getMetadataRegistry().addMetadata(this);
	}


	private int newMetadataId() {
		return env.getEnv(LLVMEnvironment.class).getNewMetadataId();
	}

	public int getMetadataId() {
		return this.metadataId;
	}

	private void ensureNotTuple(String methodName) {
		if(isTuple) {
			throw new RuntimeException(methodName + " cannot be called on tuple metadata");
		}
	}

	/**
	 * Sets the value of a given key/value pair. The key is checked against the values
	 * in the prototype to ensure it's defined.
	 * @param key
	 * @param number
	 * @return Returns <code>this</code> for easy chaining.
	 */
	public IRMetadata putNumber(String key, long number) {
		ensureNotTuple("putNumber");
		if(!prototype.containsKey(key)) {
			throw new Error("[COMPILER BUG] Missing key from prototype.");
		}
		if(prototype.get(key) != DataType.NUMBER) {
			throw new Error("[COMPILER BUG] Trying to put a number in a non-number key.");
		}
		attributes.put(key, Long.toString(number));
		return this;
	}

	/**
	 * Sets the value of a given key/value pair. The key is checked against the values
	 * in the prototype to ensure it's defined.
	 * @param key
	 * @param value
	 * @return Returns <code>this</code> for easy chaining.
	 */
	public IRMetadata putBoolean(String key, boolean bool) {
		ensureNotTuple("putBoolean");
		if(!prototype.containsKey(key)) {
			throw new Error("[COMPILER BUG] Missing key from prototype.");
		}
		if(prototype.get(key) != DataType.BOOLEAN) {
			throw new Error("[COMPILER BUG] Trying to put a boolean in a non-boolean key.");
		}
		attributes.put(key, bool ? "true" : "false");
		return this;
	}

	/**
	 * Sets the value of a given key/value pair. The key is checked against the values
	 * in the prototype to ensure it's defined.
	 * @param key
	 * @param value
	 * @return Returns <code>this</code> for easy chaining.
	 */
	public IRMetadata putAttribute(String key, String value) {
		ensureNotTuple("putAttribute");
		if(!prototype.containsKey(key)) {
			throw new Error("[COMPILER BUG] Missing key from prototype.");
		}
		if(prototype.get(key) != DataType.STRING) {
			throw new Error("[COMPILER BUG] Trying to put a string in a non-string key.");
		}
		attributes.put(key, value);
		return this;
	}

	/**
	 * Sets the value of a given key/value pair. The key is checked against the values
	 * in the prototype to ensure it's defined.
	 * @param key
	 * @param value
	 * @return Returns <code>this</code> for easy chaining.
	 */
	public IRMetadata putConst(String key, String value) {
		ensureNotTuple("putConst");
		if(!prototype.containsKey(key)) {
			throw new Error("[COMPILER BUG] Missing key from prototype.");
		}
		if(prototype.get(key) != DataType.CONST) {
			throw new Error("[COMPILER BUG] Trying to put a const in a non-const key.");
		}
		attributes.put(key, value);
		return this;
	}

	/**
	 * Sets the value of a given key/value pair that's defined as a tuple. Each parameter
	 * is unfortunately not checked for correctness, so extra care should be taken when constructing
	 * calls to this method. However, the tuple is formed correctly regardless. If the parameters in
	 * the array are strings, any quotes must be manually escaped.
	 * @param key
	 * @param array
	 * @return
	 */
	public IRMetadata putTuple(String key, String[] array) {
		ensureNotTuple("putTuple");
		if(!prototype.containsKey(key)) {
			throw new Error("[COMPILER BUG] Missing key from prototype.");
		}
		if(prototype.get(key) != DataType.TUPLE) {
			throw new Error("[COMPILER BUG] Trying to put a tuple in a non-tuple key.");
		}
		attributes.put(key, "!{" + StringUtils.Join(array, ", ") + "}");
		return this;
	}

	/**
	 * Sets the value of a given key to reference another metadata set.
	 * @param key
	 * @param data
	 * @return Returns <code>this</code> for easy chaining.
	 */
	public IRMetadata putMetadataReference(String key, IRMetadata data) {
		ensureNotTuple("putMetadataReference");
		if(!prototype.containsKey(key)) {
			throw new Error("[COMPILER BUG] Missing key from prototype.");
		}
		if(prototype.get(key) != DataType.REFERENCE) {
			throw new Error("[COMPILER BUG] Trying to put a reference in a non-reference key.");
		}
		metadataReference.put(key, data);
		return this;
	}

	public IRMetadata setIsDistinct(boolean distinct) {
		ensureNotTuple("setIsDistinct");
		this.isDistinct = distinct;
		return this;
	}

	/**
	 * Returns a list of triplets of all the attributes. The triplet is defined as name, value, datatype.
	 * @return
	 */
	public List<Triplet<String, String, DataType>> getAttributes() {
		ensureNotTuple("getAttributes");
		List<Triplet<String, String, DataType>> list = new ArrayList<>();
		for(Map.Entry<String, String> entry : attributes.entrySet()) {
			Triplet<String, String, DataType> triplet
					= new Triplet<>(entry.getKey(), entry.getValue(), prototype.get(entry.getKey()));
			list.add(triplet);
		}
		return list;
	}

	/**
	 * Returns a reference to this metadata, for instance "!0".
	 * @return
	 */
	public String getReference() {
		if(metadataId < 0) {
			throw new RuntimeException("Calling getReference on an anonymous tuple is not allowed. Use getDefinition instead.");
		}
		return "!" + metadataId;
	}

	/**
	 * Returns the definition of this metadata, in LLVM IR.
	 * @return
	 */
	public String getDefinition() {
		StringBuilder ret = new StringBuilder();
		if(!isTuple || metadataId >= 0) {
			ret.append(getReference());
			ret.append(" = ");
		}
		if(isTuple) {
			ret.append("!{");
			boolean first = true;
			for(String t : tupleList) {
				if(!first) {
					ret.append(", ");
				}
				first = false;
				ret.append(t);
			}
			ret.append("}");
		} else {
			if(isDistinct) {
				ret.append("distinct ");
			}
			ret.append("!");
			ret.append(metadataTypeName);
			ret.append("(");
			boolean first = true;
			for(Map.Entry<String, String> entry : attributes.entrySet()) {
				if(!first) {
					ret.append(", ");
				}
				first = false;

				String name = entry.getKey();
				String value = entry.getValue();
				DataType type = prototype.get(name);
				ret.append(name).append(": ");
				if(type == DataType.STRING) {
					value = "\"" + value.replaceAll("\"", "\\\\34") + "\"";
				}
				if(type == DataType.REFERENCE) {
					if(!value.matches("!\\d+")) {
						throw new RuntimeException("[COMPILER BUG] " + name + " can only refer to a reference type.");
					}
				}
				ret.append(value);
			}
			for(Map.Entry<String, IRMetadata> entry : metadataReference.entrySet()) {
				if(!first) {
					ret.append(", ");
				}
				first = false;

				String name = entry.getKey();
				String value = "!" + entry.getValue().getMetadataId();
				ret.append(name).append(": ");
				ret.append(value);
			}
			ret.append(")");
		}
		return ret.toString();
	}

	/**
	 * Assists in building a prototype object.
	 */
	public static class PrototypeBuilder {
		private final Map<String, DataType> map = new HashMap<>();

		public PrototypeBuilder put(String key, DataType type) {
			map.put(key, type);
			return this;
		}

		public Map<String, DataType> build() {
			return map;
		}
	}
}
