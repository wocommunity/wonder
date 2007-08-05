// _ERFileAttachment.java
// DO NOT EDIT.  Make changes to ERFileAttachment.java instead.
package er.attachment.model;


import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _ERFileAttachment extends er.attachment.model.ERAttachment {
	public static final String ENTITY_NAME = "ERFileAttachment";

	public static final String FILESYSTEM_PATH_KEY = "filesystemPath";


	private static Logger LOG = Logger.getLogger(_ERFileAttachment.class);

	public _ERFileAttachment() {
		super();
	}

	public ERFileAttachment localInstanceOfERFileAttachment(EOEditingContext editingContext) {
		ERFileAttachment localInstance = (ERFileAttachment)EOUtilities.localInstanceOfObject(editingContext, this);
		if (localInstance == null) {
			throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
		}
		return localInstance;
	}


	public String filesystemPath() {
		return (String) storedValueForKey("filesystemPath");
	}

	public void setFilesystemPath(String aValue) {
		if (_ERFileAttachment.LOG.isDebugEnabled()) {
			_ERFileAttachment.LOG.debug( "updating filesystemPath from "+filesystemPath()+" to "+aValue );
		}
		takeStoredValueForKey(aValue, "filesystemPath");
	}

	public static ERFileAttachment createERFileAttachment(EOEditingContext editingContext, String mimeType, java.lang.Boolean proxied, Integer size, String webPath) {
		ERFileAttachment eoObject = (ERFileAttachment)EOUtilities.createAndInsertInstance(editingContext, _ERFileAttachment.ENTITY_NAME);
		eoObject.setMimeType(mimeType);
		eoObject.setProxied(proxied);
		eoObject.setSize(size);
		eoObject.setWebPath(webPath);
		return eoObject;
	}

	public static NSArray<ERFileAttachment> fetchAllERFileAttachments(EOEditingContext editingContext) {
		return _ERFileAttachment.fetchAllERFileAttachments(editingContext, null);
	}

	public static NSArray<ERFileAttachment> fetchAllERFileAttachments(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _ERFileAttachment.fetchERFileAttachments(editingContext, null, sortOrderings);
	}

	public static NSArray<ERFileAttachment> fetchERFileAttachments(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_ERFileAttachment.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<ERFileAttachment> eoObjects = (NSArray<ERFileAttachment>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	public static ERFileAttachment fetchERFileAttachment(EOEditingContext editingContext, String keyName, Object value) {
		return _ERFileAttachment.fetchERFileAttachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static ERFileAttachment fetchERFileAttachment(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<ERFileAttachment> eoObjects = _ERFileAttachment.fetchERFileAttachments(editingContext, qualifier, null);
		ERFileAttachment eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		}
		else if (count == 1) {
			eoObject = (ERFileAttachment)eoObjects.objectAtIndex(0);
		}
		else {
			throw new IllegalStateException("There was more than one ERFileAttachment that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}
	
	public static ERFileAttachment fetchRequiredERFileAttachment(EOEditingContext editingContext, String keyName, Object value) {
		return _ERFileAttachment.fetchRequiredERFileAttachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static ERFileAttachment fetchRequiredERFileAttachment(EOEditingContext editingContext, EOQualifier qualifier) {
		ERFileAttachment eoObject = _ERFileAttachment.fetchERFileAttachment(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no ERFileAttachment that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static ERFileAttachment localInstanceOfERFileAttachment(EOEditingContext editingContext, ERFileAttachment eo) {
		ERFileAttachment localInstance = (eo == null) ? null : (ERFileAttachment)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;		
	}

}
