// DO NOT EDIT.  Make changes to Director.java instead.
package er.distribution.example.server.eo;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Director extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "Director";

  // Attribute Keys
  // Relationship Keys

  // Attributes
  // Relationships

  private static Logger LOG = Logger.getLogger(_Director.class);

  public Director localInstanceIn(EOEditingContext editingContext) {
    Director localInstance = (Director)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }


  public static Director createDirector(EOEditingContext editingContext) {
    Director eo = (Director) EOUtilities.createAndInsertInstance(editingContext, _Director.ENTITY_NAME);    
    return eo;
  }

  public static ERXFetchSpecification<Director> fetchSpec() {
    return new ERXFetchSpecification<Director>(_Director.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Director> fetchAllDirectors(EOEditingContext editingContext) {
    return _Director.fetchAllDirectors(editingContext, null);
  }

  public static NSArray<Director> fetchAllDirectors(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Director.fetchDirectors(editingContext, null, sortOrderings);
  }

  public static NSArray<Director> fetchDirectors(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Director> fetchSpec = new ERXFetchSpecification<Director>(_Director.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Director> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Director fetchDirector(EOEditingContext editingContext, String keyName, Object value) {
    return _Director.fetchDirector(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Director fetchDirector(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Director> eoObjects = _Director.fetchDirectors(editingContext, qualifier, null);
    Director eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Director that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Director fetchRequiredDirector(EOEditingContext editingContext, String keyName, Object value) {
    return _Director.fetchRequiredDirector(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Director fetchRequiredDirector(EOEditingContext editingContext, EOQualifier qualifier) {
    Director eoObject = _Director.fetchDirector(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Director that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Director localInstanceIn(EOEditingContext editingContext, Director eo) {
    Director localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
