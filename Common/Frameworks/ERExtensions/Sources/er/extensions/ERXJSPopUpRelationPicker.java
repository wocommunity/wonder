/*
 * Copyright (C) Quetzal Consulting, Inc. All rights reserved.
 *
 *    This class was originally developed by Robert A. Decker
 *    at Quetzal Consulting
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Very, very cool js component. Implements master-detail with js in two popups, ie the first popup could be say 
 * states and depending on which state is picked the second popup might reflect all of the cities of that state.<br />
 * This WOComponent displays two pop-up buttons. One pop-up displays a list of what can be thought of as parent entities. 
 * The second pop-up displays a list of what can be thought of as children entities. When a user selects an entity in 
 * the parent list, the child list is instantly modified to reflect the children entities available to the user 
 * through that parent. This is done through client-side Javascript.
 * For example:
<pre><code>
parent1(child1,child2,child3)
parent2(child4,child5)
parent3(child2,child5)
</code></pre>
 * When the user selects parent1, its appropriate children are displayed in the second popup. 
 * If the user selects child2 in the children pop-up this is the value that is returned to the 
 * user through the selectedChild variable.
 * For the display of the parent popup, if we aren't passed in a selectedParent, then we default to 
 * parentPopUpStringForAll. If we aren't given that either, then we default to the last parent in the array.
 * For the display of the child popup, if we aren't passed in a selectedChild, then we default to childPopUpStringForAll. 
 * If we aren't given that either, then we default to the last child in the array.
 * 
 * @binding parentEntitiesList array of the parent objects that appear in the first pop-up.
 * @binding parentToChildrenRelationshipName name of the relationship from the parent to its possible children. This is used to fill the values that appear in the children popup.
 * @binding selectedParent currently selected parent in the parent pop-up. This can be null, but will return the user-selected parent.
 * @binding selectedChild set to null. Returns the user-selected child.
 * @binding parentDisplayValueName keypath of the parent displayed in the parent pop-up
 * @binding parentLabel value displayed in the table interface for the parent popup.
 * @binding childLabel value displayed in the table interface for the child popup.
 * @binding defaultChildKey keypath of the parent for the default child (eg, largest city)
 * @binding childrenSortKey keypath to sort the children on
 * @binding childDisplayValueName keypath of the child displayed in the child pop-up
 * @binding parentPopUpStringForAll  to display if no parent is chosen ("- all -")
 * @binding childPopUpStringForAll to display if no child is chosen ("- all -")
 */

public class ERXJSPopUpRelationPicker extends ERXStatelessComponent {

    public ERXJSPopUpRelationPicker(WOContext aContext) {
        super(aContext);
    }

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERXJSPopUpRelationPicker.class);
    public final static ERXLogger jsLog = ERXLogger.getERXLogger("er.extensions.ERXJSPopUpRelationPicker.script");

    protected String childDisplayValueName;
    protected String parentDisplayValueName;

    protected NSArray parentEntitiesList;
    protected String parentToChildrenRelationshipName;

    protected Object selectedParent;
    protected Object selectedChild;

    protected String parentPopUpStringForAll; // for example, '-- all --' or '-- none --'. sets selectedParent to null. All children displayed for all parents if picked.
    protected String childPopUpStringForAll; // for example, '-- all --' or '-- none --'. sets selectedChild to null

    protected String parentSelectName;
    protected String childSelectName;

    protected String parentLabel;
    protected String childLabel;
    protected String childrenSortKey;
    protected String defaultChildKey;
    protected String selectedParentId;
    protected String selectedChildId;
    protected String parentsChildrenId;
    
    protected String elementID;
    
    public void awake() {
        super.awake();
    }

    protected void updateVarNames() {
        String elementID = context().elementID();
        elementID = ERXStringUtilities.replaceStringByStringInString(".", "_", elementID);
        //elementID = "foo";
        parentSelectName = "parent_" + elementID;
        childSelectName = "child_" + elementID;
        selectedParentId = "selected_parent_" + elementID;
        selectedChildId = "selected_child_" + elementID;
        parentsChildrenId = "parents_children_" + elementID;
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        updateVarNames();
        super.appendToResponse(response, context);
    }
    
    public void takeValuesFromRequest(WORequest request, WOContext context) {
        // get the form values for selected_parent_id and selected_child_id and use these to set the selectedParent and selectedChild values
        // takeValues always returns a String, but sometimes it's an empty string if no value is set on the form element.
        // the values returned correspond to the hashCode's of the objects, not the entityId's. This allows us to use this class with objects that don't inherit from NSObject.
        updateVarNames();


        String parent_id = request.stringFormValueForKey(selectedParentId);
        String child_id = request.stringFormValueForKey(selectedChildId);

        if(parent_id == null || parent_id.trim().length() == 0 || "null".equals(parent_id) || (parentPopUpStringForAll() != null && parentPopUpStringForAll().equals(parent_id))) {
            setSelectedParent(null);
        } else {
            try {
                int parentIdToHash = Integer.parseInt(parent_id);
                setSelectedParent(parentWithHashCode(parentIdToHash));
            } catch (Exception e) {
                log.info("Exception while getting parent" ,e);
            }
        }

        if(child_id == null || child_id.trim().length() == 0 || "null".equals(child_id) || (childPopUpStringForAll() != null && childPopUpStringForAll().equals(child_id))) {
            setSelectedChild(null);
        } else {
            try {
                int childIdToHash = Integer.parseInt(child_id);
                setSelectedChild(childWithHashCode(childIdToHash));
            } catch (Exception e) {
                setSelectedChild(null);
                log.info("Exception while getting child" ,e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("selected_parent_id: " + parent_id);
            log.debug("selectedParent: " + selectedParent());
            
            log.debug("selected_child_id: " + child_id);
            log.debug("selectedChild: " + selectedChild());
        }
        super.takeValuesFromRequest(request, context);
    }

    protected NSArray unsortedChildren(Object parent) {
        return (NSArray)NSKeyValueCoding.Utility.valueForKey(parent, parentToChildrenRelationshipName());
    }
    
    protected NSArray sortedChildren(Object parent) {
        EOSortOrdering sortOrdering=new EOSortOrdering(childrenSortKey(), EOSortOrdering.CompareAscending);
        NSMutableArray sortArray=new NSMutableArray(sortOrdering);
        NSArray result=EOSortOrdering.sortedArrayUsingKeyOrderArray(unsortedChildren(parent), sortArray);
        return result!=null ? result : NSArray.EmptyArray;
    }

    protected Object childWithHashCode(int hashCode) {
        // run through the parents and all of their children and find a child with the hash code and return it, else return null
        int iCount = parentEntitiesList().count();
        for (int i=0;i<iCount;i++) {
            Object aParent = (Object)parentEntitiesList().objectAtIndex(i);
            NSArray children = unsortedChildren(aParent);
            int jCount = children.count();
            for (int j=0;j<jCount;j++) {
                Object aChild = (Object)children.objectAtIndex(j);
                if (aChild.hashCode() == hashCode) {
                    return aChild;
                }
            }
        }
        return null;
    }

    protected Object parentWithHashCode(int hashCode) {
        // run through the parents and find one with the hash code and return it, else return null
        int iCount = parentEntitiesList().count();
        for (int i=0;i<iCount;i++) {
            Object aEntity = (Object)parentEntitiesList().objectAtIndex(i);
            if (aEntity.hashCode() == hashCode) {
                return aEntity;
            }
        }
        return null;
    }

    public String jsString() {
        // this method returns all the javascript we need to embed in the web page.
        StringBuffer returnString;

        returnString = new StringBuffer(2000);
        returnString.append("\n");

        // This Javascript string builds an array of Entity objects on the browser end.
        returnString.append("" + parentschildrenArrayCreationString() + "\n");
        if (parentPopUpStringForAll() != null) {
            returnString.append("var parentPopUpStringForAll = \"" + parentPopUpStringForAll() + "\";\n");
        } else {
            returnString.append("var parentPopUpStringForAll = null;\n");
        }
        if (childPopUpStringForAll() != null) {
            returnString.append("var childPopUpStringForAll = \"" + childPopUpStringForAll() + "\";\n");
        } else {
            returnString.append("var childPopUpStringForAll = null;\n");
        }
        returnString.append("\n");


        if (jsLog.isDebugEnabled()) jsLog.debug("JSPopUpRelationPicker jsString  returnString is " + returnString);
        return returnString.toString();
    }

    

    /**
     * @return
     */
    private String hiddenField(String name, Object value) {
        return "<input type=\"hidden\" name=\""+name+"\"" + (value == null ? ""  : " value=\"" + value.hashCode()) + "\" />\n";
    }
    
   /*
     Should look something like:
     <input type=hidden name=selected_parent_id value=>
     <input type=hidden name=selected_child_id value=>
     */
    public String hiddenFormElementStrings() {
        StringBuffer returnString;

        returnString = new StringBuffer(500);
        returnString.append(hiddenField(selectedParentId, selectedParent()));
        returnString.append(hiddenField(selectedChildId, selectedChild()));

        String children = "allChildren("+parentsChildrenId+")";
        if(selectedParent() != null) {
            children = "getParentEntityForId(" + parentsChildrenId + "," + selectedParent().hashCode() + ").children";
        }
        returnString.append("<script language=\"JavaScript\">\nsetSelectToArrayOfEntities("
               +"window.document." + formName() + "." + childSelectName + "," 
               + children +",childPopUpStringForAll)\n</script>\n");
        return returnString.toString();
    }



    /**
     * @returns the string to create the pop-up with the initial parent values something like:
        <select name="parent_select" onChange="parentSwapped(window.document.the_form.parent_select.options[selectedIndex].value);">
        <option selected value=1>dogs
        <option value=2>fish
        <option value=3>birds
        </select>
     */
    public String parentPopUpString() {
        String onChangeString = "parentSwapped("+parentsChildrenId+",window.document." + formName() 
        	+ "." + parentSelectName + ".options[selectedIndex].value,window.document."+ formName()
        	+ "." + childSelectName +"," + selectedParentId + "," + selectedChildId +");";
        StringBuffer returnString = selectHeader(parentSelectName, onChangeString, selectedParent(), parentPopUpStringForAll());

        // write out each of the values for the options tags. be sure to set the selected tag if necessary
        if (selectedParent()==null && parentPopUpStringForAll()==null)
            setSelectedParent(parentEntitiesList().objectAtIndex(0));
        int iCount = parentEntitiesList().count();
        for (int i=0;i<iCount;i++) {
            Object aEntity = (Object)parentEntitiesList().objectAtIndex(i);
            returnString.append("\t<option ");
            if (aEntity.equals(selectedParent())) {
                returnString.append("selected=\"selected\" ");
            }
            returnString.append("value=\"" + aEntity.hashCode() + "\">");
            returnString.append(NSKeyValueCoding.Utility.valueForKey(aEntity, parentDisplayValueName()));
            returnString.append("\n");
        }
        returnString.append("</select>\n");
        return returnString.toString();
    }

    public String formName() {
        String result;
        if(context() instanceof ERXMutableUserInfoHolderInterface) {
            result = (String)((ERXMutableUserInfoHolderInterface)context()).mutableUserInfo().valueForKey("formName");
        } else {
            result = "forms[0]";
        }
        return result;
    }
    
    /**
     * @returns the string to create the pop-up with the initial child values something like:
     <select name="children_select">
     <option value=4>poodle
     <option selected value=5>puli
     <option value=6>greyhound
     </select>
     */
    public String childPopUpString() {
        String onChangeString = "childSwapped("+parentsChildrenId + ","
        +"window.document." + formName() + "." + childSelectName + ".options[selectedIndex].value,"
        +"window.document." + formName() + "." + parentSelectName+","
        +selectedParentId+","
        +selectedChildId+");";
        StringBuffer returnString = selectHeader(childSelectName, onChangeString, selectedChild(), childPopUpStringForAll());

        String prePendText = null;
        if (selectedParent() != null) {
            if (selectedChild()==null && defaultChildKey()!=null)
                setSelectedChild(NSKeyValueCoding.Utility.valueForKey(selectedParent(), defaultChildKey()));
            NSArray children = sortedChildren(selectedParent());
            // write out each of the values for the options tags. be sure to set the selected tag if necessary
            int iCount = children.count();
            for (int i=0;i<iCount;i++) {
                Object aChild = children.objectAtIndex(i);
                returnString.append("\t<option ");
                if ((i == iCount-1) && (selectedChild() == null) && (childPopUpStringForAll() == null)) {
                    returnString.append("selected ");
                } else if (aChild.equals(selectedChild())) {
                    returnString.append("selected ");
                }
                returnString.append("value=\"" + aChild.hashCode() + "\">");
                returnString.append(NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName()));
                returnString.append("\n");
            }
        } else {
            // nothing is selected in the parent, so set the children array to all possible child values
            // run through all parents, getting each child. However, if we don't have a parentPopUpStringForAll then we only do it for the last parent.
            if (parentPopUpStringForAll() != null) {
                int iCount = parentEntitiesList().count();
                for (int i=0;i<iCount;i++) {
                    Object aParent = (Object)parentEntitiesList().objectAtIndex(i);
                    //prePendText = aParent.valueForKey(parentDisplayValueName()) + "+";
                    NSArray children = sortedChildren(aParent);
                    int jCount = children.count();
                    for (int j=0;j<jCount;j++) {
                        Object aChild = children.objectAtIndex(j);
                        returnString.append("\t<option ");
                        if ((j == jCount-1) && (selectedChild() == null) && (childPopUpStringForAll() == null)) {
                            returnString.append("selected ");
                        } else if (aChild.equals(selectedChild())) {
                            returnString.append("selected ");
                        }
                        returnString.append("value=\"" + aChild.hashCode() + "\">");
                        //returnString.append(prePendText);
                        returnString.append(NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName()));
                        returnString.append("\n");
                    }
                }
            } else {
                // only do the last parent because we don't have a selected parent AND we don't have the possibility of setting the parent to 'All'
                Object aParent = parentEntitiesList().objectAtIndex(0);
                setSelectedChild(defaultChildKey()!=null ?
                    NSKeyValueCoding.Utility.valueForKey(aParent, defaultChildKey()) : null);

                //prePendText = aParent.valueForKey(parentDisplayValueName()) + "+";
                NSArray children = sortedChildren(aParent);
                int jCount = children.count();
                for (int j=0;j<jCount;j++) {
                    Object aChild = children.objectAtIndex(j);
                    returnString.append("\t<option ");
                    if ((j == jCount-1) && (selectedChild() == null) && (childPopUpStringForAll() == null)) {
                        returnString.append("selected ");
                    } else if (aChild.equals(selectedChild())) {
                        returnString.append("selected ");
                    }
                    returnString.append("value=\"" + aChild.hashCode() + "\">");
                    //returnString.append(prePendText);
                    returnString.append(NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName()));
                    returnString.append("\n");
                }
            }
        }

        returnString.append("</select>\n");
        if (jsLog.isDebugEnabled()) jsLog.debug("JSPopUpRelationPicker childPopUpString  returnString is " + returnString);
        return returnString.toString();
    }
    protected StringBuffer selectHeader(String nm, String oc, Object selectedEntity, String additionalPopupText) {
        StringBuffer returnString;
        int i, iCount;
        Object aEntity;

        returnString = new StringBuffer(1000);
        returnString.append("<select name=\"" + nm + "\"");
        if (oc != null) {
            returnString.append(" onChange=\"" + oc + "\"");
        }
        returnString.append(">\n");

        // if we have to write out an additional option tag, do so at the beginning of the pop-up. Set this to the default selected if nothing else is selected.
        // for parents, if we aren't passed in a selectedParent, then we default to parentPopUpStringForAll. If we aren't given that either, then we default to the last parent in the array.
        // for children, if we aren't passed in a selectedChild, then we default to childPopUpStringForAll. If we aren't given that either, then we default to the last child in the array.
        if (log.isDebugEnabled()) log.debug("nm is " + nm + " and selectedEntity is " + selectedEntity);
        if (selectedEntity == null) {
            if (log.isDebugEnabled()) log.debug("selectedEntity == null");
            // we don't have a selected entity in the list. If we have an additionalPopup set it to that.
            if (additionalPopupText != null) {
                if (log.isDebugEnabled()) log.debug("selectedEntity == null and additionalPopupText != null");
                returnString.append("\t<option selected>" + additionalPopupText + "\n");
            }
        } else {
            // we have a selected entity in the list
            if (log.isDebugEnabled()) log.debug("selectedEntity != null");
            if (additionalPopupText != null) {
                if (log.isDebugEnabled()) log.debug("selectedEntity != null and additionalPopupText != null");
                returnString.append("\t<option>" + additionalPopupText + "\n");
            }
        }
        if (jsLog.isDebugEnabled()) jsLog.debug("JSPopUpRelationPicker selectHeader  returnString is " + returnString);
        return returnString;
    }
    
    public String parentschildrenArrayCreationString() {
        // here's an example of the string this method should return:
        //var parentschildren = new Array(new Entity("dogs","1",new Array(new Entity("poodle","4",null,false),new Entity("puli","5",null,true),new Entity("greyhound","5",null,false)),false), new Entity("fish","2",new Array(new Entity("trout","6",null,true),new Entity("mackerel","7",null,false),new Entity("bass","8",null,false)),true), new Entity("birds","3",new Array(new Entity("robin","9",null,false),new Entity("hummingbird","10",null,false),new Entity("crow","11",null,true)),false));

        StringBuffer returnString = new StringBuffer(1000);
        returnString.append("var "+parentsChildrenId+" = new Array(");

        int iCount = parentEntitiesList().count();
        for (int i=0;i<iCount;i++) {
            Object aParent = (Object)parentEntitiesList().objectAtIndex(i);
            returnString.append("\n\tnew Entity(");
            returnString.append(" \"" + NSKeyValueCoding.Utility.valueForKey(aParent, parentDisplayValueName()) + "\",");
            returnString.append(" \"" + aParent.hashCode() + "\",");

            // now do all the possible children of the parent. Each child should look like 'new Entity("poodle","4",null,false)'
            returnString.append(" new Array(");
            NSArray childrenOfAParent = sortedChildren(aParent);

            int jCount = childrenOfAParent.count();
            Object defaultChild=defaultChildKey()!=null ? NSKeyValueCoding.Utility.valueForKey(aParent, defaultChildKey()) : null;
            int defaultChildIndex=-1;

            for (int j=0;j<jCount;j++) {
                Object aChild = (Object)childrenOfAParent.objectAtIndex(j);
                returnString.append("\n\t\t new Entity(");
                returnString.append(" \"" + NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName()) + "\","); // visible text of pop-up
                returnString.append(" \"" + aChild.hashCode() + "\","); // value text of pop-up
                returnString.append(" null,");
                if (aChild.equals(selectedChild())) {
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
            returnString.append("),");
            if (aParent.equals(selectedParent())) {
                returnString.append(" true");
            } else {
                returnString.append(" false");
            }
            returnString.append(", ");
            returnString.append(defaultChild!=null ? "\""+defaultChildIndex+"\"" : "0");
            returnString.append(")");


            if (i != iCount - 1) {
                // append a comma and a space
                returnString.append(", ");
            }
        }
        returnString.append(");");
        return returnString.toString();
    }

    public NSArray parentEntitiesList() {
        if(parentEntitiesList == null) {
            parentEntitiesList = (NSArray)valueForBinding("parentEntitiesList");
        }
        return parentEntitiesList;
    }
    public Object selectedParent() {
        if(selectedParent == null) {
            selectedParent = valueForBinding("selectedParent");
        }
        return selectedParent;
    }
    public void setSelectedParent(Object newSelectedParent) {
        selectedParent = newSelectedParent;
        setValueForBinding(selectedParent, "selectedParent");
    }

    public Object selectedChild() {
        if(selectedChild == null) {
            selectedChild = valueForBinding("selectedChild");
        }
        return selectedChild;
    }
    public void setSelectedChild(Object newSelectedChild) {
        selectedChild = newSelectedChild;
        setValueForBinding(selectedChild, "selectedChild");
    }

    public String defaultChildKey() {
        if(defaultChildKey == null) {
            defaultChildKey = (String)valueForBinding("defaultChildKey");
        }
        return defaultChildKey;
    }
    public String childrenSortKey() {
        if(childrenSortKey == null) {
            childrenSortKey = (String)valueForBinding("childrenSortKey");
        }
        return childrenSortKey;
    }

    public String childLabel() {
        if(childLabel == null) {
            childLabel = (String)valueForBinding("childLabel");
            if(childLabel == null)
                childLabel = "Types";
        }
        return childLabel;
    }

    public String parentLabel() {
        if(parentLabel == null) {
            parentLabel = (String)valueForBinding("parentLabel");
            if(parentLabel == null)
                parentLabel = "Categories";
        }
        return parentLabel;
    }

    public String childDisplayValueName() {
        if(childPopUpStringForAll == null) {
            childDisplayValueName = (String)valueForBinding("childDisplayValueName");
        }
        return childDisplayValueName;
    }
    public String parentDisplayValueName() {
        if(parentDisplayValueName == null) {
            parentDisplayValueName = (String)valueForBinding("parentDisplayValueName");
        }
        return parentDisplayValueName;
    }
    public String parentToChildrenRelationshipName() {
        if(parentToChildrenRelationshipName == null) {
            parentToChildrenRelationshipName = (String)valueForBinding("parentToChildrenRelationshipName");
        }
        return parentToChildrenRelationshipName;
    }
    public String parentPopUpStringForAll() {
        if(parentPopUpStringForAll == null) {
            parentPopUpStringForAll = (String)valueForBinding("parentPopUpStringForAll");
        }
        return parentPopUpStringForAll;
    }
    public String childPopUpStringForAll() {
        if(childPopUpStringForAll == null) {
            childPopUpStringForAll = (String)valueForBinding("childPopUpStringForAll");
        }
        return childPopUpStringForAll;
    }
    
    public void reset() {
        super.reset();
        selectedChild = null;
        selectedParent = null;
        parentEntitiesList = null;
        childLabel = null;
        parentLabel = null;
        defaultChildKey = null;
        childrenSortKey = null;

        childDisplayValueName = null;
        parentDisplayValueName = null;
        parentToChildrenRelationshipName = null;
        parentPopUpStringForAll = null;
        childPopUpStringForAll = null;
}
}
