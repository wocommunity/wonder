// DO NOT EDIT.  Make changes to Movie.java instead.
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
public abstract class _Movie extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "Movie";

  // Attribute Keys
  public static final ERXKey<String> CATEGORY = new ERXKey<String>("category");
  public static final ERXKey<NSTimestamp> DATE_RELEASED = new ERXKey<NSTimestamp>("dateReleased");
  public static final ERXKey<String> POSTER_NAME = new ERXKey<String>("posterName");
  public static final ERXKey<String> RATED = new ERXKey<String>("rated");
  public static final ERXKey<java.math.BigDecimal> REVENUE = new ERXKey<java.math.BigDecimal>("revenue");
  public static final ERXKey<String> TITLE = new ERXKey<String>("title");
  public static final ERXKey<String> TRAILER_NAME = new ERXKey<String>("trailerName");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Talent> DIRECTORS = new ERXKey<webobjectsexamples.businesslogic.movies.common.Talent>("directors");
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.PlotSummary> PLOT_SUMMARY = new ERXKey<webobjectsexamples.businesslogic.movies.common.PlotSummary>("plotSummary");
  public static final ERXKey<er.attachment.model.ERAttachment> POSTER = new ERXKey<er.attachment.model.ERAttachment>("poster");
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Review> REVIEWS = new ERXKey<webobjectsexamples.businesslogic.movies.common.Review>("reviews");
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.MovieRole> ROLES = new ERXKey<webobjectsexamples.businesslogic.movies.common.MovieRole>("roles");
  public static final ERXKey<webobjectsexamples.businesslogic.movies.server.Studio> STUDIO = new ERXKey<webobjectsexamples.businesslogic.movies.server.Studio>("studio");
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Voting> VOTING = new ERXKey<webobjectsexamples.businesslogic.movies.common.Voting>("voting");

  // Attributes
  public static final String CATEGORY_KEY = CATEGORY.key();
  public static final String DATE_RELEASED_KEY = DATE_RELEASED.key();
  public static final String POSTER_NAME_KEY = POSTER_NAME.key();
  public static final String RATED_KEY = RATED.key();
  public static final String REVENUE_KEY = REVENUE.key();
  public static final String TITLE_KEY = TITLE.key();
  public static final String TRAILER_NAME_KEY = TRAILER_NAME.key();
  // Relationships
  public static final String DIRECTORS_KEY = DIRECTORS.key();
  public static final String PLOT_SUMMARY_KEY = PLOT_SUMMARY.key();
  public static final String POSTER_KEY = POSTER.key();
  public static final String REVIEWS_KEY = REVIEWS.key();
  public static final String ROLES_KEY = ROLES.key();
  public static final String STUDIO_KEY = STUDIO.key();
  public static final String VOTING_KEY = VOTING.key();

  private static Logger LOG = LoggerFactory.getLogger(_Movie.class);

  public Movie localInstanceIn(EOEditingContext editingContext) {
    Movie localInstance = (Movie)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String category() {
    return (String) storedValueForKey(_Movie.CATEGORY_KEY);
  }

  public void setCategory(String value) {
    if (_Movie.LOG.isDebugEnabled()) {
    	_Movie.LOG.debug( "updating category from " + category() + " to " + value);
    }
    takeStoredValueForKey(value, _Movie.CATEGORY_KEY);
  }

  public NSTimestamp dateReleased() {
    return (NSTimestamp) storedValueForKey(_Movie.DATE_RELEASED_KEY);
  }

  public void setDateReleased(NSTimestamp value) {
    if (_Movie.LOG.isDebugEnabled()) {
    	_Movie.LOG.debug( "updating dateReleased from " + dateReleased() + " to " + value);
    }
    takeStoredValueForKey(value, _Movie.DATE_RELEASED_KEY);
  }

  public String posterName() {
    return (String) storedValueForKey(_Movie.POSTER_NAME_KEY);
  }

  public void setPosterName(String value) {
    if (_Movie.LOG.isDebugEnabled()) {
    	_Movie.LOG.debug( "updating posterName from " + posterName() + " to " + value);
    }
    takeStoredValueForKey(value, _Movie.POSTER_NAME_KEY);
  }

  public String rated() {
    return (String) storedValueForKey(_Movie.RATED_KEY);
  }

  public void setRated(String value) {
    if (_Movie.LOG.isDebugEnabled()) {
    	_Movie.LOG.debug( "updating rated from " + rated() + " to " + value);
    }
    takeStoredValueForKey(value, _Movie.RATED_KEY);
  }

  public java.math.BigDecimal revenue() {
    return (java.math.BigDecimal) storedValueForKey(_Movie.REVENUE_KEY);
  }

  public void setRevenue(java.math.BigDecimal value) {
    if (_Movie.LOG.isDebugEnabled()) {
    	_Movie.LOG.debug( "updating revenue from " + revenue() + " to " + value);
    }
    takeStoredValueForKey(value, _Movie.REVENUE_KEY);
  }

  public String title() {
    return (String) storedValueForKey(_Movie.TITLE_KEY);
  }

  public void setTitle(String value) {
    if (_Movie.LOG.isDebugEnabled()) {
    	_Movie.LOG.debug( "updating title from " + title() + " to " + value);
    }
    takeStoredValueForKey(value, _Movie.TITLE_KEY);
  }

  public String trailerName() {
    return (String) storedValueForKey(_Movie.TRAILER_NAME_KEY);
  }

  public void setTrailerName(String value) {
    if (_Movie.LOG.isDebugEnabled()) {
    	_Movie.LOG.debug( "updating trailerName from " + trailerName() + " to " + value);
    }
    takeStoredValueForKey(value, _Movie.TRAILER_NAME_KEY);
  }

  public webobjectsexamples.businesslogic.movies.common.PlotSummary plotSummary() {
    return (webobjectsexamples.businesslogic.movies.common.PlotSummary)storedValueForKey(_Movie.PLOT_SUMMARY_KEY);
  }
  
  public void setPlotSummary(webobjectsexamples.businesslogic.movies.common.PlotSummary value) {
    takeStoredValueForKey(value, _Movie.PLOT_SUMMARY_KEY);
  }

  public void setPlotSummaryRelationship(webobjectsexamples.businesslogic.movies.common.PlotSummary value) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("updating plotSummary from " + plotSummary() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setPlotSummary(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.movies.common.PlotSummary oldValue = plotSummary();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Movie.PLOT_SUMMARY_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Movie.PLOT_SUMMARY_KEY);
    }
  }
  
  public er.attachment.model.ERAttachment poster() {
    return (er.attachment.model.ERAttachment)storedValueForKey(_Movie.POSTER_KEY);
  }
  
  public void setPoster(er.attachment.model.ERAttachment value) {
    takeStoredValueForKey(value, _Movie.POSTER_KEY);
  }

  public void setPosterRelationship(er.attachment.model.ERAttachment value) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("updating poster from " + poster() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setPoster(value);
    }
    else if (value == null) {
    	er.attachment.model.ERAttachment oldValue = poster();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Movie.POSTER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Movie.POSTER_KEY);
    }
  }
  
  public webobjectsexamples.businesslogic.movies.server.Studio studio() {
    return (webobjectsexamples.businesslogic.movies.server.Studio)storedValueForKey(_Movie.STUDIO_KEY);
  }
  
  public void setStudio(webobjectsexamples.businesslogic.movies.server.Studio value) {
    takeStoredValueForKey(value, _Movie.STUDIO_KEY);
  }

  public void setStudioRelationship(webobjectsexamples.businesslogic.movies.server.Studio value) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("updating studio from " + studio() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setStudio(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.movies.server.Studio oldValue = studio();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Movie.STUDIO_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Movie.STUDIO_KEY);
    }
  }
  
  public webobjectsexamples.businesslogic.movies.common.Voting voting() {
    return (webobjectsexamples.businesslogic.movies.common.Voting)storedValueForKey(_Movie.VOTING_KEY);
  }
  
  public void setVoting(webobjectsexamples.businesslogic.movies.common.Voting value) {
    takeStoredValueForKey(value, _Movie.VOTING_KEY);
  }

  public void setVotingRelationship(webobjectsexamples.businesslogic.movies.common.Voting value) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("updating voting from " + voting() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setVoting(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.movies.common.Voting oldValue = voting();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Movie.VOTING_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Movie.VOTING_KEY);
    }
  }
  
  public NSArray<webobjectsexamples.businesslogic.movies.common.Talent> directors() {
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Talent>)storedValueForKey(_Movie.DIRECTORS_KEY);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Talent> directors(EOQualifier qualifier) {
    return directors(qualifier, null);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Talent> directors(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<webobjectsexamples.businesslogic.movies.common.Talent> results;
      results = directors();
      if (qualifier != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Talent>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Talent>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToDirectors(webobjectsexamples.businesslogic.movies.common.Talent object) {
    includeObjectIntoPropertyWithKey(object, _Movie.DIRECTORS_KEY);
  }

  public void removeFromDirectors(webobjectsexamples.businesslogic.movies.common.Talent object) {
    excludeObjectFromPropertyWithKey(object, _Movie.DIRECTORS_KEY);
  }

  public void addToDirectorsRelationship(webobjectsexamples.businesslogic.movies.common.Talent object) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("adding " + object + " to directors relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToDirectors(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Movie.DIRECTORS_KEY);
    }
  }

  public void removeFromDirectorsRelationship(webobjectsexamples.businesslogic.movies.common.Talent object) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("removing " + object + " from directors relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromDirectors(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Movie.DIRECTORS_KEY);
    }
  }

  public webobjectsexamples.businesslogic.movies.common.Talent createDirectorsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.movies.common.Talent.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Movie.DIRECTORS_KEY);
    return (webobjectsexamples.businesslogic.movies.common.Talent) eo;
  }

  public void deleteDirectorsRelationship(webobjectsexamples.businesslogic.movies.common.Talent object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Movie.DIRECTORS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllDirectorsRelationships() {
    Enumeration<webobjectsexamples.businesslogic.movies.common.Talent> objects = directors().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteDirectorsRelationship(objects.nextElement());
    }
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Review> reviews() {
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Review>)storedValueForKey(_Movie.REVIEWS_KEY);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Review> reviews(EOQualifier qualifier) {
    return reviews(qualifier, null, false);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Review> reviews(EOQualifier qualifier, boolean fetch) {
    return reviews(qualifier, null, fetch);
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.Review> reviews(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<webobjectsexamples.businesslogic.movies.common.Review> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.movies.common.Review.MOVIE_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = webobjectsexamples.businesslogic.movies.common.Review.fetchReviews(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = reviews();
      if (qualifier != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Review>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<webobjectsexamples.businesslogic.movies.common.Review>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToReviews(webobjectsexamples.businesslogic.movies.common.Review object) {
    includeObjectIntoPropertyWithKey(object, _Movie.REVIEWS_KEY);
  }

  public void removeFromReviews(webobjectsexamples.businesslogic.movies.common.Review object) {
    excludeObjectFromPropertyWithKey(object, _Movie.REVIEWS_KEY);
  }

  public void addToReviewsRelationship(webobjectsexamples.businesslogic.movies.common.Review object) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("adding " + object + " to reviews relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToReviews(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Movie.REVIEWS_KEY);
    }
  }

  public void removeFromReviewsRelationship(webobjectsexamples.businesslogic.movies.common.Review object) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("removing " + object + " from reviews relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromReviews(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Movie.REVIEWS_KEY);
    }
  }

  public webobjectsexamples.businesslogic.movies.common.Review createReviewsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.movies.common.Review.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Movie.REVIEWS_KEY);
    return (webobjectsexamples.businesslogic.movies.common.Review) eo;
  }

  public void deleteReviewsRelationship(webobjectsexamples.businesslogic.movies.common.Review object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Movie.REVIEWS_KEY);
  }

  public void deleteAllReviewsRelationships() {
    Enumeration<webobjectsexamples.businesslogic.movies.common.Review> objects = reviews().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteReviewsRelationship(objects.nextElement());
    }
  }

  public NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole> roles() {
    return (NSArray<webobjectsexamples.businesslogic.movies.common.MovieRole>)storedValueForKey(_Movie.ROLES_KEY);
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
      EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.movies.common.MovieRole.MOVIE_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
    includeObjectIntoPropertyWithKey(object, _Movie.ROLES_KEY);
  }

  public void removeFromRoles(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    excludeObjectFromPropertyWithKey(object, _Movie.ROLES_KEY);
  }

  public void addToRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("adding " + object + " to roles relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToRoles(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Movie.ROLES_KEY);
    }
  }

  public void removeFromRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("removing " + object + " from roles relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromRoles(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Movie.ROLES_KEY);
    }
  }

  public webobjectsexamples.businesslogic.movies.common.MovieRole createRolesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( webobjectsexamples.businesslogic.movies.common.MovieRole.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Movie.ROLES_KEY);
    return (webobjectsexamples.businesslogic.movies.common.MovieRole) eo;
  }

  public void deleteRolesRelationship(webobjectsexamples.businesslogic.movies.common.MovieRole object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Movie.ROLES_KEY);
  }

  public void deleteAllRolesRelationships() {
    Enumeration<webobjectsexamples.businesslogic.movies.common.MovieRole> objects = roles().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRolesRelationship(objects.nextElement());
    }
  }


  public static Movie createMovie(EOEditingContext editingContext, String title
) {
    Movie eo = (Movie) EOUtilities.createAndInsertInstance(editingContext, _Movie.ENTITY_NAME);    
		eo.setTitle(title);
    return eo;
  }

  public static ERXFetchSpecification<Movie> fetchSpec() {
    return new ERXFetchSpecification<Movie>(_Movie.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Movie> fetchAllMovies(EOEditingContext editingContext) {
    return _Movie.fetchAllMovies(editingContext, null);
  }

  public static NSArray<Movie> fetchAllMovies(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Movie.fetchMovies(editingContext, null, sortOrderings);
  }

  public static NSArray<Movie> fetchMovies(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Movie> fetchSpec = new ERXFetchSpecification<Movie>(_Movie.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Movie> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Movie fetchMovie(EOEditingContext editingContext, String keyName, Object value) {
    return _Movie.fetchMovie(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Movie fetchMovie(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Movie> eoObjects = _Movie.fetchMovies(editingContext, qualifier, null);
    Movie eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Movie that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Movie fetchRequiredMovie(EOEditingContext editingContext, String keyName, Object value) {
    return _Movie.fetchRequiredMovie(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Movie fetchRequiredMovie(EOEditingContext editingContext, EOQualifier qualifier) {
    Movie eoObject = _Movie.fetchMovie(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Movie that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Movie localInstanceIn(EOEditingContext editingContext, Movie eo) {
    Movie localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
  public static NSArray<webobjectsexamples.businesslogic.movies.common.Movie> fetchDeepFetchOneMovie(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("DeepFetchOneMovie", _Movie.ENTITY_NAME);
    fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<webobjectsexamples.businesslogic.movies.common.Movie> fetchDeepFetchOneMovie(EOEditingContext editingContext,
	Integer myMovieBinding)
  {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("DeepFetchOneMovie", _Movie.ENTITY_NAME);
    NSMutableDictionary<String, Object> bindings = new NSMutableDictionary<String, Object>();
    bindings.takeValueForKey(myMovieBinding, "myMovie");
	fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<webobjectsexamples.businesslogic.movies.common.Movie> fetchQualifierVariable(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("QualifierVariable", _Movie.ENTITY_NAME);
    fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<webobjectsexamples.businesslogic.movies.common.Movie> fetchQualifierVariable(EOEditingContext editingContext,
	java.math.BigDecimal revenueBinding,
	webobjectsexamples.businesslogic.movies.server.Studio studioBinding,
	String studioNameBinding,
	String titleBinding)
  {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("QualifierVariable", _Movie.ENTITY_NAME);
    NSMutableDictionary<String, Object> bindings = new NSMutableDictionary<String, Object>();
    bindings.takeValueForKey(revenueBinding, "revenue");
    bindings.takeValueForKey(studioBinding, "studio");
    bindings.takeValueForKey(studioNameBinding, "studioName");
    bindings.takeValueForKey(titleBinding, "title");
	fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<webobjectsexamples.businesslogic.movies.common.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<NSDictionary> fetchRawFetchAllMovies(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllMovies", _Movie.ENTITY_NAME);
    fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    return (NSArray<NSDictionary>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
  public static NSArray<NSDictionary> fetchRawFetchAllMovies(EOEditingContext editingContext)
  {
    EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllMovies", _Movie.ENTITY_NAME);
    return (NSArray<NSDictionary>)editingContext.objectsWithFetchSpecification(fetchSpec);
  }
  
}
