// DO NOT EDIT.  Make changes to ERAttachmentData.java instead.
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
public abstract class _ERAttachmentData extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "ERAttachmentData";

  // Attribute Keys
  public static final ERXKey<NSData> DATA = new ERXKey<NSData>("data");
  // Relationship Keys

  // Attributes
  public static final String DATA_KEY = DATA.key();
  // Relationships

  private static Logger LOG = LoggerFactory.getLogger(_ERAttachmentData.class);

  public ERAttachmentData localInstanceIn(EOEditingContext editingContext) {
    ERAttachmentData localInstance = (ERAttachmentData)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSData data() {
    return (NSData) storedValueForKey(_ERAttachmentData.DATA_KEY);
  }

  public void setData(NSData value) {
    if (_ERAttachmentData.LOG.isDebugEnabled()) {
    	_ERAttachmentData.LOG.debug( "updating data from " + data() + " to " + value);
    }
    takeStoredValueForKey(value, _ERAttachmentData.DATA_KEY);
  }


  public static ERAttachmentData createERAttachmentData(EOEditingContext editingContext) {
    ERAttachmentData eo = (ERAttachmentData) EOUtilities.createAndInsertInstance(editingContext, _ERAttachmentData.ENTITY_NAME);    
    return eo;
  }

  public static ERXFetchSpecification<ERAttachmentData> fetchSpec() {
    return new ERXFetchSpecification<ERAttachmentData>(_ERAttachmentData.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ERAttachmentData> fetchAllERAttachmentDatas(EOEditingContext editingContext) {
    return _ERAttachmentData.fetchAllERAttachmentDatas(editingContext, null);
  }

  public static NSArray<ERAttachmentData> fetchAllERAttachmentDatas(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERAttachmentData.fetchERAttachmentDatas(editingContext, null, sortOrderings);
  }

  public static NSArray<ERAttachmentData> fetchERAttachmentDatas(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERAttachmentData> fetchSpec = new ERXFetchSpecification<ERAttachmentData>(_ERAttachmentData.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERAttachmentData> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ERAttachmentData fetchERAttachmentData(EOEditingContext editingContext, String keyName, Object value) {
    return _ERAttachmentData.fetchERAttachmentData(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERAttachmentData fetchERAttachmentData(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ERAttachmentData> eoObjects = _ERAttachmentData.fetchERAttachmentDatas(editingContext, qualifier, null);
    ERAttachmentData eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ERAttachmentData that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERAttachmentData fetchRequiredERAttachmentData(EOEditingContext editingContext, String keyName, Object value) {
    return _ERAttachmentData.fetchRequiredERAttachmentData(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERAttachmentData fetchRequiredERAttachmentData(EOEditingContext editingContext, EOQualifier qualifier) {
    ERAttachmentData eoObject = _ERAttachmentData.fetchERAttachmentData(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ERAttachmentData that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERAttachmentData localInstanceIn(EOEditingContext editingContext, ERAttachmentData eo) {
    ERAttachmentData localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
