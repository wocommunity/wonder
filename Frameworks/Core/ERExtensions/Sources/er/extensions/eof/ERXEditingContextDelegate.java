/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;

import er.extensions.foundation.ERXProperties;

/**
 * This delegate does nothing. It is used to
 * make the environment happy in that some
 * parts of the system require that a delegate
 * be set on the editing context. In those cases
 * use an instance of this delegate. All of the
 * other delegates subclass this delegate. The main
 * delegate that is used is {@link ERXDefaultEditingContextDelegate}.
 */
public class ERXEditingContextDelegate implements java.io.Serializable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERXEditingContextDelegate.class);

    /**
     * No arg constructor for Serializable.
     */
    public ERXEditingContextDelegate() {}

    /**
     * By default, and this should change in the future, all editing contexts that
     * are created and use ERXGenericRecords or subclasses need to have a delegate
     * set of instance {@link ERXEditingContextDelegate}. These delegates provide
     * the augmentation to the regular transaction mechanism, all of the will* methods
     * plus the flushCaching method. To change the default behaviour set the property:
     * <b>er.extensions.ERXRaiseOnMissingEditingContextDelegate</b> to false in your
     * WebObjects.properties file. This method is called when an object is fetched,
     * updated or inserted.
     * @param editingContext to check for the correct delegate.
     * @return if the editing context has the correct delegate set.
     */
    public static boolean _checkEditingContextDelegate(EOEditingContext editingContext) {
        if(editingContext instanceof ERXEC) {
            return true;
        }
        Object delegate=editingContext.delegate();
        if (delegate==null) {
            boolean _raiseOnMissingEditingContextDelegate = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXRaiseOnMissingEditingContextDelegate", true);
            EOObjectStore parent = editingContext.parentObjectStore();
            if(!_raiseOnMissingEditingContextDelegate && parent != null && parent instanceof EOEditingContext) {
                Object parentDelegate=((EOEditingContext)parent).delegate();
                if(parentDelegate != null && (parentDelegate instanceof ERXEditingContextDelegate)) {
                    editingContext.setDelegate(parentDelegate);
                    log.info("Found null delegate. Setting to the parent's delegate.");
                    return true;
                }
            }
            if(!_raiseOnMissingEditingContextDelegate) {
                log.warn("Found null delegate. I will fix this for now by setting it to ERXExtensions.defaultDelegate");
                ERXEC._factory().setDefaultDelegateOnEditingContext(editingContext);
                return true;
            }
            throw new IllegalStateException("Found null delegate. You can disable this check by setting er.extensions.ERXRaiseOnMissingEditingContextDelegate=false in your WebObjects.properties");
        }
        if (!(delegate instanceof ERXEditingContextDelegate)) {
            boolean _raiseOnMissingEditingContextDelegate = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXRaiseOnMissingEditingContextDelegate", true);
            if(!_raiseOnMissingEditingContextDelegate) {
                log.warn("Found unexpected delegate class: {}", delegate.getClass());
                return true;
            }
            throw new IllegalStateException("Found unexpected delegate class. You can disable this check by setting er.extensions.ERXRaiseOnMissingEditingContextDelegate=false in your WebObjects.properties");
        }
        return false;
    }
}

