package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.core.*;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.persistance.DataSourceException;
import com.laytonsmith.persistance.PersistanceNetwork;
import com.laytonsmith.persistance.ReadOnlyException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class Persistance {

	public static String docs() {
		return "Allows scripts to store data from execution to execution. See the guide on [[CommandHelper/Persistance|persistance]] for more information."
				+ " In all the functions, you may send multiple arguments for the key, which will automatically"
				+ " be concatenated with a period (the namespace separator). No magic happens here, you can"
				+ " put periods yourself, or combine manually namespaced values or automatically namespaced values"
				+ " with no side effects.";
	}

	@api(environments={GlobalEnv.class})
	@noboilerplate
	public static class store_value extends AbstractFunction {

		public String getName() {
			return "store_value";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "void {[namespace, ...,] key, value} Allows you to store a value, which can then be retrieved later. key must be a string containing"
					+ " only letters, numbers, underscores. Periods may also be used, but they form a namespace, and have special meaning."
					+ " (See get_values())";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException, ExceptionType.IOException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_2;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			String key = GetNamespace(args, args.length - 1, getName(), t);
			String value = null;
			try {
				value = Construct.json_encode(args[args.length - 1], t);
			} catch (MarshalException e) {
				throw new Exceptions.FormatException(e.getMessage(), t);
			}
			char pc = '.';
			for (int i = 0; i < key.length(); i++) {
				Character c = key.charAt(i);
				if (i != 0) {
					pc = key.charAt(i - 1);
				}
				if ((i == 0 || i == key.length() - 1 || pc == '.') && c == '.') {
					throw new ConfigRuntimeException("Periods may only be used as seperators between namespaces.", ExceptionType.FormatException, t);
				}
				if (c != '_' && c != '.' && !Character.isLetterOrDigit(c)) {
					throw new ConfigRuntimeException("Param 1 in store_value must only contain letters, digits, underscores, or dots, (which denote namespaces).",
							ExceptionType.FormatException, t);
				}
			}
			CHLog.GetLogger().Log(CHLog.Tags.PERSISTANCE, LogLevel.DEBUG, "Storing: " + key + " -> " + value, t);
			try {
				env.getEnv(GlobalEnv.class).GetPersistanceNetwork().set(env.getEnv(GlobalEnv.class).GetDaemonManager(), ("storage." + key).split("\\."), value);
			} catch(IllegalArgumentException e){
				throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t);
			} catch (Exception ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
			}
			return new CVoid(t);
		}

		public Boolean runAsync() {
			//Because we do IO
			return true;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments={GlobalEnv.class})
	@noboilerplate
	public static class get_value extends AbstractFunction {

		public String getName() {
			return "get_value";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "Mixed {[namespace, ...,] key} Returns a stored value stored with store_value. If the key doesn't exist in storage, null"
					+ " is returned. On a more detailed note: If the value stored in the persistance database is not actually a construct,"
					+ " then null is also returned.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IOException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_0_2;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws CancelCommandException, ConfigRuntimeException {
			Object o;
			String namespace = GetNamespace(args, null, getName(), t);
			CHLog.GetLogger().Log(CHLog.Tags.PERSISTANCE, LogLevel.DEBUG, "Getting value: " + namespace, t);
			try {
				Object obj;
				try {
					obj = env.getEnv(GlobalEnv.class).GetPersistanceNetwork().get(("storage." + namespace).split("\\."));
				} catch (DataSourceException ex) {
					throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
				} catch(IllegalArgumentException e){
					throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t, e);
				}
				if (obj == null) {
					return new CNull(t);
				}
				o = Construct.json_decode(obj.toString(), t);
			} catch (MarshalException ex) {
				throw ConfigRuntimeException.CreateUncatchableException(ex.getMessage(), t);
			}
			try {
				return (Construct) o;
			} catch (ClassCastException e) {
				return new CNull(t);
			}
		}

		public Boolean runAsync() {
			//Because we do IO
			return true;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments={GlobalEnv.class})
	@noboilerplate
	public static class get_values extends AbstractFunction {

		public String getName() {
			return "get_values";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "array {name[, space, ...]} Returns all the values in a particular namespace"
					+ " as an associative"
					+ " array(key: value, key: value). Only full namespace matches are considered,"
					+ " so if the key 'users.data.username.hi' existed in the database, and you tried"
					+ " get_values('users.data.user'), nothing would be returned. The last segment in"
					+ " a key is also considered a namespace, so 'users.data.username.hi' would return"
					+ " a single value (in this case).";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IOException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return true;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return new ExampleScript[]{
				new ExampleScript("Getting values", "store_value('x.top.a',true)\nstore_value('x.top.b',false)\nmsg(get_values('x'))"),
			};
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			PersistanceNetwork p = environment.getEnv(GlobalEnv.class).GetPersistanceNetwork();
			List<String> keyChain = new ArrayList<String>();
			keyChain.add("storage");
			String namespace = GetNamespace(args, null, getName(), t);
			CHLog.GetLogger().Log(CHLog.Tags.PERSISTANCE, LogLevel.DEBUG, "Getting all values from " + namespace, t);
			keyChain.addAll(Arrays.asList(namespace.split("\\.")));
			Map<String[], String> list;
			try {
				list = p.getNamespace(keyChain.toArray(new String[keyChain.size()]));
			} catch (DataSourceException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
			} catch(IllegalArgumentException e){
				throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t, e);
			}
			CArray ca = new CArray(t);
			CHLog.GetLogger().Log(CHLog.Tags.PERSISTANCE, LogLevel.DEBUG, list.size() + " value(s) are being returned", t);
			for (String[] e : list.keySet()) {
				try {
					String key = StringUtils.Join(e, ".").replaceFirst("storage\\.", ""); //Get that junk out of here
					ca.set(new CString(key, t),
							Construct.json_decode(list.get(e), t), t);
				} catch (MarshalException ex) {
					Logger.getLogger(Persistance.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			return ca;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments={GlobalEnv.class})
	@noboilerplate
	public static class has_value extends AbstractFunction {

		public String getName() {
			return "has_value";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "boolean {[namespace, ...,] key} Returns whether or not there is data stored at the specified key in the Persistance database.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IOException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_1_2;
		}

		public Boolean runAsync() {
			return true;
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			try {
				return new CBoolean(env.getEnv(GlobalEnv.class).GetPersistanceNetwork().hasKey(("storage." + GetNamespace(args, null, getName(), t)).split("\\.")), t);
			} catch (DataSourceException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
			} catch(IllegalArgumentException e){
				throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t, e);
			}
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	@api(environments={GlobalEnv.class})
	@noboilerplate
	public static class clear_value extends AbstractFunction {

		public String getName() {
			return "clear_value";
		}

		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		public String docs() {
			return "void {[namespace, ...,] key} Completely removes a value from storage. Calling has_value(key) after this call will return false.";
		}

		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.IOException, ExceptionType.FormatException};
		}

		public boolean isRestricted() {
			return true;
		}

		public CHVersion since() {
			return CHVersion.V3_3_0;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String namespace = GetNamespace(args, null, getName(), t);
			CHLog.GetLogger().Log(CHLog.Tags.PERSISTANCE, LogLevel.DEBUG, "Clearing value: " + namespace, t);
			try {
				environment.getEnv(GlobalEnv.class).GetPersistanceNetwork().clearKey(environment.getEnv(GlobalEnv.class).GetDaemonManager(), ("storage." + namespace).split("\\."));
			} catch (DataSourceException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
			} catch (ReadOnlyException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
			} catch (IOException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, t, ex);
			} catch(IllegalArgumentException e){
				throw new ConfigRuntimeException(e.getMessage(), ExceptionType.FormatException, t, e);
			}
			return new CVoid(t);
		}

		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}

	/**
	 * Generates the namespace for this value, given an array of constructs. If
	 * the entire list of arguments isn't supposed to be part of the namespace,
	 * the value to be excluded may be specified.
	 *
	 * @param args
	 * @param exclude
	 * @return
	 */
	private static String GetNamespace(Construct[] args, Integer exclude, String name, Target t) {
		if (exclude != null && args.length < 2 || exclude == null && args.length < 1) {
			throw new ConfigRuntimeException(name + " was not provided with enough arguments. Check the documentation, and try again.", ExceptionType.InsufficientArgumentsException, t);
		}
		boolean first = true;
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (exclude != null && exclude == i) {
				continue;
			}
			if (!first) {
				b.append(".");
			}
			first = false;
			b.append(args[i].val());
		}
		return b.toString();
	}
}
