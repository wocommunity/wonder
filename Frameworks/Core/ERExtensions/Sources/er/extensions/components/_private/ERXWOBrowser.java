package er.extensions.components._private;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOInput;
import com.webobjects.appserver._private.WOShared;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Crappy reimplementation of WOBrowser which would not be needed of if it could
 * handle java collections.
 * 
 * @author ak, with help from a friend named jad
 * 
 * @binding class class of the browser
 * @binding displayString the string to display for an item
 * @binding escapeHTML if the displayString should be escaped, defaults to <code>true</code>
 * @binding id id of the browser
 * @binding item the current item in the iteration
 * @binding list the array or list of items to iterate over
 * @binding multiple if multiple elements are selectable, defaults to <code>false</code>
 * @binding name the name of the browser
 * @binding selectedValues array of selected objects used for direct action
 * @binding selections array of selected objects
 * @binding size how many items to display at one time, defaults to 5
 * @binding style style of the browser
 * @binding title title of the browser
 * @binding value the value to use for an item
 */
public class ERXWOBrowser extends WOInput {
	private static Logger log = Logger.getLogger(ERXWOBrowser.class);
	
	WOAssociation _list;
	WOAssociation _item;
	WOAssociation _displayString;
	WOAssociation _selections;
	WOAssociation _selectedValues;
	WOAssociation _size;
	WOAssociation _multiple;
	private boolean _loggedSlow;

	public ERXWOBrowser(String s, NSDictionary nsdictionary, WOElement woelement) {
		super("select", nsdictionary, null);
		_loggedSlow = false;
		_list = _associations.removeObjectForKey("list");
		_item = _associations.removeObjectForKey("item");
		_displayString = _associations.removeObjectForKey("displayString");
		_multiple = _associations.removeObjectForKey("multiple");
		_size = _associations.removeObjectForKey("size");
		String suffix = (isBrowser() ? "s" : "");
		_selections = _associations.removeObjectForKey("selection" + suffix);
		_selectedValues = _associations.removeObjectForKey("selectedValue" + suffix);
		if (_list == null || (_value != null || _displayString != null) && (_item == null || !_item.isValueSettable()) || _selections != null && !_selections.isValueSettable()) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> : Invalid attributes: 'list' must be present. 'item' must not be a constant if 'value' is present.  Cannot have 'displayString' or 'value' without 'item'.  'selection' must not be a constant if present. 'value' is not allowed anymore.");
		}
		if (_selections != null && _selectedValues != null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> : Cannot have both selections and selectedValues.");
		}
	}
	
	// Tells WOHTMLDynamicElement ancestor that there are "option" tags to render 
	@Override
	protected boolean hasContent() { 
	    return true; 
	}
	
	protected boolean isBrowser() {
		return true;
	}
	
	@Override
	public String toString() {
		return new StringBuilder('<').append(getClass().getSimpleName()).append(" list: ").append(_list)
				.append(" item: ").append(_item).append(" string: ").append(_displayString)
				.append(" selections: ").append(_selections).append(" selectedValues: ")
				.append(_selectedValues).append(" multiple: ").append(_multiple)
				.append(" size: ").append(_size).append(" escapeHTML: ").append(_escapeHTML).append('>').toString();
	}

	private void _slowTakeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		if (_selections != null && !isDisabledInContext(wocontext) && wocontext.wasFormSubmitted()) {
			String s = nameInContext(wocontext, wocomponent);
			NSArray nsarray = worequest.formValuesForKey(s);
			int i = nsarray != null ? nsarray.count() : 0;
			int size = 0;
			NSMutableArray nsmutablearray = new NSMutableArray(i);
			List vector = null;
			if (i != 0) {
				NSArray nsarray1 = null;
				List vector1 = null;
				Object list = _list.valueInComponent(wocomponent);
				if (list != null) {
					if (list instanceof NSArray) {
						nsarray1 = (NSArray) list;
						size = nsarray1.count();
					}
					else if (list instanceof List) {
						vector1 = (List) list;
						nsmutablearray = null;
						vector = new ArrayList();
						size = vector1.size();
					}
					else {
						throw new IllegalArgumentException("<" + getClass().getName() + "> Evaluating 'list' binding returned a " + list.getClass().getName() + " when it should return either a com.webobjects.foundation.NSArray, or a java.lang.Vector .");
					}
				}
				boolean flag = _multiple != null ? _multiple.booleanValueInComponent(wocomponent) : false;
				for (int k = 0; k < size; k++) {
					Object obj1 = nsarray1 == null ? vector1.get(k) : nsarray1.objectAtIndex(k);
					_item.setValue(obj1, wocomponent);
					Object obj2 = _value.valueInComponent(wocomponent);
					if (obj2 != null) {
						if (!nsarray.containsObject(obj2.toString())) {
							continue;
						}
						if (nsarray1 != null) {
							nsmutablearray.addObject(obj1);
						}
						else {
							vector.add(obj1);
						}
						if (!flag) {
							break;
						}
					}
					else {
						log.debug(toString() + " 'value' evaluated to null in component " + wocomponent.toString() + ".\n" + "Unable to select item " + obj1);
					}
				}

			}
			Object newValue = (nsmutablearray != null ? nsmutablearray: vector);
			setSelectedValue(newValue, wocomponent);
		}
	}
	
	protected void setSelectedValue(Object value, WOComponent component) {
		_selections.setValue(value, component);
	}
	
	protected Object selectedValue(WOComponent wocomponent) {
		Object obj = null;
		if (_selections != null) {
			obj = _selections.valueInComponent(wocomponent);
		}
		else if (_selectedValues != null) {
			obj = _selectedValues.valueInComponent(wocomponent);
		}
		return obj;
	}

	private void _fastTakeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		WOComponent wocomponent = wocontext.component();
		if (_selections != null && !isDisabledInContext(wocontext) && wocontext.wasFormSubmitted()) {
			String s = nameInContext(wocontext, wocomponent);
			NSArray nsarray = worequest.formValuesForKey(s);
			int i = nsarray != null ? nsarray.count() : 0;
			NSMutableArray nsmutablearray = new NSMutableArray(i);
			List vector = null;
			if (i != 0) {
				NSArray nsarray1 = null;
				List vector1 = null;
				if (_list != null) {
					Object obj = _list.valueInComponent(wocomponent);
					if (obj != null) {
						if (obj instanceof NSArray) {
							nsarray1 = (NSArray) obj;
						}
						else if (obj instanceof List) {
							vector1 = (List) obj;
							nsmutablearray = null;
							vector = new ArrayList(i);
						}
						else {
							throw new IllegalArgumentException("<" + getClass().getName() + "> Evaluating 'list' binding returned a " + obj.getClass().getName() + " when it should return either a com.webobjects.foundation.NSArray, or a java.lang.Vector .");
						}
					}
				}
				for (int j = 0; j < i; j++) {
					String s1 = (String) nsarray.objectAtIndex(j);
					int k = Integer.parseInt(s1);
					if (nsarray1 != null) {
						Object obj2 = nsarray1.objectAtIndex(k);
						nsmutablearray.addObject(obj2);
					}
					else {
						Object obj3 = vector1.get(k);
						vector.add(obj3);
					}
				}

			}
			Object newValue = (nsmutablearray != null ? nsmutablearray: vector);
			setSelectedValue(newValue, wocomponent);
		}
	}

	@Override
	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		if (_value != null) {
			if (!_loggedSlow) {
				log.debug("<" + getClass().getName() + "> Warning: Avoid using the 'value' binding as it is much slower than omitting it, and it is just cosmetic.");
				_loggedSlow = true;
			}
			_slowTakeValuesFromRequest(worequest, wocontext);
		}
		else {
			_fastTakeValuesFromRequest(worequest, wocontext);
		}
	}

	@Override
	public void appendChildrenToResponse(WOResponse woresponse, WOContext wocontext) {
		NSArray nsarray = null;
		List vector = null;
		int j = 0;
		Object obj = null;
		NSMutableArray nsmutablearray = null;
		List vector1 = null;
		WOComponent wocomponent = wocontext.component();
		boolean flag1 = true;
		if (_escapeHTML != null) {
			flag1 = _escapeHTML.booleanValueInComponent(wocomponent);
		}
		Object obj1 = _list.valueInComponent(wocomponent);
		if (obj1 != null) {
			if (obj1 instanceof NSArray) {
				nsarray = (NSArray) obj1;
				j = nsarray.count();
			}
			else if (obj1 instanceof List) {
				vector = (List) obj1;
				j = vector.size();
			}
			else {
				throw new IllegalArgumentException("<" + getClass().getName() + "> Evaluating 'list' binding returned a " + obj1.getClass().getName() + " when it should return either a com.webobjects.foundation.NSArray, or a java.lang.Vector .");
			}
		}
		obj = selectedValue(wocomponent);
		if (nsarray != null) {
			if (obj != null) {
				if (!(obj instanceof NSArray)) {
					nsmutablearray = new NSMutableArray(obj);
				}
				else if (!(obj instanceof NSMutableArray)) {
					nsmutablearray = new NSMutableArray((NSArray) obj);
				}
				else {
					nsmutablearray = (NSMutableArray) obj;
				}
			}
		}
		else if (obj instanceof List) {
			vector1 = (List) obj;
		}
		for (int i = 0; i < j; i++) {
			String s = null;
			String s1 = null;
			Object obj2 = null;
			if (nsarray != null) {
				obj2 = nsarray.objectAtIndex(i);
			}
			else {
				obj2 = vector.get(i);
			}
			if (_displayString != null || _value != null) {
				_item.setValue(obj2, wocomponent);
				if (_displayString != null) {
					Object obj3 = _displayString.valueInComponent(wocomponent);
					if (obj3 != null) {
						s1 = obj3.toString();
						if (_value != null) {
							Object obj5 = _value.valueInComponent(wocomponent);
							if (obj5 != null) {
								s = obj5.toString();
							}
						}
						else {
							s = s1;
						}
					}
				}
				else {
					Object obj4 = _value.valueInComponent(wocomponent);
					if (obj4 != null) {
						s = obj4.toString();
						s1 = s;
					}
				}
			}
			else {
				s1 = obj2.toString();
				s = s1;
			}
			woresponse._appendContentAsciiString("\n<option");
			if (nsmutablearray != null || vector1 != null) {
				boolean flag2 = false;
				if (_selections != null) {
					flag2 = nsarray == null ? vector1.contains(obj2) : nsmutablearray.containsObject(obj2);
				}
				else {
					String s3 = null;
					if (_value != null) {
						s3 = s;
					}
					else {
						s3 = WOShared.unsignedIntString(i);
					}
					flag2 = nsarray == null ? vector1.contains(s3) : nsmutablearray.containsObject(s3);
				}
				if (flag2) {
					woresponse.appendContentCharacter(' ');
					woresponse._appendContentAsciiString("selected=\"selected\"");
				}
			}
			if (_value != null) {
				woresponse._appendTagAttributeAndValue("value", s, true);
			}
			else {
				String s2 = WOShared.unsignedIntString(i);
				woresponse._appendTagAttributeAndValue("value", s2, false);
			}
			woresponse.appendContentCharacter('>');
			if (flag1) {
				woresponse.appendContentHTMLString(s1);
			}
			else {
				woresponse.appendContentString(s1);
			}
			woresponse._appendContentAsciiString("</option>");
		}

	}

	@Override
	protected void _appendValueAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
	}

	@Override
	public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
		super.appendAttributesToResponse(woresponse, wocontext);
		Object obj = null;
		WOComponent wocomponent = wocontext.component();
		if (_size != null) {
			obj = _size.valueInComponent(wocomponent);
		}
		if (_size == null || obj == null || Integer.parseInt(obj.toString()) == 1) {
			obj = WOShared.unsignedIntString(5);
		}
		woresponse._appendTagAttributeAndValue("size", obj.toString(), false);
		if (_multiple != null && _multiple.booleanValueInComponent(wocomponent)) {
			woresponse.appendContentCharacter(' ');
			woresponse._appendContentAsciiString("multiple=\"multiple\"");
		}
	}
}
