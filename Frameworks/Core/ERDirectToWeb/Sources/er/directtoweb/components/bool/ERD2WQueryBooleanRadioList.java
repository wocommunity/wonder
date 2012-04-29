package er.directtoweb.components.bool;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryBoolean;
import com.webobjects.foundation.NSArray;

import er.extensions.localization.ERXLocalizer;

/**
 * Similar to ERD2WCustomQueryBoolean but displays elements in a <ul></ul> instead of table/matrix
 * @see ERD2WCustomQueryBoolean
 * 
 * @author mendis
 * @d2wKey choicesNames
 */
public class ERD2WQueryBooleanRadioList extends D2WQueryBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERD2WQueryBooleanRadioList.class);
    protected NSArray _choicesNames;
    
    public ERD2WQueryBooleanRadioList(WOContext context) {
        super(context);
    }
    
    // accessors
    public NSArray<String> choicesNames() {
        if (_choicesNames == null)
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
        return _choicesNames;
    }

    @Override
    public void reset(){
        super.reset();
        _choicesNames = null;
    }
    
    @Override
    public String displayString() {
        NSArray choicesNames = choicesNames();
        String result;
        if(choicesNames == null) {
            result = super.displayString();
        }
        int choicesIndex = index == 0 ? 2 : index - 1;
        if(choicesIndex >= choicesNames.count()) {
            result = super.displayString();
        } else {
        	result = (String)choicesNames.objectAtIndex(choicesIndex);
        }
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(result);
    }
}