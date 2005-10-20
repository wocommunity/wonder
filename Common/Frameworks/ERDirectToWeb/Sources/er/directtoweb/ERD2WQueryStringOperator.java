//
// ERD2WQueryStringOperator.java: Class file for WO Component 'ERD2WQueryStringOperator'
// Project ERDirectToWeb
//
// Created by bposokho on Mon May 19 2003
//
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

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
            String value = (String)ERXLocalizer.currentLocalizer().localizedValueForKeyWithDefault(currentOperatorString);
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
        return new ERXKeyValuePair
        (super.anOperator(), (String)ERXLocalizer.currentLocalizer().localizedValueForKeyWithDefault
         (super.anOperator()));
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
