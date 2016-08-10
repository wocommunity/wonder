package er.modern.directtoweb.components.header;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.components.ERDCustomComponent;

/**
 * Abstract base class for header components
 * 
 * @binding object
 * @binding key
 * 
 * @d2wKey displayNameForPageConfiguration
 * 
 * @author davidleber
 *
 */
public abstract class ERMD2WHeader extends ERDCustomComponent{

	public interface Keys extends ERDCustomComponent.Keys {
		public static String displayNameForPageConfiguration = "displayNameForPageConfiguration";
	}
	
	private EOEnterpriseObject _object;
	private String _key;
	protected String _headerString;
	
	public ERMD2WHeader(WOContext context) {
		super(context);
	}

    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
 
    public abstract String headerString();
    
    public boolean showHeading() {
    	return headerString() != null && headerString().length() > 0;
    }
    
    public EOEnterpriseObject object() {
    	if (_object == null) {
			_object = (EOEnterpriseObject)valueForBinding("object");
		}
		return _object;
    }
    
    @Override
    public String key() {
    	if (_key == null) {
    		_key = (String)valueForBinding("key");
		}
		return _key;
    }
}
