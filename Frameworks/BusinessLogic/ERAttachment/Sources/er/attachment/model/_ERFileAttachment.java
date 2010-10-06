// DO NOT EDIT.  Make changes to ERFileAttachment.java instead.
package er.attachment.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _ERFileAttachment extends er.attachment.model.ERAttachment {
  public static final String ENTITY_NAME = "ERFileAttachment";

  // Attribute Keys
  public static final ERXKey<Boolean> AVAILABLE = new ERXKey<Boolean>("available");
  public static final ERXKey<String> CONFIGURATION_NAME = new ERXKey<String>("configurationName");
  public static final ERXKey<NSTimestamp> CREATION_DATE = new ERXKey<NSTimestamp>("creationDate");
  public static final ERXKey<String> FILESYSTEM_PATH = new ERXKey<String>("filesystemPath");
  public static final ERXKey<Integer> HEIGHT = new ERXKey<Integer>("height");
  public static final ERXKey<String> MIME_TYPE = new ERXKey<String>("mimeType");
  public static final ERXKey<String> ORIGINAL_FILE_NAME = new ERXKey<String>("originalFileName");
  public static final ERXKey<String> OWNER_ID = new ERXKey<String>("ownerID");
  public static final ERXKey<Boolean> PROXIED = new ERXKey<Boolean>("proxied");
  public static final ERXKey<Integer> SIZE = new ERXKey<Integer>("size");
  public static final ERXKey<String> STORAGE_TYPE = new ERXKey<String>("storageType");
  public static final ERXKey<String> THUMBNAIL = new ERXKey<String>("thumbnail");
  public static final ERXKey<String> WEB_PATH = new ERXKey<String>("webPath");
  public static final ERXKey<Integer> WIDTH = new ERXKey<Integer>("width");
  // Relationship Keys
  public static final ERXKey<er.attachment.model.ERAttachment> CHILDREN_ATTACHMENTS = new ERXKey<er.attachment.model.ERAttachment>("childrenAttachments");
  public static final ERXKey<er.attachment.model.ERAttachment> PARENT_ATTACHMENT = new ERXKey<er.attachment.model.ERAttachment>("parentAttachment");

  // Attributes
  public static final String AVAILABLE_KEY = AVAILABLE.key();
  public static final String CONFIGURATION_NAME_KEY = CONFIGURATION_NAME.key();
  public static final String CREATION_DATE_KEY = CREATION_DATE.key();
  public static final String FILESYSTEM_PATH_KEY = FILESYSTEM_PATH.key();
  public static final String HEIGHT_KEY = HEIGHT.key();
  public static final String MIME_TYPE_KEY = MIME_TYPE.key();
  public static final String ORIGINAL_FILE_NAME_KEY = ORIGINAL_FILE_NAME.key();
  public static final String OWNER_ID_KEY = OWNER_ID.key();
  public static final String PROXIED_KEY = PROXIED.key();
  public static final String SIZE_KEY = SIZE.key();
  public static final String STORAGE_TYPE_KEY = STORAGE_TYPE.key();
  public static final String THUMBNAIL_KEY = THUMBNAIL.key();
  public static final String WEB_PATH_KEY = WEB_PATH.key();
  public static final String WIDTH_KEY = WIDTH.key();
  // Relationships
  public static final String CHILDREN_ATTACHMENTS_KEY = CHILDREN_ATTACHMENTS.key();
  public static final String PARENT_ATTACHMENT_KEY = PARENT_ATTACHMENT.key();

  private static Logger LOG = Logger.getLogger(_ERFileAttachment.class);

  public ERFileAttachment localInstanceIn(EOEditingContext editingContext) {
    ERFileAttachment localInstance = (ERFileAttachment)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String filesystemPath() {
    return (String) storedValueForKey(_ERFileAttachment.FILESYSTEM_PATH_KEY);
  }

  public void setFilesystemPath(String value) {
    if (_ERFileAttachment.LOG.isDebugEnabled()) {
    	_ERFileAttachment.LOG.debug( "updating filesystemPath from " + filesystemPath() + " to " + value);
    }
    takeStoredValueForKey(value, _ERFileAttachment.FILESYSTEM_PATH_KEY);
  }


  public static ERFileAttachment createERFileAttachment(EOEditingContext editingContext, Boolean available
, NSTimestamp creationDate
, String mimeType
, String originalFileName
, Boolean proxied
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

  public static ERXFetchSpecification<ERFileAttachment> fetchSpecForERFileAttachment() {
    return new ERXFetchSpecification<ERFileAttachment>(_ERFileAttachment.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ERFileAttachment> fetchAllERFileAttachments(EOEditingContext editingContext) {
    return _ERFileAttachment.fetchAllERFileAttachments(editingContext, null);
  }

  public static NSArray<ERFileAttachment> fetchAllERFileAttachments(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERFileAttachment.fetchERFileAttachments(editingContext, null, sortOrderings);
  }

  public static NSArray<ERFileAttachment> fetchERFileAttachments(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERFileAttachment> fetchSpec = new ERXFetchSpecification<ERFileAttachment>(_ERFileAttachment.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERFileAttachment> eoObjects = fetchSpec.fetchObjects(editingContext);
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
      eoObject = eoObjects.objectAtIndex(0);
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
    ERFileAttachment localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
