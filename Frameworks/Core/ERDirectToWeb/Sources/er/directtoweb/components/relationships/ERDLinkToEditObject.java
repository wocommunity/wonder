package er.directtoweb.components.relationships;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * Generic link component used to view or edit an object.
 * 
 * @binding object object to get list from
 * @binding key keypath to get list from object
 * @binding keyWhenRelationship in case the object is the value at the keypath, defines the display key
 * @binding editConfigurationName name of the page configuration to jump to
 * @binding useNestedEditingContext if the EC should be nested (default is peer)
 * @author ak
 */
public class ERDLinkToEditObject extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final Logger log = LoggerFactory.getLogger(ERDLinkToEditObject.class);

    public ERDLinkToEditObject(WOContext context) {
    	super(context);
    }

    @Override
    public boolean isStateless() {
    	return true;
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }

    public Object displayValue() {
        Object value = objectKeyPathValue();
        if (value instanceof EOEnterpriseObject) {
            return ((EOEnterpriseObject) value).valueForKey((String) valueForBinding("keyWhenRelationship"));
        }
        return value;
    }

    public WOComponent view() {
        EOEnterpriseObject eo = object();
        Object value = objectKeyPathValue();
        if (value instanceof EOEnterpriseObject) {
            eo = (EOEnterpriseObject) value;
        }
        String pageConfigurationName = (String)valueForBinding("editConfigurationName");
        InspectPageInterface ipi = (InspectPageInterface)D2W.factory().pageForConfigurationNamed(pageConfigurationName, session());
     	eo = ERXEOControlUtilities.editableInstanceOfObject(eo, booleanValueForBinding("useNestedEditingContext"));
    	ipi.setNextPage(context().page());
    	ipi.setObject(eo);
    	return (WOComponent)ipi;
    }
}
