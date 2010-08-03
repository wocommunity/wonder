// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Review.java instead.
package webobjectsexamples.businesslogic.movies.common;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Review extends er.extensions.eof.ERXGenericRecord {
	public static final String ENTITY_NAME = "Review";

	// Attributes
	public static final String REVIEW_KEY = "review";
	public static final ERXKey<String> REVIEW = new ERXKey<String>(REVIEW_KEY);
	public static final String REVIEWER_KEY = "reviewer";
	public static final ERXKey<String> REVIEWER = new ERXKey<String>(REVIEWER_KEY);

	// Relationships
	public static final String MOVIE_KEY = "movie";
	public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Movie> MOVIE = new ERXKey<webobjectsexamples.businesslogic.movies.common.Movie>(MOVIE_KEY);

  private static Logger LOG = Logger.getLogger(_Review.class);

  public Review localInstanceIn(EOEditingContext editingContext) {
    Review localInstance = (Review)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String review() {
    return (String) storedValueForKey("review");
  }

  public void setReview(String value) {
    if (_Review.LOG.isDebugEnabled()) {
    	_Review.LOG.debug( "updating review from " + review() + " to " + value);
    }
    takeStoredValueForKey(value, "review");
  }

  public String reviewer() {
    return (String) storedValueForKey("reviewer");
  }

  public void setReviewer(String value) {
    if (_Review.LOG.isDebugEnabled()) {
    	_Review.LOG.debug( "updating reviewer from " + reviewer() + " to " + value);
    }
    takeStoredValueForKey(value, "reviewer");
  }

  public webobjectsexamples.businesslogic.movies.common.Movie movie() {
    return (webobjectsexamples.businesslogic.movies.common.Movie)storedValueForKey("movie");
  }
  
  public void setMovie(webobjectsexamples.businesslogic.movies.common.Movie value) {
    takeStoredValueForKey(value, "movie");
  }

  public void setMovieRelationship(webobjectsexamples.businesslogic.movies.common.Movie value) {
    if (_Review.LOG.isDebugEnabled()) {
      _Review.LOG.debug("updating movie from " + movie() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setMovie(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.movies.common.Movie oldValue = movie();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "movie");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "movie");
    }
  }
  

  public static Review createReview(EOEditingContext editingContext, webobjectsexamples.businesslogic.movies.common.Movie movie) {
    Review eo = (Review) EOUtilities.createAndInsertInstance(editingContext, _Review.ENTITY_NAME);    
    eo.setMovieRelationship(movie);
    return eo;
  }

  public static NSArray<Review> fetchAllReviews(EOEditingContext editingContext) {
    return _Review.fetchAllReviews(editingContext, null);
  }

  public static NSArray<Review> fetchAllReviews(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Review.fetchReviews(editingContext, null, sortOrderings);
  }

  public static NSArray<Review> fetchReviews(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Review.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Review> eoObjects = (NSArray<Review>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Review fetchReview(EOEditingContext editingContext, String keyName, Object value) {
    return _Review.fetchReview(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Review fetchReview(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Review> eoObjects = _Review.fetchReviews(editingContext, qualifier, null);
    Review eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Review)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Review that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Review fetchRequiredReview(EOEditingContext editingContext, String keyName, Object value) {
    return _Review.fetchRequiredReview(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Review fetchRequiredReview(EOEditingContext editingContext, EOQualifier qualifier) {
    Review eoObject = _Review.fetchReview(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Review that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Review localInstanceIn(EOEditingContext editingContext, Review eo) {
    Review localInstance = (eo == null) ? null : (Review)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
