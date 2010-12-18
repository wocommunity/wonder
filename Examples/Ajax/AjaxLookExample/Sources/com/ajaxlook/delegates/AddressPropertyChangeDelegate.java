package com.ajaxlook.delegates;

import com.ajaxlook.model.Address;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;

import er.ajax.look.interfaces.PropertyChangedDelegate;

public class AddressPropertyChangeDelegate implements PropertyChangedDelegate {

	public NSArray<String> propertyChanged(D2WContext context) {
		String prop = context.propertyKey();
		if(Address.NATION_KEY.equals(prop)) {
			return new NSArray<String>(Address.STATE_KEY, Address.CITY_KEY);
		} else if (Address.STATE_KEY.equals(prop)) {
			return new NSArray<String>(Address.CITY_KEY);
		}
		return NSArray.emptyArray();
	}

}
