// DO NOT EDIT.  Make changes to ERDatabaseAttachment.java instead.
package er.attachment.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _ERDatabaseAttachment extends er.attachment.model.ERAttachment {
  public static final String ENTITY_NAME = "ERDatabaseAttachment";

  // Attribute Keys
  public static final ERXKey<Boolean> AVAILABLE = new ERXKey<Boolean>("available");
  public static final ERXKey<String> CONFIGURATION_NAME = new ERXKey<String>("configurationName");
  public static final ERXKey<NSTimestamp> CREATION_DATE = new ERXKey<NSTimestamp>("creationDate");
  public static final ERXKey<Integer> HEIGHT = new ERXKey<Integer>("height");
  public static final ERXKey<String> MIME_TYPE = new ERXKey<String>("mimeType");
  public static final ERXKey<String> ORIGINAL_FILE_NAME = new ERXKey<String>("originalFileName");
  public static final ERXKey<String> OWNER_ID = new ERXKey<String>("ownerID");
  public static final ERXKey<Boolean> PROXIED = new ERXKey<Boolean>("proxied");
  public static final ERXKey<Integer> SIZE = new ERXKey<Integer>("size");
  public static final ERXKey<NSData> SMALL_DATA = new ERXKey<NSData>("smallData");
  public static final ERXKey<String> STORAGE_TYPE = new ERXKey<String>("storageType");
  public static final ERXKey<String> THUMBNAIL = new ERXKey<String>("thumbnail");
  public static final ERXKey<String> WEB_PATH = new ERXKey<String>("webPath");
  public static final ERXKey<Integer> WIDTH = new ERXKey<Integer>("width");
  // Relationship Keys
  public static final ERXKey<er.attachment.model.ERAttachmentData> ATTACHMENT_DATA = new ERXKey<er.attachment.model.ERAttachmentData>("attachmentData");
  public static final ERXKey<er.attachment.model.ERAttachment> CHILDREN_ATTACHMENTS = new ERXKey<er.attachment.model.ERAttachment>("childrenAttachments");
  public static final ERXKey<er.attachment.model.ERAttachment> PARENT_ATTACHMENT = new ERXKey<er.attachment.model.ERAttachment>("parentAttachment");

  // Attributes
  public static final String AVAILABLE_KEY = AVAILABLE.key();
  public static final String CONFIGURATION_NAME_KEY = CONFIGURATION_NAME.key();
  public static final String CREATION_DATE_KEY = CREATION_DATE.key();
  public static final String HEIGHT_KEY = HEIGHT.key();
  public static final String MIME_TYPE_KEY = MIME_TYPE.key();
  public static final String ORIGINAL_FILE_NAME_KEY = ORIGINAL_FILE_NAME.key();
  public static final String OWNER_ID_KEY = OWNER_ID.key();
  public static final String PROXIED_KEY = PROXIED.key();
  public static final String SIZE_KEY = SIZE.key();
  public static final String SMALL_DATA_KEY = SMALL_DATA.key();
  public static final String STORAGE_TYPE_KEY = STORAGE_TYPE.key();
  public static final String THUMBNAIL_KEY = THUMBNAIL.key();
  public static final String WEB_PATH_KEY = WEB_PATH.key();
  public static final String WIDTH_KEY = WIDTH.key();
  // Relationships
  public static final String ATTACHMENT_DATA_KEY = ATTACHMENT_DATA.key();
  public static final String CHILDREN_ATTACHMENTS_KEY = CHILDREN_ATTACHMENTS.key();
  public static final String PARENT_ATTACHMENT_KEY = PARENT_ATTACHMENT.key();

  private static Logger LOG = LoggerFactory.getLogger(_ERDatabaseAttachment.class);

  public ERDatabaseAttachment localInstanceIn(EOEditingContext editingContext) {
    ERDatabaseAttachment localInstance = (ERDatabaseAttachment)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSData smallData() {
    return (NSData) storedValueForKey(_ERDatabaseAttachment.SMALL_DATA_KEY);
  }

  public void setSmallData(NSData value) {
    if (_ERDatabaseAttachment.LOG.isDebugEnabled()) {
    	_ERDatabaseAttachment.LOG.debug( "updating smallData from " + smallData() + " to " + value);
    }
    takeStoredValueForKey(value, _ERDatabaseAttachment.SMALL_DATA_KEY);
  }

  public er.attachment.model.ERAttachmentData attachmentData() {
    return (er.attachment.model.ERAttachmentData)storedValueForKey(_ERDatabaseAttachment.ATTACHMENT_DATA_KEY);
  }
  
  public void setAttachmentData(er.attachment.model.ERAttachmentData value) {
    takeStoredValueForKey(value, _ERDatabaseAttachment.ATTACHMENT_DATA_KEY);
  }

  public void setAttachmentDataRelationship(er.attachment.model.ERAttachmentData value) {
    if (_ERDatabaseAttachment.LOG.isDebugEnabled()) {
      _ERDatabaseAttachment.LOG.debug("updating attachmentData from " + attachmentData() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setAttachmentData(value);
    }
    else if (value == null) {
    	er.attachment.model.ERAttachmentData oldValue = attachmentData();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _ERDatabaseAttachment.ATTACHMENT_DATA_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _ERDatabaseAttachment.ATTACHMENT_DATA_KEY);
    }
  }
  

  public static ERDatabaseAttachment createERDatabaseAttachment(EOEditingContext editingContext, Boolean available
, NSTimestamp creationDate
, String mimeType
, String originalFileName
, Boolean proxied
, Integer size
, String webPath
) {
    ERDatabaseAttachment eo = (ERDatabaseAttachment) EOUtilities.createAndInsertInstance(editingContext, _ERDatabaseAttachment.ENTITY_NAME);    
		eo.setAvailable(available);
		eo.setCreationDate(creationDate);
		eo.setMimeType(mimeType);
		eo.setOriginalFileName(originalFileName);
		eo.setProxied(proxied);
		eo.setSize(size);
		eo.setWebPath(webPath);
    return eo;
  }

  public static ERXFetchSpecification<ERDatabaseAttachment> fetchSpecForERDatabaseAttachment() {
    return new ERXFetchSpecification<ERDatabaseAttachment>(_ERDatabaseAttachment.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ERDatabaseAttachment> fetchAllERDatabaseAttachments(EOEditingContext editingContext) {
    return _ERDatabaseAttachment.fetchAllERDatabaseAttachments(editingContext, null);
  }

  public static NSArray<ERDatabaseAttachment> fetchAllERDatabaseAttachments(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERDatabaseAttachment.fetchERDatabaseAttachments(editingContext, null, sortOrderings);
  }

  public static NSArray<ERDatabaseAttachment> fetchERDatabaseAttachments(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERDatabaseAttachment> fetchSpec = new ERXFetchSpecification<ERDatabaseAttachment>(_ERDatabaseAttachment.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERDatabaseAttachment> eoObjects = fetchSpec.fetchObjects(editingContext);
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
      eoObject = eoObjects.objectAtIndex(0);
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

  public static ERDatabaseAttachment localInstanceIn(EOEditingContext editingContext, ERDatabaseAttachment eo) {
    ERDatabaseAttachment localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
