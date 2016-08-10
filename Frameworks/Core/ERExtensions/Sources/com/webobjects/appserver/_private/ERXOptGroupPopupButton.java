package com.webobjects.appserver._private;


import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXPatcher.DynamicElementsPatches.PopUpButton;
import er.extensions.foundation.ERXValueUtilities;


/**
 * Quick hack at extending WOPopUpButton to use HTML 4 optgroups.  It adds two bindings:
 * group and label.  group is required.  When this value changes, a new optgroup is created.
 * label is optional.  This is used as the label for an option group.  If label is not
 * bound, an empty string is used as the option group label. 
 * 
 * Also adds bindings to style or disable individual items.
 * 
 * @binding group Object, required - keyPath to value that changes when the group of options changes
 * @binding label String, optional - String used as label for an option group
 * @binding itemClass, optional - String CSS class name for this item, browser support is inconsistent
 * @binding itemStyle, optional - String CSS style for this item, browser support is inconsistent
 * @binding itemDisabled, optional - boolean indicating if the current item should be disabled or not. 
 *          Browser support is inconsistent. Be prepared to handle the case where the user selects 
 *          a disabled option!
 */
public class ERXOptGroupPopupButton extends PopUpButton
{
    
    protected WOAssociation group;
    protected WOAssociation label;
    protected WOAssociation itemStyle;
    protected WOAssociation itemClass;
    protected WOAssociation itemDisabled;
    
    public ERXOptGroupPopupButton(String name, NSDictionary associations, WOElement template)
    {
        super(name, associations, template);
        group = _associations.removeObjectForKey("group");
        label = _associations.removeObjectForKey("label");
        itemStyle = _associations.removeObjectForKey("itemStyle");
        itemClass = _associations.removeObjectForKey("itemClass");
        itemDisabled = _associations.removeObjectForKey("itemDisabled");
        
        if (group == null)
        {
            throw new RuntimeException("Group is a required binding");
        }
    }
    

    @Override
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
             WOAssociation displayStringAssociation = null;
             if(ERXKeyValueCodingUtilities.fieldForKey(this, "_string") != null) {
            	 displayStringAssociation = (WOAssociation) ERXKeyValueCodingUtilities.privateValueForKey(this, "_string");
             } else {
            	 displayStringAssociation = (WOAssociation) ERXKeyValueCodingUtilities.privateValueForKey(this, "_displayString");
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
             
            if(itemDisabled != null) {
                if(ERXValueUtilities.booleanValue(itemDisabled.valueInComponent(parent))) {
                    response._appendTagAttributeAndValue("disabled", "disabled", false);
                }
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
