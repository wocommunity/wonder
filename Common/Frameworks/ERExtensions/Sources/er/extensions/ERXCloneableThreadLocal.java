/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * <code>ERXClonableThreadLocal</code> extends {@link InheritableThreadLocal}
 * to bequeath a cloned copy of the parent object to the child thread.<br/>
 * <br/>
 * Note: Objects used with this thread local must implement the {@link Cloneable}
 * interface and have a public <code>clone</code> method.
 */
public class ERXCloneableThreadLocal extends InheritableThreadLocal {

    /** logging support */
    protected static final Logger log = Logger.getLogger(ERXCloneableThreadLocal.class);

    /**
     * Clones a copy of the parent object for the child thread.
     * The parentValue must implement the {@link Cloneable}
     * interface and have a public <code>clone</code> method.
     * @param parentValue local object to the parent thread.
     * @return a cloned value of the parent if not null.
     */
    protected Object childValue(Object parentValue) {
        Object child = null;
        if (parentValue != null) {
            if (!(parentValue instanceof Cloneable)) {
                throw new IllegalStateException("Using a ERXCloneableThreadLocal with an object: " 
                    + parentValue.getClass() + " " + parentValue.toString() 
                    + " that does not implement the Cloneable interface ");
            }
            // This is very lame. clone() is a protected method off of object and the Cloneable
            // interface doesn't specify any methods.
            try {
                Method m = parentValue.getClass().getMethod("clone", ERXConstant.EmptyClassArray);
                child = m.invoke(parentValue, ERXConstant.EmptyObjectArray);
            } catch (InvocationTargetException ite) {
                log.error("Invocation exception occurred when invoking clone in ERXClonableThreadLocal:" 
                        + ite.getTargetException() + " backtrace: " + ERXUtilities.stackTrace(ite.getTargetException()));
            } catch (NoSuchMethodException nsme) {
                log.error("No clone method for the class: " + parentValue.getClass() + " very strange.");
            } catch (IllegalAccessException iae) {
                log.error("Clone method has protected or private access for the object: "
                        + parentValue.getClass() + " " + parentValue.toString() 
                        + " exception: " + iae.getMessage());
            }
        }            
        return child;
    }
}
