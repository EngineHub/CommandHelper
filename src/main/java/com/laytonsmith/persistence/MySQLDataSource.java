package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 *
 * @author lsmith
 */
//@datasource("mysql")
public class MySQLDataSource extends AbstractDataSource{
	
	private MySQLDataSource(){
		
	}
	
	public MySQLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);
	}

	@Override
	public Set<String[]> keySet(String[] keyBase) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String get0(String[] key) throws DataSourceException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void populate() throws DataSourceException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DataSourceModifier[] implicitModifiers() {
		return null;
	}

	@Override
	public DataSourceModifier[] invalidModifiers() {
		return null;
	}

	@Override
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

	@Override
	public CHVersion since() {
		return CHVersion.V0_0_0;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}
	
}
