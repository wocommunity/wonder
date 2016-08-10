// DO NOT EDIT.  Make changes to Movie.java instead.
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
public abstract class _Movie extends  ERXGenericRecord {
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
  public static final ERXKey<er.distribution.example.client.eo.Studio> STUDIO = new ERXKey<er.distribution.example.client.eo.Studio>("studio");

  // Attributes
  public static final String CATEGORY_KEY = CATEGORY.key();
  public static final String DATE_RELEASED_KEY = DATE_RELEASED.key();
  public static final String POSTER_NAME_KEY = POSTER_NAME.key();
  public static final String RATED_KEY = RATED.key();
  public static final String REVENUE_KEY = REVENUE.key();
  public static final String TITLE_KEY = TITLE.key();
  public static final String TRAILER_NAME_KEY = TRAILER_NAME.key();
  // Relationships
  public static final String STUDIO_KEY = STUDIO.key();

  private static Logger LOG = Logger.getLogger(_Movie.class);

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

  public er.distribution.example.client.eo.Studio studio() {
    return (er.distribution.example.client.eo.Studio)storedValueForKey(_Movie.STUDIO_KEY);
  }
  
  public void setStudio(er.distribution.example.client.eo.Studio value) {
    takeStoredValueForKey(value, _Movie.STUDIO_KEY);
  }

  public void setStudioRelationship(er.distribution.example.client.eo.Studio value) {
    if (_Movie.LOG.isDebugEnabled()) {
      _Movie.LOG.debug("updating studio from " + studio() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setStudio(value);
    }
    else if (value == null) {
    	er.distribution.example.client.eo.Studio oldValue = studio();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Movie.STUDIO_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Movie.STUDIO_KEY);
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
}
