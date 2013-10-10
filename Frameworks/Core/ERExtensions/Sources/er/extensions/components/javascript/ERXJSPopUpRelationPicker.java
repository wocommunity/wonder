/*
 * Copyright (C) Quetzal Consulting, Inc. All rights reserved.
 *
 *    This class was originally developed by Robert A. Decker
 *    at Quetzal Consulting
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.components._private.ERXWOForm;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Very, very cool js component. Implements master-detail with js in two popups, ie the first popup could be say 
 * states and depending on which state is picked the second popup might reflect all of the cities of that state.<br />
 * This WOComponent displays two pop-up buttons. One pop-up displays a list of what can be thought of as parent entities. 
 * The second pop-up displays a list of what can be thought of as children entities. When a user selects an entity in 
 * the parent list, the child list is instantly modified to reflect the children entities available to the user 
 * through that parent. This is done through client-side Javascript. Also handles to-many selections both on the 
 * parent and the children.<br />
 * For example:
<pre><code>
parent1(child1,child2,child3)
parent2(child4,child5)
parent3(child2,child5)
</code></pre>
 * When the user selects parent1, its appropriate children are displayed in the second popup. 
 * If the user selects child2 in the children pop-up this is the value that is returned to the 
 * user through the childrenSelection variable. This is either an NSArray if <code>multiple</code> is true
 * or the single selected object.
 * For the display of the parent popup, if we aren't passed in a parentSelection, then we default to 
 * parentPopUpStringForAll. If we aren't given that either, then we default to the first parent in the array.
 * For the display of the child popup, if we aren't passed in a childrenSelection, then we default to childPopUpStringForAll. 
 * If we aren't given that either, then we default to the first child in the array.
 * 
 * @binding multiple boolean the defines if there can multiple parents and children selected.
 * @binding parentEntitiesList array of the parent objects that appear in the first pop-up.
 * @binding parentToChildrenRelationshipName name of the relationship from the parent to its possible children. This is used to fill the values that appear in the children popup.
 * @binding parentSelection currently selected parent(s) in the parent pop-up. This can be null, but will return the user-selected parent.
 * @binding childrenSelection returns the user-selected child(ren).
 * @binding parentDisplayValueName keypath of the parent displayed in the parent pop-up
 * @binding parentLabel value displayed in the table interface for the parent popup.
 * @binding childLabel value displayed in the table interface for the child popup.
 * @binding defaultChildKey keypath of the parent for the default child (eg, largest city)
 * @binding childrenSortKey keypath to sort the children on
 * @binding childDisplayValueName keypath of the child displayed in the child pop-up
 * @binding parentPopUpStringForAll  to display if no parent is chosen ("- all -")
 * @binding childPopUpStringForAll to display if no child is chosen ("- all -")
 * @binding size number of rows in select boxes
 * @binding possibleChildren shows only these values for children
 */

public class ERXJSPopUpRelationPicker extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXJSPopUpRelationPicker(WOContext aContext) {
        super(aContext);
    }

    /** logging support */
    public final static Logger log = Logger.getLogger(ERXJSPopUpRelationPicker.class);
    public final static Logger jsLog = Logger.getLogger("er.extensions.ERXJSPopUpRelationPicker.script");

    protected Integer _size;
    protected String _childDisplayValueName;
    protected String _parentDisplayValueName;

    protected NSArray _parentEntitiesList;
    protected String _parentToChildrenRelationshipName;

    protected NSArray _parentSelection;
    protected NSArray _childrenSelection;

    protected String _parentPopUpStringForAll; // for example, '-- all --' or '-- none --'. sets selectedParent to null. All children displayed for all parents if picked.
    protected String _childPopUpStringForAll; // for example, '-- all --' or '-- none --'. sets selectedChild to null

    protected String _parentLabel;
    protected String _childLabel;
    protected String _childrenSortKey;
    protected String _defaultChildKey;
    protected Boolean _multiple;
    protected NSArray _possibleChildren;
    
    protected String parentSelectName;
    protected String childSelectName;
    protected String pickerName;

    protected String objectsArrayName;
    
    private static final int NOT_FOUND = -1;
    private static final NSArray UNSET = new NSArray();
    
    @Override
    public void awake() {
        super.awake();
        updateVarNames();
    }

    protected void updateVarNames() {
        String elementID = context().elementID();
        elementID = StringUtils.replace(elementID,  ".", "_");
        pickerName = "picker_"+ elementID;
        parentSelectName = "parent_" + elementID;
        childSelectName = "child_" + elementID;
        objectsArrayName = "parents_children_" + elementID;
    }

    protected int offsetForID(String id) {
        if(!(id == null || id.trim().length() == 0 || "WONoSelectionString".equals(id))) {
            try {
                return Integer.parseInt(id);
            } catch (Exception e) {
                log.info("Exception while parsing ID", e);
            }
        }
        return NOT_FOUND;
    }
    
    protected Object parentFromID(String id) {
        int offset = offsetForID(id);
        if(offset != NOT_FOUND) {
            return parentEntitiesList().objectAtIndex(offset);
        }
        return null;
    }
    
    protected Object idForParent(Object parent) {
        if(parent != null) {
            return Integer.valueOf(parentEntitiesList().indexOfObject(parent));
        }
        return null;
    }
    
    protected Object childFromID(Object parent, String id) {
        if(id != null) {
            NSArray ids = NSArray.componentsSeparatedByString(id, "|");
            if(ids.count() == 2) {
                if(parent == null) {
                    parent = parentFromID((String)ids.objectAtIndex(0));
                }
                int offset = offsetForID((String)ids.objectAtIndex(1));
                if(offset != NOT_FOUND && parent != null) {
                    return sortedChildren(parent).objectAtIndex(offset);
                }
            } else {
                log.info("Child ID not valid: " + id);
            }
        }
        return null;
    }
    
	protected int offsetForChild(Object parent, Object child) {
		return parent != null ? sortedChildren(parent).indexOfObject(child) : NOT_FOUND;
    }
	
    protected Object idForChild(Object parent, Object child) {
        if(parent != null) {
            int offset = sortedChildren(parent).indexOfObject(child);
            if(offset != NOT_FOUND) {
                return idForParent(parent) + "|" + offset;
            }
        } else {
            for (Enumeration parents = parentEntitiesList().objectEnumerator(); parents.hasMoreElements();) {
                Object aParent = parents.nextElement();
                int offset = sortedChildren(aParent).indexOfObject(child);
                if(offset != NOT_FOUND) {
                    return idForParent(aParent) + "|" + offset;
                }
           }
        }
        return null;
    }
    
   
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context) {
        NSArray parentFormValues = request.formValuesForKey(parentSelectName);
        NSArray childFormValues = request.formValuesForKey(childSelectName);
        if(parentFormValues != null && childFormValues != null) {
            if(parentFormValues.containsObject("WONoSelectionString")) {
                setSelectedParents(null);
            } else {
                NSMutableArray parents = new NSMutableArray();
                for (Enumeration ids = parentFormValues.objectEnumerator(); ids.hasMoreElements();) {
                    Object parent = parentFromID((String) ids.nextElement());
                    if(parent != null) {
                        parents.addObject(parent);
                    }
                }
                setSelectedParents(parents);
            }

            if(childFormValues.containsObject("WONoSelectionString")) {
                NSArray children = (NSArray)parentSelection().valueForKeyPath(parentToChildrenRelationshipName());
                if(parentFormValues.containsObject("WONoSelectionString")) {
                    setChildrenSelection(null);
                } else {
                    if(!multiple()) {
                        setChildrenSelection(null);
                    } else {
                        setChildrenSelection(ERXArrayUtilities.flatten(children));
                    }
                }
            } else {
                NSMutableArray children = new NSMutableArray();
                for (Enumeration ids = childFormValues.objectEnumerator(); ids.hasMoreElements();) {
                    Object child = childFromID(null, (String) ids.nextElement());
                    if(child != null) {
                        children.addObject(child);
                    }
                }
                setChildrenSelection(children);
            }
        }
        super.takeValuesFromRequest(request, context);
    }

    protected NSArray possibleChildren() {
        if(_possibleChildren != null) {
            _possibleChildren = (NSArray) valueForBinding("possibleChildren");
            if(_possibleChildren == null) {
                _possibleChildren = UNSET;
            }
        }
        return _possibleChildren == UNSET ? null : _possibleChildren;
    }
    
    protected NSArray unsortedChildren(Object parent) {
        NSArray result = (NSArray)NSKeyValueCodingAdditions.Utility.valueForKeyPath(parent, parentToChildrenRelationshipName() 
                + ((parent instanceof NSArray) ? ".@flatten.@removeNullValues" : ""));
        NSArray restrictedChoices = possibleChildren();
        if(restrictedChoices != null) {
            result = ERXArrayUtilities.intersectingElements(result, restrictedChoices);
        }
        return result;
    }
    
    protected NSArray sortedChildren(Object parent) {
        EOSortOrdering sortOrdering=new EOSortOrdering(childrenSortKey(), EOSortOrdering.CompareAscending);
        NSMutableArray sortArray=new NSMutableArray(sortOrdering);
        NSArray result=EOSortOrdering.sortedArrayUsingKeyOrderArray(unsortedChildren(parent), sortArray);
        return result!=null ? result : NSArray.EmptyArray;
    }

    public String jsString() {
        // this method returns all the javascript we need to embed in the web page.
        StringBuffer returnString;

        returnString = new StringBuffer(2000);
        returnString.append("\n");

        // This Javascript string builds an array of Entity objects on the browser end.
        returnString.append("" + objectArrayCreationString() + "\n");
        if (jsLog.isDebugEnabled()) jsLog.debug("JSPopUpRelationPicker jsString  returnString is " + returnString);
        return returnString.toString();
    }

    public String hiddenFormElementStrings() {
        StringBuffer returnString;

        returnString = new StringBuffer(500);
        returnString.append("\n<script language=\"JavaScript\">");
		String childToSelect = "";
		if (!multiple() && 
			childrenSelection()!=null && childrenSelection().count()==1 &&
			parentSelection()!=null && parentSelection().count()==1) {
			int childToSelectInt=offsetForChild(parentSelection().objectAtIndex(0), 
												childrenSelection().objectAtIndex(0));
			if (childToSelectInt!=NOT_FOUND) childToSelect=""+childToSelectInt;
		}
		
        returnString.append("\nvar " + pickerName +" = new ERXJSPopupRelationshipPicker("
        + objectsArrayName + ","
        + "window.document." + formName() + "." + parentSelectName + "," 
        + (parentPopUpStringForAll() != null ? "\"" + parentPopUpStringForAll().replace('\n',' ') + "\"" : "null") + ","
        + "window.document." + formName() + "." + childSelectName + "," 
        + (childPopUpStringForAll() != null ? "\"" + childPopUpStringForAll() + "\"" : "null")
        +");\n"
        + (parentPopUpStringForAll() == null ? pickerName + ".parentChanged("+childToSelect+");"	: "")
        +"\n</script>");
        log.debug(returnString);
		// trigger an update of the parent - this causes the child to be properly set to a sub selection (instead of listing all possible value) when
		// we are editing a new object
        return returnString.toString();
    }

    public String parentPopUpString() {
        StringBuffer returnString = selectHeader(parentSelectName, pickerName + ".parentChanged();");

        if (parentSelection().count() == 0 && parentPopUpStringForAll()==null)
            setSelectedParents(new NSArray(parentEntitiesList().objectAtIndex(0)));
        int iCount = parentEntitiesList().count();
        for (int i=0;i<iCount;i++) {
            Object aEntity = parentEntitiesList().objectAtIndex(i);
            returnString.append("\t<option ");
            if (isSelectedParent(aEntity)) {
                returnString.append("selected=\"selected\" ");
            }
            returnString.append("value=\"" + idForParent(aEntity) + "\">");
            returnString.append(NSKeyValueCodingAdditions.Utility.valueForKeyPath(aEntity, parentDisplayValueName()));
            returnString.append("</option>\n");
        }
        returnString.append("</select>\n");
        return returnString.toString();
    }

    public String formName() {
        return ERXWOForm.formName(context(), "forms[0]");
    }
    
	
    /**
     * @return the string to create the pop-up with the initial child values something like:
     <pre>&lt;select name="children_select"&gt;
     &lt;option value=4&gt;poodle
     &lt;option selected value=5&gt;puli
     &lt;option value=6&gt;greyhound
     &lt;/select&gt;</pre>
     */
    public String childPopUpString() {
        StringBuffer returnString = selectHeader(childSelectName, pickerName + ".childChanged();");

        if (parentSelection().count() != 0) {
            if (childrenSelection().count() == 0 && defaultChildKey() != null)
                setChildrenSelection((NSArray)NSKeyValueCodingAdditions.Utility.valueForKeyPath(parentSelection(), defaultChildKey() + ".@flatten"));
            appendChildPopupStringWithParent(returnString, parentSelection());
        } else {
            // nothing is selected in the parent, so set the children array to all possible child values
            // run through all parents, getting each child. However, if we don't have a parentPopUpStringForAll then we only do it for the last parent.
            if (parentPopUpStringForAll() != null) {
                appendChildPopupStringWithParent(returnString, parentEntitiesList());
            } else {
                // only do the last parent because we don't have a selected parent AND we don't have the possibility 
                // of setting the parent to 'All'
                Object aParent = parentEntitiesList().objectAtIndex(0);
                setChildrenSelection(defaultChildKey()!=null ?
                        (NSArray)NSKeyValueCodingAdditions.Utility.valueForKeyPath(parentSelection(), defaultChildKey() + ".@flatten") : NSArray.EmptyArray);
                appendChildPopupStringWithParent(returnString, new NSArray(aParent));
            }
        }

        returnString.append("</select>\n");
        if (jsLog.isDebugEnabled()) jsLog.debug("JSPopUpRelationPicker childPopUpString  returnString is " + returnString);
        return returnString.toString();
    }
    
    private void appendChildPopupStringWithParent(StringBuffer returnString, NSArray aParents) {
        NSArray children = sortedChildren(aParents);
        // write out each of the values for the options tags. be sure to set the selected tag if necessary
        int iCount = children.count();
        for (int i=0;i<iCount;i++) {
            Object aChild = children.objectAtIndex(i);
            returnString.append("\t<option ");
            if (((i == iCount-1) && (childrenSelection().count() == 0) && (childPopUpStringForAll() == null)) 
                    || (isSelectedChild(aChild))) {
                returnString.append("selected=\"selected\" ");
            }
            returnString.append("value=\"" + idForChild(null, aChild) + "\">");
            returnString.append(NSKeyValueCodingAdditions.Utility.valueForKeyPath(aChild, childDisplayValueName()));
            returnString.append("</option>\n");
        }
    }

    protected StringBuffer selectHeader(String nm, String onChange) {
        StringBuffer returnString;
        
        returnString = new StringBuffer(1000);
        returnString.append("<select name=\"" + nm + "\"" + " size=\"" + size() + "\"");
        if (onChange != null) {
            returnString.append(" onChange=\"" + onChange + "\"");
        }
        if (multiple()) {
            returnString.append(" multiple=\"multiple\"");
        }
        returnString.append(">\n");
        return returnString;
    }
    
    public String objectArrayCreationString() {
        // here's an example of the string this method should return:
        //var parentschildren = new Array(new Entity("dogs","1",new Array(new Entity("poodle","4",null,false),new Entity("puli","5",null,true),new Entity("greyhound","5",null,false)),false), new Entity("fish","2",new Array(new Entity("trout","6",null,true),new Entity("mackerel","7",null,false),new Entity("bass","8",null,false)),true), new Entity("birds","3",new Array(new Entity("robin","9",null,false),new Entity("hummingbird","10",null,false),new Entity("crow","11",null,true)),false));

        StringBuffer returnString = new StringBuffer(1000);
        returnString.append("var "+objectsArrayName+" = [");

        int iCount = parentEntitiesList().count();
        for (int i=0;i<iCount;i++) {
            Object aParent = parentEntitiesList().objectAtIndex(i);
            returnString.append("\n\tnew Entity(");
            returnString.append(" \"" + NSKeyValueCodingAdditions.Utility.valueForKeyPath(aParent, parentDisplayValueName()) + "\",");
            returnString.append(" \"" + idForParent(aParent) + "\",");
            returnString.append(" \"" + System.identityHashCode(aParent) + "\",");

            // now do all the possible children of the parent. Each child should look like 'new Entity("poodle","4",null,false)'
            returnString.append(" [");
            NSArray childrenOfAParent = sortedChildren(aParent);

            int jCount = childrenOfAParent.count();
            Object defaultChild=defaultChildKey()!=null ? NSKeyValueCodingAdditions.Utility.valueForKeyPath(aParent, defaultChildKey()) : null;
            int defaultChildIndex=-1;

            for (int j=0;j<jCount;j++) {
                Object aChild = childrenOfAParent.objectAtIndex(j);
                returnString.append("\n\t\t new Entity(");
                returnString.append(" \"" + NSKeyValueCodingAdditions.Utility.valueForKeyPath(aChild, childDisplayValueName()) + "\","); // visible text of pop-up
                returnString.append(" \"" + idForChild(aParent, aChild) + "\","); // value text of pop-up
                returnString.append(" \"" + System.identityHashCode(aChild) + "\",");
                returnString.append(" null,");
                if (isSelectedChild(aChild)) {
                    returnString.append(" true");
                } else {
                    returnString.append(" false");
                }
                returnString.append(", null");
                returnString.append(")");
                if (j != jCount - 1) {
                    // append a comma and a space
                    returnString.append(", ");
                }
                if (aChild==defaultChild) defaultChildIndex=j;
            }
            returnString.append("],");
            if (isSelectedParent(aParent)) { // in the single case, the parent will be updated when we call parent changed
                returnString.append(" true");
            } else {
                returnString.append(" false");
            }
            returnString.append(", ");
            returnString.append(defaultChild!=null ? "\""+defaultChildIndex+"\"" : "-1");
            returnString.append(")");


            if (i != iCount - 1) {
                // append a comma and a space
                returnString.append(", ");
            }
        }
        returnString.append("];");
        return returnString.toString();
    }

    /**
     * @param aParent
     */
    private boolean isSelectedParent(Object aParent) {
        return parentSelection().containsObject(aParent);
    }

    /**
     * @param aChild
     */
    private boolean isSelectedChild(Object aChild) {
        return childrenSelection().containsObject(aChild);
    }

    public NSArray parentEntitiesList() {
        if(_parentEntitiesList == null) {
            _parentEntitiesList = (NSArray)valueForBinding("parentEntitiesList");
        }
        return _parentEntitiesList;
    }
    public NSArray parentSelection() {
        if(_parentSelection == null) {
            if(multiple()) {
                _parentSelection = (NSArray)valueForBinding("parentSelection");
            } else {
                Object parent = valueForBinding("parentSelection");
                if(parent != null) {
                    _parentSelection = new NSArray(parent);
                }
            }
            if(_parentSelection == null) {
                _parentSelection = NSArray.EmptyArray;                   
            } 
        }
        return _parentSelection;
    }
    public void setSelectedParents(NSArray value) {
        if(!multiple() && canSetValueForBinding("parentSelection")) {
            setValueForBinding(value == null ? null : value.lastObject(), "parentSelection");
        } else if(multiple() && canSetValueForBinding("parentSelection")) {
            setValueForBinding(value, "parentSelection");
        }
        _parentSelection = value;
    }

    public NSArray childrenSelection() {
        if(_childrenSelection == null) {
            if(multiple()) {
                _childrenSelection = (NSArray)valueForBinding("childrenSelection");
            } else {
                Object child = valueForBinding("childrenSelection");
                if(child != null) {
                    _childrenSelection = new NSArray(child);
                }
            }
            if(_childrenSelection == null) {
                _childrenSelection = NSArray.EmptyArray;                   
            } 
       }
        return _childrenSelection;
    }
    public void setChildrenSelection(NSArray value) {
        if(!multiple() && canSetValueForBinding("childrenSelection")) {
            setValueForBinding(value == null ?  null: value.lastObject(), "childrenSelection");
        } else if(multiple() && canSetValueForBinding("childrenSelection")) {
            setValueForBinding(value, "childrenSelection");
        }
        _childrenSelection = value;
    }

    public String defaultChildKey() {
        if(_defaultChildKey == null) {
            _defaultChildKey = (String)valueForBinding("defaultChildKey");
        }
        return _defaultChildKey;
    }
    public String childrenSortKey() {
        if(_childrenSortKey == null) {
            _childrenSortKey = (String)valueForBinding("childrenSortKey");
        }
        return _childrenSortKey;
    }

    public String childLabel() {
        if(_childLabel == null) {
            _childLabel = (String)valueForBinding("childLabel");
            if(_childLabel == null)
                _childLabel = "Types";
        }
        return _childLabel;
    }

    public String parentLabel() {
        if(_parentLabel == null) {
            _parentLabel = (String)valueForBinding("parentLabel");
            if(_parentLabel == null)
                _parentLabel = "Categories";
        }
        return _parentLabel;
    }

    public String childDisplayValueName() {
        if(_childDisplayValueName == null) {
            _childDisplayValueName = (String)valueForBinding("childDisplayValueName");
        }
        return _childDisplayValueName;
    }
    public String parentDisplayValueName() {
        if(_parentDisplayValueName == null) {
            _parentDisplayValueName = (String)valueForBinding("parentDisplayValueName");
        }
        return _parentDisplayValueName;
    }
    public String parentToChildrenRelationshipName() {
        if(_parentToChildrenRelationshipName == null) {
            _parentToChildrenRelationshipName = (String)valueForBinding("parentToChildrenRelationshipName");
        }
        return _parentToChildrenRelationshipName;
    }
    public String parentPopUpStringForAll() {
        if(_parentPopUpStringForAll == null) {
            _parentPopUpStringForAll = (String)valueForBinding("parentPopUpStringForAll");
        }
        return _parentPopUpStringForAll;
    }
    public String childPopUpStringForAll() {
        if(_childPopUpStringForAll == null) {
            _childPopUpStringForAll = (String)valueForBinding("childPopUpStringForAll");
        }
        return _childPopUpStringForAll;
    }
    public int size() {
        if(_size == null) {
            _size = Integer.valueOf(intValueForBinding("size", multiple() ? 5 : 1));
        }
        return _size.intValue();
    }
    public boolean multiple() {
        if(_multiple == null) {
            _multiple = booleanValueForBinding("multiple") ? Boolean.TRUE : Boolean.FALSE;
        }
        return _multiple.booleanValue();
    }
    
    @Override
    public void reset() {
        super.reset();
        _childrenSelection = null;
        _parentSelection = null;
        _parentEntitiesList = null;
        _childLabel = null;
        _parentLabel = null;
        _defaultChildKey = null;
        _childrenSortKey = null;
        _multiple = null;

        _childDisplayValueName = null;
        _parentDisplayValueName = null;
        _parentToChildrenRelationshipName = null;
        _parentPopUpStringForAll = null;
        _childPopUpStringForAll = null;
        _possibleChildren = null;
        _size = null;
    }
}
