package er.directtoweb.components.attachments;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WComponent;

/**
 * D2W component for editing ERAttachments
 * The configurationName is computed: 'Entity.propertyKey'
 * 
 * The properties for this configuration name must be set:
 * @see {http://webobjects.mdimension.com/hudson/job/Wonder53/javadoc/er/attachment/package-summary.html}
 * 
 * Prerequisite: you must set a D2W rule for key 'enctype' with value "multipart/form-data" in the edit page template
 * 
 * @author mendis
 *
 * @binding object
 * @binding propertyKey
 * @d2wKey size
 */
public class ERD2WEditAttachment extends D2WComponent {

	public ERD2WEditAttachment(WOContext aContext) {
		super(aContext);
	}
	
    
    // accessors
    public String configurationName() {
    	return object().entityName() + "." + propertyKey();
    }
    
    public void setObjectPropertyValue(Object value) {
    	object().takeValueForKeyPath(value, propertyKey());
    }

    // actions    
    public void removeAttachment() {
    	//ERAttachment attachment = (ERAttachment) objectPropertyValue();
    	//attachment.delete();
    	setObjectPropertyValue(null);
    }

}
