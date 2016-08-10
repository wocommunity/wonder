// DO NOT EDIT.  Make changes to Talent.java instead.
package er.distribution.example.client.eo;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Talent extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "Talent";

  // Attribute Keys
  public static final ERXKey<String> FIRST_NAME = new ERXKey<String>("firstName");
  public static final ERXKey<String> LAST_NAME = new ERXKey<String>("lastName");
  // Relationship Keys
  public static final ERXKey<er.distribution.example.client.eo.Movie> MOVIES_DIRECTED = new ERXKey<er.distribution.example.client.eo.Movie>("moviesDirected");
  public static final ERXKey<er.distribution.example.client.eo.TalentPhoto> PHOTO = new ERXKey<er.distribution.example.client.eo.TalentPhoto>("photo");
  public static final ERXKey<er.distribution.example.client.eo.MovieRole> ROLES = new ERXKey<er.distribution.example.client.eo.MovieRole>("roles");

  // Attributes
  public static final String FIRST_NAME_KEY = FIRST_NAME.key();
  public static final String LAST_NAME_KEY = LAST_NAME.key();
  // Relationships
  public static final String MOVIES_DIRECTED_KEY = MOVIES_DIRECTED.key();
  public static final String PHOTO_KEY = PHOTO.key();
  public static final String ROLES_KEY = ROLES.key();

  private static Logger LOG = Logger.getLogger(_Talent.class);

  public Talent localInstanceIn(EOEditingContext editingContext) {
    Talent localInstance = (Talent)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String firstName() {
    return (String) storedValueForKey(_Talent.FIRST_NAME_KEY);
  }

  public void setFirstName(String value) {
    if (_Talent.LOG.isDebugEnabled()) {
    	_Talent.LOG.debug( "updating firstName from " + firstName() + " to " + value);
    }
    takeStoredValueForKey(value, _Talent.FIRST_NAME_KEY);
  }

  public String lastName() {
    return (String) storedValueForKey(_Talent.LAST_NAME_KEY);
  }

  public void setLastName(String value) {
    if (_Talent.LOG.isDebugEnabled()) {
    	_Talent.LOG.debug( "updating lastName from " + lastName() + " to " + value);
    }
    takeStoredValueForKey(value, _Talent.LAST_NAME_KEY);
  }

  public er.distribution.example.client.eo.TalentPhoto photo() {
    return (er.distribution.example.client.eo.TalentPhoto)storedValueForKey(_Talent.PHOTO_KEY);
  }
  
  public void setPhoto(er.distribution.example.client.eo.TalentPhoto value) {
    takeStoredValueForKey(value, _Talent.PHOTO_KEY);
  }

  public void setPhotoRelationship(er.distribution.example.client.eo.TalentPhoto value) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("updating photo from " + photo() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setPhoto(value);
    }
    else if (value == null) {
    	er.distribution.example.client.eo.TalentPhoto oldValue = photo();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Talent.PHOTO_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Talent.PHOTO_KEY);
    }
  }
  
  public NSArray<er.distribution.example.client.eo.Movie> moviesDirected() {
    return (NSArray<er.distribution.example.client.eo.Movie>)storedValueForKey(_Talent.MOVIES_DIRECTED_KEY);
  }

  public NSArray<er.distribution.example.client.eo.Movie> moviesDirected(EOQualifier qualifier) {
    return moviesDirected(qualifier, null);
  }

  public NSArray<er.distribution.example.client.eo.Movie> moviesDirected(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.distribution.example.client.eo.Movie> results;
      results = moviesDirected();
      if (qualifier != null) {
        results = (NSArray<er.distribution.example.client.eo.Movie>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.distribution.example.client.eo.Movie>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToMoviesDirected(er.distribution.example.client.eo.Movie object) {
    includeObjectIntoPropertyWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
  }

  public void removeFromMoviesDirected(er.distribution.example.client.eo.Movie object) {
    excludeObjectFromPropertyWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
  }

  public void addToMoviesDirectedRelationship(er.distribution.example.client.eo.Movie object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("adding " + object + " to moviesDirected relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToMoviesDirected(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
    }
  }

  public void removeFromMoviesDirectedRelationship(er.distribution.example.client.eo.Movie object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("removing " + object + " from moviesDirected relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromMoviesDirected(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
    }
  }

  public er.distribution.example.client.eo.Movie createMoviesDirectedRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.distribution.example.client.eo.Movie.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Talent.MOVIES_DIRECTED_KEY);
    return (er.distribution.example.client.eo.Movie) eo;
  }

  public void deleteMoviesDirectedRelationship(er.distribution.example.client.eo.Movie object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllMoviesDirectedRelationships() {
    Enumeration<er.distribution.example.client.eo.Movie> objects = moviesDirected().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteMoviesDirectedRelationship(objects.nextElement());
    }
  }

  public NSArray<er.distribution.example.client.eo.MovieRole> roles() {
    return (NSArray<er.distribution.example.client.eo.MovieRole>)storedValueForKey(_Talent.ROLES_KEY);
  }

  public NSArray<er.distribution.example.client.eo.MovieRole> roles(EOQualifier qualifier) {
    return roles(qualifier, null, false);
  }

  public NSArray<er.distribution.example.client.eo.MovieRole> roles(EOQualifier qualifier, boolean fetch) {
    return roles(qualifier, null, fetch);
  }

  public NSArray<er.distribution.example.client.eo.MovieRole> roles(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.distribution.example.client.eo.MovieRole> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.distribution.example.client.eo.MovieRole.TALENT_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.distribution.example.client.eo.MovieRole.fetchMovieRoles(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = roles();
      if (qualifier != null) {
        results = (NSArray<er.distribution.example.client.eo.MovieRole>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.distribution.example.client.eo.MovieRole>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToRoles(er.distribution.example.client.eo.MovieRole object) {
    includeObjectIntoPropertyWithKey(object, _Talent.ROLES_KEY);
  }

  public void removeFromRoles(er.distribution.example.client.eo.MovieRole object) {
    excludeObjectFromPropertyWithKey(object, _Talent.ROLES_KEY);
  }

  public void addToRolesRelationship(er.distribution.example.client.eo.MovieRole object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("adding " + object + " to roles relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToRoles(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Talent.ROLES_KEY);
    }
  }

  public void removeFromRolesRelationship(er.distribution.example.client.eo.MovieRole object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("removing " + object + " from roles relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromRoles(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Talent.ROLES_KEY);
    }
  }

  public er.distribution.example.client.eo.MovieRole createRolesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.distribution.example.client.eo.MovieRole.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Talent.ROLES_KEY);
    return (er.distribution.example.client.eo.MovieRole) eo;
  }

  public void deleteRolesRelationship(er.distribution.example.client.eo.MovieRole object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Talent.ROLES_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllRolesRelationships() {
    Enumeration<er.distribution.example.client.eo.MovieRole> objects = roles().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRolesRelationship(objects.nextElement());
    }
  }


  public static Talent createTalent(EOEditingContext editingContext, String firstName
, String lastName
) {
    Talent eo = (Talent) EOUtilities.createAndInsertInstance(editingContext, _Talent.ENTITY_NAME);    
		eo.setFirstName(firstName);
		eo.setLastName(lastName);
    return eo;
  }

  public static ERXFetchSpecification<Talent> fetchSpec() {
    return new ERXFetchSpecification<Talent>(_Talent.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Talent> fetchAllTalents(EOEditingContext editingContext) {
    return _Talent.fetchAllTalents(editingContext, null);
  }

  public static NSArray<Talent> fetchAllTalents(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Talent.fetchTalents(editingContext, null, sortOrderings);
  }

  public static NSArray<Talent> fetchTalents(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Talent> fetchSpec = new ERXFetchSpecification<Talent>(_Talent.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Talent> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Talent fetchTalent(EOEditingContext editingContext, String keyName, Object value) {
    return _Talent.fetchTalent(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Talent fetchTalent(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Talent> eoObjects = _Talent.fetchTalents(editingContext, qualifier, null);
    Talent eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Talent that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Talent fetchRequiredTalent(EOEditingContext editingContext, String keyName, Object value) {
    return _Talent.fetchRequiredTalent(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Talent fetchRequiredTalent(EOEditingContext editingContext, EOQualifier qualifier) {
    Talent eoObject = _Talent.fetchTalent(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Talent that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Talent localInstanceIn(EOEditingContext editingContext, Talent eo) {
    Talent localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
