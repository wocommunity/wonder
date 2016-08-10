//
//  ERXSelectorUtilities.java
//  ERExtensions
//
//  Created by Jonathan B. Leffert on 7/19/05.
//

package er.extensions.foundation;

import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXConstant;

/**
 * NSSelector utilities.  These exist mostly to allow selector invocation without explicitly
 * catching the non-runtime exceptions that can be thrown by <code>NSSelector.invoke()</code>.
 */
public class ERXSelectorUtilities {

    /**
     * Just like the NSSelector method of the same name, except only NSForwardExceptions
     * are thrown.
     * @param sel the selector to invoke
     * @param o the target object of the selector
     * @param params the arguments for the invoked selector method
     * @return the result of the invoked method
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object, Object[])
     */
    public static <T> T invoke(NSSelector<T> sel, Object o, Object[] params) {
        T result = null;

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
     * @param sel the selector to invoke
     * @param o the target object of the selector
     * @return the result of the invoked method
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object)
     */
    public static <T> T invoke(NSSelector<T> sel, Object o) {
        return invoke(sel, o, null);
    }

    /**
     * Just like the NSSelector method of the same name, except only runtime
     * exceptions are thrown.
     * @param sel the selector to invoke
     * @param o the target object of the selector
     * @param argument1 the method argument
     * @return the result of the invoked method
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object, Object)
     */
    public static <T> T invoke(NSSelector<T> sel, Object o, Object argument1) {
        return invoke(sel, o, new Object[] { argument1 });
    }

    /**
     * Just like the NSSelector method of the same name, except only runtime
     * exceptions are thrown.
     * @param sel the selector to invoke
     * @param o the target object of the selector
     * @param argument1 the first method argument
     * @param argument2 the second method argument
     * @return the result of the invoked method
     *
     * @see com.webobjects.foundation.NSSelector#invoke(Object, Object, Object)
     */
    public static <T> T invoke(NSSelector<T> sel, Object o, Object argument1, Object argument2) {
        return invoke(sel, o, new Object[] { argument1, argument2 });
    }
    
    /**
     * Utility that returns a selector you can use with the NSNotificationCenter.
     * @param methodName
     * @return A selector suitable for firing a notification
     */
    public static NSSelector<Void> notificationSelector(String methodName) {
        return new NSSelector<Void>(methodName, ERXConstant.NotificationClassArray);
    }
}
