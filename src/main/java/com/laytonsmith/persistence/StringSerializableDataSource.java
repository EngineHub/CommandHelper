package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * For data sources that can input and output strings as the complete data model, this class should be extended. The
 * data source may not be written to file, but it is for sure going to be stored (or at least retrievable) from a UTF-8
 * encoded string.
 *
 */
public abstract class StringSerializableDataSource extends AbstractDataSource {

	/**
	 * A reference to the DataSourceModel used by the set and get methods.
	 */
	protected DataSourceModel model;

	/**
	 * When a transaction starts or stops, this is set to true. If this is true, populate will run, then set this to
	 * false, and if this is false, populate will simply return.
	 */
	private boolean doPopulate = true;
	/**
	 * If in a transaction, and we made a change, we know we need to write it out when the transaction finishes.
	 */
	private boolean hasChanges = false;

	protected StringSerializableDataSource() {

	}

	protected StringSerializableDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	/**
	 * Writes the stringified data to whatever output is associated with this data source.
	 *
	 * @throws IOException
	 */
	protected void writeData(DaemonManager dm, String data) throws IOException, ReadOnlyException, DataSourceException {
		getConnectionMixin().writeData(dm, data);
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws DataSourceException, ReadOnlyException, IOException {
		model.clearKey(key);
		writeData(dm, serializeModel());
	}

	@Override
	protected final void startTransaction0(DaemonManager dm) {
		doPopulate = true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * When we rollback, we simply re-populate the data, instead of tracking changes that happened since the transaction
	 * started.
	 *
	 * @param rollback
	 * @throws DataSourceException
	 * @throws IOException
	 */
	@Override
	protected final void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		doPopulate = true;
		if (hasChanges) {
			hasChanges = false;
			if (rollback) {
				populate();
			} else {
				try {
					writeData(dm, serializeModel());
				} catch (ReadOnlyException ex) {
					//This shouldn't happen, because we won't have been allowed to set any 
					Logger.getLogger(StringSerializableDataSource.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	@Override
	public void populate() throws DataSourceException {
		if (inTransaction()) {
			if (doPopulate) {
				doPopulate = false;
			} else {
				return;
			}
		}
		String data;
		try {
			data = getConnectionMixin().getData();
		} catch (DataSourceException | IOException e) {
			throw new DataSourceException("Could not populate the data source (" + uri + ") with data: " + e.getMessage(), e);
		}
		populateModel(data);
	}

	@Override
	public Set<String[]> keySet(String[] keyBase) {
		Set<String[]> keys = new HashSet<>();
		String kb = StringUtils.Join(keyBase, ".");
		for (String[] key : model.keySet()) {
			if (StringUtils.Join(key, ".").startsWith(kb)) {
				keys.add(key);
			}
		}
		return keys;
	}

	@Override
	protected final String get0(String[] key) throws DataSourceException {
		return model.get(key);
	}

	@Override
	protected final boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, IOException, DataSourceException {
		if (modifiers.contains(DataSourceModifier.READONLY)) {
			throw new ReadOnlyException();
		}
		String old = get(key);
		if ((old == null && value == null) || (old != null && old.equals(value))) {
			return false;
		}
		model.set(key, value);
		if (!inTransaction()) {
			//We need to output the model now
			writeData(dm, serializeModel());
		} else {
			hasChanges = true;
		}
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
	 * Serializes the underlying model to a string, which can be written out to disk/network
	 *
	 * @return
	 */
	protected abstract String serializeModel();

	/**
	 * Subclasses that need a certain type of file to be the "blank" version of a data model can override this. By
	 * default, an empty string is returned.
	 *
	 * @return
	 */
	@Override
	protected String getBlankDataModel() {
		return "";
	}

	@Override
	public void disconnect() {
		// By default, we assume that string based data sources don't need disconnecting.
		// If this assumption is bad, the subclass can override this method.
	}

}
