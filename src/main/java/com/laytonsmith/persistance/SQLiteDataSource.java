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
@datasource("sqlite")
public class SQLiteDataSource extends AbstractDataSource{
	public SQLiteDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
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
		return "SQLite {sqlite:// TODO} This type store data in a SQLite database."
			+ " All the pros and cons of MySQL apply here.";
	}

	public CHVersion since() {
		return CHVersion.V0_0_0;
	}
}
