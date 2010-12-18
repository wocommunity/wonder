package com.ajaxlook.model;

import java.util.EnumMap;

import org.apache.log4j.Logger;

import com.ajaxlook.enums.City;
import com.ajaxlook.enums.Nation;
import com.ajaxlook.enums.State;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXValueUtilities;

public class Address extends com.ajaxlook.model.gen._Address {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(Address.class);

    public static final AddressClazz<Address> clazz = new AddressClazz<Address>();
    public static class AddressClazz<T extends Address> extends com.ajaxlook.model.gen._Address._AddressClazz<T> {
        /* more clazz methods here */
    }

    private static EnumMap<Nation, NSArray<State>> nationStates = new EnumMap<Nation, NSArray<State>>(Nation.class);
    private static EnumMap<State, NSArray<City>> stateCities = new EnumMap<State, NSArray<City>>(State.class);
    
    static{
    	nationStates.put(Nation.US, new NSArray<State>(State.FLORIDA, State.GEORGIA));
    	nationStates.put(Nation.UK, new NSArray<State>(State.SCOTLAND, State.WALES));
    	
    	stateCities.put(State.FLORIDA, new NSArray<City>(City.MIAMI, City.ORLANDO));
    	stateCities.put(State.GEORGIA, new NSArray<City>(City.ATLANTA, City.SAVANNAH));
    	stateCities.put(State.SCOTLAND, new NSArray<City>(City.DUNDEE, City.GLASGOW));
    	stateCities.put(State.WALES, new NSArray<City>(City.CARDIFF, City.BANGOR));
    }

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }

    public NSArray<Nation> nationChoices() {
    	return new NSArray<Nation>(Nation.values());
    }
    
    public NSArray<State> stateChoices() {
    	if(nation() != null) {
    		NSArray<State> states = nationStates.get(nation());
    		return states;
    	}
    	return NSArray.emptyArray();
    }
    
    public NSArray<City> cityChoices() {
    	if(state() != null) {
    		NSArray<City> cities = stateCities.get(state());
    		return cities;
    	}
    	return NSArray.emptyArray();
    }
    
    public void setNation(Nation value) {
    	if(ERXValueUtilities.isNull(value) || !value.equals(nation())) {
    		setState(null);
    		setCity(null);    		
    	}
    	super.setNation(value);
    }
    
    public void setState(State value) {
    	if(ERXValueUtilities.isNull(value) || !value.equals(state())) {
    		setCity(null);    		
    	}
    	super.setState(value);
    }
}
