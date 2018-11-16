package com.laytonsmith.persistence;

import com.laytonsmith.core.MSVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.net.URI;

/**
 *
 *
 */
//@datasource("csv")
public class CSVDataSource extends StringSerializableDataSource {

	private CSVDataSource() {

	}

	public CSVDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	@Override
	protected void populateModel(String data) throws DataSourceException {

	}

	@Override
	protected String serializeModel() {
		return "";
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
		return "CSV {csv:///path/to/csv/file.csv} This type stores data"
				+ " in a CSV format. All the pros and cons of yml apply"
				+ " here, but instead of using the yml style to store the"
				+ " data, values are stored as a CSV file. The CSV file"
				+ " must have exactly two entries per line, the key, then"
				+ " the value, then a newline.";
	}

	@Override
	public MSVersion since() {
		return MSVersion.V0_0_0;
	}

}
