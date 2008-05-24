package er.corebusinesslogic.audittrail;

import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.ERXGenericRecord;
import er.extensions.ERXKeyGlobalID;

public class ERCAuditTrailEntry extends _ERCAuditTrailEntry {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCAuditTrailEntry.class);

    public static final ERCAuditTrailEntryClazz clazz = new ERCAuditTrailEntryClazz();

    public static class ERCAuditTrailEntryClazz extends _ERCAuditTrailEntry._ERCAuditTrailEntryClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERCAuditTrailEntry.Key {
    }

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
