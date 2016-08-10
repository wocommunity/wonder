package er.corebusinesslogic.audittrail;

import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKeyGlobalID;

public class ERCAuditTrailEntry extends _ERCAuditTrailEntry {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final ERCAuditTrailEntryClazz clazz = new ERCAuditTrailEntryClazz();

    public static class ERCAuditTrailEntryClazz extends _ERCAuditTrailEntry._ERCAuditTrailEntryClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERCAuditTrailEntry.Key {
    }

    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
        EOEnterpriseObject user = ERCoreBusinessLogic.actor(ec);
        if (user != null && user instanceof ERXGenericRecord) {
            ERXKeyGlobalID gid = ERXKeyGlobalID.globalIDForGID(((ERXGenericRecord) user).permanentGlobalID());
            setUserGlobalID(gid);
        }
        setCreated(new NSTimestamp());
    }

    public EOEnterpriseObject user() {
        if (userGlobalID() == null) {
            return null;
        }
        EOKeyGlobalID gid = userGlobalID().globalID();
        EOEnterpriseObject eo = editingContext().faultForGlobalID(gid, editingContext());
        try {
            eo.willRead();
            return eo;
        } catch (EOObjectNotAvailableException e) {
            return null;
        }
    }

    public void setOldValue(Object value) {
        setOldValues(NSPropertyListSerialization.stringFromPropertyList(value));
    }

    public void setNewValue(Object value) {
        setNewValues(NSPropertyListSerialization.stringFromPropertyList(value));
    }
}
