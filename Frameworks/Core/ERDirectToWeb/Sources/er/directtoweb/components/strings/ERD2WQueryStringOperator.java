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

/**
 * <div class="en">
 * </div>
 * 
 * <div class="ja">
 * このプロパティ・レベル・コンポーネントは string のクエリをビルドします。
 * 例：("starts with" 又は "contains")
 * </div>
 * 
 * @d2wKey name <div class="en"></div>
 *              <div class="ja">テキストフィールドの name タグ</div>
 * @d2wKey qualifierOperators <div class="en"></div>
 *                            <div class="ja">指定 qualifier (NSArray&lt;String&gt;)</div>
 */
public class ERD2WQueryStringOperator extends D2WQueryStringOperator {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXKeyValuePair<String, String> currentElement;
    
    public ERD2WQueryStringOperator(WOContext context) {
        super(context);
    }
    
    public NSArray<ERXKeyValuePair<String, String>> allQualifierOperators(){
        NSArray<String> operators = qualifierOperatorsOverrideFromRules() != null ? qualifierOperatorsOverrideFromRules() : _allQualifierOperators;
        int count = operators.count();
        NSMutableArray<ERXKeyValuePair<String, String>> result = new NSMutableArray<ERXKeyValuePair<String, String>>( count );
        for( int i = 0; i < count; i++ ) {
            String currentOperatorString = operators.objectAtIndex(i);
            String value = (String)ERXLocalizer.currentLocalizer().valueForKey(currentOperatorString);
            if(value == null) {
                value = currentOperatorString;
            }
            result.addObject(new ERXKeyValuePair<>(currentOperatorString, value));
        }
        return result;
    }
    
    private static NSArray<String> _stringQualifierOperators;
    private static NSArray<String> _allQualifierOperators;
    
    /**
     * <span class="ja">
     * _allQualifierOperators をオーバライドできます。
     * 
     * @return qualifier NSArray
     * </span>
     */
    public NSArray<String> qualifierOperatorsOverrideFromRules(){
        return (NSArray<String>)d2wContext().valueForKey("qualifierOperators");
    }
    
    public ERXKeyValuePair<String, String> selectedElement() {
        String value = anOperator();
        String choice = (String) ERXLocalizer.currentLocalizer().valueForKey(value);
        if(choice == null) {
            choice = value;
        }
        return new ERXKeyValuePair<>(value, choice);
    }
    
    public void  setSelectedElement(ERXKeyValuePair<String, String> newSelection) {
        setAnOperator(newSelection != null ? newSelection.key() : null );
    }
    
    @Override
    public void reset() {
        super.reset();
        currentElement = null;
    }
    
    static {
        _stringQualifierOperators = new NSArray<>(new String[]{"starts with", "contains", "ends with", "is", "like"});
        _allQualifierOperators = _stringQualifierOperators.arrayByAddingObjectsFromArray(EOQualifier.relationalQualifierOperators());
    }
}
