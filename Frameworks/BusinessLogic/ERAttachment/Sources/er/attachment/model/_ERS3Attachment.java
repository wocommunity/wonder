// DO NOT EDIT.  Make changes to ERS3Attachment.java instead.
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
public abstract class _ERS3Attachment extends er.attachment.model.ERAttachment {
  public static final String ENTITY_NAME = "ERS3Attachment";

  // Attribute Keys
  public static final ERXKey<Boolean> AVAILABLE = new ERXKey<Boolean>("available");
  public static final ERXKey<String> CONFIGURATION_NAME = new ERXKey<String>("configurationName");
  public static final ERXKey<NSTimestamp> CREATION_DATE = new ERXKey<NSTimestamp>("creationDate");
  public static final ERXKey<Integer> HEIGHT = new ERXKey<Integer>("height");
  public static final ERXKey<String> MIME_TYPE = new ERXKey<String>("mimeType");
  public static final ERXKey<String> ORIGINAL_FILE_NAME = new ERXKey<String>("originalFileName");
  public static final ERXKey<String> OWNER_ID = new ERXKey<String>("ownerID");
  public static final ERXKey<Boolean> PROXIED = new ERXKey<Boolean>("proxied");
  public static final ERXKey<String> S3_PATH = new ERXKey<String>("s3Path");
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
  public static final String S3_PATH_KEY = S3_PATH.key();
  public static final String SIZE_KEY = SIZE.key();
  public static final String STORAGE_TYPE_KEY = STORAGE_TYPE.key();
  public static final String THUMBNAIL_KEY = THUMBNAIL.key();
  public static final String WEB_PATH_KEY = WEB_PATH.key();
  public static final String WIDTH_KEY = WIDTH.key();
  // Relationships
  public static final String CHILDREN_ATTACHMENTS_KEY = CHILDREN_ATTACHMENTS.key();
  public static final String PARENT_ATTACHMENT_KEY = PARENT_ATTACHMENT.key();

  private static Logger LOG = Logger.getLogger(_ERS3Attachment.class);

  public ERS3Attachment localInstanceIn(EOEditingContext editingContext) {
    ERS3Attachment localInstance = (ERS3Attachment)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String s3Path() {
    return (String) storedValueForKey(_ERS3Attachment.S3_PATH_KEY);
  }

  public void setS3Path(String value) {
    if (_ERS3Attachment.LOG.isDebugEnabled()) {
    	_ERS3Attachment.LOG.debug( "updating s3Path from " + s3Path() + " to " + value);
    }
    takeStoredValueForKey(value, _ERS3Attachment.S3_PATH_KEY);
  }


  public static ERS3Attachment createERS3Attachment(EOEditingContext editingContext, Boolean available
, NSTimestamp creationDate
, String mimeType
, String originalFileName
, Boolean proxied
, Integer size
, String webPath
) {
    ERS3Attachment eo = (ERS3Attachment) EOUtilities.createAndInsertInstance(editingContext, _ERS3Attachment.ENTITY_NAME);    
		eo.setAvailable(available);
		eo.setCreationDate(creationDate);
		eo.setMimeType(mimeType);
		eo.setOriginalFileName(originalFileName);
		eo.setProxied(proxied);
		eo.setSize(size);
		eo.setWebPath(webPath);
    return eo;
  }

  public static ERXFetchSpecification<ERS3Attachment> fetchSpecForERS3Attachment() {
    return new ERXFetchSpecification<ERS3Attachment>(_ERS3Attachment.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ERS3Attachment> fetchAllERS3Attachments(EOEditingContext editingContext) {
    return _ERS3Attachment.fetchAllERS3Attachments(editingContext, null);
  }

  public static NSArray<ERS3Attachment> fetchAllERS3Attachments(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERS3Attachment.fetchERS3Attachments(editingContext, null, sortOrderings);
  }

  public static NSArray<ERS3Attachment> fetchERS3Attachments(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERS3Attachment> fetchSpec = new ERXFetchSpecification<ERS3Attachment>(_ERS3Attachment.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERS3Attachment> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ERS3Attachment fetchERS3Attachment(EOEditingContext editingContext, String keyName, Object value) {
    return _ERS3Attachment.fetchERS3Attachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERS3Attachment fetchERS3Attachment(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ERS3Attachment> eoObjects = _ERS3Attachment.fetchERS3Attachments(editingContext, qualifier, null);
    ERS3Attachment eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ERS3Attachment that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERS3Attachment fetchRequiredERS3Attachment(EOEditingContext editingContext, String keyName, Object value) {
    return _ERS3Attachment.fetchRequiredERS3Attachment(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERS3Attachment fetchRequiredERS3Attachment(EOEditingContext editingContext, EOQualifier qualifier) {
    ERS3Attachment eoObject = _ERS3Attachment.fetchERS3Attachment(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ERS3Attachment that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERS3Attachment localInstanceIn(EOEditingContext editingContext, ERS3Attachment eo) {
    ERS3Attachment localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
