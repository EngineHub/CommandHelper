package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
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
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.database.Profiles;
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
public class SQL {

	public static String docs() {
		return "This class of functions provides methods for accessing various SQL servers.";
	}

	@api
	public static class query extends AbstractFunction implements Optimizable{

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.SQLException};
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
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			try {
				Profiles.Profile profile;
				if (args[0] instanceof CArray) {
					Map<String, String> data = new HashMap<String, String>();
					for (String key : ((CArray) args[0]).keySet()) {
						data.put(key, ((CArray) args[0]).get(key).val());
					}
					profile = Profiles.getProfile(data);
				} else {
					Profiles profiles = environment.getEnv(GlobalEnv.class).getSQLProfiles();
					profile = profiles.getProfileById(args[0].val());
				}
				String query = args[1].val();
				Construct[] params = new Construct[args.length - 2];
				for (int i = 2; i < args.length; i++) {
					int index = i - 2;
					params[index] = args[i];
				}
				//Parameters are now all parsed into java objects.
				Connection conn = DriverManager.getConnection(profile.getConnectionString());
				PreparedStatement ps = null;
				try {
					ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
					for (int i = 0; i < params.length; i++) {
						int type = ps.getParameterMetaData().getParameterType(i + 1);
						if (params[i] == null) {
							if (ps.getParameterMetaData().isNullable(i + 1) == ParameterMetaData.parameterNoNulls) {
								throw new ConfigRuntimeException("Parameter " + (i + 1) + " cannot be set to null. Check your parameters and try again.", ExceptionType.SQLException, t);
							} else {
								ps.setNull(i + 1, type);
								continue;
							}
						}
						try {
							if (params[i] instanceof CInt) {
								ps.setLong(i + 1, Static.getInt(params[i], t));
							} else if (params[i] instanceof CDouble) {
								ps.setDouble(i + 1, (Double) Static.getDouble(params[i], t));
							} else if (params[i] instanceof CString) {
								ps.setString(i + 1, (String) params[i].val());
							} else if (params[i] instanceof CByteArray) {
								ps.setBytes(i + 1, ((CByteArray) params[i]).asByteArrayCopy());
							} else if (params[i] instanceof CBoolean) {
								ps.setBoolean(i + 1, Static.getBoolean(params[i]));
							}else{
								throw new ConfigRuntimeException("The type " + params[i].getClass().getSimpleName() 
										+ " of parameter " + (i + 1) + " is not supported."
										, ExceptionType.CastException, t);
							}
						} catch (ClassCastException ex) {
							throw new ConfigRuntimeException("Could not cast parameter " + (i + 1) + " to "
									+ ps.getParameterMetaData().getParameterTypeName(i + 1) + " from " 
									+ params[i].getClass().getSimpleName() + "."
									, ExceptionType.CastException, t, ex);
						}
					}
					boolean isResultSet = ps.execute();
					if (isResultSet) {
						//Result set
						CArray ret = new CArray(t);
						ResultSetMetaData md = ps.getMetaData();
						ResultSet rs = ps.getResultSet();
						while (rs.next()) {
							CArray row = new CArray(t);
							for (int i = 1; i <= md.getColumnCount(); i++) {
								Construct value;
								int columnType = md.getColumnType(i);
								if (columnType == Types.INTEGER 
										|| columnType == Types.TINYINT
										|| columnType == Types.SMALLINT
										|| columnType == Types.BIGINT) {
									value = new CInt(rs.getLong(i), t);
								} else if (columnType == Types.FLOAT
										|| columnType == Types.DOUBLE
										|| columnType == Types.REAL
										|| columnType == Types.DECIMAL
										|| columnType == Types.NUMERIC) {
									value = new CDouble(rs.getDouble(i), t);
								} else if (columnType == Types.VARCHAR
										|| columnType == Types.CHAR 
										|| columnType == Types.LONGVARCHAR) {
									value = new CString(rs.getString(i), t);
								} else if (columnType == Types.BLOB 
										|| columnType == Types.BINARY 
										|| columnType == Types.VARBINARY 
										|| columnType == Types.LONGVARBINARY) {
									value = CByteArray.wrap(rs.getBytes(i), t);
								} else if (columnType == Types.DATE
										|| columnType == Types.TIME
										|| columnType == Types.TIMESTAMP) {
									if (md.getColumnTypeName(i).equals("YEAR")){
										value = new CInt(rs.getLong(i), t);
									} else {
										value = new CInt(rs.getTimestamp(i).getTime(), t);
									}
								} else if (columnType == Types.BOOLEAN
										|| columnType == Types.BIT) {
									value = new CBoolean(rs.getBoolean(i), t);
								} else {
									throw new ConfigRuntimeException("SQL returned a unhandled column type " 
											+ md.getColumnTypeName(i) + " for column " + md.getColumnName(i) + "." 
											, ExceptionType.CastException, t);
								}
								row.set(md.getColumnName(i), value, t);
							}
							ret.push(row);
						}
						return ret;
					} else {
						ResultSet rs = ps.getGeneratedKeys();
						if (rs.next()) {
						//This was an insert or something that returned generated keys. So we return
							//that here.
							return new CInt(rs.getInt(1), t);
						}
						//Update count. Just return null.
						return new CNull(t);
					}
				} finally {
					if (ps != null) {
						ps.close();
					}
					if (conn != null) { 
						conn.close();
					}
				}
			} catch (Profiles.InvalidProfileException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.SQLException, t, ex);
			} catch (SQLException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.SQLException, t, ex);
			}
		}

		@Override
		public ParseTree optimizeDynamic(Target t, List<ParseTree> children) throws ConfigCompileException, ConfigRuntimeException {
			//We can check 2 things here, one, that the statement isn't dynamic, and if not, then
			//2, that the parameter count matches the ? count. No checks can be done for typing,
			//without making a connection to the db though, so we won't do that here.
			Construct queryData = children.get(1).getData();
			if(queryData instanceof CFunction){
				//If it's a concat or sconcat, warn them that this is bad
				if("sconcat".equals(queryData.val()) || "concat".equals(queryData.val())){
					CHLog.GetLogger().w(CHLog.Tags.COMPILER, "Use of concatenated query detected! This"
							+ " is very bad practice, and could lead to SQL injection vulnerabilities"
							+ " in your code. It is highly recommended that you use prepared queries,"
							+ " which ensure that your parameters are properly escaped.", t);
				}
			} else if(queryData instanceof CString){
				//It's a hard coded query, so we can double check parameter lengths
				int count = 0;
				for(char c : queryData.val().toCharArray()){
					if(c == '?'){
						count++;
					}
				}
				//-2 accounts for the profile data and query
				if(children.size() - 2 != count){
					throw new ConfigCompileException(
							StringUtils.PluralTemplateHelper(count, "%d parameter token was", "%d parameter tokens were") 
									+ " found in the query, but "
									+ StringUtils.PluralTemplateHelper(children.size() - 2, "%d parameter was", "%d parameters were")
									+ " provided to query().", t);
				}
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
			return CHVersion.V3_3_1;
		}

		@Override
		public Set<OptimizationOption> optimizationOptions() {
			return EnumSet.of(OptimizationOption.OPTIMIZE_DYNAMIC);
		}

	}
	
	@api
	public static class query_async extends AbstractFunction {
		
		RunnableQueue queue = new RunnableQueue("MethodScript-queryAsync");
		boolean started = false;
		
		private void startup(){
			if(!started){
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
						started = false;
					}
				});
				started = true;
			}
		}

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.CastException};
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
		public Construct exec(final Target t, final Environment environment, Construct... args) throws ConfigRuntimeException {
			startup();
			Construct arg = args[args.length - 1];
			if(!(arg instanceof CClosure)){
				throw new ConfigRuntimeException("The last argument to " + getName() + " must be a closure.", ExceptionType.CastException, t);
			}
			final CClosure closure = ((CClosure)arg);
			final Construct[] newArgs = new Construct[args.length - 1];
			//Make a new array minus the closure
			System.arraycopy(args, 0, newArgs, 0, newArgs.length);
			queue.invokeLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

				@Override
				public void run() {
					Construct returnValue = new CNull();
					Construct exception = new CNull();
					try{
						returnValue = new query().exec(t, environment, newArgs);
					} catch(ConfigRuntimeException ex){
						exception = ObjectGenerator.GetGenerator().exception(ex, t);
					}
					final Construct cret = returnValue;
					final Construct cex = exception;
					StaticLayer.GetConvertor().runOnMainThreadLater(environment.getEnv(GlobalEnv.class).GetDaemonManager(), new Runnable() {

						@Override
						public void run() {
							closure.execute(new Construct[]{cret, cex});
						}
					});
				}
			});
			return new CVoid(t);
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
			return CHVersion.V3_3_1;
		}
		
	}
}
