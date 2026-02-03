package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.eof.ERXGuardedObjectInterface;
import er.extensions.eof.ERXNonNullObjectInterface;
import er.extensions.foundation.ERXValueUtilities;

/**
 * An assignment for object related actions.
 */
public class R2DDefaultBranchChoicesAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final NSArray<String> leftControllerDependentKeys = 
		new NSArray<String>("task","entity","isEntityInspectable","object.isNonNull");
	public static final NSArray<String> rightControllerDependentKeys = 
		new NSArray<String>("task","entity","isEntityDeletable","isEntityEditable","object.canDelete","object.canUpdate","readOnly");
	public static final NSArray<String> toManyRelationshipDependentKeys =
		new NSArray<String>("task","entity","parentRelationship","frame","isEntityEditable","readOnly");
	public static final NSArray<String> toOneRelationshipDependentKeys =
		new NSArray<String>("task","entity","parentRelationship","frame","isEntityDeletable","isEntityEditable","isEntityInspectable","readOnly","object.canDelete","object.canUpdate","object.isNonNull");
	public static final NSArray<String> inspectControllerDependentKeys = 
		new NSArray<String>("task","entity","frame","isEntityDeletable","isEntityEditable","readOnly","object.canDelete","object.canUpdate");
	public static final NSArray<String> editControllerDependentKeys = 
		new NSArray<String>("task","subTask","tabCount","tabIndex");
	public static final NSArray<String> listControllerDependentKeys = 
		new NSArray<String>("task","frame","subTask");
	public static final NSArray<String> queryControllerDependentKeys = 
		new NSArray<String>("task","frame");
	public static final NSArray<String> queryAllControllerDependentKeys = 
		new NSArray<String>("task","frame");
	
	public static final NSDictionary<String, NSArray<String>> dependentKeys;
	
	static {
		NSMutableDictionary<String, NSArray<String>> keys = new NSMutableDictionary<String, NSArray<String>>();
		keys.setObjectForKey(leftControllerDependentKeys, "leftControllerChoices");
		keys.setObjectForKey(rightControllerDependentKeys, "rightControllerChoices");
		keys.setObjectForKey(toManyRelationshipDependentKeys, "toManyControllerChoices");
		keys.setObjectForKey(toOneRelationshipDependentKeys, "toOneControllerChoices");
		keys.setObjectForKey(inspectControllerDependentKeys, "inspectControllerChoices");
		keys.setObjectForKey(editControllerDependentKeys, "editControllerChoices");
		keys.setObjectForKey(listControllerDependentKeys, "listControllerChoices");
		keys.setObjectForKey(queryControllerDependentKeys, "queryControllerChoices");
		keys.setObjectForKey(queryAllControllerDependentKeys, "queryAllControllerChoices");
		dependentKeys = keys.immutableClone();
	}
	
	public R2DDefaultBranchChoicesAssignment(EOKeyValueUnarchiver u) {super(u);}
	public R2DDefaultBranchChoicesAssignment(String key, Object value) {super(key, value);}
	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDefaultBranchChoicesAssignment(unarchiver);
	}

	@Override
	public NSArray<String> dependentKeys(String keyPath) {
		return dependentKeys.objectForKey(keyPath);
	}
	
	private Class<?> classForEntity(EOEntity entity) {
		try {
			return Class.forName(entity.className());
		} catch(ClassNotFoundException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
	
	public Object leftControllerChoices(D2WContext c) {
		NSMutableArray<String> choices = new NSMutableArray<String>();
		EOEntity e = c.entity();
		boolean nullInterface = ERXNonNullObjectInterface.class.isAssignableFrom(classForEntity(e));
		boolean isNonNull = ERXValueUtilities.booleanValue(c.valueForKeyPath("object.isNonNull"));
		boolean isEntityInspectable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityInspectable"));
		String task = c.task();
		
		if("select".equals(task)) {
			choices.add("_select");
		}
		if(isEntityInspectable && (isNonNull || (!nullInterface))){
			String choice = "editRelationship".equals(task)?"_inspectRelated":"_inspect";
			choices.add(choice);
		}
		return choices;
	}
	
	public Object rightControllerChoices(D2WContext c) {
		NSMutableArray<String> choices = new NSMutableArray<String>();
		EOEnterpriseObject eo = (EOEnterpriseObject)c.valueForKey("object");
		EORelationship rel = (EORelationship)c.valueForKey("parentRelationship");
		EOEntity e = c.entity();
		boolean unguarded = !ERXGuardedObjectInterface.class.isAssignableFrom(classForEntity(e));
		boolean isEntityEditable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityEditable"));
		boolean isEntityDeletable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityDeletable"));
		boolean canUpdate = eo instanceof ERXGuardedObjectInterface?((ERXGuardedObjectInterface)eo).canUpdate():unguarded;
		boolean canDelete = eo instanceof ERXGuardedObjectInterface?((ERXGuardedObjectInterface)eo).canDelete():unguarded;
		boolean isEntityWritable = !ERXValueUtilities.booleanValue(c.valueForKey("readOnly"));
		String task = c.task();
		
		if(isEntityWritable && isEntityEditable && canUpdate) {
			if("editRelationship".equals(task)) {
				choices.add("_editRelated");
			} else if(!"edit".equals(task)) {
				choices.add("_edit");
			}
		}
		if("editRelationship".equals(task) && rel != null && (!rel.ownsDestination())) {
			choices.add("_removeRelated");
		}
		if(isEntityWritable && isEntityDeletable && canDelete) {
			if("edit".equals(task) || "inspect".equals(task)) {
				choices.add("_deleteReturn");
			} else {
				choices.add("_delete");
			}
		}
		return choices;
	}
	
	public Object toManyControllerChoices(D2WContext c) {
		NSMutableArray<String> choices = new NSMutableArray<String>();
		EORelationship rel = (EORelationship)c.valueForKey("parentRelationship");
		EOEntity e = c.entity();
		boolean isEntityEditable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityEditable"));
		boolean isEntityWritable = !ERXValueUtilities.booleanValue(c.valueForKey("readOnly"));
		boolean isConcrete = !e.isAbstractEntity();
		
		if(!c.frame()) {
			choices.add("_returnRelated");
		}
		if(rel.inverseRelationship() == null || isEntityWritable) {
			choices.add("_queryRelated");
		}
		if(isEntityWritable && isEntityEditable && isConcrete && e.subEntities().isEmpty()) {
			choices.add("_createRelated");
		}
		return choices;
	}
	
	public Object toOneControllerChoices(D2WContext c) {
		NSMutableArray<String> choices = new NSMutableArray<String>();
		EOEnterpriseObject eo = (EOEnterpriseObject)c.valueForKey("object");
		EORelationship rel = (EORelationship)c.valueForKey("parentRelationship");
		EOEntity e = c.entity();
		boolean nullInterface = ERXNonNullObjectInterface.class.isAssignableFrom(classForEntity(e));
		boolean isNonNull = ERXValueUtilities.booleanValue(c.valueForKeyPath("object.isNonNull"));
		boolean unguarded = !ERXGuardedObjectInterface.class.isAssignableFrom(classForEntity(e));
		boolean isEntityEditable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityEditable"));
		boolean isEntityDeletable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityDeletable"));
		boolean isEntityInspectable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityInspectable"));
		boolean canUpdate = eo instanceof ERXGuardedObjectInterface?((ERXGuardedObjectInterface)eo).canUpdate():unguarded;
		boolean canDelete = eo instanceof ERXGuardedObjectInterface?((ERXGuardedObjectInterface)eo).canDelete():unguarded;
		boolean isEntityWritable = !ERXValueUtilities.booleanValue(c.valueForKey("readOnly"));
		boolean isConcrete = !e.isAbstractEntity();

		if(!c.frame()) {
			choices.add("_returnRelated");
		}
		if(rel.inverseRelationship() == null || isEntityWritable ) {
			choices.add("_queryRelated");
		}
		if(isEntityInspectable && (isNonNull || (!nullInterface))){
			choices.add("_inspectRelated");
		}
		if(isEntityWritable && isEntityEditable && isConcrete && e.subEntities().isEmpty()) {
			choices.add("_createRelated");
		}
		if(isEntityWritable && isEntityEditable && canUpdate) {
			choices.add("_editRelated");
			if(!rel.ownsDestination()) {
				choices.add("_removeRelated");
			}
		}
		if(isEntityWritable && isEntityDeletable && canDelete) {
			choices.add("_delete");
		}
		return choices;
	}
	
	public Object inspectControllerChoices(D2WContext c) {
		NSMutableArray<String> choices = new NSMutableArray<String>();
		EOEnterpriseObject eo = (EOEnterpriseObject)c.valueForKey("object");
		EOEntity e = c.entity();
		boolean unguarded = !ERXGuardedObjectInterface.class.isAssignableFrom(classForEntity(e));
		boolean isEntityEditable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityEditable"));
		boolean isEntityDeletable = ERXValueUtilities.booleanValue(c.valueForKey("isEntityDeletable"));
		boolean canUpdate = eo instanceof ERXGuardedObjectInterface?((ERXGuardedObjectInterface)eo).canUpdate():unguarded;
		boolean canDelete = eo instanceof ERXGuardedObjectInterface?((ERXGuardedObjectInterface)eo).canDelete():unguarded;
		boolean isEntityWritable = !ERXValueUtilities.booleanValue(c.valueForKey("readOnly"));

		if(!c.frame()) {
			choices.add("_return");
		}
		if(isEntityWritable && isEntityEditable && canUpdate) {
			choices.add("_edit");
		}
		if(isEntityWritable && isEntityDeletable && canDelete) {
			choices.add("_deleteReturn");
		}
		return choices;
	}

	public Object editControllerChoices(D2WContext c) {
		String subTask = (String)c.valueForKey("subTask");
		if("wizard".equals(subTask)) {
			Integer count = (Integer)c.valueForKey("tabCount");
			Integer index = (Integer)c.valueForKey("tabIndex");
			if(count != null && index != null && count.intValue() > 1) {
				if(index == 0) {
					return new NSArray<String>("_cancelEdit","_nextStep");
				} else if(index.intValue() + 1 == count.intValue()) {
					return new NSArray<String>("_cancelEdit","_prevStep","_save");
				} else {
					return new NSArray<String>("_cancelEdit","_prevStep","_nextStep");
				}
			}
		}
		return new NSArray<String>("_cancelEdit","_save");
	}

	public Object listControllerChoices(D2WContext c) {
		if("pick".equals(c.valueForKey("subTask"))) {
			return new NSArray<String>("_selectAll","_selectNone");
		}
		if(c.frame()) { return NSArray.emptyArray(); }
		return new NSArray<String>("_return");
	}

	public Object queryControllerChoices(D2WContext c) {
		if(c.frame()) { return new NSArray<String>("_query"); }
		return new NSArray<String>("_return","_query");
	}

	public Object queryAllControllerChoices(D2WContext c) {
		if(c.frame()) { return NSArray.emptyArray(); }
		return new NSArray<String>("_return");
	}
}
