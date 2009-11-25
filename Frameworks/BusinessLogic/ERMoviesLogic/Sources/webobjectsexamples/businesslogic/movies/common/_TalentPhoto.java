// $LastChangedRevision: 5810 $ DO NOT EDIT.  Make changes to TalentPhoto.java instead.
package webobjectsexamples.businesslogic.movies.common;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _TalentPhoto extends er.extensions.eof.ERXGenericRecord {
	public static final String ENTITY_NAME = "TalentPhoto";

	// Attributes
	public static final String PHOTO_KEY = "photo";

	// Relationships
	public static final String TALENT_KEY = "talent";

  private static Logger LOG = Logger.getLogger(_TalentPhoto.class);

  public TalentPhoto localInstanceIn(EOEditingContext editingContext) {
    TalentPhoto localInstance = (TalentPhoto)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSData photo() {
    return (NSData) storedValueForKey("photo");
  }

  public void setPhoto(NSData value) {
    if (_TalentPhoto.LOG.isDebugEnabled()) {
    	_TalentPhoto.LOG.debug( "updating photo from " + photo() + " to " + value);
    }
    takeStoredValueForKey(value, "photo");
  }

  public webobjectsexamples.businesslogic.movies.common.Talent talent() {
    return (webobjectsexamples.businesslogic.movies.common.Talent)storedValueForKey("talent");
  }

  public void setTalentRelationship(webobjectsexamples.businesslogic.movies.common.Talent value) {
    if (_TalentPhoto.LOG.isDebugEnabled()) {
      _TalentPhoto.LOG.debug("updating talent from " + talent() + " to " + value);
    }
    if (value == null) {
    	webobjectsexamples.businesslogic.movies.common.Talent oldValue = talent();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "talent");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "talent");
    }
  }
  

  public static TalentPhoto createTalentPhoto(EOEditingContext editingContext, webobjectsexamples.businesslogic.movies.common.Talent talent) {
    TalentPhoto eo = (TalentPhoto) EOUtilities.createAndInsertInstance(editingContext, _TalentPhoto.ENTITY_NAME);    
    eo.setTalentRelationship(talent);
    return eo;
  }

  public static NSArray<TalentPhoto> fetchAllTalentPhotos(EOEditingContext editingContext) {
    return _TalentPhoto.fetchAllTalentPhotos(editingContext, null);
  }

  public static NSArray<TalentPhoto> fetchAllTalentPhotos(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _TalentPhoto.fetchTalentPhotos(editingContext, null, sortOrderings);
  }

  public static NSArray<TalentPhoto> fetchTalentPhotos(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_TalentPhoto.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<TalentPhoto> eoObjects = (NSArray<TalentPhoto>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static TalentPhoto fetchTalentPhoto(EOEditingContext editingContext, String keyName, Object value) {
    return _TalentPhoto.fetchTalentPhoto(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static TalentPhoto fetchTalentPhoto(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<TalentPhoto> eoObjects = _TalentPhoto.fetchTalentPhotos(editingContext, qualifier, null);
    TalentPhoto eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (TalentPhoto)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one TalentPhoto that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static TalentPhoto fetchRequiredTalentPhoto(EOEditingContext editingContext, String keyName, Object value) {
    return _TalentPhoto.fetchRequiredTalentPhoto(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static TalentPhoto fetchRequiredTalentPhoto(EOEditingContext editingContext, EOQualifier qualifier) {
    TalentPhoto eoObject = _TalentPhoto.fetchTalentPhoto(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no TalentPhoto that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static TalentPhoto localInstanceIn(EOEditingContext editingContext, TalentPhoto eo) {
    TalentPhoto localInstance = (eo == null) ? null : (TalentPhoto)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
