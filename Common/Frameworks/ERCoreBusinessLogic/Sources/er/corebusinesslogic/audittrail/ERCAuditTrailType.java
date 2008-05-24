package er.corebusinesslogic.audittrail;

import er.extensions.ERXConstant.StringConstant;

public class ERCAuditTrailType extends StringConstant {

    public final static ERCAuditTrailType INSERTED = new ERCAuditTrailType("inserted", "Inserted");
    public final static ERCAuditTrailType UPDATED = new ERCAuditTrailType("updated", "Changed");
    public final static ERCAuditTrailType DELETED = new ERCAuditTrailType("deleted", "Deleted");
    public final static ERCAuditTrailType ADDED = new ERCAuditTrailType("added", "Added");
    public final static ERCAuditTrailType REMOVED = new ERCAuditTrailType("removed", "Removed");
    
    public ERCAuditTrailType(String value, String name) {
        super(value, name);
    }

    public static ERCAuditTrailType trailType(String key) {
        return (ERCAuditTrailType) constantForClassNamed(key, ERCAuditTrailType.class.getName());
    }
}
