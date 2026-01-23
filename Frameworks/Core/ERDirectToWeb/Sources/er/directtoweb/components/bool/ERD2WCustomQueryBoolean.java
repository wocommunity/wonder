package er.directtoweb.components.bool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryBoolean;
import com.webobjects.foundation.NSArray;

import er.extensions.localization.ERXLocalizer;

/**
 * <span class="en">
 * Better D2WQueryBoolean, which allows you to sprecify the choices names via a context key, 
 * containing the labels in a format like ("Don't care", "Yes", "No") or ("Yes", "No").
 * Also keeps the selected value. 
 * 
 * @d2wKey choicesNames
 * </span>
 * 
 * <span class="ja">
 * D2WQueryBoolean 拡張版
 * コンテキスト・キーでローカライズが可能です。
 *  ("Don't care", "Yes", "No") 又は ("Yes", "No").
 *  
 * さらに、選択されている値を保存します 
 * 
 * @d2wKey choicesNames - ローカライズ名：("ERD2WBoolean.Yes", "ERD2WBoolean.No", "ERD2WBoolean.DontCare")
 * </span>
 * 
 * @author ak on Mon Dec 22 2003
 */
public class ERD2WCustomQueryBoolean extends D2WQueryBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERD2WCustomQueryBoolean.class);
    protected NSArray<String> _choicesNames;
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WCustomQueryBoolean(WOContext context) {
        super(context);
    }

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
    public Object value() {
        return displayGroup().queryMatch().valueForKey(propertyKey());
    }

	@Override
    public void setValue(Object obj) {
        displayGroup().queryOperator().removeObjectForKey(propertyKey());
        displayGroup().queryMatch().removeObjectForKey(propertyKey());
        if(obj == null) {
          if(log.isDebugEnabled())
            log.debug("Don't care");
        } else {
            displayGroup().queryMatch().takeValueForKey(obj, propertyKey());
            if(log.isDebugEnabled())
              log.debug(obj.toString());
        }
    }

    public String stringForYes() {
        return choicesNames().objectAtIndex(0);
    }
    
    public String stringForNo() {
        return choicesNames().objectAtIndex(1);
    }
    
    public String stringForNull() {
        if(allowsNull()) {
            return choicesNames().objectAtIndex(2);
        }
        return null;
    }

    public boolean allowsNull() {
        return choicesNames().count() > 2;
    }

	@Override
    public String displayString() {
        NSArray<String> choicesNames = choicesNames();
        String result;
        if(choicesNames == null) {
            result = super.displayString();
        }
        else {
	        int choicesIndex = index == 0 ? 2 : index - 1;
	        if(choicesIndex >= choicesNames.count()) {
	            result = super.displayString();
	        } else {
	        	result = choicesNames.objectAtIndex(choicesIndex);
	        }
        }
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(result);
    }

    public String uiMode() {
    	String uiMode = "radio";
    	if(d2wContext().valueForKey("uiMode") != null) {
    		uiMode = (String) d2wContext().valueForKey("uiMode");
    	}
    	return uiMode;
    }
}
