// DO NOT EDIT.  Make changes to Voting.java instead.
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
public abstract class _Voting extends er.extensions.eof.ERXGenericRecord {
  public static final String ENTITY_NAME = "Voting";

  // Attribute Keys
  public static final ERXKey<Integer> NUMBER_OF_VOTES = new ERXKey<Integer>("numberOfVotes");
  public static final ERXKey<Double> RUNNING_AVERAGE = new ERXKey<Double>("runningAverage");
  // Relationship Keys
  public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Movie> MOVIE = new ERXKey<webobjectsexamples.businesslogic.movies.common.Movie>("movie");

  // Attributes
  public static final String NUMBER_OF_VOTES_KEY = NUMBER_OF_VOTES.key();
  public static final String RUNNING_AVERAGE_KEY = RUNNING_AVERAGE.key();
  // Relationships
  public static final String MOVIE_KEY = MOVIE.key();

  private static Logger LOG = LoggerFactory.getLogger(_Voting.class);

  public Voting localInstanceIn(EOEditingContext editingContext) {
    Voting localInstance = (Voting)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Integer numberOfVotes() {
    return (Integer) storedValueForKey(_Voting.NUMBER_OF_VOTES_KEY);
  }

  public void setNumberOfVotes(Integer value) {
    if (_Voting.LOG.isDebugEnabled()) {
    	_Voting.LOG.debug( "updating numberOfVotes from " + numberOfVotes() + " to " + value);
    }
    takeStoredValueForKey(value, _Voting.NUMBER_OF_VOTES_KEY);
  }

  public Double runningAverage() {
    return (Double) storedValueForKey(_Voting.RUNNING_AVERAGE_KEY);
  }

  public void setRunningAverage(Double value) {
    if (_Voting.LOG.isDebugEnabled()) {
    	_Voting.LOG.debug( "updating runningAverage from " + runningAverage() + " to " + value);
    }
    takeStoredValueForKey(value, _Voting.RUNNING_AVERAGE_KEY);
  }

  public webobjectsexamples.businesslogic.movies.common.Movie movie() {
    return (webobjectsexamples.businesslogic.movies.common.Movie)storedValueForKey(_Voting.MOVIE_KEY);
  }
  
  public void setMovie(webobjectsexamples.businesslogic.movies.common.Movie value) {
    takeStoredValueForKey(value, _Voting.MOVIE_KEY);
  }

  public void setMovieRelationship(webobjectsexamples.businesslogic.movies.common.Movie value) {
    if (_Voting.LOG.isDebugEnabled()) {
      _Voting.LOG.debug("updating movie from " + movie() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setMovie(value);
    }
    else if (value == null) {
    	webobjectsexamples.businesslogic.movies.common.Movie oldValue = movie();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Voting.MOVIE_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Voting.MOVIE_KEY);
    }
  }
  

  public static Voting createVoting(EOEditingContext editingContext, webobjectsexamples.businesslogic.movies.common.Movie movie) {
    Voting eo = (Voting) EOUtilities.createAndInsertInstance(editingContext, _Voting.ENTITY_NAME);    
    eo.setMovieRelationship(movie);
    return eo;
  }

  public static ERXFetchSpecification<Voting> fetchSpec() {
    return new ERXFetchSpecification<Voting>(_Voting.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Voting> fetchAllVotings(EOEditingContext editingContext) {
    return _Voting.fetchAllVotings(editingContext, null);
  }

  public static NSArray<Voting> fetchAllVotings(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Voting.fetchVotings(editingContext, null, sortOrderings);
  }

  public static NSArray<Voting> fetchVotings(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Voting> fetchSpec = new ERXFetchSpecification<Voting>(_Voting.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Voting> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Voting fetchVoting(EOEditingContext editingContext, String keyName, Object value) {
    return _Voting.fetchVoting(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Voting fetchVoting(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Voting> eoObjects = _Voting.fetchVotings(editingContext, qualifier, null);
    Voting eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Voting that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Voting fetchRequiredVoting(EOEditingContext editingContext, String keyName, Object value) {
    return _Voting.fetchRequiredVoting(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Voting fetchRequiredVoting(EOEditingContext editingContext, EOQualifier qualifier) {
    Voting eoObject = _Voting.fetchVoting(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Voting that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Voting localInstanceIn(EOEditingContext editingContext, Voting eo) {
    Voting localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
