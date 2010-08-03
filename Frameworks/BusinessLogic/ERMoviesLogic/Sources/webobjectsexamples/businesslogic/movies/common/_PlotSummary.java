// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to PlotSummary.java instead.
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
public abstract class _PlotSummary extends er.extensions.eof.ERXGenericRecord {
	public static final String ENTITY_NAME = "PlotSummary";

	// Attributes
	public static final String SUMMARY_KEY = "summary";
	public static final ERXKey<String> SUMMARY = new ERXKey<String>(SUMMARY_KEY);

	// Relationships
	public static final String MOVIE_KEY = "movie";
	public static final ERXKey<webobjectsexamples.businesslogic.movies.common.Movie> MOVIE = new ERXKey<webobjectsexamples.businesslogic.movies.common.Movie>(MOVIE_KEY);

  private static Logger LOG = Logger.getLogger(_PlotSummary.class);

  public PlotSummary localInstanceIn(EOEditingContext editingContext) {
    PlotSummary localInstance = (PlotSummary)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String summary() {
    return (String) storedValueForKey("summary");
  }

  public void setSummary(String value) {
    if (_PlotSummary.LOG.isDebugEnabled()) {
    	_PlotSummary.LOG.debug( "updating summary from " + summary() + " to " + value);
    }
    takeStoredValueForKey(value, "summary");
  }

  public webobjectsexamples.businesslogic.movies.common.Movie movie() {
    return (webobjectsexamples.businesslogic.movies.common.Movie)storedValueForKey("movie");
  }
  
  public void setMovie(webobjectsexamples.businesslogic.movies.common.Movie value) {
    takeStoredValueForKey(value, "movie");
  }

  public void setMovieRelationship(webobjectsexamples.businesslogic.movies.common.Movie value) {
    if (_PlotSummary.LOG.isDebugEnabled()) {
      _PlotSummary.LOG.debug("updating movie from " + movie() + " to " + value);
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
  

  public static PlotSummary createPlotSummary(EOEditingContext editingContext, webobjectsexamples.businesslogic.movies.common.Movie movie) {
    PlotSummary eo = (PlotSummary) EOUtilities.createAndInsertInstance(editingContext, _PlotSummary.ENTITY_NAME);    
    eo.setMovieRelationship(movie);
    return eo;
  }

  public static NSArray<PlotSummary> fetchAllPlotSummaries(EOEditingContext editingContext) {
    return _PlotSummary.fetchAllPlotSummaries(editingContext, null);
  }

  public static NSArray<PlotSummary> fetchAllPlotSummaries(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _PlotSummary.fetchPlotSummaries(editingContext, null, sortOrderings);
  }

  public static NSArray<PlotSummary> fetchPlotSummaries(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_PlotSummary.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<PlotSummary> eoObjects = (NSArray<PlotSummary>)editingContext.objectsWithFetchSpecification(fetchSpec);
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
      eoObject = (PlotSummary)eoObjects.objectAtIndex(0);
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
    PlotSummary localInstance = (eo == null) ? null : (PlotSummary)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
