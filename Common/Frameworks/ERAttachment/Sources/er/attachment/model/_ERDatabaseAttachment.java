// _ERDatabaseAttachment.java
// DO NOT EDIT.  Make changes to ERDatabaseAttachment.java instead.
package er.attachment.model;


import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _ERDatabaseAttachment extends er.attachment.model.ERAttachment {
	public static final String ENTITY_NAME = "ERDatabaseAttachment";

	public static final String SMALL_DATA_KEY = "smallData";

	public static final String ATTACHMENT_DATA_KEY = "attachmentData";

	private static Logger LOG = Logger.getLogger(_ERDatabaseAttachment.class);

	public _ERDatabaseAttachment() {
		super();
	}

	public ERDatabaseAttachment localInstanceOfERDatabaseAttachment(EOEditingContext editingContext) {
		ERDatabaseAttachment localInstance = (ERDatabaseAttachment)EOUtilities.localInstanceOfObject(editingContext, this);
		if (localInstance == null) {
			throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
		}
		return localInstance;
	}


	public NSData smallData() {
		return (NSData) storedValueForKey("smallData");
	}

	public void setSmallData(NSData aValue) {
		if (_ERDatabaseAttachment.LOG.isDebugEnabled()) {
			_ERDatabaseAttachment.LOG.debug( "updating smallData from "+smallData()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "smallData");
	}

	public er.attachment.model.ERAttachmentData attachmentData() {
		return (er.attachment.model.ERAttachmentData)storedValueForKey("attachmentData");
	}

	public void setAttachmentDataRelationship(er.attachment.model.ERAttachmentData aValue) {
		if (_ERDatabaseAttachment.LOG.isDebugEnabled()) {
			_ERDatabaseAttachment.LOG.debug("updating attachmentData from " + attachmentData() + " to " + aValue);
		}
		if (aValue == null) {
			er.attachment.model.ERAttachmentData object = attachmentData();
			if (object != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(object, "attachmentData");
			}
		} else {
	    		addObjectToBothSidesOfRelationshipWithKey(aValue, "attachmentData");
		}
	}

	public static ERDatabaseAttachment createERDatabaseAttachment(EOEditingContext editingContext, String mimeType, String originalFileName, java.lang.Boolean proxied, Integer size, String webPath) {
		ERDatabaseAttachment eoObject = (ERDatabaseAttachment)EOUtilities.createAndInsertInstance(editingContext, _ERDatabaseAttachment.ENTITY_NAME);
		eoObject.setMimeType(mimeType);
		eoObject.setOriginalFileName(originalFileName);
		eoObject.setProxied(proxied);
		eoObject.setSize(size);
		eoObject.setWebPath(webPath);
		return eoObject;
	}

	public static NSArray<ERDatabaseAttachment> fetchAllERDatabaseAttachments(EOEditingContext editingContext) {
		return _ERDatabaseAttachment.fetchAllERDatabaseAttachments(editingContext, null);
	}

	public static NSArray<ERDatabaseAttachment> fetchAllERDatabaseAttachments(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _ERDatabaseAttachment.fetchERDatabaseAttachments(editingContext, null, sortOrderings);
	}

	public static NSArray<ERDatabaseAttachment> fetchERDatabaseAttachments(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_ERDatabaseAttachment.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<ERDatabaseAttachment> eoObjects = (NSArray<ERDatabaseAttachment>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	public static ERDatabaseAttachment fetchERDatabaseAttachment(EOEditingContext editingContext, String keyName, Object value) {
		return _ERDatabaseAttachment.fetchERDatabaseAttachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static ERDatabaseAttachment fetchERDatabaseAttachment(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<ERDatabaseAttachment> eoObjects = _ERDatabaseAttachment.fetchERDatabaseAttachments(editingContext, qualifier, null);
		ERDatabaseAttachment eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		}
		else if (count == 1) {
			eoObject = (ERDatabaseAttachment)eoObjects.objectAtIndex(0);
		}
		else {
			throw new IllegalStateException("There was more than one ERDatabaseAttachment that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}
	
	public static ERDatabaseAttachment fetchRequiredERDatabaseAttachment(EOEditingContext editingContext, String keyName, Object value) {
		return _ERDatabaseAttachment.fetchRequiredERDatabaseAttachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static ERDatabaseAttachment fetchRequiredERDatabaseAttachment(EOEditingContext editingContext, EOQualifier qualifier) {
		ERDatabaseAttachment eoObject = _ERDatabaseAttachment.fetchERDatabaseAttachment(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no ERDatabaseAttachment that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static ERDatabaseAttachment localInstanceOfERDatabaseAttachment(EOEditingContext editingContext, ERDatabaseAttachment eo) {
		ERDatabaseAttachment localInstance = (eo == null) ? null : (ERDatabaseAttachment)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;		
	}

}
