/*
 * Created on 26.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.webobjects.directtoweb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;


/**
 * Optimizes custom attribute handling and fixes a problem when
 * a context can't find its task or entity even though it is given in the rules.
 * @author david caching
 * @author ak factory, thread safety, fix
 */
public class ERD2WContext extends D2WContext implements Serializable {

    private static Map customAttributes = new HashMap();
    private static final Object NOT_FOUND = new Object();
    
    static {
        if(WOApplication.application().isConcurrentRequestHandlingEnabled()) {
            customAttributes = Collections.synchronizedMap(customAttributes);
        }
    }
    
    /**
     * Factory to create D2WContext's. You can provide your own subclass and
     * set it via {@link #setFactory(Factory)}. The static methods newContext(...)
     * should be used throughout ERD2W.
     * @author ak
     */
    public static class Factory {
        public D2WContext newContext() {
            return new ERD2WContext();
        }
        public D2WContext newContext(WOSession session) {
            return new ERD2WContext(session);
        }
        public D2WContext newContext(D2WContext context) {
            return new ERD2WContext(context);
        }
    }
    
    private static Factory _factory = new Factory();
    
    public static D2WContext newContext() {
        return _factory.newContext();
    }
    public static D2WContext newContext(WOSession session) {
        return _factory.newContext(session);
    }
    public static D2WContext newContext(D2WContext context) {
        return _factory.newContext(context);
    }
   
    public static void setFactory(Factory factory) {
        _factory = factory;
    }
    
    public ERD2WContext() {
        super();
    }

    public ERD2WContext(WOSession session) {
        super(session);
    }

    public ERD2WContext(D2WContext session) {
        super(session);
    }

    /**
     * Overrridden because when a page config is set, task and entity are cleared, 
     * but not re-set when you just call task() or entity(). This leads to NPEs, 
     * errors that a pageName can't be found and others. Setting it here fixes it.
     */
    public void setDynamicPage(String page) {
        super.setDynamicPage(page);
        setTask(task());
        setEntity(entity());
    }
    
    /**
     * Overridden so that custom attributes are cached as a performance
     * optimization.
     */
    EOAttribute customAttribute(String s, EOEntity eoentity) {
        String s1 = eoentity.name() + "." + s;
        Object o = customAttributes.get(s1);
        if(o == NOT_FOUND) {
            return null;
        } 
        EOAttribute eoattribute = (EOAttribute)o;
        if (eoattribute == null && s != null) {
            Class class1 = D2WUtils.dataTypeForCustomKeyAndEntity(s, eoentity);
            if (class1 != null) {
                eoattribute = new EOAttribute();
                eoattribute.setName(s);
                eoattribute.setClassName(class1.getName());
                customAttributes.put(s1, eoattribute);
            } else {
                // this should be cached, too
                // can save up to 100 millis and more for complex pages
                customAttributes.put(s1, NOT_FOUND);
            }
        }
        return eoattribute;
    }
    
//    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[] {
//    	new ObjectStreamField(D2WModel.SessionKey, WOSession.class),
//    	new ObjectStreamField(D2WModel.FrameKey, Boolean.TYPE),
//    	new ObjectStreamField(D2WModel.TaskKey, String.class),
//    	new ObjectStreamField("entityName", String.class),
//    	new ObjectStreamField(D2WModel.PropertyKeyKey, String.class),
//    	new ObjectStreamField(D2WModel.DynamicPageKey, String.class),
//    	new ObjectStreamField("object", EOEnterpriseObject.class),
//    };
    
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//		GetField fields = in.readFields();
//		takeValueForKey(fields.get(D2WModel.SessionKey, null), D2WModel.SessionKey);
//		takeValueForKey(fields.get(D2WModel.FrameKey, false)?D2WModel.One:D2WModel.Zero, D2WModel.FrameKey);
//		takeValueForKey(fields.get(D2WModel.TaskKey, null), D2WModel.TaskKey);
//		String entityName = (String) fields.get("entityName", null);
//		EOEntity entity = entityName == null?null:EOModelGroup.defaultGroup().entityNamed(entityName);
//		takeValueForKey(entity, D2WModel.EntityKey);
//		takeValueForKey(fields.get(D2WModel.PropertyKeyKey, null), D2WModel.PropertyKeyKey);
//		takeValueForKey(fields.get(D2WModel.DynamicPageKey, null), D2WModel.DynamicPageKey);
//		takeValueForKey(fields.get("object", null), "object");

		takeValueForKey(in.readObject(), D2WModel.SessionKey);
		takeValueForKey(in.readBoolean()?D2WModel.One:D2WModel.Zero, D2WModel.FrameKey);
		takeValueForKey(in.readObject(), D2WModel.TaskKey);
		String entityName = (String) in.readObject();
		EOEntity entity = entityName == null?null:EOModelGroup.defaultGroup().entityNamed(entityName);
		takeValueForKey(entity, D2WModel.EntityKey);
		takeValueForKey(in.readObject(), D2WModel.PropertyKeyKey);
		takeValueForKey(in.readObject(), D2WModel.DynamicPageKey);
		/*
		 * The ec must be deserialized before the EO. Otherwise, when the EO is
		 * deserialized, it attempts to deserialize the EC, which turns around
		 * and tries to deserialize the EO again. The EO is returned in its partially
		 * deserialized state, which results in a NullPointerException when the EC
		 * starts to try to load values into the EO's dictionary... which is null.
		 */
		EOEditingContext ec = (EOEditingContext) in.readObject();
		takeValueForKey(in.readObject(), "object");
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
//		PutField fields = out.putFields();
//		fields.put(D2WModel.SessionKey, valueForKey(D2WModel.SessionKey));
//		fields.put(D2WModel.FrameKey, frame());
//		fields.put(D2WModel.TaskKey, task());
//		fields.put("entityName", entity() == null?null:entity().name());
//		fields.put(D2WModel.PropertyKeyKey, propertyKey());
//		fields.put(D2WModel.DynamicPageKey, dynamicPage());
//		fields.put("object", valueForKey("object"));
//		out.writeFields();
		
		out.writeObject(valueForKey(D2WModel.SessionKey));
		out.writeBoolean(frame());
		out.writeObject(task());
		out.writeObject(entity() == null?null:entity().name());
		out.writeObject(propertyKey());
		out.writeObject(dynamicPage());
		EOEnterpriseObject obj = (EOEnterpriseObject) valueForKey("object");
		EOEditingContext ec = (obj == null || obj.editingContext() == null)?null:obj.editingContext();
		/*
		 * The ec must be deserialized before the EO. Otherwise, when the EO is
		 * deserialized, it attempts to deserialize the EC, which turns around
		 * and tries to deserialize the EO again. The EO is returned in its partially
		 * deserialized state, which results in a NullPointerException when the EC
		 * starts to try to load values into the EO's dictionary... which is null.
		 */
		out.writeObject(ec);
		out.writeObject(obj);
	}
}
