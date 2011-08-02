// DO NOT EDIT.  Make changes to Country.java instead.
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
public abstract class _Country extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "Country";

  // Attribute Keys
  public static final ERXKey<String> CODE = new ERXKey<String>("code");
  public static final ERXKey<String> CODE2 = new ERXKey<String>("code2");
  public static final ERXKey<er.plugintest.model.Continent> CONTINENT = new ERXKey<er.plugintest.model.Continent>("continent");
  public static final ERXKey<NSData> FLAG = new ERXKey<NSData>("flag");
  public static final ERXKey<java.math.BigDecimal> G_NP = new ERXKey<java.math.BigDecimal>("gNP");
  public static final ERXKey<java.math.BigDecimal> G_NP_OLD = new ERXKey<java.math.BigDecimal>("gNPOld");
  public static final ERXKey<String> GOVERNMENT_FORM = new ERXKey<String>("governmentForm");
  public static final ERXKey<String> HEAD_OF_STATE = new ERXKey<String>("headOfState");
  public static final ERXKey<Integer> INDEP_YEAR = new ERXKey<Integer>("indepYear");
  public static final ERXKey<Double> LIFE_EXPECTANCY = new ERXKey<Double>("lifeExpectancy");
  public static final ERXKey<String> LOCAL_NAME = new ERXKey<String>("localName");
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  public static final ERXKey<Integer> POPULATION = new ERXKey<Integer>("population");
  public static final ERXKey<String> REGION = new ERXKey<String>("region");
  public static final ERXKey<Double> SURFACE_AREA = new ERXKey<Double>("surfaceArea");
  // Relationship Keys
  public static final ERXKey<er.plugintest.model.City> CAPITAL = new ERXKey<er.plugintest.model.City>("capital");
  public static final ERXKey<er.plugintest.model.City> CITIES = new ERXKey<er.plugintest.model.City>("cities");
  public static final ERXKey<er.plugintest.model.CountryLanguage> COUNTRY_LANGUAGES = new ERXKey<er.plugintest.model.CountryLanguage>("countryLanguages");

  // Attributes
  public static final String CODE_KEY = CODE.key();
  public static final String CODE2_KEY = CODE2.key();
  public static final String CONTINENT_KEY = CONTINENT.key();
  public static final String FLAG_KEY = FLAG.key();
  public static final String G_NP_KEY = G_NP.key();
  public static final String G_NP_OLD_KEY = G_NP_OLD.key();
  public static final String GOVERNMENT_FORM_KEY = GOVERNMENT_FORM.key();
  public static final String HEAD_OF_STATE_KEY = HEAD_OF_STATE.key();
  public static final String INDEP_YEAR_KEY = INDEP_YEAR.key();
  public static final String LIFE_EXPECTANCY_KEY = LIFE_EXPECTANCY.key();
  public static final String LOCAL_NAME_KEY = LOCAL_NAME.key();
  public static final String NAME_KEY = NAME.key();
  public static final String POPULATION_KEY = POPULATION.key();
  public static final String REGION_KEY = REGION.key();
  public static final String SURFACE_AREA_KEY = SURFACE_AREA.key();
  // Relationships
  public static final String CAPITAL_KEY = CAPITAL.key();
  public static final String CITIES_KEY = CITIES.key();
  public static final String COUNTRY_LANGUAGES_KEY = COUNTRY_LANGUAGES.key();

  private static Logger LOG = Logger.getLogger(_Country.class);

  public Country localInstanceIn(EOEditingContext editingContext) {
    Country localInstance = (Country)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String code() {
    return (String) storedValueForKey(_Country.CODE_KEY);
  }

  public void setCode(String value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating code from " + code() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.CODE_KEY);
  }

  public String code2() {
    return (String) storedValueForKey(_Country.CODE2_KEY);
  }

  public void setCode2(String value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating code2 from " + code2() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.CODE2_KEY);
  }

  public er.plugintest.model.Continent continent() {
    return (er.plugintest.model.Continent) storedValueForKey(_Country.CONTINENT_KEY);
  }

  public void setContinent(er.plugintest.model.Continent value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating continent from " + continent() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.CONTINENT_KEY);
  }

  public NSData flag() {
    return (NSData) storedValueForKey(_Country.FLAG_KEY);
  }

  public void setFlag(NSData value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating flag from " + flag() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.FLAG_KEY);
  }

  public java.math.BigDecimal gNP() {
    return (java.math.BigDecimal) storedValueForKey(_Country.G_NP_KEY);
  }

  public void setGNP(java.math.BigDecimal value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating gNP from " + gNP() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.G_NP_KEY);
  }

  public java.math.BigDecimal gNPOld() {
    return (java.math.BigDecimal) storedValueForKey(_Country.G_NP_OLD_KEY);
  }

  public void setGNPOld(java.math.BigDecimal value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating gNPOld from " + gNPOld() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.G_NP_OLD_KEY);
  }

  public String governmentForm() {
    return (String) storedValueForKey(_Country.GOVERNMENT_FORM_KEY);
  }

  public void setGovernmentForm(String value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating governmentForm from " + governmentForm() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.GOVERNMENT_FORM_KEY);
  }

  public String headOfState() {
    return (String) storedValueForKey(_Country.HEAD_OF_STATE_KEY);
  }

  public void setHeadOfState(String value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating headOfState from " + headOfState() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.HEAD_OF_STATE_KEY);
  }

  public Integer indepYear() {
    return (Integer) storedValueForKey(_Country.INDEP_YEAR_KEY);
  }

  public void setIndepYear(Integer value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating indepYear from " + indepYear() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.INDEP_YEAR_KEY);
  }

  public Double lifeExpectancy() {
    return (Double) storedValueForKey(_Country.LIFE_EXPECTANCY_KEY);
  }

  public void setLifeExpectancy(Double value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating lifeExpectancy from " + lifeExpectancy() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.LIFE_EXPECTANCY_KEY);
  }

  public String localName() {
    return (String) storedValueForKey(_Country.LOCAL_NAME_KEY);
  }

  public void setLocalName(String value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating localName from " + localName() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.LOCAL_NAME_KEY);
  }

  public String name() {
    return (String) storedValueForKey(_Country.NAME_KEY);
  }

  public void setName(String value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.NAME_KEY);
  }

  public Integer population() {
    return (Integer) storedValueForKey(_Country.POPULATION_KEY);
  }

  public void setPopulation(Integer value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating population from " + population() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.POPULATION_KEY);
  }

  public String region() {
    return (String) storedValueForKey(_Country.REGION_KEY);
  }

  public void setRegion(String value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating region from " + region() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.REGION_KEY);
  }

  public Double surfaceArea() {
    return (Double) storedValueForKey(_Country.SURFACE_AREA_KEY);
  }

  public void setSurfaceArea(Double value) {
    if (_Country.LOG.isDebugEnabled()) {
    	_Country.LOG.debug( "updating surfaceArea from " + surfaceArea() + " to " + value);
    }
    takeStoredValueForKey(value, _Country.SURFACE_AREA_KEY);
  }

  public er.plugintest.model.City capital() {
    return (er.plugintest.model.City)storedValueForKey(_Country.CAPITAL_KEY);
  }
  
  public void setCapital(er.plugintest.model.City value) {
    takeStoredValueForKey(value, _Country.CAPITAL_KEY);
  }

  public void setCapitalRelationship(er.plugintest.model.City value) {
    if (_Country.LOG.isDebugEnabled()) {
      _Country.LOG.debug("updating capital from " + capital() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setCapital(value);
    }
    else if (value == null) {
    	er.plugintest.model.City oldValue = capital();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Country.CAPITAL_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Country.CAPITAL_KEY);
    }
  }
  
  public NSArray<er.plugintest.model.City> cities() {
    return (NSArray<er.plugintest.model.City>)storedValueForKey(_Country.CITIES_KEY);
  }

  public NSArray<er.plugintest.model.City> cities(EOQualifier qualifier) {
    return cities(qualifier, null, false);
  }

  public NSArray<er.plugintest.model.City> cities(EOQualifier qualifier, boolean fetch) {
    return cities(qualifier, null, fetch);
  }

  public NSArray<er.plugintest.model.City> cities(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.plugintest.model.City> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.plugintest.model.City.COUNTRY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.plugintest.model.City.fetchCities(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = cities();
      if (qualifier != null) {
        results = (NSArray<er.plugintest.model.City>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.plugintest.model.City>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToCities(er.plugintest.model.City object) {
    includeObjectIntoPropertyWithKey(object, _Country.CITIES_KEY);
  }

  public void removeFromCities(er.plugintest.model.City object) {
    excludeObjectFromPropertyWithKey(object, _Country.CITIES_KEY);
  }

  public void addToCitiesRelationship(er.plugintest.model.City object) {
    if (_Country.LOG.isDebugEnabled()) {
      _Country.LOG.debug("adding " + object + " to cities relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToCities(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Country.CITIES_KEY);
    }
  }

  public void removeFromCitiesRelationship(er.plugintest.model.City object) {
    if (_Country.LOG.isDebugEnabled()) {
      _Country.LOG.debug("removing " + object + " from cities relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromCities(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Country.CITIES_KEY);
    }
  }

  public er.plugintest.model.City createCitiesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.plugintest.model.City.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Country.CITIES_KEY);
    return (er.plugintest.model.City) eo;
  }

  public void deleteCitiesRelationship(er.plugintest.model.City object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Country.CITIES_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllCitiesRelationships() {
    Enumeration<er.plugintest.model.City> objects = cities().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteCitiesRelationship(objects.nextElement());
    }
  }

  public NSArray<er.plugintest.model.CountryLanguage> countryLanguages() {
    return (NSArray<er.plugintest.model.CountryLanguage>)storedValueForKey(_Country.COUNTRY_LANGUAGES_KEY);
  }

  public NSArray<er.plugintest.model.CountryLanguage> countryLanguages(EOQualifier qualifier) {
    return countryLanguages(qualifier, null, false);
  }

  public NSArray<er.plugintest.model.CountryLanguage> countryLanguages(EOQualifier qualifier, boolean fetch) {
    return countryLanguages(qualifier, null, fetch);
  }

  public NSArray<er.plugintest.model.CountryLanguage> countryLanguages(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.plugintest.model.CountryLanguage> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.plugintest.model.CountryLanguage.COUNTRY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.plugintest.model.CountryLanguage.fetchCountryLanguages(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = countryLanguages();
      if (qualifier != null) {
        results = (NSArray<er.plugintest.model.CountryLanguage>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.plugintest.model.CountryLanguage>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToCountryLanguages(er.plugintest.model.CountryLanguage object) {
    includeObjectIntoPropertyWithKey(object, _Country.COUNTRY_LANGUAGES_KEY);
  }

  public void removeFromCountryLanguages(er.plugintest.model.CountryLanguage object) {
    excludeObjectFromPropertyWithKey(object, _Country.COUNTRY_LANGUAGES_KEY);
  }

  public void addToCountryLanguagesRelationship(er.plugintest.model.CountryLanguage object) {
    if (_Country.LOG.isDebugEnabled()) {
      _Country.LOG.debug("adding " + object + " to countryLanguages relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToCountryLanguages(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Country.COUNTRY_LANGUAGES_KEY);
    }
  }

  public void removeFromCountryLanguagesRelationship(er.plugintest.model.CountryLanguage object) {
    if (_Country.LOG.isDebugEnabled()) {
      _Country.LOG.debug("removing " + object + " from countryLanguages relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromCountryLanguages(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Country.COUNTRY_LANGUAGES_KEY);
    }
  }

  public er.plugintest.model.CountryLanguage createCountryLanguagesRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.plugintest.model.CountryLanguage.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Country.COUNTRY_LANGUAGES_KEY);
    return (er.plugintest.model.CountryLanguage) eo;
  }

  public void deleteCountryLanguagesRelationship(er.plugintest.model.CountryLanguage object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Country.COUNTRY_LANGUAGES_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllCountryLanguagesRelationships() {
    Enumeration<er.plugintest.model.CountryLanguage> objects = countryLanguages().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteCountryLanguagesRelationship(objects.nextElement());
    }
  }


  public static Country createCountry(EOEditingContext editingContext, String code
, String name
) {
    Country eo = (Country) EOUtilities.createAndInsertInstance(editingContext, _Country.ENTITY_NAME);    
		eo.setCode(code);
		eo.setName(name);
    return eo;
  }

  public static ERXFetchSpecification<Country> fetchSpec() {
    return new ERXFetchSpecification<Country>(_Country.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Country> fetchAllCountries(EOEditingContext editingContext) {
    return _Country.fetchAllCountries(editingContext, null);
  }

  public static NSArray<Country> fetchAllCountries(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Country.fetchCountries(editingContext, null, sortOrderings);
  }

  public static NSArray<Country> fetchCountries(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Country> fetchSpec = new ERXFetchSpecification<Country>(_Country.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Country> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Country fetchCountry(EOEditingContext editingContext, String keyName, Object value) {
    return _Country.fetchCountry(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Country fetchCountry(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Country> eoObjects = _Country.fetchCountries(editingContext, qualifier, null);
    Country eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Country that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Country fetchRequiredCountry(EOEditingContext editingContext, String keyName, Object value) {
    return _Country.fetchRequiredCountry(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Country fetchRequiredCountry(EOEditingContext editingContext, EOQualifier qualifier) {
    Country eoObject = _Country.fetchCountry(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Country that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Country localInstanceIn(EOEditingContext editingContext, Country eo) {
    Country localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
