// DO NOT EDIT.  Make changes to ERTag.java instead.
package er.taggable.model;

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
public abstract class _ERTag extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "ERTag";

  // Attribute Keys
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys

  // Attributes
  public static final String NAME_KEY = NAME.key();
  // Relationships

  private static Logger LOG = LoggerFactory.getLogger(_ERTag.class);

  public ERTag localInstanceIn(EOEditingContext editingContext) {
    ERTag localInstance = (ERTag)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String name() {
    return (String) storedValueForKey(_ERTag.NAME_KEY);
  }

  public void setName(String value) {
    if (_ERTag.LOG.isDebugEnabled()) {
    	_ERTag.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _ERTag.NAME_KEY);
  }


  public static ERTag createERTag(EOEditingContext editingContext) {
    ERTag eo = (ERTag) EOUtilities.createAndInsertInstance(editingContext, _ERTag.ENTITY_NAME);    
    return eo;
  }

  public static ERXFetchSpecification<ERTag> fetchSpec() {
    return new ERXFetchSpecification<ERTag>(_ERTag.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ERTag> fetchAllERTags(EOEditingContext editingContext) {
    return _ERTag.fetchAllERTags(editingContext, null);
  }

  public static NSArray<ERTag> fetchAllERTags(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ERTag.fetchERTags(editingContext, null, sortOrderings);
  }

  public static NSArray<ERTag> fetchERTags(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ERTag> fetchSpec = new ERXFetchSpecification<ERTag>(_ERTag.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ERTag> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ERTag fetchERTag(EOEditingContext editingContext, String keyName, Object value) {
    return _ERTag.fetchERTag(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERTag fetchERTag(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ERTag> eoObjects = _ERTag.fetchERTags(editingContext, qualifier, null);
    ERTag eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ERTag that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERTag fetchRequiredERTag(EOEditingContext editingContext, String keyName, Object value) {
    return _ERTag.fetchRequiredERTag(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ERTag fetchRequiredERTag(EOEditingContext editingContext, EOQualifier qualifier) {
    ERTag eoObject = _ERTag.fetchERTag(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ERTag that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ERTag localInstanceIn(EOEditingContext editingContext, ERTag eo) {
    ERTag localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
