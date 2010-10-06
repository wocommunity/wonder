// DO NOT EDIT.  Make changes to ERAttachment.java instead.
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
public abstract class _ERAttachment extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "ERAttachment";

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

  private static Logger LOG = Logger.getLogger(_ERAttachment.class);

  public ERAttachment localInstanceIn(EOEditingContext editingContext) {
    ERAttachment localInstance = (ERAttachment)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Boolean available() {
    return (Boolean) storedValueForKey(_ERAttachment.AVAILABLE_KEY);
  }

  public void setAvailable(Boolean value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating available from " + available() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.AVAILABLE_KEY);
  }

  public String configurationName() {
    return (String) storedValueForKey(_ERAttachment.CONFIGURATION_NAME_KEY);
  }

  public void setConfigurationName(String value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating configurationName from " + configurationName() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.CONFIGURATION_NAME_KEY);
  }

  public NSTimestamp creationDate() {
    return (NSTimestamp) storedValueForKey(_ERAttachment.CREATION_DATE_KEY);
  }

  public void setCreationDate(NSTimestamp value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating creationDate from " + creationDate() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.CREATION_DATE_KEY);
  }

  public Integer height() {
    return (Integer) storedValueForKey(_ERAttachment.HEIGHT_KEY);
  }

  public void setHeight(Integer value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating height from " + height() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.HEIGHT_KEY);
  }

  public String mimeType() {
    return (String) storedValueForKey(_ERAttachment.MIME_TYPE_KEY);
  }

  public void setMimeType(String value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating mimeType from " + mimeType() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.MIME_TYPE_KEY);
  }

  public String originalFileName() {
    return (String) storedValueForKey(_ERAttachment.ORIGINAL_FILE_NAME_KEY);
  }

  public void setOriginalFileName(String value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating originalFileName from " + originalFileName() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.ORIGINAL_FILE_NAME_KEY);
  }

  public String ownerID() {
    return (String) storedValueForKey(_ERAttachment.OWNER_ID_KEY);
  }

  public void setOwnerID(String value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating ownerID from " + ownerID() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.OWNER_ID_KEY);
  }

  public Boolean proxied() {
    return (Boolean) storedValueForKey(_ERAttachment.PROXIED_KEY);
  }

  public void setProxied(Boolean value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating proxied from " + proxied() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.PROXIED_KEY);
  }

  public Integer size() {
    return (Integer) storedValueForKey(_ERAttachment.SIZE_KEY);
  }

  public void setSize(Integer value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating size from " + size() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.SIZE_KEY);
  }

  public String storageType() {
    return (String) storedValueForKey(_ERAttachment.STORAGE_TYPE_KEY);
  }

  public void setStorageType(String value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating storageType from " + storageType() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.STORAGE_TYPE_KEY);
  }

  public String thumbnail() {
    return (String) storedValueForKey(_ERAttachment.THUMBNAIL_KEY);
  }

  public void setThumbnail(String value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating thumbnail from " + thumbnail() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.THUMBNAIL_KEY);
  }

  public String webPath() {
    return (String) storedValueForKey(_ERAttachment.WEB_PATH_KEY);
  }

  public void setWebPath(String value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating webPath from " + webPath() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.WEB_PATH_KEY);
  }

  public Integer width() {
    return (Integer) storedValueForKey(_ERAttachment.WIDTH_KEY);
  }

  public void setWidth(Integer value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
    	_ERAttachment.LOG.debug( "updating width from " + width() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachment.WIDTH_KEY);
  }

  public er.attachment.model.ERAttachment parentAttachment() {
    return (er.attachment.model.ERAttachment)storedValueForKey(_ERAttachment.PARENT_ATTACHMENT_KEY);
  }
  
  public void setParentAttachment(er.attachment.model.ERAttachment value) {
    takeStoredValueForKey(value, _ERAttachment.PARENT_ATTACHMENT_KEY);
  }

  public void setParentAttachmentRelationship(er.attachment.model.ERAttachment value) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
      _ERAttachment.LOG.debug("updating parentAttachment from " + parentAttachment() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setParentAttachment(value);
    }
    else if (value == null) {
    	er.attachment.model.ERAttachment oldValue = parentAttachment();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _ERAttachment.PARENT_ATTACHMENT_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _ERAttachment.PARENT_ATTACHMENT_KEY);
    }
  }
  
  public NSArray<er.attachment.model.ERAttachment> childrenAttachments() {
    return (NSArray<er.attachment.model.ERAttachment>)storedValueForKey(_ERAttachment.CHILDREN_ATTACHMENTS_KEY);
  }

  public NSArray<er.attachment.model.ERAttachment> childrenAttachments(EOQualifier qualifier) {
    return childrenAttachments(qualifier, null, false);
  }

  public NSArray<er.attachment.model.ERAttachment> childrenAttachments(EOQualifier qualifier, boolean fetch) {
    return childrenAttachments(qualifier, null, fetch);
  }

  public NSArray<er.attachment.model.ERAttachment> childrenAttachments(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.attachment.model.ERAttachment> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.attachment.model.ERAttachment.PARENT_ATTACHMENT_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.attachment.model.ERAttachment.fetchERAttachments(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = childrenAttachments();
      if (qualifier != null) {
        results = (NSArray<er.attachment.model.ERAttachment>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.attachment.model.ERAttachment>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToChildrenAttachments(er.attachment.model.ERAttachment object) {
    includeObjectIntoPropertyWithKey(object, _ERAttachment.CHILDREN_ATTACHMENTS_KEY);
  }

  public void removeFromChildrenAttachments(er.attachment.model.ERAttachment object) {
    excludeObjectFromPropertyWithKey(object, _ERAttachment.CHILDREN_ATTACHMENTS_KEY);
  }

  public void addToChildrenAttachmentsRelationship(er.attachment.model.ERAttachment object) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
      _ERAttachment.LOG.debug("adding " + object + " to childrenAttachments relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToChildrenAttachments(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ERAttachment.CHILDREN_ATTACHMENTS_KEY);
    }
  }

  public void removeFromChildrenAttachmentsRelationship(er.attachment.model.ERAttachment object) {
    if (_ERAttachment.LOG.isDebugEnabled()) {
      _ERAttachment.LOG.debug("removing " + object + " from childrenAttachments relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromChildrenAttachments(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ERAttachment.CHILDREN_ATTACHMENTS_KEY);
    }
  }

  public er.attachment.model.ERAttachment createChildrenAttachmentsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.attachment.model.ERAttachment.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ERAttachment.CHILDREN_ATTACHMENTS_KEY);
    return (er.attachment.model.ERAttachment) eo;
  }

  public void deleteChildrenAttachmentsRelationship(er.attachment.model.ERAttachment object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ERAttachment.CHILDREN_ATTACHMENTS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllChildrenAttachmentsRelationships() {
    Enumeration<er.attachment.model.ERAttachment> objects = childrenAttachments().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteChildrenAttachmentsRelationship(objects.nextElement());
    }
  }


  public static ERAttachment createERAttachment(EOEditingContext editingContext, Boolean available
, NSTimestamp creationDate
, String mimeType
, String originalFileName
, Boolean proxied
, Integer size
, String webPath
) {
    ERAttachment eo = (ERAttachment) EOUtilities.createAndInsertInstance(editingContext, _ERAttachment.ENTITY_NAME);    
		eo.setAvailable(available);
		eo.setCreationDate(creationDate);
		eo.setMimeType(mimeType);
		eo.setOriginalFileName(originalFileName);
		eo.setProxied(proxied);
		eo.setSize(size);
		eo.setWebPath(webPath);
    return eo;
  }

  public static ERXFetchSpecification<ERAttachment> fetchSpec() {
    return new ERXFetchSpecification<ERAttachment>(_ERAttachment.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ERAttachment> fetchAllERAttachments(EOEditingContext editingContext) {
    return _ERAttachment.fetchAllERAttachments(editingContext, null);
  }

  public static NSArray<ERAttachment> fetchAllERAttachments(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERAttachment.fetchERAttachments(editingContext, null, sortOrderings);
  }

  public static NSArray<ERAttachment> fetchERAttachments(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERAttachment> fetchSpec = new ERXFetchSpecification<ERAttachment>(_ERAttachment.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERAttachment> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ERAttachment fetchERAttachment(EOEditingContext editingContext, String keyName, Object value) {
    return _ERAttachment.fetchERAttachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERAttachment fetchERAttachment(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ERAttachment> eoObjects = _ERAttachment.fetchERAttachments(editingContext, qualifier, null);
    ERAttachment eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ERAttachment that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERAttachment fetchRequiredERAttachment(EOEditingContext editingContext, String keyName, Object value) {
    return _ERAttachment.fetchRequiredERAttachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERAttachment fetchRequiredERAttachment(EOEditingContext editingContext, EOQualifier qualifier) {
    ERAttachment eoObject = _ERAttachment.fetchERAttachment(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ERAttachment that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERAttachment localInstanceIn(EOEditingContext editingContext, ERAttachment eo) {
    ERAttachment localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
