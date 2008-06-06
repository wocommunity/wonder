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
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXKeyValuePair;
import er.extensions.localization.ERXLocalizer;

public class ERD2WQueryStringOperator extends D2WQueryStringOperator {
    public ERXKeyValuePair currentElement;
    
    public ERD2WQueryStringOperator(WOContext context) {
        super(context);
    }
    
    public NSArray allQualifierOperators(){
        NSArray operators = qualifierOperatorsOverrideFromRules() != null ? qualifierOperatorsOverrideFromRules() : _allQualifierOperators;
        int count = operators.count();
        NSMutableArray result = new NSMutableArray( count );
        for( int i = 0; i < count; i++ ) {
            String currentOperatorString = (String)operators.objectAtIndex(i);
            String value = (String)ERXLocalizer.currentLocalizer().valueForKey(currentOperatorString);
            if(value == null) {
                value = currentOperatorString;
            }
            result.addObject(new ERXKeyValuePair(currentOperatorString, value));
        }
        return result;
    }
    
    private static NSArray _stringQualifierOperators;
    private static NSArray _allQualifierOperators;
    
    public NSArray qualifierOperatorsOverrideFromRules(){
        return (NSArray)d2wContext().valueForKey("qualifierOperators");
    }
    
    public ERXKeyValuePair selectedElement() {
        String value = (String) anOperator();
        String choice = (String) ERXLocalizer.currentLocalizer().valueForKey(value);
        if(choice == null) {
            choice = value;
        }
        return new ERXKeyValuePair(value, choice);        
    }
    
    public void  setSelectedElement(ERXKeyValuePair newSelection) {
        setAnOperator(newSelection != null ? (String) newSelection.key() : null );
    }
    
    public void reset() {
        super.reset();
        currentElement = null;
    }
    
    static {
        _stringQualifierOperators = new NSArray(new String[]{"starts with", "contains", "ends with", "is", "like"});
        _allQualifierOperators = _stringQualifierOperators.arrayByAddingObjectsFromArray(EOQualifier.relationalQualifierOperators());
    }
}
