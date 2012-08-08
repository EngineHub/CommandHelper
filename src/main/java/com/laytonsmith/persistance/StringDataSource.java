package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.WebUtility;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * For data sources that can input and output strings, this class should be
 * extended.
 *
 * @author lsmith
 */
public abstract class StringDataSource extends AbstractDataSource {
	/**
	 * A reference to the DataSourceModel used by the set and get methods.
	 */
	protected DataSourceModel model;

	protected StringDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	/**
	 * Writes the stringified data to whatever output is associated with
	 * this data source.
	 *
	 * @throws IOException
	 */
	protected void writeData(String data) throws IOException, ReadOnlyException, DataSourceException {
		if (modifiers.contains(DataSourceModifier.READONLY)) {
			throw new ReadOnlyException();
		}
		getConnectionMixin().writeData(data);
	}

	@Override
	public void clearKey(String[] key) throws DataSourceException, ReadOnlyException, IOException {
		model.clearKey(key);
		writeData(serializeModel());
	}

	public void populate() throws DataSourceException {
		String data;
		try {
			data = getConnectionMixin().getData();
		} catch (Exception e) {
			throw new DataSourceException("Could not populate the data source with data", e);
		}
		populateModel(data);
	}

	public List<String[]> keySet() {
		return model.keySet();
	}

	public String get(String[] key, boolean bypassTransient) throws DataSourceException {
		if (!bypassTransient) {
			checkGet();
		}
		return model.get(key);
	}

	public boolean set(String[] key, String value) throws ReadOnlyException, IOException, DataSourceException {
		checkSet();
		String old = get(key, false);
		if ((old == null && value == null) || (old != null && old.equals(value))) {
			return false;
		}
		model.set(key, value);
		//We need to output the model now
		writeData(serializeModel());
		return true;
	}

	/**
	 * Given some data retrieved from who knows where, populate the model.
	 *
	 * @param data
	 * @throws Exception
	 */
	protected abstract void populateModel(String data) throws DataSourceException;

	/**
	 * Serializes the underlying model to a string, which can be written out
	 * to disk/network
	 *
	 * @return
	 */
	protected abstract String serializeModel();

	/**
	 * Subclasses that need a certain type of file to be the "blank" version
	 * of a data model can override this. By default, an empty string is
	 * returned.
	 *
	 * @return
	 */
	protected String getBlankDataModel() {
		return "";
	}
}
