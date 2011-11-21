// DO NOT EDIT.  Make changes to PlotSummary.java instead.
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
public abstract class _PlotSummary extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "PlotSummary";

  // Attribute Keys
  public static final ERXKey<String> SUMMARY = new ERXKey<String>("summary");
  // Relationship Keys
  public static final ERXKey<er.distribution.example.client.eo.Movie> MOVIE = new ERXKey<er.distribution.example.client.eo.Movie>("movie");

  // Attributes
  public static final String SUMMARY_KEY = SUMMARY.key();
  // Relationships
  public static final String MOVIE_KEY = MOVIE.key();

  private static Logger LOG = Logger.getLogger(_PlotSummary.class);

  public PlotSummary localInstanceIn(EOEditingContext editingContext) {
    PlotSummary localInstance = (PlotSummary)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String summary() {
    return (String) storedValueForKey(_PlotSummary.SUMMARY_KEY);
  }

  public void setSummary(String value) {
    if (_PlotSummary.LOG.isDebugEnabled()) {
    	_PlotSummary.LOG.debug( "updating summary from " + summary() + " to " + value);
    }
    takeStoredValueForKey(value, _PlotSummary.SUMMARY_KEY);
  }

  public er.distribution.example.client.eo.Movie movie() {
    return (er.distribution.example.client.eo.Movie)storedValueForKey(_PlotSummary.MOVIE_KEY);
  }
  
  public void setMovie(er.distribution.example.client.eo.Movie value) {
    takeStoredValueForKey(value, _PlotSummary.MOVIE_KEY);
  }

  public void setMovieRelationship(er.distribution.example.client.eo.Movie value) {
    if (_PlotSummary.LOG.isDebugEnabled()) {
      _PlotSummary.LOG.debug("updating movie from " + movie() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setMovie(value);
    }
    else if (value == null) {
    	er.distribution.example.client.eo.Movie oldValue = movie();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _PlotSummary.MOVIE_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _PlotSummary.MOVIE_KEY);
    }
  }
  

  public static PlotSummary createPlotSummary(EOEditingContext editingContext, er.distribution.example.client.eo.Movie movie) {
    PlotSummary eo = (PlotSummary) EOUtilities.createAndInsertInstance(editingContext, _PlotSummary.ENTITY_NAME);    
    eo.setMovieRelationship(movie);
    return eo;
  }

  public static ERXFetchSpecification<PlotSummary> fetchSpec() {
    return new ERXFetchSpecification<PlotSummary>(_PlotSummary.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<PlotSummary> fetchAllPlotSummaries(EOEditingContext editingContext) {
    return _PlotSummary.fetchAllPlotSummaries(editingContext, null);
  }

  public static NSArray<PlotSummary> fetchAllPlotSummaries(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _PlotSummary.fetchPlotSummaries(editingContext, null, sortOrderings);
  }

  public static NSArray<PlotSummary> fetchPlotSummaries(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<PlotSummary> fetchSpec = new ERXFetchSpecification<PlotSummary>(_PlotSummary.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<PlotSummary> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static PlotSummary fetchPlotSummary(EOEditingContext editingContext, String keyName, Object value) {
    return _PlotSummary.fetchPlotSummary(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static PlotSummary fetchPlotSummary(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<PlotSummary> eoObjects = _PlotSummary.fetchPlotSummaries(editingContext, qualifier, null);
    PlotSummary eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one PlotSummary that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static PlotSummary fetchRequiredPlotSummary(EOEditingContext editingContext, String keyName, Object value) {
    return _PlotSummary.fetchRequiredPlotSummary(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static PlotSummary fetchRequiredPlotSummary(EOEditingContext editingContext, EOQualifier qualifier) {
    PlotSummary eoObject = _PlotSummary.fetchPlotSummary(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no PlotSummary that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static PlotSummary localInstanceIn(EOEditingContext editingContext, PlotSummary eo) {
    PlotSummary localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
