// DO NOT EDIT.  Make changes to Talent.java instead.
package webobjectsexamples.businesslogic.movies.common;

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
public abstract class _Talent extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "Talent";

  // Attribute Keys
  public static final ERXKey<String> FIRST_NAME = new ERXKey<String>("firstName");
  public static final ERXKey<String> LAST_NAME = new ERXKey<String>("lastName");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Movie> MOVIES_DIRECTED = new ERXKey<webobjectsexamples.businesslogic.movies.common.Movie>("moviesDirected");
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.TalentPhoto> PHOTO = new ERXKey<webobjectsexamples.businesslogic.movies.common.TalentPhoto>("photo");
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.MovieRole> ROLES = new ERXKey<webobjectsexamples.businesslogic.movies.common.MovieRole>("roles");

  // Attributes
  public static final String FIRST_NAME_KEY = FIRST_NAME.key();
  public static final String LAST_NAME_KEY = LAST_NAME.key();
  // Relationships
  public static final String MOVIES_DIRECTED_KEY = MOVIES_DIRECTED.key();
  public static final String PHOTO_KEY = PHOTO.key();
  public static final String ROLES_KEY = ROLES.key();

  private static Logger LOG = LoggerFactory.getLogger(_Talent.class);

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

  public webobjectsexamples.businesslogic.movies.common.TalentPhoto photo() {
    return (webobjectsexamples.businesslogic.movies.common.TalentPhoto)storedValueForKey(_Talent.PHOTO_KEY);
  }
  
  public void setPhoto(webobjectsexamples.businesslogic.movies.common.TalentPhoto value) {
    takeStoredValueForKey(value, _Talent.PHOTO_KEY);
  }

  public void setPhotoRelationship(webobjectsexamples.businesslogic.movies.common.TalentPhoto value) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("updating photo from " + photo() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setPhoto(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.movies.common.TalentPhoto oldValue = photo();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Talent.PHOTO_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Talent.PHOTO_KEY);
    }
  }
  
  public NSArray<webobjectsexamples.businesslogic.movies.common.Movie> moviesDirected() {
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)storedValueForKey(_Talent.MOVIES_DIRECTED_KEY);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Movie> moviesDirected(EOQualifier qualifier) {
    return moviesDirected(qualifier, null);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Movie> moviesDirected(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<webobjectsexamples.businesslogic.movies.common.Movie> results;
      results = moviesDirected();
      if (qualifier != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToMoviesDirected(webobjectsexamples.businesslogic.movies.common.Movie object) {
    includeObjectIntoPropertyWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
  }

  public void removeFromMoviesDirected(webobjectsexamples.businesslogic.movies.common.Movie object) {
    excludeObjectFromPropertyWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
  }

  public void addToMoviesDirectedRelationship(webobjectsexamples.businesslogic.movies.common.Movie object) {
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

  public void removeFromMoviesDirectedRelationship(webobjectsexamples.businesslogic.movies.common.Movie object) {
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

  public webobjectsexamples.businesslogic.movies.common.Movie createMoviesDirectedRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.movies.common.Movie.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Talent.MOVIES_DIRECTED_KEY);
    return (webobjectsexamples.businesslogic.movies.common.Movie) eo;
  }

  public void deleteMoviesDirectedRelationship(webobjectsexamples.businesslogic.movies.common.Movie object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Talent.MOVIES_DIRECTED_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllMoviesDirectedRelationships() {
    Enumeration<webobjectsexamples.businesslogic.movies.common.Movie> objects = moviesDirected().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteMoviesDirectedRelationship(objects.nextElement());
    }
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole> roles() {
    return (NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole>)storedValueForKey(_Talent.ROLES_KEY);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole> roles(EOQualifier qualifier) {
    return roles(qualifier, null, false);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole> roles(EOQualifier qualifier, boolean fetch) {
    return roles(qualifier, null, fetch);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole> roles(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.movies.common.MovieRole.TALENT_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = webobjectsexamples.businesslogic.movies.common.MovieRole.fetchMovieRoles(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = roles();
      if (qualifier != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToRoles(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    includeObjectIntoPropertyWithKey(object, _Talent.ROLES_KEY);
  }

  public void removeFromRoles(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    excludeObjectFromPropertyWithKey(object, _Talent.ROLES_KEY);
  }

  public void addToRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
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

  public void removeFromRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
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

  public webobjectsexamples.businesslogic.movies.common.MovieRole createRolesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.movies.common.MovieRole.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Talent.ROLES_KEY);
    return (webobjectsexamples.businesslogic.movies.common.MovieRole) eo;
  }

  public void deleteRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Talent.ROLES_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllRolesRelationships() {
    Enumeration<webobjectsexamples.businesslogic.movies.common.MovieRole> objects = roles().immutableClone().objectEnumerator();
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
