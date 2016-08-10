/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/** 
 * Groups items into sections.For example: Employees belong to a department, you want to 
 * group on department. So the parent will need to consist of something like:
 * <pre>
 * 
 * [erxgroupingrepetition]
 * [wostring value=currentDepartment.name] 
 * [worepetition list=currentEmployees item=currentEmployee] 
 *    [wostring value=currentEmployees.firstName] 
 * [/worepetition] 
 * [/erxgroupingrepetition]
 * 
 * </pre>
 * and then you'd set up the bindings of the grouping repetition like:
 * <pre>
 * 
 * list=allEmployees : list of employees to group
 * item=currentEmployee : will be set so the next key can get evaluated
 * sectionForItem=departmentForCurrentEmployee : a method in the parent that returns sth like currentEmployee.department()
 * sectionKey="name" : assuming department has a name, but can be unbound; note that you can group on "city", too!
 * subListSection=currentDepartment : instance variable in the parent that will get set to the current department
 * subList=currentEmployees : instance variable in the parent that will get set to the employees of the current department
 * sortKey="@sortAsc.name" : sorts the department list by name
 * 
 * </pre>
 * If a user could belong to many departments, you could either set the <code>splitArrays</code> binding to true,
 * in which case the sections would be all the departments and the user would be added in each section he belongs
 * or you could leave it out. Then the sections will be each combination of departments a user belongs to.
 * Please see the page BugsPerUser.wo from the BugTracker application to find another example on how to use it. 
 * @binding list list of objects to group
 * @binding item current item, will get pushed to the parent, so that it can evaluate sectionForItem
 * @binding sectionForItem value pulled from the parent, after "item" gets pushed
 * @binding sectionKey key to group departments on (usually primaryKey or hashCode)
 * @binding subListSection will get set to the current section
 * @binding subList will get set to the grouped items for the section
 * @binding sortKey optional key for sorting the group list (sth like '@sortAsc.name')
 * @binding splitArrays optional boolean specifying if array keys are regarded as distinct keys
 * @binding ignoreNulls optional boolean specifying if nulls are ignored
 */

public class ERXGroupingRepetition extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXGroupingRepetition(WOContext aContext) {
        super(aContext);
    }

    private static final Logger log = LoggerFactory.getLogger(ERXGroupingRepetition.class);
    
    private NSMutableArray _sections;
    private Object _sectionItem;
    private NSMutableDictionary _itemsPerSection=new NSMutableDictionary();
    private final static Object NULL="N/A";
    
    private String _sectionKey;
    public String sectionKey() {
        if (_sectionKey==null) {
            _sectionKey=stringValueForBinding("sectionKey", "hashCode");
        }
        return _sectionKey;
    }
    
    public NSArray sections() {
        if (_sections==null) {
            _sections= new NSMutableArray();
            NSArray list=(NSArray)valueForBinding("list");
            _itemsPerSection=new NSMutableDictionary();
            if (list!=null) {
                boolean ignoreNulls = booleanValueForBinding("ignoreNulls", false);
                
                for (Enumeration e=list.objectEnumerator(); e.hasMoreElements();) {
                    Object item=e.nextElement();
                    log.debug("item = {}", item);
                    
                    // push value up, so parent can tell us the group
                    setValueForBinding(item,"item");
                    
                    // Sections have to be copiable objects -- no EOs!!
                    Object section=valueForBinding("sectionForItem");
                    if (section==NSKeyValueCoding.NullValue) section=null;
                    Object sectionKey;

                    if(section == null) {
                        if(ignoreNulls) {
                            continue;
                        }
                        section=NULL;
                    }
                    sectionKey = keyForSection(section);
                    if(sectionKey instanceof NSArray) {
                        NSArray array = (NSArray)sectionKey;
                        int index = 0;
                        for (Enumeration keys = ((NSArray)sectionKey).objectEnumerator(); keys.hasMoreElements(); ) {
                            Object currentKey = keys.nextElement();
                            Object currentSection = ((NSArray)section).objectAtIndex(index++);
                            NSMutableArray currentItemsForSection=(NSMutableArray)_itemsPerSection.objectForKey(currentKey);
                            if (currentItemsForSection==null) {
                                _sections.addObject(currentSection);
                                currentItemsForSection=new NSMutableArray();
                                _itemsPerSection.setObjectForKey(currentItemsForSection,currentKey);
                            }
                            currentItemsForSection.addObject(item);
                        }
                    } else {
                        NSMutableArray currentItemsForSection=(NSMutableArray)_itemsPerSection.objectForKey(sectionKey);
                        if (currentItemsForSection==null) {
                            _sections.addObject(section);
                            currentItemsForSection=new NSMutableArray();
                            _itemsPerSection.setObjectForKey(currentItemsForSection,sectionKey);
                        }
                        currentItemsForSection.addObject(item);
                    }
                }
            }
            String sortKey = (String)valueForBinding("sortKey");
            //the key act on the array, so it must be in the form "@sortAsc.someKey"
            if(sortKey != null) {
                _sections = (NSMutableArray)_sections.valueForKeyPath(sortKey);
            }
        }
        return _sections;
    }
    
    /**
     * @param splitArrays
     * @param section
     */
    private Object keyForSection(Object section) {
        Object sectionKey = NULL;
        if(section != null && section != NULL) {
            sectionKey = NSKeyValueCodingAdditions.Utility.valueForKeyPath(section,sectionKey());
            if(!splitArrays() && (sectionKey instanceof NSArray)) {
                sectionKey = ((NSArray)((NSArray)section).valueForKey(_sectionKey)).componentsJoinedByString(",");
            }
        }
        return sectionKey;
    }

    
    private Boolean _splitArrays;
    private boolean splitArrays() {
        if(_splitArrays == null) {
            _splitArrays = booleanValueForBinding("splitArrays", false) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _splitArrays.booleanValue();
    }

    public Object sectionItem() { 
        return _sectionItem; 
    }
    
    public void setSectionItem(Object section) {
        _sectionItem=section;
        setValueForBinding(_sectionItem!=NULL ? _sectionItem : null, "subListSection");
        setValueForBinding(_itemsPerSection.objectForKey(keyForSection(_sectionItem)), "subList");
    }
    
    @Override
    public void reset() {
        _sections=null;
        _splitArrays=null;
        _sectionItem=null;
        _sectionKey=null;
        _itemsPerSection=null;
    }
}
