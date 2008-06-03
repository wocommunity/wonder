// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Record.java instead.
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKey;

@SuppressWarnings("all")
public abstract class _Record extends  ERXGenericRecord {
	public static final String ENTITY_NAME = "ToOneRelation";

	// Attributes

	// Relationships
	public static final String VALUE_KEY = "value";
	public static final ERXKey<Record> VALUE = new ERXKey<Record>(VALUE_KEY);

  private static Logger LOG = Logger.getLogger(_Record.class);

  public Record localInstanceIn(EOEditingContext editingContext) {
    Record localInstance = (Record)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Record value() {
    return (Record)storedValueForKey("value");
  }
  
  public void setValue(Record value) {
    takeStoredValueForKey(value, "value");
  }

  public void setValueRelationship(Record value) {
    if (_Record.LOG.isDebugEnabled()) {
      _Record.LOG.debug("updating value from " + value() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setValue(value);
    }
    else if (value == null) {
    	Record oldValue = value();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "value");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "value");
    }
  }
  

  public static Record createToOneRelation(EOEditingContext editingContext) {
    Record eo = (Record) EOUtilities.createAndInsertInstance(editingContext, _Record.ENTITY_NAME);    
    return eo;
  }

  public static NSArray<Record> fetchAllToOneRelations(EOEditingContext editingContext) {
    return _Record.fetchAllToOneRelations(editingContext, null);
  }

  public static NSArray<Record> fetchAllToOneRelations(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Record.fetchToOneRelations(editingContext, null, sortOrderings);
  }

  public static NSArray<Record> fetchToOneRelations(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Record.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Record> eoObjects = (NSArray<Record>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Record fetchToOneRelation(EOEditingContext editingContext, String keyName, Object value) {
    return _Record.fetchToOneRelation(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Record fetchToOneRelation(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Record> eoObjects = _Record.fetchToOneRelations(editingContext, qualifier, null);
    Record eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Record)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ToOneRelation that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Record fetchRequiredToOneRelation(EOEditingContext editingContext, String keyName, Object value) {
    return _Record.fetchRequiredToOneRelation(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Record fetchRequiredToOneRelation(EOEditingContext editingContext, EOQualifier qualifier) {
    Record eoObject = _Record.fetchToOneRelation(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ToOneRelation that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Record localInstanceIn(EOEditingContext editingContext, Record eo) {
    Record localInstance = (eo == null) ? null : (Record)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
