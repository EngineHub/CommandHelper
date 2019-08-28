package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.core;
import com.laytonsmith.annotations.seealso;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.ProfilesImpl;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.CompilerWarning;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CRESQLException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.database.SQLProfile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@core
public class SQL {

	public static String docs() {
		return "This class of functions provides methods for accessing various SQL servers.";
	}

	@api
	@seealso({unsafe_query.class, query_async.class, com.laytonsmith.tools.docgen.templates.SQL.class,
		com.laytonsmith.tools.docgen.templates.Profiles.class})
	public static class query extends AbstractFunction implements Optimizable {

		private final boolean doWarn;

		public query() {
			this(true);
		}

		protected query(boolean doWarn) {
			this.doWarn = doWarn;
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRESQLException.class};
		}

		@Override
		public boolean isRestricted() {
			return true;
		}

		@Override
		public Boolean runAsync() {
			return null;
		}

		private static final Object CONNECTION_POOL_LOCK = new Object();
		private static Map<String, Connection> connectionPool = null;
		private static final boolean USE_CONNECTION_POOL = true;

		private Connection getConnection(String connectionString, Target t) throws SQLException {
			if(!USE_CONNECTION_POOL) {
				return DriverManager.getConnection(connectionString);
			}
			synchronized(CONNECTION_POOL_LOCK) {
				if(connectionPool == null) {
					connectionPool = new HashMap<>();
					StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

						@Override
						public void run() {
							synchronized(CONNECTION_POOL_LOCK) {
								for(Connection c : connectionPool.values()) {
									try {
										c.close();
									} catch (SQLException ex) {
										//
									}
								}
								connectionPool = null;
							}
						}
					});
				}
				if(!connectionPool.containsKey(connectionString)) {
					connectionPool.put(connectionString, DriverManager.getConnection(connectionString));
				}
				Connection c = connectionPool.get(connectionString);
				boolean isValid = false;
				try {
					isValid = c.isValid(3);
				} catch (AbstractMethodError ex) {
					// isValid is added in later versions. We want to continue working, (as if the connection
					// is not valid) but still warn the user that this will
					// be slower.
					MSLog.GetLogger().Log(MSLog.Tags.GENERAL, LogLevel.WARNING, "SQL driver does not support the \"isValid\" method, which"
							+ " is causing " + Implementation.GetServerType().getBranding() + " to use a slower method.", t);
				}
				if(c.isClosed() || !isValid) {
					// The connection is closed or invalid, so redo it.
					c = DriverManager.getConnection(connectionString);
					connectionPool.put(connectionString, c);
				}
				return c;
			}
		}

		@Override
		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			try {
				Profiles.Profile profile;
				if(args[0].isInstanceOf(CArray.TYPE)) {
					Map<String, String> data = new HashMap<>();
					for(String key : ((CArray) args[0]).stringKeySet()) {
						data.put(key, ((CArray) args[0]).get(key, t).val());
					}
					profile = ProfilesImpl.getProfile(data);
				} else {
					Profiles profiles = environment.getEnv(GlobalEnv.class).getProfiles();
					profile = profiles.getProfileById(args[0].val());
				}
				if(!(profile instanceof SQLProfile)) {
					throw new CRECastException("Profile must be an SQL type profile, but found \"" + profile.getType() + "\"", t);
				}
				String query = args[1].val();
				Mixed[] params = new Mixed[args.length - 2];
				for(int i = 2; i < args.length; i++) {
					int index = i - 2;
					params[index] = args[i];
					if(params[index] instanceof CNull) {
						params[index] = null;
					}
				}
				//Parameters are now all parsed into java objects.
				SQLProfile sqlProfile = (SQLProfile) profile;
				Connection conn = getConnection(sqlProfile.getConnectionString(), t);
				int autogeneratedKeys = Statement.RETURN_GENERATED_KEYS;
				if(!sqlProfile.getAutogeneratedKeys(query)) {
					autogeneratedKeys = Statement.NO_GENERATED_KEYS;
				}
				try(PreparedStatement ps = conn.prepareStatement(query, autogeneratedKeys)) {
					for(int i = 0; i < params.length; i++) {
						int type = ps.getParameterMetaData().getParameterType(i + 1);
						if(params[i] == null) {
							try {
								if(ps.getParameterMetaData().isNullable(i + 1) == ParameterMetaData.parameterNoNulls) {
									throw new CRESQLException("Parameter " + (i + 1) + " cannot be set to null. Check your parameters and try again.", t);
								}
							} catch (SQLException ex) {
								//Ignored. This appears to be able to happen in various cases, but in the case where it *does* work, we don't want
								//to completely disable the feature.
							}
							ps.setNull(i + 1, type);
							continue;
						}
						try {
							if(params[i].isInstanceOf(CInt.TYPE)) {
								ps.setLong(i + 1, Static.getInt(params[i], t));
							} else if(params[i].isInstanceOf(CDouble.TYPE)) {
								ps.setDouble(i + 1, (Double) Static.getDouble(params[i], t));
							} else if(params[i].isInstanceOf(CString.TYPE)) {
								ps.setString(i + 1, (String) params[i].val());
							} else if(params[i].isInstanceOf(CByteArray.TYPE)) {
								ps.setBytes(i + 1, ((CByteArray) params[i]).asByteArrayCopy());
							} else if(params[i].isInstanceOf(CBoolean.TYPE)) {
								ps.setBoolean(i + 1, ArgumentValidation.getBoolean(params[i], t));
							} else {
								throw new CRECastException("The type " + params[i].getClass().getSimpleName()
										+ " of parameter " + (i + 1) + " is not supported.", t);
							}
						} catch (ClassCastException ex) {
							throw new CRECastException("Could not cast parameter " + (i + 1) + " to "
									+ ps.getParameterMetaData().getParameterTypeName(i + 1) + " from "
									+ params[i].getClass().getSimpleName() + ".", t, ex);
						}
					}
					boolean isResultSet = ps.execute();
					if(isResultSet) {
						//Result set
						CArray ret = new CArray(t);
						ResultSetMetaData md = ps.getMetaData();
						ResultSet rs = ps.getResultSet();
						while(rs != null && rs.next()) {
							CArray row = CArray.GetAssociativeArray(t);
							for(int i = 1; i <= md.getColumnCount(); i++) {
								Construct value;
								int columnType = md.getColumnType(i);
								switch(columnType) {
									case Types.INTEGER:
									case Types.TINYINT:
									case Types.SMALLINT:
									case Types.BIGINT:
										value = new CInt(rs.getLong(i), t);
										break;
									case Types.FLOAT:
									case Types.DOUBLE:
									case Types.REAL:
									case Types.DECIMAL:
									case Types.NUMERIC:
										value = new CDouble(rs.getDouble(i), t);
										break;
									case Types.VARCHAR:
									case Types.CHAR:
									case Types.LONGVARCHAR:
										value = new CString(rs.getString(i), t);
										break;
									case Types.BLOB:
									case Types.BINARY:
									case Types.VARBINARY:
									case Types.LONGVARBINARY:
										value = CByteArray.wrap(rs.getBytes(i), t);
										break;
									case Types.DATE:
									case Types.TIME:
									case Types.TIMESTAMP:
										if(md.getColumnTypeName(i).equals("YEAR")) {
											value = new CInt(rs.getLong(i), t);
										} else if(rs.getTimestamp(i) == null) {
											// Normally we check for null below, but since
											// we want to dereference the value now, we have
											// to have a specific null check here.
											value = CNull.NULL;
										} else {
											value = new CInt(rs.getTimestamp(i).getTime(), t);
										}
										break;
									case Types.BOOLEAN:
									case Types.BIT:
										value = CBoolean.get(rs.getBoolean(i));
										break;
									default:
										throw new CRECastException("SQL returned a unhandled column type "
												+ md.getColumnTypeName(i) + " for column " + md.getColumnName(i) + ".", t);
								}
								if(rs.wasNull()) {
									// Since mscript can assign null to primitives, we
									// can set it to null regardless of the data type.
									value = CNull.NULL;
								}
								// We *could* use getColumnName here, but if the column has been renamed,
								// for instance SELECT foo AS bar... then we would get "foo" from that. Instead,
								// we use the column label, which in the example, would return "bar", which is what
								// the user will expect in the results.
								row.set(md.getColumnLabel(i), value, t);
							}
							ret.push(row, t);
						}
						return ret;
					} else {
						ResultSet rs = ps.getGeneratedKeys();
						if(rs.next()) {
							//This was an insert or something that returned generated keys. So we return
							//that here.
							return new CInt(rs.getInt(1), t);
						}
						//Update count. Just return null.
						return CNull.NULL;
					}
				} finally {
					if(!USE_CONNECTION_POOL) {
						conn.close();
					}
				}
			} catch (Profiles.InvalidProfileException | SQLException ex) {
				throw new CRESQLException(ex.getMessage(), t, ex);
			}
		}

		@Override
		public ParseTree optimizeDynamic(Target t, Environment env,
				Set<Class<? extends Environment.EnvironmentImpl>> envs,
				List<ParseTree> children, FileOptions fileOptions)
				throws ConfigCompileException, ConfigRuntimeException {
			if(children.size() < 2) {
				throw new ConfigCompileException(getName() + " expects at least 2 arguments", t);
			}
			//We can check 2 things here, one, that the statement isn't dynamic, and if not, then
			//2, that the parameter count matches the ? count. No checks can be done for typing,
			//without making a connection to the db though, so we won't do that here.
			Mixed queryData = children.get(1).getData();
			if(queryData instanceof CFunction) {
				//If it's a concat or sconcat, warn them that this is bad
				if(doWarn && ("sconcat".equals(queryData.val()) || "concat".equals(queryData.val()))) {
					String msg = "Use of concatenated query detected! This"
							+ " is very bad practice, and could lead to SQL injection vulnerabilities"
							+ " in your code. It is highly recommended that you use prepared queries,"
							+ " which ensure that your parameters are properly escaped. If you really"
							+ " must use concatenation, and you promise you know what you're doing, you"
							+ " can use " + new unsafe_query().getName() + "() to supress this warning.";
					env.getEnv(CompilerEnvironment.class).addCompilerWarning(fileOptions,
							new CompilerWarning(msg, t, null));
				}
			} else if(queryData.isInstanceOf(CString.TYPE)) {
				//It's a hard coded query, so we can double check parameter lengths and other things
				String query = queryData.val();
				int count = 0;
				for(char c : query.toCharArray()) {
					if(c == '?') {
						count++;
					}
				}
				//-2 accounts for the profile data and query
				if(children.size() - 2 != count) {
					throw new ConfigCompileException(
							StringUtils.PluralTemplateHelper(count, "%d parameter token was", "%d parameter tokens were")
							+ " found in the query, but "
							+ StringUtils.PluralTemplateHelper(children.size() - 2, "%d parameter was", "%d parameters were")
							+ " provided to query().", t);
				}
				//TODO: Need to get the SQL Profile data from the environment before this can be done.
				//Profile validation will simply ensure that the profile stated is listed in the profiles,
				//and that a connection can in fact be made.
				//Also need to figure out how to validate a prepared statement.
//				if(children.get(0).isConst() && children.get(0).getData().isInstanceOf(CString.TYPE)){
//					if(true){ //Prefs.verifyQueries()
//						String profileName = children.get(0).getData().val();
//						SQLProfiles.Profile profile = null;
//						Connection conn;
//						try {
//							conn = DriverManager.getConnection(profile.getConnectionString());
//							try(PreparedStatement statement = conn.prepareStatement(query)){
//
//							}
//						} catch (SQLException ex) {
//							// Do nothing, but we can't validate this query
//						}
//					}
//				}
			}
			return null;
		}

		@Override
		public String getName() {
			return "query";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return getBundledDocs();
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}

	@api
	@seealso({query.class, com.laytonsmith.tools.docgen.templates.SQL.class,
		com.laytonsmith.tools.docgen.templates.Profiles.class})
	public static class unsafe_query extends query {

		public unsafe_query() {
			super(false);
		}

		@Override
		public String docs() {
			return "mixed {profile, query, [parameters...]} Executes a query, just like the {{function|query}} function, however,"
					+ " no validation is done to ensure that SQL injections might occur (essentially allowing for concatenation directly"
					+ " in the query). Otherwise, functions exactly the same as query().";
		}

		@Override
		public String getName() {
			return "unsafe_query";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

		@Override
		public ExampleScript[] examples() throws ConfigCompileException {
			return null;
		}

	}

	@api
	@seealso({query.class, com.laytonsmith.tools.docgen.templates.SQL.class,
		com.laytonsmith.tools.docgen.templates.Profiles.class})
	public static class query_async extends AbstractFunction {

		RunnableQueue queue = null;
		boolean started = false;

		private synchronized void startup() {
			if(queue == null) {
				queue = new RunnableQueue("MethodScript-queryAsync");
			}
			if(!started) {
				queue.invokeLater(null, new Runnable() {

					@Override
					public void run() {
						//This warms up the queue. Apparently.
					}
				});
				StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

					@Override
					public void run() {
						queue.shutdown();
						queue = null;
						started = false;
					}
				});
				started = true;
			}
		}

		@Override
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class};
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
		public Mixed exec(final Target t, final Environment environment, Mixed... args) throws ConfigRuntimeException {
			startup();
			Mixed arg = args[args.length - 1];
			if(!(arg.isInstanceOf(CClosure.TYPE))) {
				throw new CRECastException("The last argument to " + getName() + " must be a closure.", t);
			}
			final CClosure closure = ((CClosure) arg);
			final Mixed[] newArgs = new Mixed[args.length - 1];
			//Make a new array minus the closure
			System.arraycopy(args, 0, newArgs, 0, newArgs.length);
			queue.invokeLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

				@Override
				public void run() {
					Mixed returnValue = CNull.NULL;
					Mixed exception = CNull.NULL;
					try {
						returnValue = new query().exec(t, environment, newArgs);
					} catch (ConfigRuntimeException ex) {
						exception = ObjectGenerator.GetGenerator().exception(ex, environment, t);
					}
					final Mixed cret = returnValue;
					final Mixed cex = exception;
					StaticLayer.GetConvertor().runOnMainThreadLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

						@Override
						public void run() {
							closure.executeCallable(new Mixed[]{cret, cex});
						}
					});
				}
			});
			return CVoid.VOID;
		}

		@Override
		public String getName() {
			return "query_async";
		}

		@Override
		public Integer[] numArgs() {
			return new Integer[]{Integer.MAX_VALUE};
		}

		@Override
		public String docs() {
			return "void {profile, query, [params...], callback} Asynchronously makes a query to an SQL server."
					+ " The profile, query, and params arguments work the same as {{function|query}}, so see"
					+ " the documentation of that function for details about those parameters."
					+ " The callback should have the following signature: closure(@contents, @exception){ &lt;code&gt; }."
					+ " @contents will contain the return value that query would normally return. If @exception is not"
					+ " null, then an exception occurred during the query, and that exception will be passed in. If"
					+ " @exception is null, then no error occured, though @contents may still be null if query() would"
					+ " otherwise have returned null.";
		}

		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}

	}
}
