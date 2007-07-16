package er.extensions.migration;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;

/**
 ERX* ModelVersion represents a particular version of an EOModel.
 * 
 * @author mschrag
 */
public class ERXModelVersion {
	private EOModel _model;
	private int _version;

	/**
	 * @param model
	 *            a model
	 * @param version
	 *            the version of that model
	 */
	public ERXModelVersion(EOModel model, int version) {
		_model = model;
		_version = version;
	}

	/**
	 * @param modelName
	 *            the name of a model
	 * @param version
	 *            the version of that model
	 */
	public ERXModelVersion(String modelName, int version) {
		this(EOModelGroup.defaultGroup().modelNamed(modelName), version);
	}

	/**
	 * Returns the model for this version.
	 * 
	 * @return the model for this version
	 */
	public EOModel model() {
		return _model;
	}

	/**
	 * Returns the version dependency for this model.
	 * 
	 * @return the version dependency for this model
	 */
	public int version() {
		return _version;
	}
}
