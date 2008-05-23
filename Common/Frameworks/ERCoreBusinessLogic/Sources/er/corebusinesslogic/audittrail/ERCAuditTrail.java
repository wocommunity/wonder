package er.corebusinesslogic.audittrail;


import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOProperty;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXGenericRecord;
import er.extensions.ERXKeyGlobalID;
import er.extensions.ERXModelGroup;
import er.extensions.ERXProperties;
import er.extensions.ERXQ;
import er.extensions.ERXSelectorUtilities;
import er.extensions.ERXStringUtilities;

/**
 * Bracket for all single audit trail actions. It serves as a "shadow" of the object in question.
 * @author ak
 *
 */
public class ERCAuditTrail extends _ERCAuditTrail {
    
    @SuppressWarnings("unused")
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCAuditTrail.class);

    public static final ERCAuditTrailClazz clazz = new ERCAuditTrailClazz();

    public interface Delegate {
    }

    public static class ERCAuditTrailClazz extends _ERCAuditTrail._ERCAuditTrailClazz {

        public ERCAuditTrail auditTrailForObject(EOEditingContext ec, EOEnterpriseObject eo) {
            return auditTrailForGlobalID(ec, ec.globalIDForObject(eo));
        }

        public ERCAuditTrail auditTrailForGlobalID(EOEditingContext ec, EOGlobalID gid) {
            return (ERCAuditTrail) ERXEOControlUtilities.objectWithQualifier(ec, ENTITY_NAME, ERXQ.equals(Key.GID, gid));
        }

        public ERCAuditTrail createAuditTrailForObject(EOEditingContext ec, EOEnterpriseObject eo) {
            ERCAuditTrail trail = createAndInsertObject(ec);

            trail.setObject(eo);
            return trail;
        }
    }

    public interface Key extends _ERCAuditTrail.Key {

    }

    public void init(EOEditingContext ec) {
        super.init(ec);
    }

    public void setObject(EOEnterpriseObject eo) {
        EOGlobalID gid;
        if (gid() == null) {
            if (eo instanceof ERXGenericRecord) {
                gid = ((ERXGenericRecord) eo).permanentGlobalID();
            } else {
                throw new IllegalArgumentException("Can't handle non ERXGenericRecord");
            }
            ERXKeyGlobalID keyGID = ERXKeyGlobalID.globalIDForGID((EOKeyGlobalID) gid);
            setGid(keyGID);
        }
    }

    public EOEnterpriseObject object() {
        EOKeyGlobalID gid = gid().globalID();
        return editingContext().faultForGlobalID(gid, editingContext());
    }
}
