package er.directtoweb.components.misc;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.qualifiers.ERXRegExQualifier;

/**
 * Allows you to query a set of keys - supplied by queryAttributes - with a regular expression query.
 * As an example, you could query your User entity by login, email and name in one go. 
 * @binding queryAttributes NSArray of attributes to query
 * @binding displayGroup display group (must be a subclass of ERXDisplayGroup)
 * @author ak
 */
public class ERDQueryAnyKey extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDQueryAnyKey.class);
    private Object _value; 
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERDQueryAnyKey(WOContext context) {
        super(context);
    }
    
    public Object value() {
        return _value;
    }
    
    public NSArray queryAttributes() {
    	NSArray result = (NSArray)valueForBinding("queryAttributes");
    	if(result == null) {
    		result = ERXEOControlUtilities.stringAttributeListForEntityNamed(session().defaultEditingContext(), (String) valueForBinding("entityName"));
    	}
        return result;
    }
    
    public void setValue(Object newValue) {
        _value = newValue;
        //log.info(displayGroup());
        ERXDisplayGroup dg = (ERXDisplayGroup)displayGroup();
        EOQualifier q = null;
        if(newValue != null) {
            if(newValue != null && newValue.toString().indexOf("*") == 0) {
                newValue = newValue.toString().substring(1);
            }
            if(false) {
                q = ERXEOControlUtilities.qualifierMatchingAnyKey(queryAttributes(), EOQualifier.QualifierOperatorCaseInsensitiveLike, "*" +newValue + "*");
            } else {
                q = ERXEOControlUtilities.qualifierMatchingAnyKey(queryAttributes(), ERXRegExQualifier.MatchesSelector, newValue);
            }
        }
        dg.setQualifierForKey(q, key());
    }
}
