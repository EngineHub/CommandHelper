package com.laytonsmith.persistence;

import com.laytonsmith.core.MSVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.net.URI;

/**
 *
 *
 */
//@datasource("xml")
public class XMLDataSource extends StringSerializableDataSource {

	private XMLDataSource() {

	}

	public XMLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	@Override
	protected void populateModel(String data) throws DataSourceException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected String serializeModel() {
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
		return "XML {xml://path/to/xml/file.xml} --";
	}

	@Override
	public MSVersion since() {
		return MSVersion.V0_0_0;
	}
}
