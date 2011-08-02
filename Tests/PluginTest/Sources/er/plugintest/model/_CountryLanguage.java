// DO NOT EDIT.  Make changes to CountryLanguage.java instead.
package er.plugintest.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _CountryLanguage extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "CountryLanguage";

  // Attribute Keys
  public static final ERXKey<Boolean> IS_OFFICIAL = new ERXKey<Boolean>("isOfficial");
  public static final ERXKey<String> LANGUAGE = new ERXKey<String>("language");
  public static final ERXKey<java.math.BigDecimal> PERCENTAGE = new ERXKey<java.math.BigDecimal>("percentage");
  // Relationship Keys
  public static final ERXKey<er.plugintest.model.Country> COUNTRY = new ERXKey<er.plugintest.model.Country>("country");

  // Attributes
  public static final String IS_OFFICIAL_KEY = IS_OFFICIAL.key();
  public static final String LANGUAGE_KEY = LANGUAGE.key();
  public static final String PERCENTAGE_KEY = PERCENTAGE.key();
  // Relationships
  public static final String COUNTRY_KEY = COUNTRY.key();

  private static Logger LOG = Logger.getLogger(_CountryLanguage.class);

  public CountryLanguage localInstanceIn(EOEditingContext editingContext) {
    CountryLanguage localInstance = (CountryLanguage)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public Boolean isOfficial() {
    return (Boolean) storedValueForKey(_CountryLanguage.IS_OFFICIAL_KEY);
  }

  public void setIsOfficial(Boolean value) {
    if (_CountryLanguage.LOG.isDebugEnabled()) {
    	_CountryLanguage.LOG.debug( "updating isOfficial from " + isOfficial() + " to " + value);
    }
    takeStoredValueForKey(value, _CountryLanguage.IS_OFFICIAL_KEY);
  }

  public String language() {
    return (String) storedValueForKey(_CountryLanguage.LANGUAGE_KEY);
  }

  public void setLanguage(String value) {
    if (_CountryLanguage.LOG.isDebugEnabled()) {
    	_CountryLanguage.LOG.debug( "updating language from " + language() + " to " + value);
    }
    takeStoredValueForKey(value, _CountryLanguage.LANGUAGE_KEY);
  }

  public java.math.BigDecimal percentage() {
    return (java.math.BigDecimal) storedValueForKey(_CountryLanguage.PERCENTAGE_KEY);
  }

  public void setPercentage(java.math.BigDecimal value) {
    if (_CountryLanguage.LOG.isDebugEnabled()) {
    	_CountryLanguage.LOG.debug( "updating percentage from " + percentage() + " to " + value);
    }
    takeStoredValueForKey(value, _CountryLanguage.PERCENTAGE_KEY);
  }

  public er.plugintest.model.Country country() {
    return (er.plugintest.model.Country)storedValueForKey(_CountryLanguage.COUNTRY_KEY);
  }
  
  public void setCountry(er.plugintest.model.Country value) {
    takeStoredValueForKey(value, _CountryLanguage.COUNTRY_KEY);
  }

  public void setCountryRelationship(er.plugintest.model.Country value) {
    if (_CountryLanguage.LOG.isDebugEnabled()) {
      _CountryLanguage.LOG.debug("updating country from " + country() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setCountry(value);
    }
    else if (value == null) {
    	er.plugintest.model.Country oldValue = country();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _CountryLanguage.COUNTRY_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _CountryLanguage.COUNTRY_KEY);
    }
  }
  

  public static CountryLanguage createCountryLanguage(EOEditingContext editingContext, Boolean isOfficial
, String language
, java.math.BigDecimal percentage
) {
    CountryLanguage eo = (CountryLanguage) EOUtilities.createAndInsertInstance(editingContext, _CountryLanguage.ENTITY_NAME);    
		eo.setIsOfficial(isOfficial);
		eo.setLanguage(language);
		eo.setPercentage(percentage);
    return eo;
  }

  public static ERXFetchSpecification<CountryLanguage> fetchSpec() {
    return new ERXFetchSpecification<CountryLanguage>(_CountryLanguage.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<CountryLanguage> fetchAllCountryLanguages(EOEditingContext editingContext) {
    return _CountryLanguage.fetchAllCountryLanguages(editingContext, null);
  }

  public static NSArray<CountryLanguage> fetchAllCountryLanguages(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _CountryLanguage.fetchCountryLanguages(editingContext, null, sortOrderings);
  }

  public static NSArray<CountryLanguage> fetchCountryLanguages(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<CountryLanguage> fetchSpec = new ERXFetchSpecification<CountryLanguage>(_CountryLanguage.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<CountryLanguage> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static CountryLanguage fetchCountryLanguage(EOEditingContext editingContext, String keyName, Object value) {
    return _CountryLanguage.fetchCountryLanguage(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static CountryLanguage fetchCountryLanguage(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<CountryLanguage> eoObjects = _CountryLanguage.fetchCountryLanguages(editingContext, qualifier, null);
    CountryLanguage eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one CountryLanguage that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static CountryLanguage fetchRequiredCountryLanguage(EOEditingContext editingContext, String keyName, Object value) {
    return _CountryLanguage.fetchRequiredCountryLanguage(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static CountryLanguage fetchRequiredCountryLanguage(EOEditingContext editingContext, EOQualifier qualifier) {
    CountryLanguage eoObject = _CountryLanguage.fetchCountryLanguage(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no CountryLanguage that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static CountryLanguage localInstanceIn(EOEditingContext editingContext, CountryLanguage eo) {
    CountryLanguage localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
