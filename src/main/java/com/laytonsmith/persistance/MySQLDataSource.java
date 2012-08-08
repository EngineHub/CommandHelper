package com.laytonsmith.persistance;

import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 *
 * @author lsmith
 */
@datasource("mysql")
public class MySQLDataSource extends AbstractDataSource{
	
	public MySQLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);
	}

	public List<String[]> keySet() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String get(String[] key, boolean bypassTransient) throws DataSourceException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean set(String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void populate() throws DataSourceException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public DataSourceModifier[] implicitModifiers() {
		return null;
	}

	public DataSourceModifier[] invalidModifiers() {
		return null;
	}

	public String docs() {
		return "MySQL {mysql://[user[:password]@]host[:port]/database/table}"
			+ " This type stores data in a MySQL database. Unlike the"
			+ " file based systems, this is extremely efficient, but"
			+ " requires a database connection already set up to work."
			+ " This also always allows for simultaneous connections"
			+ " from multiple data sink/sources at once, which is not"
			+ " possible without the potential for corruption in file"
			+ " based data sources, without risking either data corruption,"
			+ " or extremely low efficiency. The layout of the table"
			+ " in the database is required to be of a specific format: TODO";
	}

	public CHVersion since() {
		return CHVersion.V0_0_0;
	}
	
}
