/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.directtoweb.pages.ERD2WGroupingListPage;
import er.extensions.eof.ERXConstant;

/**
 * Displays a grouped list of eos in an xml template.
 * 
 * @d2wKey resolvedUnit
 * @d2wKey componentName
 * @d2wKey wrap
 * @d2wKey width
 * @d2wKey hasThirdLevel
 * @d2wKey showHeader
 * @d2wKey title
 * @d2wKey pageWrapperName
 * @d2wKey propertyKey
 * @d2wKey displayPropertyKeys
 * @d2wKey displayNameForProperty
 * @d2wKey groupingKeyDisplayKey
 * @d2wKey thirdLevelRelationshipKey
 */
public class ERD2WGroupingListXMLPageTemplate extends ERD2WGroupingListPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WGroupingListXMLPageTemplate(WOContext context) {super(context);}
    
    /** logging support */
    public final static Logger log = LoggerFactory.getLogger(ERD2WGroupingListXMLPageTemplate.class);

    private final static String NULL="N/A";
    
    public Object value() {
        return object().valueForKeyPath((String)d2wContext().valueForKey("propertyKey"));
    }


    public String displayNameForGroupingKey(){
        d2wContext().takeValueForKey(d2wContext().valueForKey("groupingKey"), "propertyKey");
        return (String)d2wContext().valueForKey("displayNameForProperty");
    }

    public String valueForGroup(){
        String result = "";
        String groupingKeyDisplayKey = (String)d2wContext().valueForKey("groupingKeyDisplayKey");
        if(log.isDebugEnabled()) log.debug("groupingKeyDisplayKey = "+groupingKeyDisplayKey);
        if(log.isDebugEnabled()) log.debug("sublistSection = "+sublistSection);
        if (sublistSection != null){
        if (groupingKeyDisplayKey!=null && !groupingKeyDisplayKey.equals("")){
                if (sublistSection instanceof EOEnterpriseObject) {
                    result = (String)((EOEnterpriseObject)sublistSection).valueForKey(groupingKeyDisplayKey);
                }else if (sublistSection instanceof NSArray) {
                    NSArray values = (NSArray)((NSArray)sublistSection).valueForKey(groupingKeyDisplayKey);
                    result = values.componentsJoinedByString(", ");
                }
            } else {
                result = sublistSection.toString();
            }
        }else {
            result = NULL;
        }
       return result;
    }

    public String componentName() {
        d2wContext().takeValueForKey(ERXConstant.OneInteger, "frame");
        d2wContext().takeValueForKey(d2wContext().valueForKey("thirdLevelRelationshipKey"), "propertyKey");
        return (String)d2wContext().valueForKey("componentName");
    }
}
