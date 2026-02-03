package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;

import er.attachment.model.ERAttachment;
import er.directtoweb.assignments.ERDAssignment;
import er.extensions.foundation.ERXValueUtilities;

public class R2DDefaultAttachmentAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String FORM_ENCODING = "multipart/form-data";
	private static final NSArray<String> dependentKeys = new NSArray<String>(new String[] {D2WModel.EntityKey ,D2WModel.DisplayPropertyKeysKey});
	
	public R2DDefaultAttachmentAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDefaultAttachmentAssignment(String key, Object value) {
		super(key, value);
	}

	public NSArray<String> dependentKeys(String keyPath) {
		return dependentKeys;
	}
	
	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDefaultAttachmentAssignment(unarchiver);
	}

	public Object formEncoding(D2WContext c) {
		String result = null;
		NSArray<String> displayPropertyKeys = ERXValueUtilities.arrayValueWithDefault(c.valueForKey(D2WModel.DisplayPropertyKeysKey), NSArray.EmptyArray);
		EOEntity entity = (EOEntity)c.valueForKey(D2WModel.EntityKey);
		for(String propertyKey: displayPropertyKeys) {
			EORelationship r = entity.relationshipNamed(propertyKey);
			if(r != null) {
				EOEntity de = r.destinationEntity();
				try {
					Class<?> entityClass = Class.forName(de.className());
					if(ERAttachment.class.isAssignableFrom(entityClass)) {
						result = FORM_ENCODING;
						break;
					}
				} catch (ClassNotFoundException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}

			}
		}
		return result;
	}

}
