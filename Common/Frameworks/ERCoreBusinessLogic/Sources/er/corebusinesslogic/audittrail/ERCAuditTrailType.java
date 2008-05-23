package er.corebusinesslogic.audittrail;

import er.extensions.ERXConstant.StringConstant;

public class ERCAuditTrailType extends StringConstant {

    public ERCAuditTrailType INSERTED = new ERCAuditTrailType("insert", "Inserted");
    public ERCAuditTrailType UPDATED = new ERCAuditTrailType("update", "Changed");
    public ERCAuditTrailType DELETED = new ERCAuditTrailType("delete", "Deleted");
    public ERCAuditTrailType ADDED = new ERCAuditTrailType("add", "Added");
    public ERCAuditTrailType REMOVED = new ERCAuditTrailType("removed", "Removed");
    
    public ERCAuditTrailType(String value, String name) {
        super(value, name);
    }

}
