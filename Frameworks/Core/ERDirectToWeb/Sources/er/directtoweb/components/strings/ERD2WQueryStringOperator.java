//
// ERD2WQueryStringOperator.java: Class file for WO Component 'ERD2WQueryStringOperator'
// Project ERDirectToWeb
//
// Created by bposokho on Mon May 19 2003
//
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryStringOperator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

public class ERD2WQueryStringOperator extends D2WQueryStringOperator {

    public ERD2WQueryStringOperator(WOContext context) {
        super(context);
    }

    public NSArray allQualifierOperators(){
        return qualifierOperatorsOverrideFromRules() != null ? qualifierOperatorsOverrideFromRules() : _allQualifierOperators;
    }

    private static NSArray _stringQualifierOperators;
    private static NSArray _allQualifierOperators;
    
    public NSArray qualifierOperatorsOverrideFromRules(){
        return (NSArray)d2wContext().valueForKey("qualifierOperators");
    }

    static {
        _stringQualifierOperators = new NSArray(new String[]{"starts with", "contains", "ends with", "is", "like"});
        _allQualifierOperators = _stringQualifierOperators.arrayByAddingObjectsFromArray(EOQualifier.relationalQualifierOperators());
    }
}
