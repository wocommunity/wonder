// DO NOT EDIT.  Make changes to com.ajaxlook.model.Address.java instead.
package com.ajaxlook.model.gen;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;


@SuppressWarnings("all")
public abstract class _Address extends ERXGenericRecord {
  public static final String ENTITY_NAME = "Address";

  // Attribute Keys
  public static final ERXKey<com.ajaxlook.enums.City> CITY = new ERXKey<com.ajaxlook.enums.City>("city");
  public static final ERXKey<com.ajaxlook.enums.Nation> NATION = new ERXKey<com.ajaxlook.enums.Nation>("nation");
  public static final ERXKey<com.ajaxlook.enums.State> STATE = new ERXKey<com.ajaxlook.enums.State>("state");
  public static final ERXKey<String> STREET_ADDRESS = new ERXKey<String>("streetAddress");
  public static final ERXKey<String> ZIP_CODE = new ERXKey<String>("zipCode");

  // Relationship Keys

  // Attributes
  public static final String CITY_KEY = CITY.key();
  public static final String NATION_KEY = NATION.key();
  public static final String STATE_KEY = STATE.key();
  public static final String STREET_ADDRESS_KEY = STREET_ADDRESS.key();
  public static final String ZIP_CODE_KEY = ZIP_CODE.key();

  // Relationships

  public static class _AddressClazz<T extends com.ajaxlook.model.Address> extends ERXGenericRecord.ERXGenericRecordClazz<T> {
    /* more clazz methods here */
  }

  private static Logger LOG = Logger.getLogger(_Address.class);

	
  public com.ajaxlook.enums.City city() {
    return (com.ajaxlook.enums.City) storedValueForKey(_Address.CITY_KEY);
  }

  public void setCity(com.ajaxlook.enums.City value) {
    if (_Address.LOG.isDebugEnabled()) {
    	_Address.LOG.debug( "updating city from " + city() + " to " + value);
    }
    takeStoredValueForKey(value, _Address.CITY_KEY);
  }

	
  public com.ajaxlook.enums.Nation nation() {
    return (com.ajaxlook.enums.Nation) storedValueForKey(_Address.NATION_KEY);
  }

  public void setNation(com.ajaxlook.enums.Nation value) {
    if (_Address.LOG.isDebugEnabled()) {
    	_Address.LOG.debug( "updating nation from " + nation() + " to " + value);
    }
    takeStoredValueForKey(value, _Address.NATION_KEY);
  }

	
  public com.ajaxlook.enums.State state() {
    return (com.ajaxlook.enums.State) storedValueForKey(_Address.STATE_KEY);
  }

  public void setState(com.ajaxlook.enums.State value) {
    if (_Address.LOG.isDebugEnabled()) {
    	_Address.LOG.debug( "updating state from " + state() + " to " + value);
    }
    takeStoredValueForKey(value, _Address.STATE_KEY);
  }

	
  public String streetAddress() {
    return (String) storedValueForKey(_Address.STREET_ADDRESS_KEY);
  }

  public void setStreetAddress(String value) {
    if (_Address.LOG.isDebugEnabled()) {
    	_Address.LOG.debug( "updating streetAddress from " + streetAddress() + " to " + value);
    }
    takeStoredValueForKey(value, _Address.STREET_ADDRESS_KEY);
  }

	
  public String zipCode() {
    return (String) storedValueForKey(_Address.ZIP_CODE_KEY);
  }

  public void setZipCode(String value) {
    if (_Address.LOG.isDebugEnabled()) {
    	_Address.LOG.debug( "updating zipCode from " + zipCode() + " to " + value);
    }
    takeStoredValueForKey(value, _Address.ZIP_CODE_KEY);
  }


  public static com.ajaxlook.model.Address createAddress(EOEditingContext editingContext, com.ajaxlook.enums.City city, com.ajaxlook.enums.Nation nation, com.ajaxlook.enums.State state, String streetAddress, String zipCode) {
    com.ajaxlook.model.Address eo = (com.ajaxlook.model.Address) EOUtilities.createAndInsertInstance(editingContext, _Address.ENTITY_NAME);    
	eo.setCity(city);
	eo.setNation(nation);
	eo.setState(state);
	eo.setStreetAddress(streetAddress);
	eo.setZipCode(zipCode);
    return eo;
  }
}
