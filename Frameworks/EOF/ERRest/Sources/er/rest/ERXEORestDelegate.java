package er.rest;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.UUIDUtilities;

/**
 * EODelegate is an implementation of the ERXRestRequestNode.Delegate interface that understands EOF.
 * 
 * @author mschrag
 */
public class ERXEORestDelegate extends ERXAbstractRestDelegate {
	public ERXEORestDelegate() {
	}
	
	@Override
	public boolean __hasNumericPrimaryKeys(EOClassDescription classDescription) {
		boolean numericPKs = false;
		if (classDescription instanceof EOEntityClassDescription) {
			EOEntity entity = ((EOEntityClassDescription)classDescription).entity();
			NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
			if (primaryKeyAttributes.count() == 1) {
				EOAttribute primaryKeyAttribute = (EOAttribute) primaryKeyAttributes.objectAtIndex(0);
				Class primaryKeyClass = _NSUtilities.classWithName(primaryKeyAttribute.className());
				numericPKs = primaryKeyClass != null && Number.class.isAssignableFrom(primaryKeyClass);
			}
		}
		return numericPKs;
	}
	
	public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		EOEditingContext editingContext = context.editingContext();
		if (editingContext == null) {
			throw new IllegalArgumentException("There was no editing context attached to this rest context.");
		}
		editingContext.lock();
		try {
			EOEnterpriseObject eo = entity.createInstanceWithEditingContext(editingContext, null);
			editingContext.insertObject(eo);
			return eo;
		}
		finally {
			editingContext.unlock();
		}
	}
	
	public Object primaryKeyForObject(Object obj, ERXRestContext context) {
		Object pkValue;
		if (obj == null) {
			pkValue = null;
		}
		else {
			EOEnterpriseObject eo = (EOEnterpriseObject) obj;
			EOEditingContext editingContext = eo.editingContext();
			if (editingContext != null) {
				editingContext.lock();
				try {
					pkValue = ERXEOControlUtilities.primaryKeyObjectForObject(eo);
					if (pkValue == null) {
						EOGlobalID gid = editingContext.globalIDForObject(eo);
						if (gid instanceof EOTemporaryGlobalID) {
							pkValue = new NSData(((EOTemporaryGlobalID) gid)._rawBytes());
						}
					}
				}
				finally {
					editingContext.unlock();
				}
			}
			else {
				pkValue = ERXEOControlUtilities.primaryKeyObjectForObject(eo);
			}
		}
		if (pkValue instanceof NSData) {
			NSData pkData = (NSData) pkValue;
			if (pkData.length() == 16) {
				pkValue = UUIDUtilities.encodeAsPrettyString(pkData);
			}
		}

		return pkValue;
	}

	public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
		String strPKValue = String.valueOf(id);
		EOEditingContext editingContext = context.editingContext();
		if (editingContext == null) {
			throw new IllegalArgumentException("There was no editing context attached to this rest context.");
		}
		editingContext.lock();
		try {
			EOGlobalID gid = ERXEOControlUtilities.globalIDForString(editingContext, entity.entityName(), strPKValue);
			return editingContext.faultForGlobalID(gid, editingContext);
		}
		finally {
			editingContext.unlock();
		}
	}
}