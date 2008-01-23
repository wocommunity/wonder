//
//  ERXSelectorUtilities.java
//  ERExtensions
//
//  Created by Jonathan B. Leffert on 7/19/05.
//

package er.extensions;

import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSSelector;

/**
 * NSSelector utilities.  These exist mostly to allow selector invocation without explicitly
 * catching the non-runtime exceptions that can be thrown by <code>NSSelector.invoke()</code>.
 */
public class ERXSelectorUtilities {

    /**
     * Just like the NSSelector method of the same name, except only NSForwardExceptions
     * are thrown.
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object, Object[])
     */
    public static Object invoke(NSSelector sel, Object o, Object[] params) {
        Object result = null;

        try {
            result = sel.invoke(o, params);
        }
        catch ( Exception e ) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }

        return result;
    }

    /**
     * Just like the NSSelector method of the same name, except only runtime
     * exceptions are thrown.
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object)
     */
    public static Object invoke(NSSelector sel, Object o) {
        return invoke(sel, o, null);
    }

    /**
     * Just like the NSSelector method of the same name, except only runtime
     * exceptions are thrown.
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object, Object)
     */
    public static Object invoke(NSSelector sel, Object o, Object argument1) {
        return invoke(sel, o, new Object[] { argument1 });
    }

    /**
     * Just like the NSSelector method of the same name, except only runtime
     * exceptions are thrown.
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object, Object, Object)
     */
    public static Object invoke(NSSelector sel, Object o, Object argument1, Object argument2) {
        return invoke(sel, o, new Object[] { argument1, argument2 });
    }
    
    /**
     * Utility that returns a selector you can use with the NSNotificationCenter.
     * @param methodName
     */
    public static NSSelector notificationSelector(String methodName) {
        return new NSSelector(methodName, new Class[] {NSNotification.class});
    }
}
