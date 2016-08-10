package er.modern.directtoweb.components.header;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Header for an EditRelationshipPage
 * 
 * @binding object
 * @binding key
 * 
 * @d2wKey displayNameForPageConfiguration
 * @d2wKey displayKeyForEntity
 * 
 * @author davidleber
 *
 */
public class ERMD2WEditRelationshipHeader extends ERMD2WHeader {
	
	public interface Keys extends ERMD2WHeader.Keys {
		public static final String displayKeyForEntity = "displayKeyForEntity";
	}
	
    public ERMD2WEditRelationshipHeader(WOContext context) {
        super(context);
    }
    
    // FIXME switch to using a localized template
    @Override
    public String headerString() {
    	if (_headerString == null) {
    	    if (object() != null) {
    	        D2WContext tempContext = new D2WContext();
    	        tempContext.setEntity(EOUtilities.entityNamed(object().editingContext(),object().entityName()));
    	        tempContext.setPropertyKey(key());
    	        tempContext.setTask("editRelationship");
    	        String key = (String)tempContext.valueForKey(Keys.displayKeyForEntity);
    			if (key.equals("entity.name")) {
    				_headerString = localizedValueForDisplayNameOfKeyPath(key, object());
    			} else {
    				_headerString = (String)object().valueForKeyPath(key);
    			}
    		    _headerString = _headerString + "'s " + localizedValueForEOPropertyKey(key(), object());
    		}
    		if (_headerString == null) {
    			_headerString = stringValueForBinding(Keys.displayNameForPageConfiguration);
    		}
    	}
    	return _headerString;
    }
    
    protected String localizedValueForDisplayNameOfKeyPath(String keyPath, EOEnterpriseObject eo) {
    	String realName = (String)eo.valueForKeyPath(keyPath);
    	realName = ERXStringUtilities.displayNameForKey(realName);
        String result = ERXLocalizer.currentLocalizer().localizedStringForKey(realName);
        if(result == null) {
            result = realName;
        }
        return result;
    }
    
    protected String localizedValueForEOPropertyKey(String propertyKey, EOEnterpriseObject eo) {
    	String result = null;
    	if (eo != null) {
    		String entityName = eo.entityName();
    		result = ERXLocalizer.currentLocalizer().localizedStringForKey(entityName + "." + propertyKey);
    	} 
    	if (result == null) {
    		result = ERXLocalizer.currentLocalizer().localizedStringForKey(propertyKey);
    	}
    	if (result == null) {
    		result = propertyKey;
    	}
    	return ERXStringUtilities.displayNameForKey(result);
    }
   
}