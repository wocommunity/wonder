/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.KeyValuePath;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXConstant;
import er.extensions.ERXDictionaryUtilities;
import er.extensions.ERXEOAccessUtilities;

/**
 * A bunch of methods used for pulling default values from EOModels.<br />
 * Provides defaults for the following keys:
 * <ul>
 * <li><code>smartAttribute</code></li>
 * <li><code>smartRelationship</code></li>
 * <li><code>smartDefaultRows</code></li>
 * <li><code>smartDefaultAttributeWidth</code></li>
 * <li><code>entity</code></li>
 * <li><code>dummyEntity</code></li>
 * <li><code>destinationEntity</code></li>
 * <li><code>entityForControllerName</code></li>
 * <li><code>entityForPageConfiguration</code></li>
 * <li><code>sortKeyForList</code></li>
 * </ul>
 */

public class ERDDefaultModelAssignment extends ERDAssignment {

    /** logging support */
    public final static Logger log = Logger.getLogger(ERDDefaultModelAssignment.class);

    /** holds the array of keys this assignment depends upon */
    protected static final NSDictionary keys = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys( new Object [] {
        new NSArray(new Object[] {"propertyKey", "object.entityName"}), "smartAttribute",
        new NSArray(new Object[] {"propertyKey", "object.entityName"}), "smartRelationship",
        new NSArray(new Object[] {"smartAttribute"}), "attributeConstants",
        new NSArray(new Object[] {"smartAttribute"}), "smartDefaultRows",
        new NSArray(new Object[] {"smartAttribute"}), "smartDefaultAttributeWidth",
        new NSArray(new Object[] {"smartRelationship"}), "destinationEntity",
        new NSArray(new Object[] {"propertyKey", "keyWhenRelationship"}), "sortKeyForList",
        new NSArray(new Object[] {"controllerName"}), "entityForControllerName",
        new NSArray(new Object[] {"pageConfiguration"}), "entityForPageConfiguration",
        NSArray.EmptyArray, "entity",
        NSArray.EmptyArray, "dummyEntity"
    });

    /**
     * Implementation of the {@link ERDComputingAssignmentInterface}. This array
     * of keys is used when constructing the
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for.
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        return (NSArray)keys.valueForKey(keyPath);
    }
    
    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
     // ENHANCEME: Only ever need one of these assignments.
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultModelAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDefaultModelAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultModelAssignment (String key, Object value) { super(key,value); }
        
    protected int attributeWidthAsInt(D2WContext c) {
        EOAttribute a = (EOAttribute)c.valueForKey("smartAttribute");
        return a!=null ? a.width() : 0;
    }

    protected int smartDefaultAttributeWidthAsInt(D2WContext c) {
        int i=attributeWidthAsInt(c);
        return i<50 ? ( i==0 ? 20 : i ) : 50;        
    }

    public Object smartDefaultAttributeWidth(D2WContext c) {
        return String.valueOf(smartDefaultAttributeWidthAsInt(c));
    }

    public Object smartDefaultRows(D2WContext c) {
        int i = attributeWidthAsInt(c);
        int j = smartDefaultAttributeWidthAsInt(c);
        int k = j == 0 ? i : (int)((double)(i / j) + 0.5D);
        if(k > 8) k = 8;
        return String.valueOf(k);
    }


    /**
     * Resolves the {@link EOAttribute} in a smarter manner using
     * the current object from the context as well as the propertyKey
     * to determine the current attribute. Works even with inheirtance.
     * Works around the following problem:
     * An entity A has a relationship b to an entity B, which has a
     * subentity B1. B1 has an attribute k, which B does not have.
     * If in an inspect page for entity A, you use b.k as a display
     * key, then the D2W rules which are based on d2wContext.attribute
     * will not fire properly. This is because attribute is null, instead
     * of containing <EOAttribute entity=B1 name=k>. The reason D2W does
     * not find it is that it uses the Model to find out the EOAttribute
     * and starts from A. Following the relationship b, gives a B, and
     * asking B for an attribute named k returns nil and you lose.
     * @param c current D2W context
     * @return attribute for the current propertyKey object combination.
     */
    
    public Object smartAttribute(D2WContext c) {
        EOAttribute result=null;
        String propertyKey=c.propertyKey();
        Object rawObject=c.valueForKey("object");
        if (rawObject!=null && rawObject instanceof EOEnterpriseObject && propertyKey!=null) {
            EOEnterpriseObject object=(EOEnterpriseObject)rawObject;
            EOEnterpriseObject lastEO=object;
            if (lastEO!=null && propertyKey.indexOf(".")!=-1 && propertyKey.indexOf("@")==-1) {
                String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(propertyKey);
                Object rawLastEO=object.valueForKeyPath(partialKeyPath);
                lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
            }
            if (lastEO!=null) {
                EOEntity entity=EOModelGroup.defaultGroup().entityNamed(lastEO.entityName());
                String lastKey=KeyValuePath.lastPropertyKeyInKeyPath(propertyKey);
                result=entity.attributeNamed(lastKey);
            }
        }
        if (result==null) {
            // default to the basic attribute if the above didnt' work
            if (propertyKey!=null) result=c.attribute();
        }
        return result;
    }

    /**
     * Resolves the {@link EORelationship} in a smarter manner using
     * the current object from the context as well as the propertyKey
     * to determine the current relationship. Works even with inheritance.
     * Works around the following problem:</br>
     * An entity A has a relationship b to an entity B, which
     * has a subentity B1. B1 has a relationship k, which B does
     * not have. If in an inspect page for entity A, you use b.k
     * as a display key, then the D2W rules which are based on
     * d2wContext.relationship will not fire properly. This is
     * because relationship is null, instead of containing
     * <EORelationship entity=B1 name=k>. The reason D2W does not
     * find it is that it uses the Model to find out the EORelationship
     * and starts from A. Following the relationship b, gives a B, and
     * asking B for a relationship named k returns null and you lose.
     * @param c current D2W context
     * @return relationship for the current propertyKey object combination.
     */
    public Object smartRelationship(D2WContext c) {
        Object result = null;
        Object rawObject=c.valueForKey("object");
        String propertyKey=c.propertyKey();
        if(propertyKey != null) {
            if (rawObject!=null && rawObject instanceof EOEnterpriseObject) {
                EOEnterpriseObject object=(EOEnterpriseObject)rawObject;
                EOEnterpriseObject lastEO=object;
                if (propertyKey.indexOf(".")!=-1 && propertyKey.indexOf("@")==-1) {
                    String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(propertyKey);
                    Object rawLastEO=object.valueForKeyPath(partialKeyPath);
                    lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
                }
                if (lastEO!=null) {
                    EOEntity entity=EOModelGroup.defaultGroup().entityNamed(lastEO.entityName());
                    String lastKey=KeyValuePath.lastPropertyKeyInKeyPath(propertyKey);
                    result=entity.relationshipNamed(lastKey);
                }
            }
            if (result==null) {
                // working around D2W bug
                result=c.relationship();
            }
        }
        return result;
    }

    private EOEntity _dummyEntity;
    /** Utility to create a fake entity that can be used for tasks such as error/confirm. */
    // CHECKME ak We may have to insert the entity into the default model group
    protected EOEntity dummyEntity() {
        if (_dummyEntity==null) {
            _dummyEntity=new EOEntity();
            _dummyEntity.setName("*all*");
        }
        return _dummyEntity;
    }

    /**
     * Returns a fake entity that can be used for tasks such as error/confirm.
     * @param c current D2W context
     * @return dummy entity.
     */
    public Object dummyEntity(D2WContext c) {
        return dummyEntity();
    }

    /**
     * Returns a fake entity that can be used for tasks such as error/confirm.
     * @param c current D2W context
     * @return dummy entity.
     */
    public Object entity(D2WContext c) {
        return dummyEntity();
    }

    protected boolean isTaskWithoutEntity(String task) {
        return ("queryAll".equals(task) || "confirm".equals(task) || "error".equals(task));
    }

    protected Object entityForKey(D2WContext c, String key) {
        Object result = null;
        if(key != null) {
            result = ERXEOAccessUtilities.entityMatchingString((EOEditingContext)c.valueForKey("session.defaultEditingContext"), (String)c.valueForKey(key));
        }
        if(result == null && isTaskWithoutEntity(c.task())) {
            result = dummyEntity();
        }
        return result;
    }


    /**
     * Returns the default value for the entity based on the controllerName. 
     * @param c current D2W context
     * @return the entity.
     */
    public Object entityForControllerName(D2WContext c) {
        return entityForKey(c, "controllerName");
    }



    /**
     * Returns the default value for the entity based on the controllerName. 
     * @param c current D2W context
     * @return the entity.
     */
    public Object attributeConstants(D2WContext c) {
    	EOAttribute attr = (EOAttribute)c.valueForKey("smartAttribute");
    	if(attr != null && attr.userInfo() != null) {
    		String clazzName = (String)attr.userInfo().objectForKey("ERXConstantClassName");
    		if(clazzName != null) {
    			return ERXConstant.constantsForClassName(clazzName);
    		}
    	}
        return null;
    }

    /**
     * Returns the default value for the entity based on the pageConfiguration. 
     * @param c current D2W context
     * @return the entity.
     */

    public Object entityForPageConfiguration(D2WContext c) {
        return entityForKey(c, "pageConfiguration");
    }

    /**
     * Returns the default value for the destination entity. This is a value add because
     * we are now also able to set the destination entity in other rules.
     * @param c current D2W context
     * @return the destination entity.
     */
    public Object destinationEntity(D2WContext c) {
        EOEntity destinationEntity = (EOEntity)c.valueForKeyPath("smartRelationship.destinationEntity");
        return destinationEntity;
    }
    /**
     * Called when firing this assignment with the key-path:
     * <b>sortKeyForList</b>.
     * @return the current propertyKey + "." + the current value for
     *		keyWhenRelationship.
     */
    public Object sortKeyForList(D2WContext context) {
        return context.valueForKey("propertyKey")+"."+context.valueForKey("keyWhenRelationship");
    }
}
