package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
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
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SQL {

	public static String docs() {
		return "This class of functions provides methods for accessing various SQL servers.";
	}

	@api
	public static class query extends AbstractFunction {

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
							if (type == Types.INTEGER) {
								ps.setInt(i + 1, (Integer) Static.getInt32(params[i], t));
							} else if (type == Types.DOUBLE) {
								ps.setDouble(i + 1, (Double) Static.getDouble(params[i], t));
							} else if (type == Types.VARCHAR || type == Types.CHAR) {
								ps.setString(i + 1, (String) params[i].val());
							} else if (type == Types.BOOLEAN) {
								ps.setBoolean(i + 1, (Boolean) Static.getBoolean(params[i]));
							}
						} catch (ClassCastException ex) {
							throw new ConfigRuntimeException("Could not cast parameter " + (i + 1) + " to "
									+ ps.getParameterMetaData().getParameterTypeName(i + 1) + " from " + params[i].getClass().getSimpleName(),
									ExceptionType.CastException, t, ex);
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
								if (columnType == Types.INTEGER) {
									value = new CInt(rs.getInt(i), t);
								} else if (columnType == Types.DOUBLE) {
									value = new CDouble(rs.getDouble(i), t);
								} else if (columnType == Types.VARCHAR || columnType == Types.CHAR) {
									value = new CString(rs.getString(i), t);
								} else if (columnType == Types.BOOLEAN) {
									value = new CBoolean(rs.getBoolean(i), t);
								} else {
									//For now, continue. Perhaps later, we can do something different.
									continue;
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
					if(conn != null){ 
						conn.close();
					}
					if(ps != null){
						ps.close();
					}
				}
			} catch (Profiles.InvalidProfileException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.SQLException, t, ex);
			} catch (SQLException ex) {
				throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.SQLException, t, ex);
			}
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
			return "mixed {profile, query, [params...]} Executes an SQL query, and returns various data depending on the query type. See the extended information"
					+ " for more details. The profile is either a string, which represents a pre-configured database connection, or an array, which"
					+ " can include dynamic connection information. The query is the SQL query itself, with question marks (?) that represent input parameters,"
					+ " and the params are the input parameters themselves. Compile time checking is done, if possible, to ensure that the correct number of"
					+ " parameters is passed, based on the number of question marks in the query. It is never a good idea to dynamically create the query, so"
					+ " a compiler warning is issued if a query is dynamically being built. See [[CommandHelper/SQL|this page]] for more details about the SQL"
					+ " module. ----"
					+ " For SELECT queries, an array of associative arrays is returned. It is not guaranteed that the arrays themselves are edittable, so this"
					+ " returned array should be considerd \"read-only\". Optimizations will be added later to make this more efficient. For INSERT queries,"
					+ " either null or an integer is returned. If the insert caused an auto-increment to occur, that auto-increment ID is returned. Otherwise,"
					+ " null is returned. For UPDATE, DELETE, or schema changing queries, null is always returned. In the event that an SQL query is incorrect"
					+ " or otherwise causes an error, an SQLException is thrown. Only primitive data types are supported for the parameters, arrays are not.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}

	}
}
