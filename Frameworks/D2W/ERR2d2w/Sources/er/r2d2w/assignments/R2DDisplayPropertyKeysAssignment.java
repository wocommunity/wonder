package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.ERDAssignment;
import er.directtoweb.assignments.ERDLocalizableAssignmentInterface;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXModelGroup;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;
import er.r2d2w.ERR2d2w;

public class R2DDisplayPropertyKeysAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final boolean fallbackDefault = ERXGenericRecord.localizationShouldFallbackToDefaultLanguage();
	private static final NSArray<String> displayPropertyKeysDependentKeys = 
		new NSArray<String>(new String[] { D2WModel.EntityKey , D2WModel.TaskKey , D2WModel.DynamicPageKey });
	private static final NSArray<String> localizationShouldFallbackDependentKeys =
		new NSArray<String>(new String[] {D2WModel.EntityKey});
	private static final NSArray<NSArray<String>> allKeys = new NSArray<NSArray<String>>(displayPropertyKeysDependentKeys).arrayByAddingObject(localizationShouldFallbackDependentKeys);
	private static final NSDictionary<String, NSArray<String>> dependentKeyDict = 
		new NSDictionary<String, NSArray<String>>(allKeys, new NSArray<String>(new String[] {D2WModel.DisplayPropertyKeysKey, "localizationShouldFallback"}));
	
	public R2DDisplayPropertyKeysAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDisplayPropertyKeysAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDisplayPropertyKeysAssignment(unarchiver);
	}
	
	public NSArray<String> dependentKeys(String keyPath) {
		return dependentKeyDict.get(keyPath);
	}
	    
	public Object displayPropertyKeys(D2WContext c) {
		NSMutableArray<String> result = new NSMutableArray<String>();
		EOEntityClassDescription ecd = new EOEntityClassDescription(c.entity());
		for(String prop: ecd.attributeKeys()) {
			
			// Don't display derived count attributes
			if(prop.endsWith(ERR2d2w.DERIVED_COUNT)) {continue;}
			
			EOAttribute attr = c.entity().attributeNamed(prop);
			//FIXME set prop key on c to get correct values for fallback per key

			if(attr.adaptorValueType() == EOAttribute.AdaptorBytesType && 
					!ERXValueUtilities.booleanValue(c.valueForKey("displayBytesTypeKeys"))) {
				continue;
			}
			
			// Only displays password attributes when editing
			if(!"edit".equals(c.task()) && attr.userInfo() != null) {
				String proto = (String)attr.userInfo().valueForKey("erPrototype");
				if("password".equals(proto) || R2DDefaultUserEntityAssignment.PASSWORD_PROTO_KEY.equals(proto)) {continue;}
			}

			// Determine how to display localized attributes
			if(attr.userInfo() != null && attr.userInfo().containsKey(ERXModelGroup.LANGUAGES_KEY)) {
				// If display all, just add.
				String languageCode = ERXLocalizer.currentLocalizer().languageCode();
				if (ERXValueUtilities.booleanValue(c.valueForKey("displayAllLocalesForAttribute"))) {
					result.add(prop);
				
				} else if (prop.endsWith(languageCode)) {
					if(ERXValueUtilities.booleanValue(c.valueForKey("localizationShouldFallback")) && attr.allowsNull()) {
						prop = prop.substring(0, prop.length()-(languageCode.length() + 1));
					}
					result.add(prop);
				}
				continue;
			}

			result.add(prop);
		}
		
		// Determine if relationships should be displayed
		if(ERXValueUtilities.booleanValue(c.valueForKey("displayToManyRelationshipKeys"))) {
			result.addObjectsFromArray(ecd.toManyRelationshipKeys());
		}
		
		if(ERXValueUtilities.booleanValue(c.valueForKey("displayToOneRelationshipKeys"))) {
			result.addObjectsFromArray(ecd.toOneRelationshipKeys());
		}
		return result;
	}
	
	public Boolean localizationShouldFallback(D2WContext c) {
		Boolean result = Boolean.FALSE;
		if(!fallbackDefault) { return result; }
		if(ERXValueUtilities.booleanValue(c.valueForKey("displayAllLocalesForAttribute"))) { return result; }
		Class<?> entityClass = null;
		try {
			entityClass = Class.forName(c.entity().className());
		} catch (ClassNotFoundException e) {
			NSForwardException._runtimeExceptionForThrowable(e);
		}
		if(ERXGenericRecord.class.isAssignableFrom(entityClass)) { result = Boolean.TRUE; }		
		return result;
	}

}
