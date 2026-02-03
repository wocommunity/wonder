package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.eof.ERXEC;

public class R2DDefaultUserEntityAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String PROTOTYPE_KEY = "erPrototype";
	public static final String DUMMY_ENTITY_KEY = "dummyEntity";
	public static final String DEFAULT_USER_ENTITY_KEY = "defaultUserEntity";
	public static final String USER_PROTO_KEY = "userEntityPrototype";
	public static final String LANGUAGE_PROTO_KEY = "userLanguagePrototype";
	public static final String USERNAME_PROTO_KEY = "userIDPrototype";
	public static final String PASSWORD_PROTO_KEY = "userCredentialPrototype";
	public static final String EMAIL_PROTO_KEY = "userContactPrototype";

	
	public R2DDefaultUserEntityAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDefaultUserEntityAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDefaultUserEntityAssignment(unarchiver);
	}

	public NSArray<Object> dependentKeys(String keyPath) {
		return new NSArray<Object>(new Object[] {});
	}
	
	/**
	 * Searches the attributes belonging to the defaultUserEntity in the
	 * given d2wContext. The method finds and returns the attribute with
	 * a userInfo key that equals erPrototype and a userInfo value that
	 * equals the userContactPrototype in the d2wContext. 
	 * @param c - the given d2wContext
	 * @return The EOAttribute, or null if no attribute is found.
	 */
	public Object userContactAttribute(D2WContext c) {
		EOAttribute result = null;
		Object val = c.valueForKey(EMAIL_PROTO_KEY);
		Object entity = c.valueForKey(DEFAULT_USER_ENTITY_KEY);
		Object dummy = c.valueForKey(DUMMY_ENTITY_KEY);
		if(!dummy.equals(entity) && val != null) {
			result = attributeForPrototype((EOEntity)entity, val.toString());
		}
		return result;
	}
	
	/**
	 * Searches the attributes belonging to the defaultUserEntity in the
	 * given d2wContext. The method finds and returns the attribute with
	 * a userInfo key that equals erPrototype and a userInfo value that
	 * equals the userCredentialPrototype in the d2wContext. 
	 * @param c - the given d2wContext
	 * @return The EOAttribute, or null if no attribute is found.
	 */
	public Object userCredentialAttribute(D2WContext c) {
		EOAttribute result = null;
		Object val = c.valueForKey(PASSWORD_PROTO_KEY);
		Object entity = c.valueForKey(DEFAULT_USER_ENTITY_KEY);
		Object dummy = c.valueForKey(DUMMY_ENTITY_KEY);
		if(!dummy.equals(entity) && val != null) {
			result = attributeForPrototype((EOEntity)entity, val.toString());
		}
		return result;
	}
	
	/**
	 * Searches the attributes belonging to the defaultUserEntity in the
	 * given d2wContext. The method finds and returns the attribute with
	 * a userInfo key that equals erPrototype and a userInfo value that
	 * equals the userIDPrototype in the d2wContext. 
	 * @param c - the given d2wContext
	 * @return The EOAttribute, or null if no attribute is found.
	 */
	public Object userIDAttribute(D2WContext c) {
		EOAttribute result = null;
		Object val = c.valueForKey(USERNAME_PROTO_KEY);
		Object entity = c.valueForKey(DEFAULT_USER_ENTITY_KEY);
		Object dummy = c.valueForKey(DUMMY_ENTITY_KEY);
		if(!dummy.equals(entity) && val != null) {
			result = attributeForPrototype((EOEntity)entity, val.toString());
		}
		return result;
	}
	
	/**
	 * Searches the attributes belonging to the defaultUserEntity in the
	 * given d2wContext. The method finds and returns the attribute with
	 * a userInfo key that equals erPrototype and a userInfo value that
	 * equals the userLanguagePrototype in the d2wContext. 
	 * @param c - the given d2wContext
	 * @return The EOAttribute, or null if no attribute is found.
	 */
	public Object userLanguageAttribute(D2WContext c) {
		EOAttribute result = null;
		Object val = c.valueForKey(LANGUAGE_PROTO_KEY);
		Object entity = c.valueForKey(DEFAULT_USER_ENTITY_KEY);
		Object dummy = c.valueForKey(DUMMY_ENTITY_KEY);
		if(!dummy.equals(entity) && val != null) {
			result = attributeForPrototype((EOEntity)entity, val.toString());
		}
		return result;
	}
	
//	/**
//	 * Not currently implemented. Returns null.
//	 * @param c - the given d2wContext
//	 * @return null
//	 */
//	public Object userStoredCredential?something?(D2WContext c) {
//		return null;
//	}
	
	/**
	 * Searches the given context for the user entity. The user entity
	 * must have 
	 * <ul>
	 * <li>an erPrototype matching the value for the userEntityPrototype 
	 * d2wContext key</li>
	 * <li>an attribute with an erPrototype matching the value for the 
	 * userIDPrototype d2wContext key</li>
	 * <li>an attribute with an erPrototype matching the value for the 
	 * userCredentialPrototype d2wContext key</li>
	 * </ul>
	 * @param c - the given d2wContext
	 * @return the matching user entity or the dummyEntity if no entity 
	 * is found.
	 */
	public Object defaultUserEntity(D2WContext c) {
		EOEntity result = (EOEntity)c.valueForKey(DUMMY_ENTITY_KEY);
		
		// Find entity
		String userProto = c.valueForKey(USER_PROTO_KEY).toString();
		EOEntity entity = entityForPrototype(c, userProto);
		if(entity == null) {return result;}
		
		// Find attributes
		Object userIDProto = c.valueForKey(USERNAME_PROTO_KEY);
		Object userCredentialProto = c.valueForKey(PASSWORD_PROTO_KEY);
		if(userIDProto == null || userCredentialProto == null) {return result;}
		
		if(attributeForPrototype(entity, userIDProto.toString()) != null && 
				attributeForPrototype(entity, userCredentialProto.toString()) != null) {
			result = entity;
		}
		return result;
	}
	
    /**
     * Calculates and returns the default embedded inspect page 
     * configuration to be used for the user entity.
     * @param c current context
     * @return page configuration of the form: "InspectEmbedded" + the 
     *		the value of the user entity name.
     */
    public String defaultEmbeddedInspectUserConfiguration(D2WContext c) {
        return "InspectEmbedded" + c.valueForKeyPath("defaultUserEntity.name");
    }
	
    /**
     * Calculates and returns the default embedded inspect page 
     * configuration to be used for the plain text user entity.
     * @param c current context
     * @return page configuration of the form: "InspectEmbeddedPlain" + the 
     *		the value of the user entity name.
     */
    public String plainTextEmbeddedInspectUserConfiguration(D2WContext c) {
        return "InspectEmbeddedPlain" + c.valueForKeyPath("defaultUserEntity.name");
    }
	
	/**
	 * Searches entity for the erPrototype named proto.
	 * @param entity - the EOEntity to search
	 * @param proto - the string value for the key erPrototype in the 
	 * attribute's userInfo dictionary
	 * @return the first EOAttribute with a userInfo entry containing 
	 * erPrototype key with a value of proto. If no attribute matches, 
	 * the method returns null.
	 */
	protected EOAttribute attributeForPrototype(EOEntity entity, String proto) {
		EOAttribute result = null;
		for(EOAttribute attr: entity.attributes()) {
			NSDictionary<String,Object> userInfo = attr.userInfo();
			if(userInfo != null) {
				Object val = userInfo.objectForKey(PROTOTYPE_KEY);
				if(proto.equals(val)) {
					result = attr;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Searches the visible entities in the given context for the 
	 * prototype named proto
	 * @param c - the D2WContext
	 * @param proto- the string value for the key erPrototype in the 
	 * entity's userInfo dictionary
	 * @return the first EOEntity with a userInfo entry containing 
	 * erPrototype key with a value of proto. If no entity matches, 
	 * the method returns null.
	 */
	protected EOEntity entityForPrototype(D2WContext c, String proto) {
		EOEntity result = null;
		EOEditingContext ec = ERXEC.newEditingContext();
		NSArray<String> entityNames = (NSArray<String>)c.valueForKey(D2WModel.VisibleEntityNamesKey);
		for(String name: entityNames) {
			EOEntity entity = EOUtilities.entityNamed(ec, name);
			NSDictionary<String,Object> userInfo = entity.userInfo();
			if(userInfo != null) {
				Object val = userInfo.objectForKey(PROTOTYPE_KEY);
				if(proto.equals(val)){
					result = entity;
					break;
				}
			}
		}
		return result;
	}
	

}
