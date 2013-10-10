package er.directtoweb.components.misc;

import java.util.Enumeration;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.components.ERDCustomEditComponent;

public class ERDHasChangesMarker extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDHasChangesMarker(WOContext context) {
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
    
    public boolean hasChanges() {
        EOEnterpriseObject object = object();
        EOEditingContext ec = object.editingContext();
        boolean result = ec != null && ec.hasChanges();
        if(result) {
            if(object.changesFromSnapshot(ec.committedSnapshotForObject(object)).count() == 0) {
                for (Enumeration e = ec.registeredObjects().objectEnumerator(); e.hasMoreElements();) {
                    EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
                    if(eo.changesFromSnapshot(ec.committedSnapshotForObject(eo)).count() != 0) {
                        return true;
                    }
                }
                result = false;
            }
        }
        return result;
    }
}
