// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Studio.java instead.
package webobjectsexamples.businesslogic.movies.server;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Studio extends er.extensions.eof.ERXGenericRecord {
	public static final String ENTITY_NAME = "Studio";

	// Attributes
	public static final String BUDGET_KEY = "budget";
	public static final ERXKey<java.math.BigDecimal> BUDGET = new ERXKey<java.math.BigDecimal>(BUDGET_KEY);
	public static final String NAME_KEY = "name";
	public static final ERXKey<String> NAME = new ERXKey<String>(NAME_KEY);

	// Relationships
	public static final String MOVIES_KEY = "movies";
	public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Movie> MOVIES = new ERXKey<webobjectsexamples.businesslogic.movies.common.Movie>(MOVIES_KEY);

  private static Logger LOG = Logger.getLogger(_Studio.class);

  public Studio localInstanceIn(EOEditingContext editingContext) {
    Studio localInstance = (Studio)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public java.math.BigDecimal budget() {
    return (java.math.BigDecimal) storedValueForKey("budget");
  }

  public void setBudget(java.math.BigDecimal value) {
    if (_Studio.LOG.isDebugEnabled()) {
    	_Studio.LOG.debug( "updating budget from " + budget() + " to " + value);
    }
    takeStoredValueForKey(value, "budget");
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_Studio.LOG.isDebugEnabled()) {
    	_Studio.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Movie> movies() {
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)storedValueForKey("movies");
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Movie> movies(EOQualifier qualifier) {
    return movies(qualifier, null, false);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Movie> movies(EOQualifier qualifier, boolean fetch) {
    return movies(qualifier, null, fetch);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Movie> movies(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<webobjectsexamples.businesslogic.movies.common.Movie> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.movies.common.Movie.STUDIO_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = webobjectsexamples.businesslogic.movies.common.Movie.fetchMovies(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = movies();
      if (qualifier != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToMovies(webobjectsexamples.businesslogic.movies.common.Movie object) {
    includeObjectIntoPropertyWithKey(object, "movies");
  }

  public void removeFromMovies(webobjectsexamples.businesslogic.movies.common.Movie object) {
    excludeObjectFromPropertyWithKey(object, "movies");
  }

  public void addToMoviesRelationship(webobjectsexamples.businesslogic.movies.common.Movie object) {
    if (_Studio.LOG.isDebugEnabled()) {
      _Studio.LOG.debug("adding " + object + " to movies relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToMovies(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "movies");
    }
  }

  public void removeFromMoviesRelationship(webobjectsexamples.businesslogic.movies.common.Movie object) {
    if (_Studio.LOG.isDebugEnabled()) {
      _Studio.LOG.debug("removing " + object + " from movies relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromMovies(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "movies");
    }
  }

  public webobjectsexamples.businesslogic.movies.common.Movie createMoviesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Movie");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "movies");
    return (webobjectsexamples.businesslogic.movies.common.Movie) eo;
  }

  public void deleteMoviesRelationship(webobjectsexamples.businesslogic.movies.common.Movie object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "movies");
    editingContext().deleteObject(object);
  }

  public void deleteAllMoviesRelationships() {
    Enumeration objects = movies().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteMoviesRelationship((webobjectsexamples.businesslogic.movies.common.Movie)objects.nextElement());
    }
  }


  public static Studio createStudio(EOEditingContext editingContext, java.math.BigDecimal budget
, String name
) {
    Studio eo = (Studio) EOUtilities.createAndInsertInstance(editingContext, _Studio.ENTITY_NAME);    
		eo.setBudget(budget);
		eo.setName(name);
    return eo;
  }

  public static NSArray<Studio> fetchAllStudios(EOEditingContext editingContext) {
    return _Studio.fetchAllStudios(editingContext, null);
  }

  public static NSArray<Studio> fetchAllStudios(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Studio.fetchStudios(editingContext, null, sortOrderings);
  }

  public static NSArray<Studio> fetchStudios(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Studio.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Studio> eoObjects = (NSArray<Studio>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Studio fetchStudio(EOEditingContext editingContext, String keyName, Object value) {
    return _Studio.fetchStudio(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Studio fetchStudio(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Studio> eoObjects = _Studio.fetchStudios(editingContext, qualifier, null);
    Studio eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Studio)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Studio that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Studio fetchRequiredStudio(EOEditingContext editingContext, String keyName, Object value) {
    return _Studio.fetchRequiredStudio(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Studio fetchRequiredStudio(EOEditingContext editingContext, EOQualifier qualifier) {
    Studio eoObject = _Studio.fetchStudio(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Studio that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Studio localInstanceIn(EOEditingContext editingContext, Studio eo) {
    Studio localInstance = (eo == null) ? null : (Studio)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
  public static NSArray<NSDictionary> fetchRawFetchAllStudios(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllStudios", "Studio");
    fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<NSDictionary> fetchRawFetchAllStudios(EOEditingContext editingContext)
  {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllStudios", "Studio");
    return editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
}
