package com.webobjects.appserver._private;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXPatcher.DynamicElementsPatches.Browser;


/**
 * Quick hack at extending WOBrowser to use HTML 4 optgroups.  It adds two bindings:
 * group and label.  group is required.  When this value changes, a new optgroup is created.
 * label is optional.  This is used as the label for an option group.  If label is not
 * bound, an empty string is used as the option group label. 
 * 
 * @binding group Object, required - keyPath to value that changes when the group of options changes
 * @binding label String, optional - String used as label for an option group
 * @binding itemClass, optional - String CSS class name for this item, browser support is inconsistent
 * @binding itemStyle, optional - String CSS style for this item, browser support is inconsistent
 */
public class ERXOptGroupBrowser extends Browser {

	
    protected WOAssociation group;
    protected WOAssociation label;
    protected WOAssociation itemStyle;
    protected WOAssociation itemClass;
    
	public ERXOptGroupBrowser(String arg0, NSDictionary arg1, WOElement arg2) {
		super(arg0, arg1, arg2);
        group = _associations.removeObjectForKey("group");
        label = _associations.removeObjectForKey("label");
        itemStyle = _associations.removeObjectForKey("itemStyle");
        itemClass = _associations.removeObjectForKey("itemClass");
        
        if (group == null)
        {
            throw new RuntimeException("Group is a required binding");
        }
	}
	
	


    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
         WOComponent parent = context.component();
         
        Object selections = null;
        if (_selections != null)
        {
        	selections = _selections.valueInComponent(parent);
        }
        else if (_selectedValues != null)
        {
        	selections = _selectedValues.valueInComponent(parent);
        }
        
        NSMutableArray selectedObjects = null;
        if (selections != null) {
            if ( ! (selections instanceof NSArray)) {
            	selectedObjects = new NSMutableArray(selections);	
            }                
           else if ( !(selections instanceof NSMutableArray)) {
               selectedObjects = new NSMutableArray((NSArray)selections);   
           }
           else {
        	   selectedObjects = (NSMutableArray)selections;
           }
        }
        	
        NSArray list = (NSArray) _list.valueInComponent(parent);
        
        Object previousGroup = null;
        boolean didOpenOptionGroup = false;
        boolean shouldEscapeHTML = _escapeHTML != null ? _escapeHTML.booleanValueInComponent(parent) : true;
        
        for(int i = 0; i < list.count(); i++)
        {
             Object listItem = list.objectAtIndex(i);
             _item.setValue(listItem, parent);
             
             Object currentGroup = group.valueInComponent(parent);
             if ( ! currentGroup.equals(previousGroup))
             {
                 previousGroup = currentGroup;

                 if (didOpenOptionGroup)
                 {
                     response._appendContentAsciiString("\n</optgroup>");
                 }
                 
                 response._appendContentAsciiString("\n<optgroup label=\"");
                 if (label != null)
                 {
                     if (shouldEscapeHTML)
                     {
                         response.appendContentHTMLString(label.valueInComponent(parent).toString());
                     }
                     else
                     {
                         response.appendContentString(label.valueInComponent(parent).toString());
                     }
                 }

                response._appendContentAsciiString("\">");
                didOpenOptionGroup = true;
             }
 
             response._appendContentAsciiString("\n<option");
             
             if (itemStyle != null) {
                 String style = (String) itemStyle.valueInComponent(parent);
                 if (style != null) {
                	 response._appendTagAttributeAndValue("style", style, true);
                 }
             }
             if (itemClass != null) {
                 String cssClass = (String) itemClass.valueInComponent(parent);
                 if (cssClass != null) {
                	 response._appendTagAttributeAndValue("class", cssClass, true);
                 }
             }
             
             String valueAsString = null;
             String displayStringAsString = null;
             if (_displayString != null || _value != null)
             {

                if (_displayString != null)
                {
                    Object displayString = _displayString.valueInComponent(parent);
                    if (displayString != null)
                    {
                        displayStringAsString = displayString.toString();
                        if (_value != null)
                        {
                            Object value = _value.valueInComponent(parent);
                            if (value != null)
                            {
                                valueAsString = value.toString();
                            }
                        } 
                        else
                        {
                            valueAsString = displayStringAsString;
                        }
                   }
                } 
                else
                {
                    Object value = _value.valueInComponent(parent);
                    if (value != null)
                    {
                        valueAsString = value.toString();
                        displayStringAsString = valueAsString;
                    }
               }
           } 
           else
           {
               displayStringAsString = listItem.toString();
               valueAsString = displayStringAsString;
           }
            
            boolean isSelectedItem = false;
            if (_selections != null)
            {
                isSelectedItem = selectedObjects == null ? false : selectedObjects.containsObject(listItem);
            }
            else if (_selectedValues != null)
            {
                if (_value != null)
                {
                    isSelectedItem = selectedObjects == null ? false : selectedObjects.containsObject(valueAsString);
                }
            }
             
            if (isSelectedItem)
            {
                response.appendContentCharacter(' ');
                response._appendContentAsciiString("selected");
            }
             
            if (_value != null)
            {
                response._appendTagAttributeAndValue("value", valueAsString, true);
            }
            else
            {
                String indexAsValue = WOShared.unsignedIntString(i);
                response._appendTagAttributeAndValue("value", indexAsValue, false);
            }
             
            response.appendContentCharacter('>');
             
            if (shouldEscapeHTML)
            {
                response.appendContentHTMLString(displayStringAsString);
            }
            else
            {
                response.appendContentString(displayStringAsString);
            }
              
            response._appendContentAsciiString("</option>");
         }
         
         if (didOpenOptionGroup)
         {
             response._appendContentAsciiString("\n</optgroup>");
         }
    }
    

}
