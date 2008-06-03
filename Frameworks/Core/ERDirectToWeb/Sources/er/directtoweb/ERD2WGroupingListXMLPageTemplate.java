/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXConstant;

/**
 * Displays a grouped list of eos in an xml template.<br />
 * 
 */

public class ERD2WGroupingListXMLPageTemplate extends ERD2WGroupingListPage {

    public ERD2WGroupingListXMLPageTemplate(WOContext context) {super(context);}
    
    /** logging support */
    public final static Logger log = Logger.getLogger(ERD2WGroupingListXMLPageTemplate.class);

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
