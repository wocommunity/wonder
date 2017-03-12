package er.extensions.migration;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXProperties;

/**
 * This stub exists to make memory migrations work.
 * 
 * @author mschrag
 */
public class ERXMemoryMigrationLock implements IERXMigrationLock {
	private NSMutableDictionary<String, Integer> _modelVersions;

	public ERXMemoryMigrationLock() {
		_modelVersions = new NSMutableDictionary<>();
	}

	public void setVersionNumber(EOAdaptorChannel channel, EOModel model, int versionNumber) {
		_modelVersions.setObjectForKey(Integer.valueOf(versionNumber), model.name());
	}

	public boolean tryLock(EOAdaptorChannel channel, EOModel model, String lockOwnerName) {
		return true;
	}

	public void unlock(EOAdaptorChannel channel, EOModel model) {
	}

	public int versionNumber(EOAdaptorChannel channel, EOModel model) {
		Integer versionInteger = _modelVersions.objectForKey(model.name());
		int version;
		if (versionInteger == null) {
			version = initialVersionForModel(model);
		}
		else {
			version = Math.max(versionInteger.intValue(), initialVersionForModel(model));
		}
		return version;
	}

	protected int initialVersionForModel(EOModel model) {
		String modelName = model.name();
		int initialVersion = ERXProperties.intForKeyWithDefault(modelName + ".InitialMigrationVersion", -1);
		return initialVersion;
	}

}
