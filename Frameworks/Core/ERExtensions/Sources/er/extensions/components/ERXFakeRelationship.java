//
// ERXFakeRelationship.java: Class file for WO Component 'ERXFakeRelationship'
// Project simple
//
// Created by ak on Tue Mar 26 2002
//
package er.extensions.components;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEOControlUtilities;

/**
 * UI and support methods to edit "relations" to objects flattened into a text field (e.g. languages = "-de-en-"). Useful when you don't need referential integrity but only a quick place to store flags and the like.<br />
 * 
 * @binding dataSource
 * @binding destinationDisplayKey
 * @binding isMandatory
 * @binding relationshipKey
 * @binding sourceEntityName
 * @binding sourceObject
 * @binding uiStyle
 * @binding isToMany
 * @binding destinationEntityName
 * @binding size
 * @binding maxColumns
 */

public class ERXFakeRelationship extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(ERXFakeRelationship.class.getName());

    // temps for our children
    NSArray theList;
    EOEnterpriseObject theCurrentItem;
    String theCurrentValue;
    boolean isMandatoryRead;
    NSArray selections;

    // bindings from the parent component
    EOEnterpriseObject sourceObject;
    String destinationEntityName;
    String sourceEntityName;
    EODatabaseDataSource dataSource;
    boolean isMandatory;
    String delimiter;
    String relationshipKey;
    String destinationDisplayKey;
    String uiStyle;

    
    public ERXFakeRelationship(WOContext context) {
        super(context);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public String delimiter() {
	if(delimiter == null) {
	    if(hasBinding("delimiter"))
		delimiter = (String)valueForBinding("delimiter");
	    else
		delimiter = "\n";
	}
	return delimiter;
    }
    
    public NSArray theList() {
	if(theList == null) {
            NSMutableArray list = dataSource().fetchObjects().mutableClone();
	    NSArray orderings = new NSArray(EOSortOrdering.sortOrderingWithKey(destinationDisplayKey(), EOSortOrdering.CompareAscending));
	    EOSortOrdering.sortArrayUsingKeyOrderArray(list, orderings);
	    theList = list;
	}
	return theList;
    }
    public void setTheList(NSArray aValue) {
	theList = aValue;
    }

    public String theCurrentValue()  {
	return (String)((NSKeyValueCoding)theCurrentItem()).valueForKey(destinationDisplayKey());
    }
    public void setTheCurrentValue(String aValue) {
	theCurrentValue = aValue;
    }

    public EOEnterpriseObject theCurrentItem()  {
	return theCurrentItem;
    }
    public void setTheCurrentItem(EOEnterpriseObject aValue) {
	theCurrentItem = aValue;
    }

    public static void setFakeRelationshipForKey(EOEnterpriseObject sourceObject, NSArray objects, String relationshipKey, String destinationEntityName, String delimiter) {
	if(objects.count() > 0) {
	    StringBuilder newValue = new StringBuilder();
	    Enumeration e = objects.objectEnumerator();

	    while(e.hasMoreElements()) {
		EOEnterpriseObject rel = (EOEnterpriseObject)e.nextElement();
		newValue.append(ERXEOControlUtilities.primaryKeyStringForObject(rel));
		newValue.append(delimiter);
	    }
	    sourceObject.takeValueForKey(delimiter + newValue.toString(), relationshipKey);
	} else {
	    sourceObject.takeValueForKey(null, relationshipKey);
	}
    }

    public static NSArray fakeRelationshipForKey(EOEnterpriseObject sourceObject, String relationshipKey, String destinationEntityName, String delimiter) {
	String selectionString = (String)sourceObject.valueForKey(relationshipKey);
	NSArray selectionsKeys = NSArray.componentsSeparatedByString(selectionString, delimiter);
	NSMutableArray selections = new NSMutableArray(selectionsKeys.count());
	Enumeration e = selectionsKeys.objectEnumerator();
	EOEntityClassDescription cd = (EOEntityClassDescription)EOClassDescription.classDescriptionForEntityName(destinationEntityName);
	NSArray pks = cd.entity().primaryKeyAttributes();

	if(pks.count() != 1) {
	    throw new IllegalArgumentException("The destination entity's primary key can't be compound.");
	}

	EOAttribute pk = (EOAttribute)pks.objectAtIndex(0);

	// this could be more bullet proof
	boolean hasStringPk = pk.className().equals("java.lang.String");
	log.debug(pk.className());

	while(e.hasMoreElements()) {
	    String s = (String)e.nextElement();
	    Object pkValue = null;
	    try {
		if(s.length() > 0) {
		    if(!hasStringPk)  {
			pkValue = Integer.valueOf(s);
		    } else {
			pkValue = s;
		    }
		    EOEnterpriseObject eo = EOUtilities.objectWithPrimaryKeyValue(sourceObject.editingContext(), destinationEntityName, pkValue);
		    selections.addObject(eo);
		}
	    } catch(Exception ex) {
		log.warn(ex + " with pkValue " + pkValue);
		// we do nothing here, when we reconstruct the array on setSelection, we simply ignore this value
	    }
	}
	return selections;
    }

    public NSArray selections() {
	if(selections == null) {
	    selections = ERXFakeRelationship.fakeRelationshipForKey(sourceObject(), relationshipKey(), destinationEntityName(), delimiter());
	}
	return selections;
    }

    public void setSelections(NSArray aValue)  {
	ERXFakeRelationship.setFakeRelationshipForKey(sourceObject(), aValue, relationshipKey(), destinationEntityName(), delimiter());
	selections = aValue;
    }


    public EOEnterpriseObject selection() {
	NSArray sel =  selections();
	if(sel.count() == 1)
	    return (EOEnterpriseObject)sel.objectAtIndex(0);
	return null;
    }

    public void setSelection(EOEnterpriseObject aValue) {
	NSArray sel;
	if(aValue == null)
	    sel = new NSArray();
	else
	    sel = new NSArray(aValue);
	setSelections(sel);
    }

    // bindings we pull
    
    public String sourceEntityName() {
        if(sourceEntityName == null) {
	    sourceEntityName = (String)valueForBinding("sourceEntityName");
	    if(sourceEntityName == null) {
		sourceEntityName = sourceObject().entityName();
	    }
	}
	return sourceEntityName;
    }

    public EOEnterpriseObject sourceObject() {
        if(sourceObject == null) {
	    sourceObject = (EOEnterpriseObject)valueForBinding("sourceObject");
	}
	return sourceObject;
    }

    public String relationshipKey() {
        if(relationshipKey == null) {
	    relationshipKey = (String)valueForBinding("relationshipKey");
	}
	return relationshipKey;
    }

    public String destinationDisplayKey() {
        if(destinationDisplayKey == null) {
	    destinationDisplayKey = (String)valueForBinding("destinationDisplayKey");
	    if(destinationDisplayKey == null) {
		destinationDisplayKey = "userPresentableDescription";
	    }
	}
	return destinationDisplayKey;
    }

    public String destinationEntityName() {
        if(destinationEntityName == null) {
	    destinationEntityName = (String)valueForBinding("destinationEntityName");
	}
	return destinationEntityName;
    }

    public EODatabaseDataSource dataSource() {
        if(dataSource == null) {
	    dataSource = (EODatabaseDataSource)valueForBinding("dataSource");
	    if(dataSource == null) {
		dataSource = new EODatabaseDataSource(sourceObject().editingContext(), destinationEntityName());
	    }
	}
	return dataSource;
    }

    public String uiStyle() {
        if(uiStyle == null) {
	    uiStyle = (String)valueForBinding("uiStyle");
	    if(uiStyle == null)
		uiStyle = "browser";
	}
        return uiStyle;
    }

    boolean isMandatory() {
        if(!isMandatoryRead) {
	    EOEntityClassDescription cd = (EOEntityClassDescription)EOClassDescription.classDescriptionForEntityName(sourceEntityName());
	    isMandatory = !cd.entity().attributeNamed(destinationDisplayKey()).allowsNull();
	    isMandatoryRead = true;
	}
        return isMandatory;
    }

    public void _invalidateCaches() {
	theList = null;
	theCurrentItem = null;
	theCurrentValue = null;
	isMandatoryRead = false;
	selections = null;

	// bindings from the parent component
	sourceObject = null;
	destinationEntityName = null;
	sourceEntityName = null;
	dataSource = null;
	isMandatory = false;
	delimiter = null;
	relationshipKey = null;
	destinationDisplayKey = null;
	uiStyle = null;
    }

    @Override
    public void reset() {
        _invalidateCaches();
    }

    public boolean isBrowser() {
	return uiStyle().equals("browser");
    }

}
