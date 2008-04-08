package er.imadaptor.components;

import java.util.Enumeration;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXStringUtilities;
import er.imadaptor.InstantMessengerAdaptor;

abstract class AbstractIMSearchAction extends IMAction {
	private WOAssociation _value;
	private WOAssociation _values;
	private WOAssociation _quicksilver;
	private WOAssociation _optionsDictionary;
	private WOAssociation _optionsArray;
	private WOAssociation _optionKeyPath;

	public AbstractIMSearchAction(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
		_value = (WOAssociation) associations.objectForKey("value");
		_values = (WOAssociation) associations.objectForKey("values");
		if (_value == null && _values == null) {
			throw new WODynamicElementCreationException("Only one of 'value' or 'values' can be bound at any time.");
		}
		_quicksilver = (WOAssociation) associations.objectForKey("quicksilver");
		_optionsDictionary = (WOAssociation) associations.objectForKey("optionsDictionary");
		_optionsArray = (WOAssociation) associations.objectForKey("optionsArray");
		if (_optionsArray == null && _optionsDictionary == null) {
			throw new WODynamicElementCreationException("Only one of 'optionsArray' or 'optionsDictionary' can be bound at any time.");
		}
		if (_optionsArray != null && _optionsDictionary != null) {
			throw new WODynamicElementCreationException("Both 'optionsArray' and 'optionsDictionary' cannot be bound at the same time.");
		}
		_optionKeyPath = (WOAssociation) associations.objectForKey("optionKeyPath");
		if (_optionKeyPath != null && _optionsDictionary != null) {
			throw new WODynamicElementCreationException("Both 'optionKeyPath' and 'optionsDictionary' cannot be bound at the same time.");
		}
	}

	protected abstract boolean searchInsideMessage();

	public static NSArray selectedValues(NSDictionary options, boolean quicksilver, String message, boolean searchInsideMessage) {
		NSMutableArray selectedValues = new NSMutableArray();
		String lowercaseMessage = message.toLowerCase();
		Enumeration keyEnum = options.keyEnumerator();
		while (keyEnum.hasMoreElements()) {
			String key = (String) keyEnum.nextElement();

			String stringToSearch;
			String stringToSearchFor;
			if (searchInsideMessage) {
				stringToSearch = lowercaseMessage;
				stringToSearchFor = key;
			}
			else {
				stringToSearch = key;
				stringToSearchFor = lowercaseMessage;
			}

			String selectedKey = null;
			if (quicksilver && ERXStringUtilities.quicksilverContains(stringToSearch, stringToSearchFor)) {
				selectedKey = key;
			}
			else if (!quicksilver && stringToSearch.indexOf(stringToSearchFor) != -1) {
				selectedKey = key;
			}

			if (selectedKey != null) {
				Object selectedValue = options.objectForKey(selectedKey);
				selectedValues.addObject(selectedValue);
			}
		}
		return selectedValues;
	}

	public static NSArray selectedValues(NSArray options, String optionKeyPath, boolean quicksilver, String message, boolean searchInsideMessage) {
		NSMutableArray selectedValues = new NSMutableArray();
		String lowercaseMessage = message.toLowerCase();
		Enumeration optionEnum = options.objectEnumerator();
		while (optionEnum.hasMoreElements()) {
			Object option = optionEnum.nextElement();
			String optionStr;
			if (optionKeyPath == null) {
				optionStr = option.toString();
			}
			else {
				Object optionKeyPathValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(option, optionKeyPath);
				if (optionKeyPathValue == null) {
					optionStr = "";
				}
				else {
					optionStr = optionKeyPathValue.toString();
				}
			}

			String stringToSearch;
			String stringToSearchFor;
			if (searchInsideMessage) {
				stringToSearch = lowercaseMessage;
				stringToSearchFor = optionStr;
			}
			else {
				stringToSearch = optionStr;
				stringToSearchFor = lowercaseMessage;
			}

			if (quicksilver && ERXStringUtilities.quicksilverContains(stringToSearch, stringToSearchFor)) {
				selectedValues.addObject(option);
			}
			else if (!quicksilver && stringToSearch.indexOf(stringToSearchFor) != -1) {
				selectedValues.addObject(option);
			}
		}
		return selectedValues;
	}

	@Override
	protected void actionInvoked(WORequest request, WOContext context) {
		WOComponent component = context.component();
		String message = InstantMessengerAdaptor.message(request);
		boolean quicksilver = (_quicksilver != null && ((Boolean) _quicksilver.valueInComponent(component)).booleanValue());
		NSArray selectedValues;
		if (_optionsDictionary != null) {
			NSDictionary options = (NSDictionary) _optionsDictionary.valueInComponent(component);
			selectedValues = AbstractIMSearchAction.selectedValues(options, quicksilver, message, searchInsideMessage());
		}
		else if (_optionsArray != null) {
			NSArray options = (NSArray) _optionsArray.valueInComponent(component);
			String optionKeyPath = null;
			if (_optionKeyPath != null) {
				optionKeyPath = (String) _optionKeyPath.valueInComponent(component);
			}
			selectedValues = AbstractIMSearchAction.selectedValues(options, optionKeyPath, quicksilver, message, searchInsideMessage());
		}
		else {
			throw new IllegalArgumentException("You must specify either optionsDictionary or optionsArray.");
		}
		if (_values != null) {
			_values.setValue(selectedValues, component);
		}
		if (_value != null) {
			Object selectedValue = null;
			if (selectedValues.count() == 1) {
				selectedValue = selectedValues.objectAtIndex(0);
			}
			_value.setValue(selectedValue, component);
		}
	}

}
