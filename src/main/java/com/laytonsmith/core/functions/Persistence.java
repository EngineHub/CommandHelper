package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientArgumentsException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.persistence.DataSourceException;
import com.laytonsmith.persistence.PersistenceNetwork;
import com.laytonsmith.persistence.ReadOnlyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@core
public class Persistence {

	public static String docs() {
		return "Allows scripts to store data from execution to execution. See the guide on [[Persistence|persistence]] for more information."
				+ " In all the functions, you may send multiple arguments for the key, which will automatically"
				+ " be concatenated with a period (the namespace separator). No magic happens here, you can"
				+ " put periods yourself, or combine manually namespaced values or automatically namespaced values"
				+ " with no side effects. All the functions in the Persistence API are threadsafe (though not necessarily"
				+ " process safe).";
	}

	@api(environments = {GlobalEnv.class})
	@noboilerplate
	@seealso({get_value.class, clear_value.class, com.laytonsmith.tools.docgen.templates.PersistenceNetwork.class})
	public static class store_value extends AbstractFunction {

		@Override
		public String getName() {
			return "store_value";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {[namespace, ...,] key, value} Allows you to store a value, which can then be retrieved later. key must be a string containing"
					+ " only letters, numbers, underscores. Periods may also be used, but they form a namespace, and have special meaning."
					+ " (See get_values())";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class, CREIOException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_2;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			String key = GetNamespace(args, args.length - 1, getName(), t);
			String value = null;
			try {
				value = Construct.json_encode(args[args.length - 1], t);
			} catch (MarshalException e) {
				throw new CREFormatException(e.getMessage(), t);
			}
			char pc = '.';
			for(int i = 0; i < key.length(); i++) {
				Character c = key.charAt(i);
				if(i != 0) {
					pc = key.charAt(i - 1);
				}
				if((i == 0 || i == key.length() - 1 || pc == '.') && c == '.') {
					throw new CREFormatException("Periods may only be used as seperators between namespaces.", t);
				}
				if(c != '_' && c != '.' && !Character.isLetterOrDigit(c)) {
					throw new CREFormatException("Param 1 in store_value must only contain letters, digits, underscores, or dots, (which denote namespaces).", t);
				}
			}
			MSLog.GetLogger().Log(MSLog.Tags.PERSISTENCE, LogLevel.DEBUG, "Storing: " + key + " -> " + value, t);
			try {
				env.getEnv(GlobalEnv.class).GetPersistenceNetwork().set(env.getEnv(GlobalEnv.class).GetDaemonManager(), ("storage." + key).split("\\."), value);
			} catch (IllegalArgumentException e) {
				throw new CREFormatException(e.getMessage(), t);
			} catch (Exception ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			}
			return CVoid.VOID;
		}

		@Override
		public Boolean runAsync() {
			//Because we do IO
			return true;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments = {GlobalEnv.class})
	@noboilerplate
	@seealso({store_value.class, get_values.class, has_value.class, com.laytonsmith.tools.docgen.templates.PersistenceNetwork.class})
	public static class get_value extends AbstractFunction {

		@Override
		public String getName() {
			return "get_value";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "mixed {[namespace, ...,] key} Returns a stored value stored with store_value. If the key doesn't exist in storage, null"
					+ " is returned. On a more detailed note: If the value stored in the persistence database is not actually a construct,"
					+ " then null is also returned.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_0_2;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws CancelCommandException, ConfigRuntimeException {
			Object o;
			String namespace = GetNamespace(args, null, getName(), t);
			MSLog.GetLogger().Log(MSLog.Tags.PERSISTENCE, LogLevel.DEBUG, "Getting value: " + namespace, t);
			try {
				Object obj;
				try {
					obj = env.getEnv(GlobalEnv.class).GetPersistenceNetwork().get(("storage." + namespace).split("\\."));
				} catch (DataSourceException ex) {
					throw new CREIOException(ex.getMessage(), t, ex);
				} catch (IllegalArgumentException e) {
					throw new CREFormatException(e.getMessage(), t, e);
				}
				if(obj == null) {
					return CNull.NULL;
				}
				o = Construct.json_decode(obj.toString(), t);
			} catch (MarshalException ex) {
				throw ConfigRuntimeException.CreateUncatchableException(ex.getMessage(), t);
			}
			try {
				return (Mixed) o;
			} catch (ClassCastException e) {
				return CNull.NULL;
			}
		}

		@Override
		public Boolean runAsync() {
			//Because we do IO
			return true;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments = {GlobalEnv.class})
	@noboilerplate
	@seealso({com.laytonsmith.tools.docgen.templates.PersistenceNetwork.class})
	public static class get_values extends AbstractFunction {

		@Override
		public String getName() {
			return "get_values";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "array {name[, space, ...]} Returns all the values in a particular namespace"
					+ " as an associative"
					+ " array(key: value, key: value). Only full namespace matches are considered,"
					+ " so if the key 'users.data.username.hi' existed in the database, and you tried"
					+ " get_values('users.data.user'), nothing would be returned. The last segment in"
					+ " a key is also considered a namespace, so 'users.data.username.hi' would return"
					+ " a single value (in this case).";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Getting values", "store_value('x.top.a',true)\n"
						+ "store_value('x.top.b',false)\n"
						+ "msg(get_values('x'))", "{x.top.a: true, x.top.b: false}")};
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			PersistenceNetwork p = environment.getEnv(GlobalEnv.class).GetPersistenceNetwork();
			List<String> keyChain = new ArrayList<String>();
			keyChain.add("storage");
			String namespace = GetNamespace(args, null, getName(), t);
			MSLog.GetLogger().Log(MSLog.Tags.PERSISTENCE, LogLevel.DEBUG, "Getting all values from " + namespace, t);
			keyChain.addAll(Arrays.asList(namespace.split("\\.")));
			Map<String[], String> list;
			try {
				list = p.getNamespace(keyChain.toArray(new String[keyChain.size()]));
			} catch (DataSourceException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			} catch (IllegalArgumentException e) {
				throw new CREFormatException(e.getMessage(), t, e);
			}
			CArray ca = CArray.GetAssociativeArray(t);
			MSLog.GetLogger().Log(MSLog.Tags.PERSISTENCE, LogLevel.DEBUG, list.size() + " value(s) are being returned", t);
			for(String[] e : list.keySet()) {
				try {
					String key = StringUtils.Join(e, ".").replaceFirst("storage\\.", ""); //Get that junk out of here
					ca.set(new CString(key, t),
							Construct.json_decode(list.get(e), t), t);
				} catch (MarshalException ex) {
					Logger.getLogger(Persistence.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			return ca;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments = {GlobalEnv.class})
	@noboilerplate
	@seealso({get_value.class, com.laytonsmith.tools.docgen.templates.PersistenceNetwork.class})
	public static class has_value extends AbstractFunction {

		@Override
		public String getName() {
			return "has_value";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "boolean {[namespace, ...,] key} Returns whether or not there is data stored at the specified key in the Persistence database.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_1_2;
		}

		@Override
		public Boolean runAsync() {
			return true;
		}

		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			try {
				return CBoolean.get(env.getEnv(GlobalEnv.class).GetPersistenceNetwork().hasKey(("storage." + GetNamespace(args, null, getName(), t)).split("\\.")));
			} catch (DataSourceException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			} catch (IllegalArgumentException e) {
				throw new CREFormatException(e.getMessage(), t, e);
			}
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments = {GlobalEnv.class})
	@noboilerplate
	@seealso({store_value.class, com.laytonsmith.tools.docgen.templates.PersistenceNetwork.class})
	public static class clear_value extends AbstractFunction {

		@Override
		public String getName() {
			return "clear_value";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {[namespace, ...,] key} Completely removes a value from storage. Calling has_value(key) after this call will return false.";
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIOException.class, CREFormatException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_0;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			String namespace = GetNamespace(args, null, getName(), t);
			MSLog.GetLogger().Log(MSLog.Tags.PERSISTENCE, LogLevel.DEBUG, "Clearing value: " + namespace, t);
			try {
				environment.getEnv(GlobalEnv.class).GetPersistenceNetwork().clearKey(environment.getEnv(GlobalEnv.class).GetDaemonManager(), ("storage." + namespace).split("\\."));
			} catch (DataSourceException | ReadOnlyException | IOException ex) {
				throw new CREIOException(ex.getMessage(), t, ex);
			} catch (IllegalArgumentException e) {
				throw new CREFormatException(e.getMessage(), t, e);
			}
			return CVoid.VOID;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	/**
	 * Generates the namespace for this value, given an array of constructs. If the entire list of arguments isn't
	 * supposed to be part of the namespace, the value to be excluded may be specified.
	 *
	 * @param args
	 * @param exclude
	 * @return
	 */
	private static String GetNamespace(Mixed[] args, Integer exclude, String name, Target t) {
		if(exclude != null && args.length < 2 || exclude == null && args.length < 1) {
			throw new CREInsufficientArgumentsException(name + " was not provided with enough arguments. Check the documentation, and try again.", t);
		}
		boolean first = true;
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < args.length; i++) {
			if(exclude != null && exclude == i) {
				continue;
			}
			if(!first) {
				b.append(".");
			}
			first = false;
			b.append(args[i].val());
		}
		return b.toString();
	}
}
