package er.r2d2w.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.components.relationships.ERD2WEditToManyRelationship;
import er.extensions.foundation.ERXStringUtilities;

public class R2D2WEditToManyRelationship extends ERD2WEditToManyRelationship {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WEditToManyRelationship(WOContext context) {
        super(context);
    }

	private String labelID;

	public void reset() {
		labelID = null;
		super.reset();
	}
	
	/**
	 * @return the labelID
	 */
	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id", '_');
		}
		return labelID;
	}
	
	public EOEnterpriseObject object() {
		return (EOEnterpriseObject) d2wContext().valueForKey("object");
	}

	public void setObject(EOEnterpriseObject object) {
		d2wContext().takeValueForKey(object, "object");
	}
}