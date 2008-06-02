// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ERFileAttachment.java instead.
package er.attachment.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKey;

@SuppressWarnings("all")
public abstract class _ERFileAttachment extends er.attachment.model.ERAttachment {
	public static final String ENTITY_NAME = "ERFileAttachment";

	// Attributes
	public static final String AVAILABLE_KEY = "available";
	public static final ERXKey AVAILABLE = new ERXKey(AVAILABLE_KEY);
	public static final String CONFIGURATION_NAME_KEY = "configurationName";
	public static final ERXKey CONFIGURATION_NAME = new ERXKey(CONFIGURATION_NAME_KEY);
	public static final String CREATION_DATE_KEY = "creationDate";
	public static final ERXKey CREATION_DATE = new ERXKey(CREATION_DATE_KEY);
	public static final String FILESYSTEM_PATH_KEY = "filesystemPath";
	public static final ERXKey FILESYSTEM_PATH = new ERXKey(FILESYSTEM_PATH_KEY);
	public static final String HEIGHT_KEY = "height";
	public static final ERXKey HEIGHT = new ERXKey(HEIGHT_KEY);
	public static final String MIME_TYPE_KEY = "mimeType";
	public static final ERXKey MIME_TYPE = new ERXKey(MIME_TYPE_KEY);
	public static final String ORIGINAL_FILE_NAME_KEY = "originalFileName";
	public static final ERXKey ORIGINAL_FILE_NAME = new ERXKey(ORIGINAL_FILE_NAME_KEY);
	public static final String OWNER_ID_KEY = "ownerID";
	public static final ERXKey OWNER_ID = new ERXKey(OWNER_ID_KEY);
	public static final String PROXIED_KEY = "proxied";
	public static final ERXKey PROXIED = new ERXKey(PROXIED_KEY);
	public static final String SIZE_KEY = "size";
	public static final ERXKey SIZE = new ERXKey(SIZE_KEY);
	public static final String STORAGE_TYPE_KEY = "storageType";
	public static final ERXKey STORAGE_TYPE = new ERXKey(STORAGE_TYPE_KEY);
	public static final String THUMBNAIL_KEY = "thumbnail";
	public static final ERXKey THUMBNAIL = new ERXKey(THUMBNAIL_KEY);
	public static final String WEB_PATH_KEY = "webPath";
	public static final ERXKey WEB_PATH = new ERXKey(WEB_PATH_KEY);
	public static final String WIDTH_KEY = "width";
	public static final ERXKey WIDTH = new ERXKey(WIDTH_KEY);

	// Relationships
	public static final String CHILDREN_ATTACHMENTS_KEY = "childrenAttachments";
	public static final ERXKey CHILDREN_ATTACHMENTS = new ERXKey(CHILDREN_ATTACHMENTS_KEY);
	public static final String PARENT_ATTACHMENT_KEY = "parentAttachment";
	public static final ERXKey PARENT_ATTACHMENT = new ERXKey(PARENT_ATTACHMENT_KEY);

  private static Logger LOG = Logger.getLogger(_ERFileAttachment.class);

  public ERFileAttachment localInstanceIn(EOEditingContext editingContext) {
    ERFileAttachment localInstance = (ERFileAttachment)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String filesystemPath() {
    return (String) storedValueForKey("filesystemPath");
  }

  public void setFilesystemPath(String value) {
    if (_ERFileAttachment.LOG.isDebugEnabled()) {
    	_ERFileAttachment.LOG.debug( "updating filesystemPath from " + filesystemPath() + " to " + value);
    }
    takeStoredValueForKey(value, "filesystemPath");
  }


  public static ERFileAttachment createERFileAttachment(EOEditingContext editingContext, java.lang.Boolean available
, NSTimestamp creationDate
, String mimeType
, String originalFileName
, java.lang.Boolean proxied
, Integer size
, String webPath
) {
    ERFileAttachment eo = (ERFileAttachment) EOUtilities.createAndInsertInstance(editingContext, _ERFileAttachment.ENTITY_NAME);    
		eo.setAvailable(available);
		eo.setCreationDate(creationDate);
		eo.setMimeType(mimeType);
		eo.setOriginalFileName(originalFileName);
		eo.setProxied(proxied);
		eo.setSize(size);
		eo.setWebPath(webPath);
    return eo;
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

  public static ERFileAttachment localInstanceIn(EOEditingContext editingContext, ERFileAttachment eo) {
    ERFileAttachment localInstance = (eo == null) ? null : (ERFileAttachment)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
