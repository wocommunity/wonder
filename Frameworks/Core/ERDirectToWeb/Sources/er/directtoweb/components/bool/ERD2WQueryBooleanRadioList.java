package er.directtoweb.components.bool;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryBoolean;
import com.webobjects.foundation.NSArray;

import er.extensions.localization.ERXLocalizer;

/**
 * <div class="en">
 * Similar to ERD2WCustomQueryBoolean but displays elements in a <ul></ul> instead of table/matrix
 * </div>
 * 
 * <div class="ja">
 * ERD2WCustomQueryBoolean と全く同じです。交換性の為に残しています。
 * </div>
 * 
 * @d2wKey choicesNames <div class="en"></div>
 *                      <div class="ja">ローカライズ名：("ERD2WBoolean.Yes", "ERD2WBoolean.No", "ERD2WBoolean.Unset")</div>
 * 
 * @see ERD2WCustomQueryBoolean
 * 
 * @author mendis
 */
public class ERD2WQueryBooleanRadioList extends D2WQueryBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    @SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ERD2WQueryBooleanRadioList.class);
    protected NSArray<String> _choicesNames;
    
    public ERD2WQueryBooleanRadioList(WOContext context) {
        super(context);
    }
    
    // accessors
    @SuppressWarnings("unchecked")
	public NSArray<String> choicesNames() {
        if (_choicesNames == null)
            _choicesNames = (NSArray<String>)d2wContext().valueForKey("choicesNames");
        return _choicesNames;
    }

    @Override
    public void reset(){
        super.reset();
        _choicesNames = null;
    }
    
    @Override
    public String displayString() {
        NSArray<String> choicesNames = choicesNames();
        String result;
        if(choicesNames == null) {
            result = super.displayString();
        }
        int choicesIndex = index == 0 ? 2 : index - 1;
        if(choicesIndex >= choicesNames.count()) {
            result = super.displayString();
        } else {
        	result = choicesNames.objectAtIndex(choicesIndex);
        }
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(result);
    }
}