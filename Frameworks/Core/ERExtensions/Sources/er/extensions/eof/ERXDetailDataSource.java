package er.extensions.eof;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

/**
 * Enhanced version of EODetailDataSource to allow setting of an auxiliary
 * qualifier that is applied on the detail objects for filtering as
 * EODatabaseDataSource does.
 * 
 * @author jw
 * @see EODatabaseDataSource
 */
public class ERXDetailDataSource extends EODetailDataSource {
	private static final long serialVersionUID = 1L;
	protected EOQualifier _auxiliaryQualifier;

	/**
	 * Creates and returns a new ERXDetailDataSource object. The new data
	 * source's <code>masterObject</code> is associated with the class
	 * description of the given <code>masterEntityName</code>, and
	 * <code>key</code> is assigned to the new data source's
	 * <code>detailKey</code>. The constructor invokes
	 * <code>qualifyWithRelationshipKey</code> specifying key as the
	 * relationship key and <code>null</code> as the object.
	 * 
	 * @param masterEntityName
	 *            the entity name of the master object
	 * @param key
	 *            keypath defining relationship on the master object
	 */
	public ERXDetailDataSource(String masterEntityName, String key) {
		super(ERXEntityClassDescription.classDescriptionForEntityName(masterEntityName), key);
	}

	/**
	 * Creates and returns a new ERXDetailDataSource object. The new data
	 * source's <code>masterObject</code> is associated with
	 * <code>masterClassDescription</code>, and <code>key</code> is assigned to
	 * the new data source's <code>detailKey</code>. The constructor invokes
	 * <code>qualifyWithRelationshipKey</code> specifying key as the
	 * relationship key and <code>null</code> as the object.
	 * 
	 * @param masterClassDescription
	 *            class description for the master object
	 * @param key
	 *            keypath defining relationship on the master object
	 */
	public ERXDetailDataSource(EOClassDescription masterClassDescription, String key) {
		super(masterClassDescription, key);
	}

	/**
	 * Creates and returns a new ERXDetailDataSource object. The new data source
	 * provides destination objects for the relationship named by a
	 * <code>key</code> from a <code>masterObject</code> in
	 * <code>masterDataSource</code>.
	 * 
	 * @param masterDataSource
	 *            provider of the master objects
	 * @param key
	 *            the String giving the new relationship
	 */
	public ERXDetailDataSource(EODataSource masterDataSource, String key) {
		super(masterDataSource, key);
	}

	/**
	 * Sets the auxiliary qualifier to <code>newQualifier</code>. The auxiliary
	 * qualifier is used to filter the resulting detail objects of the master
	 * object.
	 * 
	 * @param newQualifier
	 *            the new auxiliary EOQualifier
	 */
	public void setAuxiliaryQualifier(EOQualifier newQualifier) {
		_auxiliaryQualifier = newQualifier;
	}

	/**
	 * Returns the auxiliary EOQualifier used to filter the detail objects of
	 * the master object.
	 * 
	 * @return the auxiliary EOQualifier
	 */
	public EOQualifier auxiliaryQualifier() {
		return _auxiliaryQualifier;
	}

	@Override
	public NSArray fetchObjects() {
		NSArray details = super.fetchObjects();
		if (_auxiliaryQualifier != null && details != NSArray.EmptyArray) {
			details = ERXQ.filtered(details, _auxiliaryQualifier);
		}
		return details;
	}

	/**
	 * Provides conformance to EOKeyValueArchiving.
	 * 
	 * @param unarchiver
	 *            the unarchiver object
	 * @return decoded object from the archive
	 */
	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		String entityName = (String) unarchiver.decodeObjectForKey("masterClassDescription");
		String detailKey = (String) unarchiver.decodeObjectForKey("detailKey");
		EOQualifier auxiliaryQualifier = (EOQualifier) unarchiver.decodeObjectForKey("auxiliaryQualifier");
		ERXDetailDataSource dataSource = new ERXDetailDataSource(EOClassDescription.classDescriptionForEntityName(entityName), detailKey);
		dataSource.setAuxiliaryQualifier(auxiliaryQualifier);
		dataSource.qualifyWithRelationshipKey(detailKey, null);
		return dataSource;
	}

	@Override
	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
		archiver.encodeObject(_auxiliaryQualifier, "auxiliaryQualifier");
	}
}
