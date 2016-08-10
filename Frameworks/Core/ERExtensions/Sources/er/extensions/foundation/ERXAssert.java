package er.extensions.foundation;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Provides flexible and powerful assertion. Is modeled a bit like log4j in that you can have multiple asserters that can have 
 * different behavior. In particular, each can be disabled, set to logging mode or set to raise an exception. Also you can set
 * your own failure handler.
 * 
 * In WebObjects.properties, you can have:
 * 
 * <blockquote><pre>
 * ERXAssert.instances=RAISE
 * ERXAssert.instances.com.somepackage=LOG
 * ERXAssert.instances.com.somepackage.someclass=RAISE
 * </pre></blockquote>
 * 
 * In your code's static initialization, you can write: 
 *
 * <blockquote><pre>
 * private static ERXAssert Assert = ERXAssert.getAssert(Foo.class);
 * private static ERXAssert Pre = ERXAssert.PRE;
 * private static ERXAssert Post = ERXAssert.POST;
 * </pre></blockquote>
 *
 * And finally, in your methods, you call it via:
 *
 * <blockquote><pre>
 * Pre.notNull(someObject);
 * Assert.notNull("someObject" ,someObject);
 * Post.notNull(someObject);
 * </pre></blockquote>
 *
 * or you can use the supplied assertors directly
 *
 * <blockquote><pre>
 * ERXAssert.DURING.notNull("someObject", someObject);
 * </pre></blockquote>
 * 
 * Most of this code is derived from Jonathan "Wolf" Rentzsch's JAssert, which can be found here:
 * <a href="http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/redshed/JAssert/">http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/redshed/JAssert/</a>.
**/
public class ERXAssert {
    private static final NSMutableDictionary _instances = new NSMutableDictionary();
    private static final NSMutableDictionary _handlersForKey = new NSMutableDictionary();
    private static final Logger log = LoggerFactory.getLogger(ERXAssert.class);

    public static final ERXAssert DURING;
    public static final ERXAssert PRE;
    public static final ERXAssert POST;

    //-------------------------------------------
    // Public assertion failure handler interface

    public interface FailureHandler {
        public void handleFailure(ERXAssert instance, String message);
    }

    protected static class EmptyHandler implements FailureHandler {
        public void handleFailure(ERXAssert instance, String message) {
        }
    }
    protected static class LoggingHandler implements FailureHandler {
        public void handleFailure(ERXAssert instance, String message) {
            String output = "Assertion failed (" + instance.name() + "): ";
            if(message != null)
                output += message;
            log.error(output, new Throwable());
        }
    }
    protected static class ThrowingHandler implements FailureHandler {
        protected void raise(String message) {
            throw new RuntimeException(message);
        }
        public void handleFailure(ERXAssert instance, String message) {
            String output = "Assertion failed (" + instance.name() + "): ";
            if(message != null)
                output += message;
            raise(output);
        }
    }
    protected static class IllegalArgumentHandler extends ThrowingHandler {
        @Override
        protected void raise(String message) {
            throw new IllegalArgumentException(message);
        }
    }
    protected static class IllegalStateHandler extends ThrowingHandler {
        @Override
        protected void raise(String message) {
            throw new IllegalStateException(message);
        }
    }

    static {
        _handlersForKey.setObjectForKey(new EmptyHandler(), "NONE");
        _handlersForKey.setObjectForKey(new LoggingHandler(), "LOG");
        _handlersForKey.setObjectForKey(new ThrowingHandler(), "RAISE");

        PRE = getAssert("PRE");
        POST = getAssert("POST");
        DURING = getAssert("DURING");
        
        PRE.setFailureHandler(new IllegalArgumentHandler());
        POST.setFailureHandler(new IllegalStateHandler());
        DURING.setFailureHandler(new IllegalStateHandler());
    }
    
    public static ERXAssert getAssert(String name) {
        synchronized(_instances) {
            ERXAssert value = (ERXAssert)_instances.objectForKey(name);
            if(value == null) {
                value = new ERXAssert(name);
                _instances.setObjectForKey(value, name);
            }
            setLevel(value);
            return value;
        }
    }

    protected static String getLevel(ERXAssert value) {
        NSArray arr = NSArray.componentsSeparatedByString(value.name(), ".");
        String prefix = "ERXAssert.instances";
        String level = System.getProperty(prefix);
        if(level == null)
            level = "LOG";
        for(Enumeration e = arr.objectEnumerator(); e.hasMoreElements(); ) {
            String s = (String)e.nextElement();
            String possibleLevel = System.getProperty(prefix + "." + s);
            if(possibleLevel != null) {
                level = possibleLevel;
            }
            prefix = prefix + "." + s;
        }
        return level;
    }

    public static void setHandlerForLevel(FailureHandler object, String level) {
        _handlersForKey.setObjectForKey(object, level);
    }

    protected static void setLevel(ERXAssert value) {
        String level = getLevel(value);
        FailureHandler handler = (FailureHandler)_handlersForKey.objectForKey(level);
        if(handler == null) {
            throw new IllegalStateException("Can't find handler for level \"" + level + "\" for assert: " + value.name());
        }
        value.setFailureHandler(handler);
    }

    public static ERXAssert getAssert(Class c) {
        return getAssert(c.getName());
    }

    public static ERXAssert getAssert(String prefix, Class c) {
        return getAssert(prefix + "." + c.getName());
    }

    protected String _name;
    
    private ERXAssert(String value) {
        _name = value;
    }

    //----------------------------------
    // Public client assertion interface

    public void isTrue(boolean value) {
        if(value == false) fail("expected true");
    }

    public void isTrue(String message, boolean value) {
        if(value == false) fail(message, "expected true");
    }

    public void isFalse(boolean value) {
        if(value) fail("expected false");
    }

    public void isFalse(String message, boolean value) {
        if(value) fail(message, "expected false");
    }

    public void isNull(Object value) {
        if(value != null) fail("expected null object, got " + value);
    }

    public void isNull(String message, Object value) {
        if(value != null) fail(message, "expected null, got " + value);
    }

    public void notNull(Object value) {
        if(value == null) fail("expected non-null object");
    }

    public void notNull(String message, Object value) {
        if(value == null) fail(message, "expected non-null object");
    }

    public void isEmpty(String value) {
        if(value != null && !value.equals("")) fail("expected empty String, got " + value);
    }

    public void notEmpty(String value) {
        if(value == null)
            fail("expected non-empty String, got null");
        else if(value.equals(""))
            fail("expected non-empty String, got empty string");
    }

    public void isZero(int value) {
        if(value != 0) fail("expected zero int, got " + value);
    }

    public void isZero(long value) {
        if(value != 0L) fail("expected zero long, got " + value);
    }

    public void isZero(double value) {
        if(value != 0.0) fail("expected zero double, got " + value);
    }

    public void notZero(int value) {
        if(value == 0) fail("expected non-zero int, got " + value);
    }

    public void notZero(long value) {
        if(value == 0L) fail("expected non-zero long, got " + value);
    }

    public void notZero(double value) {
        if(value == 0.0) fail("expected non-zero double, got " + value);
    }

    public void isNegative(int value) {
        if(value >= 0) fail("expected negative int, got " + value);
    }

    public void isNegative(long value) {
        if(value >= 0L) fail("expected negative long, got " + value);
    }

    public void isNegative(double value) {
        if(value >= 0.0) fail("expected negative double, got " + value);
    }

    public void notNegative(int value) {
        if(value < 0) fail("expected non-negative int, got " + value);
    }

    public void notNegative(long value) {
        if(value < 0L) fail("expected non-negative long, got " + value);
    }

    public void notNegative(double value) {
        if(value < 0.0) fail("expected non-negative double, got " + value);
    }

    public void isPositive(int value) {
        if(value < 0) fail("expected positive int, got " + value);
    }

    public void isPositive(long value) {
        if(value < 0L) fail("expected positive long, got " + value);
    }

    public void isPositive(double value) {
        if(value < 0.0) fail("expected positive double, got " + value);
    }

    public void notPositive(int value) {
        if(value >= 0) fail("expected non-positive int, got " + value);
    }

    public void notPositive(long value) {
        if(value >= 0L) fail("expected non-positive long, got " + value);
    }

    public void notPositive(double value) {
        if(value >= 0.0) fail("expected non-positive double, got " + value);
    }

    public void isEqual(int value1, int value2) {
        if(value1 != value2) fail("expected equal integers, got " + value1 + " & " + value2);
    }

    public void isEqual(String message, int value1, int value2) {
        if(value1 != value2) fail(message, "expected equal integers, got " + value1 + " & " + value2);
    }

    public void isEqual(long value1, long value2) {
        if(value1 != value2) fail("expected equal longs, got " + value1 + " & " + value2);
    }

    public void isEqual(double value1, double value2) {
        if(value1 != value2) fail("expected equal doubles, got " + value1 + " & " + value2);
    }

    public void isEqual(Object value1, Object value2) {
        if((value1 != null && !value1.equals(value2)) || value2 == null ) fail("expected equal objects, got " + value1 + " & " + value2);
    }

    public void isEqual(String message, Object value1, Object value2) {
        if((value1 != null && !value1.equals(value2)) || value2 == null ) fail(message, "expected equal objects, got " + value1 + " & " + value2);
    }
    
    public void notEqual(int value1, int value2) {
        if(value1 == value2) fail("expected unequal integers, got " + value1 + " & " + value2);
    }

    public void notEqual(long value1, long value2) {
        if(value1 == value2) fail("expected unequal longs, got " + value1 + " & " + value2);
    }

    public void notEqual(double value1, double value2) {
        if(value1 == value2) fail("expected unequal doubles, got " + value1 + " & " + value2);
    }

    public void notEqual(Object value1, Object value2) {
        if((value1 != null && value1.equals(value2)) || value2 == null ) fail("expected unequal objects, got " + value1 + " & " + value2);
    }

    public void lessThan(int value1, int value2) {
        if(value1 >= value2) fail("expected " + value1 + " to be less than " + value2);
    }

    public void lessThan(long value1, long value2) {
        if(value1 >= value2) fail("expected " + value1 + " to be less than " + value2);
    }

    public void lessThan(double value1, double value2) {
        if(value1 >= value2) fail("expected " + value1 + " to be less than " + value2);
    }

    public void lessThanOrEqual(int value1, int value2) {
        if(value1 > value2) fail("expected " + value1 + " to be less than or equal to " + value2);
    }

    public void lessThanOrEqual(long value1, long value2) {
        if(value1 > value2) fail("expected " + value1 + " to be less than or equal to " + value2);
    }

    public void lessThanOrEqual(double value1, double value2) {
        if(value1 > value2) fail("expected " + value1 + " to be less than or equal to " + value2);
    }

    public void greaterThan(int value1, int value2) {
        if(value1 <= value2) fail("expected " + value1 + " to be greater than " + value2);
    }

    public void greaterThan(long value1, long value2) {
        if(value1 <= value2) fail("expected " + value1 + " to be greater than " + value2);
    }

    public void greaterThan(double value1, double value2) {
        if(value1 <= value2) fail("expected " + value1 + " to be greater than " + value2);
    }

    public void greaterThanOrEqual(int value1, int value2) {
        if(value1 < value2) fail("expected " + value1 + " to be greater than or equal to " + value2);
    }

    public void greaterThanOrEqual(long value1, long value2) {
        if(value1 < value2) fail("expected " + value1 + " to be greater than or equal to " + value2);
    }

    public void greaterThanOrEqual(double value1, double value2) {
        if(value1 < value2) fail("expected " + value1 + " to be greater than or equal to " + value2);
    }

    public void unknownSwitchCase(int value) {
        fail("unknown switch case: " + value);
    }

    public String name() {
        return _name;
    }

    public FailureHandler failureHandler() {
        return _failureHandler;
    }

    public void setFailureHandler(FailureHandler handler) {
        _failureHandler = handler;
    }

    protected FailureHandler _failureHandler;

    public void fail(String message) {
        _failureHandler.handleFailure(this, message);
    }

    public void fail(String message, String supplement) {
        _failureHandler.handleFailure(this, message + ": " + supplement);
    }
}
