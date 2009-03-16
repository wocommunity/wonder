/*
 * Created on 26.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.webobjects.directtoweb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;


/**
 * Optimizes custom attribute handling and fixes a problem when
 * a context can't find its task or entity even though it is given in the rules.
 * @author david caching
 * @author ak factory, thread safety, fix
 */
public class ERD2WContext extends D2WContext {

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
}
