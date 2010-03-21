// $LastChangedRevision: 5810 $ DO NOT EDIT.  Make changes to Talent.java instead.
package webobjectsexamples.businesslogic.movies.common;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _Talent extends er.extensions.eof.ERXGenericRecord {
	public static final String ENTITY_NAME = "Talent";

	// Attributes
	public static final String FIRST_NAME_KEY = "firstName";
	public static final String LAST_NAME_KEY = "lastName";

	// Relationships
	public static final String MOVIES_DIRECTED_KEY = "moviesDirected";
	public static final String PHOTO_KEY = "photo";
	public static final String ROLES_KEY = "roles";

  private static Logger LOG = Logger.getLogger(_Talent.class);

  public Talent localInstanceIn(EOEditingContext editingContext) {
    Talent localInstance = (Talent)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String firstName() {
    return (String) storedValueForKey("firstName");
  }

  public void setFirstName(String value) {
    if (_Talent.LOG.isDebugEnabled()) {
    	_Talent.LOG.debug( "updating firstName from " + firstName() + " to " + value);
    }
    takeStoredValueForKey(value, "firstName");
  }

  public String lastName() {
    return (String) storedValueForKey("lastName");
  }

  public void setLastName(String value) {
    if (_Talent.LOG.isDebugEnabled()) {
    	_Talent.LOG.debug( "updating lastName from " + lastName() + " to " + value);
    }
    takeStoredValueForKey(value, "lastName");
  }

  public webobjectsexamples.businesslogic.movies.common.TalentPhoto photo() {
    return (webobjectsexamples.businesslogic.movies.common.TalentPhoto)storedValueForKey("photo");
  }

  public void setPhotoRelationship(webobjectsexamples.businesslogic.movies.common.TalentPhoto value) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("updating photo from " + photo() + " to " + value);
    }
    if (value == null) {
    	webobjectsexamples.businesslogic.movies.common.TalentPhoto oldValue = photo();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "photo");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "photo");
    }
  }
  
  public NSArray<EOGenericRecord> moviesDirected() {
    return (NSArray<EOGenericRecord>)storedValueForKey("moviesDirected");
  }

  public NSArray<EOGenericRecord> moviesDirected(EOQualifier qualifier) {
    return moviesDirected(qualifier, null);
  }

  public NSArray<EOGenericRecord> moviesDirected(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<EOGenericRecord> results;
      results = moviesDirected();
      if (qualifier != null) {
        results = (NSArray<EOGenericRecord>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<EOGenericRecord>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToMoviesDirectedRelationship(EOGenericRecord object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("adding " + object + " to moviesDirected relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "moviesDirected");
  }

  public void removeFromMoviesDirectedRelationship(EOGenericRecord object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("removing " + object + " from moviesDirected relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "moviesDirected");
  }

  public EOGenericRecord createMoviesDirectedRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Movie");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "moviesDirected");
    return (EOGenericRecord) eo;
  }

  public void deleteMoviesDirectedRelationship(EOGenericRecord object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "moviesDirected");
    editingContext().deleteObject(object);
  }

  public void deleteAllMoviesDirectedRelationships() {
    Enumeration objects = moviesDirected().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteMoviesDirectedRelationship((EOGenericRecord)objects.nextElement());
    }
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole> roles() {
    return (NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole>)storedValueForKey("roles");
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
        NSMutableArray qualifiers = new NSMutableArray();
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
  
  public void addToRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("adding " + object + " to roles relationship");
    }
    addObjectToBothSidesOfRelationshipWithKey(object, "roles");
  }

  public void removeFromRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    if (_Talent.LOG.isDebugEnabled()) {
      _Talent.LOG.debug("removing " + object + " from roles relationship");
    }
    removeObjectFromBothSidesOfRelationshipWithKey(object, "roles");
  }

  public webobjectsexamples.businesslogic.movies.common.MovieRole createRolesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("MovieRole");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "roles");
    return (webobjectsexamples.businesslogic.movies.common.MovieRole) eo;
  }

  public void deleteRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "roles");
    editingContext().deleteObject(object);
  }

  public void deleteAllRolesRelationships() {
    Enumeration objects = roles().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRolesRelationship((webobjectsexamples.businesslogic.movies.common.MovieRole)objects.nextElement());
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

  public static NSArray<Talent> fetchAllTalents(EOEditingContext editingContext) {
    return _Talent.fetchAllTalents(editingContext, null);
  }

  public static NSArray<Talent> fetchAllTalents(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Talent.fetchTalents(editingContext, null, sortOrderings);
  }

  public static NSArray<Talent> fetchTalents(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Talent.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Talent> eoObjects = (NSArray<Talent>)editingContext.objectsWithFetchSpecification(fetchSpec);
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
      eoObject = (Talent)eoObjects.objectAtIndex(0);
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
    Talent localInstance = (eo == null) ? null : (Talent)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
