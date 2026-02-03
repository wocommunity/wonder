package er.r2d2w.assignments;

import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.foundation.ERXValueUtilities;

public class R2DDefaultSubEntitiesAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final NSArray<String> dependentKeys = new NSArray<String>("entity","readOnly","isEntityEditable");
	
	public R2DDefaultSubEntitiesAssignment(EOKeyValueUnarchiver unarchiver) {
		super(unarchiver);
	}

	public R2DDefaultSubEntitiesAssignment(String key, Object value) {
		super(key, value);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDefaultSubEntitiesAssignment(unarchiver);
	}
	
	@Override
	public NSArray<String> dependentKeys(String keyPath) {
		return dependentKeys;
	}
	
	public NSArray<EOEntity> createSubEntities(D2WContext c) {
		WOSession session = (WOSession)c.valueForKey(D2WModel.SessionKey);
		D2WContext subContext = ERD2WContext.newContext(session);
		NSArray<EOEntity> e = new NSArray<EOEntity>(c.entity());
		return _createSubEntities(e, subContext);
	}
	
	private NSArray<EOEntity> _createSubEntities(NSArray<EOEntity> entities, D2WContext c) {
		NSMutableArray<EOEntity> result = new NSMutableArray<EOEntity>();
		for(EOEntity e : entities) {
			if(!e.isAbstractEntity()){
				c.setEntity(e);
				boolean isEntityEditable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityEditable"));
				boolean readOnly = ERXValueUtilities.booleanValue(c.valueForKey("readOnly"));
				if(!readOnly && isEntityEditable) {
					result.add(e);
				}
			}
			result.addObjectsFromArray(_createSubEntities(e.subEntities(), c));
		}
		return result;
	}
}
