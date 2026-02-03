package er.r2d2w.components.relationships;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WDisplayToOneFault;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.localization.ERXLocalizer;
import er.r2d2w.R2D2WDirectAction;

public class R2D2WDisplayToOneFault extends D2WDisplayToOneFault {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WDisplayToOneFault(WOContext context) {
        super(context);
    }

    public String helpString() {
    	return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("R2D2W.displayToOneFault.helpString", d2wContext());
    }
    
    public WOComponent toOneAction() {
    	EOEntity destinationEntity = (EOEntity)d2wContext().valueForKey("destinationEntity");
    	InspectPageInterface ipi = D2W.factory().inspectPageForEntityNamed(destinationEntity.name(), session());
    	EOEnterpriseObject eo = (EOEnterpriseObject)object().valueForKeyPath(propertyKey());
        ipi.setObject(eo);
        ipi.setNextPage(context().page());
        return (WOComponent)ipi;
    }
    
    public String directActionName() {
    	EOEntity entity = (EOEntity)d2wContext().valueForKey("destinationEntity");
    	return "Inspect" + entity.name();
    }
    
	public NSDictionary<String, Object> queryDictionary() {
		NSDictionary<String,Object> d = EOUtilities.destinationKeyForSourceObject(object().editingContext(), object(), propertyKey());
		if(d != null && d.count() > 0) {
			Object o = d.allValues().objectAtIndex(0);
			if(o.equals(NSKeyValueCoding.NullValue)) {
				NSMutableDictionary<String, Object> qd = new NSMutableDictionary<String, Object>();
				if(ERXLocalizer.isLocalizationEnabled()) {
					qd.put(R2D2WDirectAction.LANGUAGE_KEY, ERXLocalizer.currentLocalizer().languageCode());
				}
				return qd;
			} else {
				return R2D2WDirectAction.inspectQueryDictionaryForKey(o.toString());
			}
		} else {
			//CHECKME set up prefetch defaults?
			return R2D2WDirectAction.inspectQueryDictionary((EOEnterpriseObject)object().valueForKeyPath(propertyKey()));			
		}
    }
}