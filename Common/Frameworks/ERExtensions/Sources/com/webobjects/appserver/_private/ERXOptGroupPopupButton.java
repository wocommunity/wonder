package com.webobjects.appserver._private;


import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXKeyValueCodingUtilities;


/**
 * Quick hack at extending WOPopUpButton to use HTML 4 optgroups.  It adds two bindings:
 * group and label.  group is required.  When this value changes, a new optgroup is created.
 * label is optional.  This is used as the label for an option group.  If label is not
 * bound, an empty string is used as the option group label. 
 * 
 * @binding group Object, required - keyPath to value that changes when the group of options changes
 * @binding label String, optional - String used as label for an option group
 */
public class ERXOptGroupPopupButton extends WOPopUpButton
{
    
    protected WOAssociation group;
    protected WOAssociation label;
    
    public ERXOptGroupPopupButton(String name, NSDictionary associations, WOElement template)
    {
        super(name, associations, template);
        group = (WOAssociation)_associations.removeObjectForKey("group");
        label = (WOAssociation)_associations.removeObjectForKey("label");
        
        if (group == null)
        {
            throw new RuntimeException("Group is a required binding");
        }
    }
    

    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
         WOComponent parent = context.component();
         
         if (_noSelectionString != null)
         {
             Object noSelectionString = _noSelectionString.valueInComponent(parent);
             if (noSelectionString != null)
             {
                 response.appendContentString("\n<option value=\"WONoSelectionString\">");
                 response.appendContentHTMLString(noSelectionString.toString());
                 response._appendContentAsciiString("</option>");
             } 
         }

         
        Object selectionValue = null;
        Object selectedValue = null;
        if (_selection != null)
        {
            selectionValue = _selection.valueInComponent(parent);
        }
        else if (_selectedValue != null)
        {
            selectedValue = _selectedValue.valueInComponent(parent);
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
             
             String valueAsString = null;
             String displayStringAsString = null;
             WOAssociation displayStringAssociation = null;
             if(ERXKeyValueCodingUtilities.fieldForKey(this, "_string") != null) {
            	 displayStringAssociation = (WOAssociation) ERXKeyValueCodingUtilities.privateValueForKey(this, "_string");
             } else {
            	 displayStringAssociation = (WOAssociation) ERXKeyValueCodingUtilities.privateValueForKey(this, "_disabled");
             }

             if (displayStringAssociation != null || _value != null) {

				if (displayStringAssociation != null) {
                    Object displayString = displayStringAssociation.valueInComponent(parent);
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
            if (_selection != null)
            {
                isSelectedItem = selectionValue == null ? false : selectionValue.equals(listItem);
            }
            else if (_selectedValue != null)
            {
                if (_value != null)
                {
                    isSelectedItem = selectedValue == null ? false : selectedValue.equals(valueAsString);
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
