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

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class ERXJSPopUpRelationPicker extends WOComponent {

    public ERXJSPopUpRelationPicker(WOContext aContext) {
        super(aContext);
    }

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERXJSPopUpRelationPicker.class);

    /* !!!WARNING!!!
    1. You can only have one of these components per page. This is because the names of the select boxes are static and not dynamic. There can only be one select box on the page with the name parent_select and one with the name children_select. This should be somewhat trivial to fix if we deem it necessary.
    2. At this time this class can only be used with entity objects, or other objects that inherit from NSObject. In the future we can make it a little more generic, as well as add error checking. This should be pretty trivial.
    3. This wocomponent appears as two rows in a table. Therefore, it must appear in a table.
    */
    /*
     This WOComponent displays two pop-up buttons. One pop-up displays a list of what can be thought of as parent entities. The second pop-up displays a list of what can be thought of as children entities. When a user selects an entity in the parent list, the child list is instantly modified to reflect the children entities available to the user through that parent. This is done through client-side Javascript.

     For example,
     parent1(child1,child2,child3)
     parent2(child4,child5)
     parent3(child2,child5)

     When the user selects parent1, its appropriate children are displayed in the second popup. If the user selects child2 in the children pop-up this is the value that is returned to the user through the selectedChild variable.

     Users should set:
     parentEntitiesList - This is an array of the parent entities that appear in the first pop-up.
     parentToChildrenRelationshipName - the name of the relationship from the parent to its possible children. This is used to fill the values that appear in the children popup.
     selectedParent - the currently selected parent in the parent pop-up. This can be null, but will return the user-selected parent.
     selectedChild - set to null. Returns the user-selected child.
     parentDisplayValueName - the name of the variable displayed in the parent pop-up gotten by parent.valueForKey(parentDisplayValueName)
     childDisplayValueName - the name of the variable displayed in the child pop-up.
     parentLabel - the value displayed in the table interface for the parent popup.
     relationLabel - the value displayed in the table interface for the child popup.

     For the display of the parent popup, if we aren't passed in a selectedParent, then we default to parentPopUpStringForAll. If we aren't given that either, then we default to the last parent in the array.
     For the display of the child popup, if we aren't passed in a selectedChild, then we default to childPopUpStringForAll. If we aren't given that either, then we default to the last child in the array.
     */

    protected String childDisplayValueName;
    protected String parentDisplayValueName;

    /** @TypeInfo com.apple.yellow.foundation.NSObject */
    protected NSArray parentEntitiesList;
    protected String parentToChildrenRelationshipName;

    protected String childrenSortKey;
    protected Object selectedParent;
    protected Object selectedChild;
    protected String defaultChildKey;

    protected String parentPopUpStringForAll; // for example, '-- all --' or '-- none --'. sets selectedParent to null. All children displayed for all parents if picked.
    protected String childPopUpStringForAll; // for example, '-- all --' or '-- none --'. sets selectedChild to null

    protected String parentSelectName="parent_select";
    protected String childSelectName="children_select";;

    protected String parentLabel;
    protected String childLabel;

    protected String childLabel() { return childLabel != null ? childLabel : "Types"; }
    protected String parentLabel() { return parentLabel != null ? parentLabel : "Categories"; }

    public void takeValuesFromRequest(WORequest request, WOContext context) {
        // get the form values for selected_parent_id and selected_child_id and use these to set the selectedParent and selectedChild values
        // takeValues always returns a String, but sometimes it's an empty string if no value is set on the form element.
        // the values returned correspond to the hashCode's of the objects, not the entityId's. This allows us to use this class with objects that don't inherit from NSObject.
        String parent_id, child_id;
        int parentIdToHash, childIdToHash;

        super.takeValuesFromRequest(request, context);

        parent_id = (String)(request.formValueForKey("selected_parent_id"));
        child_id = (String)(request.formValueForKey("selected_child_id"));
        if (log.isDebugEnabled()) log.debug("selected_parent_id is " + parent_id + " and the class is " + parent_id.getClass());
        if (log.isDebugEnabled()) log.debug("selected_child_id is " + child_id + " and the class is " + child_id.getClass());

        try {
            parentIdToHash = Integer.parseInt(parent_id);
            selectedParent = parentWithHashCode(parentIdToHash);
        } catch (Exception e) {
            selectedParent = null;
        }
        if (log.isDebugEnabled()) log.debug("selectedParent is " + ((EOEnterpriseObject)selectedParent).eoShallowDescription());

        try {
            childIdToHash = Integer.parseInt(child_id);
            selectedChild = childWithHashCode(childIdToHash);
        } catch (Exception e) {
            selectedChild = null;
        }
        if (log.isDebugEnabled()) {
            if (selectedChild != null) {
                log.debug("selectedChild is " + ((EOEnterpriseObject)selectedChild).eoShallowDescription());
            } else {
                log.debug("selectedChild is null");
            }
        }
    }

    protected NSArray sortedChildren(Object parent) {
        NSArray unsortedChildren=(NSArray)NSKeyValueCoding.Utility.valueForKey(parent, parentToChildrenRelationshipName);
        
        EOSortOrdering sortOrdering=new EOSortOrdering(childrenSortKey,
                                                       EOSortOrdering.CompareAscending);
        NSMutableArray sortArray=new NSMutableArray(sortOrdering);
        NSArray result=EOSortOrdering.sortedArrayUsingKeyOrderArray(unsortedChildren, sortArray);
        return result!=null ? result : ERXConstant.EmptyArray;
    }

    protected Object childWithHashCode(int hashCode) {
        // run through the parents and all of their children and find a child with the hash code and return it, else return null
        int i, iCount, j, jCount;
        Object aParent, aChild;
        NSArray children;

        iCount = parentEntitiesList.count();
        for (i=0;i<iCount;i++) {
            aParent = (Object)parentEntitiesList.objectAtIndex(i);
            children = sortedChildren(aParent);
            jCount = children.count();
            for (j=0;j<jCount;j++) {
                aChild = (Object)children.objectAtIndex(j);
                if (aChild.hashCode() == hashCode) {
                    return aChild;
                }
            }
        }
        return null;
    }

    protected Object parentWithHashCode(int hashCode) {
        // run through the parents and find one with the hash code and return it, else return null
        int i, iCount;
        Object aEntity;

        iCount = parentEntitiesList.count();
        for (i=0;i<iCount;i++) {
            aEntity = (Object)parentEntitiesList.objectAtIndex(i);
            if (aEntity.hashCode() == hashCode) {
                return aEntity;
            }
        }
        return null;
    }

    protected String jsString() {
        // this method returns all the javascript we need to embed in the web page.
        StringBuffer returnString;

        returnString = new StringBuffer(2000);
        returnString.append("\n");

        // This Javascript string builds an array of Entity objects on the browser end.
        returnString.append("" + parentschildrenArrayCreationString() + "\n");
        if (parentPopUpStringForAll != null) {
            returnString.append("var parentPopUpStringForAll = \"" + parentPopUpStringForAll + "\";\n");
        } else {
            returnString.append("var parentPopUpStringForAll = null;\n");
        }
        if (childPopUpStringForAll != null) {
            returnString.append("var childPopUpStringForAll = \"" + childPopUpStringForAll + "\";\n");
        } else {
            returnString.append("var childPopUpStringForAll = null;\n");
        }
        returnString.append("\n");

        // This Javascript function creates Entity objects. No children means it's a parent entity.
        returnString.append("function Entity(n,id,ch,sel,def) {" + "\n");
        returnString.append("\t" + "name = n;" + "\n");
        returnString.append("\t" + "entityId = id;" + "\n");
        returnString.append("\t" + "children = ch;" + "\n");
        returnString.append("\t" + "selected = sel;" + "\n");
        returnString.append("\t" + "def = def;" + "\n");
        returnString.append("}" + "\n");

        // This Javascript function runs through the parentschildren array and finds a parent with the parent_id id.
        returnString.append("function getParentEntityForId(parent_id) {" + "\n");
        returnString.append("\t" + "for(loop = 0; loop < parentschildren.length; loop++) {" + "\n");
        returnString.append("\t\t" + "if (parentschildren[loop].entityId == parent_id) {" + "\n");
        returnString.append("\t\t\t" + "return parentschildren[loop];" + "\n");
        returnString.append("\t\t" + "}" + "\n");
        returnString.append("\t" + "}" + "\n");
        returnString.append("\t" + "return null;" + "\n");
        returnString.append("}" + "\n");

        // This Javascript function runs through all the parents and builds an array of all their children to return
        returnString.append("function allChildren() {" + "\n");
        returnString.append("\t" + "var allChildren = new Array();" + "\n");
        returnString.append("\t" + "for (loop=0; loop < parentschildren.length; loop++) {" + "\n");
        returnString.append("\t\t" + "if (parentschildren[loop].children != null) {" + "\n");
        returnString.append("\t\t\t" + "allChildren = allChildren.concat(parentschildren[loop].children);" + "\n");
        returnString.append("\t\t" + "}" + "\n");
        returnString.append("\t" + "}" + "\n");
        returnString.append("\t" + "return allChildren;" + "\n");
        returnString.append("}" + "\n");

        // This Javascript function is called when the parent pop-up is switched. Populate the child pop-up and set the selected_parent_id form element to the new selected entity's entityId. By default, set the child pop-up and form to the first item in the popup
        returnString.append("function parentSwapped(new_parent_id) {" + "\n");
        returnString.append("\t" + "var parent_entity = getParentEntityForId(new_parent_id);" + "\n");
        // refuse selections of separator items
        returnString.append("\t" + "if (parent_entity.name.length < 2) return false;" + "\n");

        //window.document.forms[0]." + parentSelectName

        returnString.append("\t" + "window.document.forms[0].selected_parent_id.value = new_parent_id;" + "\n");
        returnString.append("\t" + "var children_array = null;" + "\n");
        returnString.append("\t" + "if (parent_entity != null) {" + "\n");
        returnString.append("\t\t" + "children_array = parent_entity.children;" + "\n");
        returnString.append("\t\t" + "setSelectToArrayOfEntities(window.document.forms[0]." + childSelectName + ", children_array, childPopUpStringForAll);" + "\n");
        returnString.append("\t" + "} else {" + "\n");
        returnString.append("\t\t" + "children_array = allChildren();" + "\n");
        returnString.append("\t\t" + "setSelectToArrayOfEntities(window.document.forms[0]." + childSelectName + ", allChildren(), childPopUpStringForAll);" + "\n");
        returnString.append("\t" + "}" + "\n");
        returnString.append("\t" + "window.document.forms[0]." + childSelectName + ".options[parent_entity.def].selected = true;" + "\n");// child array selection
            returnString.append("\t" + "childSwapped(window.document.forms[0]." + childSelectName + ".options[parent_entity.def].value)" + "\n");// form selection
                returnString.append("}" + "\n");

                // This Javascript function is called when the child pop-up is switched. Set the selected_child_id form element to the value of the newly selected entity
                returnString.append("function childSwapped(new_child_id) {" + "\n");
                returnString.append("\t" + "window.document.forms[0].selected_child_id.value = new_child_id;" + "\n");
                returnString.append("}" + "\n");

                // This Javascript function sets the values of a pop-up to the array of entities that are passed in.
                returnString.append("function setSelectToArrayOfEntities(the_select, the_entities_array, allString) {" + "\n");
                returnString.append("\t" + "var isSelectedEntity = false;" + "\n");
                returnString.append("\t" + "var loopStart = 0;" + "\n");
                returnString.append("\t" + "var loopEnd = the_entities_array.length;" + "\n");
                returnString.append("\t" + "var entitiesLoopOffset = 0;" + "\n");
                // empty the current options array
                returnString.append("\t" + "for (loop=0; loop < the_select.options.length; loop++) {" + "\n");
                returnString.append("\t\t" + "the_select.options[loop] = null;" + "\n");
                returnString.append("\t" + "}" + "\n");
                // if we have an allString, put this at the head of the loop. Make sure we then don't write over anything in the select
                returnString.append("\t" + "if (allString != null) {" + "\n");
                returnString.append("\t\t" + "the_select.options[0] = new Option(allString, null, false, false);" + "\n");
                returnString.append("\t\t" + "loopStart = 1;" + "\n");
                returnString.append("\t\t" + "loopEnd = loopEnd + 1;" + "\n");
                returnString.append("\t\t" + "entitiesLoopOffset = -1;" + "\n");
                returnString.append("\t" + "}" + "\n");
                // fill the rest of the select
                returnString.append("\t" + "for (loop=loopStart; loop < loopEnd; loop++) {" + "\n");
                returnString.append("\t\t" + "the_select.options[loop] = new Option(the_entities_array[loop + entitiesLoopOffset].name, the_entities_array[loop + entitiesLoopOffset].entityId, false, the_entities_array[loop + entitiesLoopOffset].selected);" + "\n");
                // this redundancy is needed to support Netscape.
                returnString.append("\t\t" + "if ((the_entities_array[loop + entitiesLoopOffset].selected) == true) {" + "\n");
                returnString.append("\t\t\t" + "the_select.options[loop].selected = true;" + "\n");
                returnString.append("\t\t\t" + "isSelectedEntity = true;" + "\n");
                returnString.append("\t\t" + "}" + "\n");
                // if we're at the end of the loop and still don't have a selected, then make the first one the selected.
                returnString.append("\t\t" + "if (isSelectedEntity == false) {" + "\n");
                returnString.append("\t\t\t" + "the_select.options[0].selected = true;" + "\n");
                returnString.append("\t\t" + "}" + "\n");
                returnString.append("\t" + "}" + "\n");
                returnString.append("}" + "\n");

                if (log.isDebugEnabled()) log.debug("JSPopUpRelationPicker jsString  returnString is " + returnString);
                return returnString.toString();
        }


    /*
     Should look something like:
     <input type=hidden name=selected_parent_id value=>
     <input type=hidden name=selected_child_id value=>
     */
    public String hiddenFormElementStrings() {
        StringBuffer returnString;

        returnString = new StringBuffer(500);
        if (selectedParent != null) {
            returnString.append("<input type=hidden name=selected_parent_id value=" + selectedParent.hashCode() + ">\n");
        } else {
            returnString.append("<input type=hidden name=selected_parent_id>\n");
        }
        if (selectedChild != null) {
            returnString.append("<input type=hidden name=selected_child_id value=" + selectedChild.hashCode() + ">\n");
        } else {
            returnString.append("<input type=hidden name=selected_child_id value=>\n");
        }

        return returnString.toString();
    }



    /*    protected NSArray allChildren() {
        int i, iCount;
    NSMutableArray children;
    NSObject aParent;

    iCount = parentEntitiesList.count();
    children = new NSMutableArray();
    for (i=0;i<iCount;i++) {
        aParent = (NSObject)parentEntitiesList.objectAtIndex(i);
        children.addObjectsFromArray(((NSArray)aParent.valueForKey(parentToChildrenRelationshipName)));
    }
    return new NSArray(children);
    }*/
    protected String parentPopUpString() {
        /* returns the string to create the pop-up with the initial parent values something like:
        <select name="parent_select" onChange="parentSwapped(window.document.the_form.parent_select.options[selectedIndex].value);">
        <option selected value=1>dogs
        <option value=2>fish
        <option value=3>birds
        </select>
        */
        String onChangeString;
        StringBuffer returnString;
        int i, iCount;
        Object aEntity;

        onChangeString = "parentSwapped(window.document.forms[0]." + parentSelectName + ".options[selectedIndex].value);";
        returnString = selectHeader(parentSelectName, onChangeString, selectedParent, parentPopUpStringForAll);

        // write out each of the values for the options tags. be sure to set the selected tag if necessary
        if (selectedParent==null) selectedParent=(Object)parentEntitiesList.objectAtIndex(0);
        iCount = parentEntitiesList.count();
        for (i=0;i<iCount;i++) {
            aEntity = (Object)parentEntitiesList.objectAtIndex(i);
            returnString.append("\t<option ");
            if (aEntity.equals(selectedParent)) {
                returnString.append("selected ");
            }
            returnString.append("value=\"" + aEntity.hashCode() + "\">");
            returnString.append(NSKeyValueCoding.Utility.valueForKey(aEntity, parentDisplayValueName));
            returnString.append("\n");
        }
        returnString.append("</select>\n");
        return returnString.toString();
    }
    protected String childPopUpString() {
        /* returns the string to create the pop-up with the initial child values something like:
        <select name="children_select">
        <option value=4>poodle
        <option selected value=5>puli
        <option value=6>greyhound
        </select>
        // should be based on the selected parent's children relationship
        */
        String onChangeString;
        NSArray children;
        StringBuffer returnString;
        int i, iCount, j, jCount;
        Object aParent, aChild;
        String prePendText;

        onChangeString = "childSwapped(window.document.forms[0]." + childSelectName + ".options[selectedIndex].value);";
        returnString = selectHeader(childSelectName, onChangeString, selectedChild, childPopUpStringForAll);

        prePendText = null;
        if (selectedParent != null) {
            if (selectedChild==null && defaultChildKey!=null)
                selectedChild=NSKeyValueCoding.Utility.valueForKey(selectedParent, defaultChildKey);
            //prePendText = selectedParent.valueForKey(parentDisplayValueName) + "+";
            children = sortedChildren(selectedParent);
            // write out each of the values for the options tags. be sure to set the selected tag if necessary
            iCount = children.count();
            for (i=0;i<iCount;i++) {
                aChild = (Object)children.objectAtIndex(i);
                returnString.append("\t<option ");
                if ((i == iCount-1) && (selectedChild == null) && (childPopUpStringForAll == null)) {
                    returnString.append("selected ");
                } else if (aChild.equals(selectedChild)) {
                    returnString.append("selected ");
                }
                returnString.append("value=\"" + aChild.hashCode() + "\">");
                //returnString.append(prePendText);
                returnString.append(NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName));
                returnString.append("\n");
            }
        } else {
            // nothing is selected in the parent, so set the children array to all possible child values
            // run through all parents, getting each child. However, if we don't have a parentPopUpStringForAll then we only do it for the last parent.
            if (parentPopUpStringForAll != null) {
                iCount = parentEntitiesList.count();
                for (i=0;i<iCount;i++) {
                    aParent = (Object)parentEntitiesList.objectAtIndex(i);
                    //prePendText = aParent.valueForKey(parentDisplayValueName) + "+";
                    children = sortedChildren(aParent);
                    jCount = children.count();
                    for (j=0;j<jCount;j++) {
                        aChild = (Object)children.objectAtIndex(j);
                        returnString.append("\t<option ");
                        if ((j == jCount-1) && (selectedChild == null) && (childPopUpStringForAll == null)) {
                            returnString.append("selected ");
                        } else if (aChild.equals(selectedChild)) {
                            returnString.append("selected ");
                        }
                        returnString.append("value=\"" + aChild.hashCode() + "\">");
                        //returnString.append(prePendText);
                        returnString.append(NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName));
                        returnString.append("\n");
                    }
                }
            } else {
                // only do the last parent because we don't have a selected parent AND we don't have the possibility of setting the parent to 'All'
                aParent = (Object)parentEntitiesList.objectAtIndex(0);
                selectedChild= defaultChildKey!=null ?
                    NSKeyValueCoding.Utility.valueForKey(aParent, defaultChildKey) : null;

                //prePendText = aParent.valueForKey(parentDisplayValueName) + "+";
                children = sortedChildren(aParent);
                jCount = children.count();
                for (j=0;j<jCount;j++) {
                    aChild = (Object)children.objectAtIndex(j);
                    returnString.append("\t<option ");
                    if ((j == jCount-1) && (selectedChild == null) && (childPopUpStringForAll == null)) {
                        returnString.append("selected ");
                    } else if (aChild.equals(selectedChild)) {
                        returnString.append("selected ");
                    }
                    returnString.append("value=\"" + aChild.hashCode() + "\">");
                    //returnString.append(prePendText);
                    returnString.append(NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName));
                    returnString.append("\n");
                }
            }
        }

        returnString.append("</select>\n");
        if (log.isDebugEnabled()) log.debug("JSPopUpRelationPicker childPopUpString  returnString is " + returnString);
        return returnString.toString();
    }
    private StringBuffer selectHeader(String nm, String oc, Object selectedEntity, String additionalPopupText) {
        StringBuffer returnString;
        int i, iCount;
        Object aEntity;

        returnString = new StringBuffer(1000);
        returnString.append("<SELECT NAME=\"" + nm + "\"");
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
        if (log.isDebugEnabled()) log.debug("JSPopUpRelationPicker selectHeader  returnString is " + returnString);
        return returnString;
    }
    /*    private String popUpString(String nm, String oc, NSArray list, NSObject selectedEntity, String displayValue, String additionalPopupText, String nmPrependText) {
        StringBuffer returnString;
    int i, iCount;
    NSObject aEntity;

    returnString = new StringBuffer(1000);
    returnString.append("<SELECT NAME=\"" + nm + "\"");
    if (oc != null) {
        returnString.append(" onChange=\"" + oc + "\"");
    }
    returnString.append(">\n");

    // if we have to write out an additional option tag, do so at the beginning of the pop-up. Set this to the default selected if nothing else is selected.
    // for parents, if we aren't passed in a selectedParent, then we default to parentPopUpStringForAll. If we aren't given that either, then we default to the last parent in the array.
    // for children, if we aren't passed in a selectedChild, then we default to childPopUpStringForAll. If we aren't given that either, then we default to the last child in the array.
    //System.out.println("nm is " + nm + " and selectedEntity is " + selectedEntity);
    if (selectedEntity == null) {
        //System.out.println("selectedEntity == null");
        // we don't have a selected entity in the list. If we have an additionalPopup set it to that.
        if (additionalPopupText != null) {
            //System.out.println("selectedEntity == null and additionalPopupText != null");
            returnString.append("\t<option selected>" + additionalPopupText + "\n");
        } else {
            // we don't have an additional popup and we don't have a selected entity, so just set it to the last entity
            //System.out.println("selectedEntity == null and additionalPopupText == null");
            selectedEntity = (NSObject)list.lastObject();
        }
    } else {
        // we have a selected entity in the list
        //System.out.println("selectedEntity != null");
        if (additionalPopupText != null) {
            //System.out.println("selectedEntity != null and additionalPopupText != null");
            returnString.append("\t<option>" + additionalPopupText + "\n");
        }
    }

    // write out each of the values for the options tags. be sure to set the selected tag if necessary
    iCount = list.count();
    for (i=0;i<iCount;i++) {
        aEntity = (NSObject)list.objectAtIndex(i);
        returnString.append("\t<option ");
        if ((i == iCount-1) && (selectedEntity == null) && (additionalPopupText == null)) {
            returnString.append("selected ");
        } else if (aEntity.equals(selectedEntity)) {
            returnString.append("selected ");
        }
        returnString.append("value=\"" + aEntity.hashCode() + "\">");
        returnString.append(aEntity.valueForKey(displayValue));
        returnString.append("\n");
    }
    returnString.append("</select>\n");
    return returnString.toString();
    }*/

    protected String parentschildrenArrayCreationString() {
        // here's an example of the string this method should return:
        //var parentschildren = new Array(new Entity("dogs","1",new Array(new Entity("poodle","4",null,false),new Entity("puli","5",null,true),new Entity("greyhound","5",null,false)),false), new Entity("fish","2",new Array(new Entity("trout","6",null,true),new Entity("mackerel","7",null,false),new Entity("bass","8",null,false)),true), new Entity("birds","3",new Array(new Entity("robin","9",null,false),new Entity("hummingbird","10",null,false),new Entity("crow","11",null,true)),false));
        StringBuffer returnString;
        int i, iCount, j, jCount;
        Object aParent, aChild;
        NSArray childrenOfAParent;

        returnString = new StringBuffer(1000);
        returnString.append("var parentschildren = new Array(");

        iCount = parentEntitiesList.count();
        for (i=0;i<iCount;i++) {
            aParent = (Object)parentEntitiesList.objectAtIndex(i);
            returnString.append("new Entity(");
            returnString.append(" \"" + NSKeyValueCoding.Utility.valueForKey(aParent, parentDisplayValueName) + "\",");
            returnString.append(" \"" + aParent.hashCode() + "\",");

            // now do all the possible children of the parent. Each child should look like 'new Entity("poodle","4",null,false)'
            returnString.append(" new Array(");
            childrenOfAParent = sortedChildren(aParent);

            jCount = childrenOfAParent.count();
            Object defaultChild=defaultChildKey!=null ? NSKeyValueCoding.Utility.valueForKey(aParent, defaultChildKey) : null;
            int defaultChildIndex=-1;

            for (j=0;j<jCount;j++) {
                aChild = (Object)childrenOfAParent.objectAtIndex(j);
                returnString.append(" new Entity(");
                returnString.append(" \"" + NSKeyValueCoding.Utility.valueForKey(aChild, childDisplayValueName) + "\","); // visible text of pop-up
                returnString.append(" \"" + aChild.hashCode() + "\","); // value text of pop-up
                returnString.append(" null,");
                if (aChild.equals(selectedChild)) {
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
            if (aParent.equals(selectedParent)) {
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

        if (log.isDebugEnabled()) log.debug("JSPopUpRelationPicker parentschildrenArrayCreationString  returnString is " + returnString);
        return returnString.toString();
    }


    public String childDisplayValueName() { return childDisplayValueName; }
    public void setChildDisplayValueName(String newChildDisplayValueName) { childDisplayValueName = newChildDisplayValueName; }

    public String parentDisplayValueName() { return parentDisplayValueName; }
    public void setParentDisplayValueName(String newParentDisplayValueName) { parentDisplayValueName = newParentDisplayValueName; }

    public NSArray parentEntitiesList() { return parentEntitiesList; }
    public void setParentEntitiesList(NSArray newParentEntitiesList) { parentEntitiesList = newParentEntitiesList; }

    public String parentToChildrenRelationshipName() { return parentToChildrenRelationshipName; }
    public void setParentToChildrenRelationshipName(String newParentToChildrenRelationshipName) {
        parentToChildrenRelationshipName = newParentToChildrenRelationshipName;
    }

    public String parentPopUpStringForAll() { return parentPopUpStringForAll; }
    public void setParentPopUpStringForAll(String newParentPopUpStringForAll) { parentPopUpStringForAll = newParentPopUpStringForAll; }

    public String childPopUpStringForAll() { return childPopUpStringForAll; }
    public void setChildPopUpStringForAll(String newChildPopUpStringForAll) { childPopUpStringForAll = newChildPopUpStringForAll; }

    public Object selectedParent() { return selectedParent; }
    public void setSelectedParent(Object newSelectedParent) { selectedParent = newSelectedParent; }

    public Object selectedChild() { return selectedChild; }
    public void setSelectedChild(Object newSelectedChild) { selectedChild = newSelectedChild; }

    /*private String valueClassNameForEntityAndKeypath(EOEntity entity, String keyPath) {
        // returns the class type of last entity from the keypath. This method is no longer needed, but I'll keep the source around since it may be useful in the future.
        NSArray keys;
    int i, count;
    String key;
    EOEntity subentity;
    EOAttribute finalattribute;
    EORelationship relationship;

    key = null;
    subentity = entity;
    finalattribute = null;
    keys = NSArray.componentsSeparatedByString(keyPath, ".");

    count = keys.count();
    for (i=0;i<count;i++) {
        key = (String)keys.objectAtIndex(i);
        relationship = subentity.relationshipNamed(key);
        if (relationship != null) {
            if (relationship.destinationEntity() != null) {
                subentity = subentity.relationshipNamed(key).destinationEntity();
            }
        }
    }
    if (subentity != null) {
        finalattribute = subentity.attributeNamed(key);
    }


    return finalattribute.valueClassName();
    }*/
    
}
