
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.hide;
import com.laytonsmith.core.CHVersion;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SQL {
	public static String docs(){
		return "This class of functions provides methods for accessing various SQL servers.";
	}
	
	@api
	@hide("Unfinished. Will be unhidden once it is completed.")
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
				if(args[0] instanceof CArray){
					Map<String, String> data = new HashMap<String, String>();
					for(String key : ((CArray)args[0]).keySet()){
						data.put(key, ((CArray)args[0]).get(key).val());
					}
					profile = Profiles.getProfile(data);
				} else {
					Profiles profiles = environment.getEnv(GlobalEnv.class).getSQLProfiles();
					profile = profiles.getProfileById(args[0].val());
				}
				String query = args[1].val();
				Object[] params = new Object[args.length - 2];
				for(int i = 2; i < args.length; i++){
					int index = i - 2;
					Construct c = args[i];
					if(c instanceof CInt){
						params[index] = ((CInt)c).getInt();
					} else if(c instanceof CDouble){
						params[index] = ((CDouble)c).getDouble();
					} else if(c instanceof CString){
						params[index] = ((CString)c).val();
					} else if(c instanceof CNull){
						params[index] = null;
					} else if(c instanceof CBoolean){
						params[index] = ((CBoolean)c).getBoolean();
					} else {
						throw new ConfigRuntimeException("Unsupported type passed to " + getName(), ExceptionType.SQLException, t);
					}
				}
				//Parameters are now all parsed into java objects.
				//////////////TODO Finish starting here
				return null;
			} catch (Profiles.InvalidProfileException ex) {
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
					+ " null is returned. For UPDATE, DELETE, or schema changing queries, null is always returned. In the event that n SQL query is incorrect"
					+ " or otherwise causes an error, an SQLException is thrown. Only primitive data types are supported for the parameters, arrays are not.";
		}

		@Override
		public Version since() {
			return CHVersion.V3_3_1;
		}
		
	}
}
