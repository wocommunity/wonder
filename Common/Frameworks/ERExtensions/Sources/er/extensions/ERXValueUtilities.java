package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

/**
 * ERXValueUtilities has usefull conversion methods for
 * reading and transforming <code>boolean</code>,
 * <code>int</code> and <code>float</code>values.
 *
 * @created ak on Mon Oct 28 2002
 * @project ERExtensions
 */

public class ERXValueUtilities {
    /**
     * This method resolves bindings from WOComponents to
     * boolean values. The added benifit (and this might not
     * still be the case) is that when <code>false</code> is
     * bound to a binding will pass through null. This makes
     * it difficult to handle the case where a binding should
     * default to true but false was actually bound to the
     * binding.<br/>
     * Note: This is only needed for non-syncronizing components
     * @param binding name of the binding
     * @param component to resolve binding request
     * @param def default value if binding is not set
     * @return boolean resolution of the object returned from the
     *		valueForBinding request.
     */
    public static boolean booleanValueForBindingOnComponentWithDefault(String binding, WOComponent component, boolean def) {
        // CHECKME: I don't believe the statement below is true with WO 5
        // this method is useful because binding=NO in fact sends null, which in turns
        // leads booleanValueWithDefault(valueForBinding("binding", true) to return true when binding=NO was specified
        boolean result=def;
        if (component!=null) {
            if (component.canGetValueForBinding(binding)) {
                Object value=component.valueForBinding(binding);
                result=value==null ? false : booleanValueWithDefault(value, def);
            }
        }
        return result;
    }

    /**
     * Basic utility method for determining if an object
     * represents either a true or false value. The current
     * implementation tests if the object is an instance of
     * a String or a Number. Numbers are false if they equal
     * <code>0</code>, Strings are false if they equal (case insensitive)
     * 'no', 'false' or parse to 0.
     * @param obj object to be evaluated
     * @return boolean evaluation of the given object
     */
    public static boolean booleanValue(Object obj) {
        return booleanValueWithDefault(obj,false);
    }

    /**
     * Basic utility method for determining if an object
     * represents either a true or false value. The current
     * implementation tests if the object is an instance of
     * a String or a Number. Numbers are false if they equal
     * <code>0</code>, Strings are false if they equal (case insensitive)
     * 'no', 'false' or parse to 0. The default value is used if
     * the object is null.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return boolean evaluation of the given object
     */
    public static boolean booleanValueWithDefault(Object obj, boolean def) {
        boolean flag = true;
        if (obj != null) {
            // FIXME: Should add support for the BooleanOperation interface
            if (obj instanceof Number) {
                if (((Number)obj).intValue() == 0)
                    flag = false;
            } else if(obj instanceof String) {
                String s = (String)obj;
                if (s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("n"))
                    flag = false;
                else if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y"))
                    flag = true;
                else
                    try {
                        if (Integer.parseInt(s) == 0)
                            flag = false;
                    } catch(NumberFormatException numberformatexception) {
                        throw new RuntimeException("error parsing boolean from value " + s);
                    }
            } else if (obj instanceof Boolean)
                flag = ((Boolean)obj).booleanValue();
        } else {
            flag = def;
        }
        return flag;
    }

    /**
     * This method resolves bindings from WOComponents to
     * <code>int</code> values.
     * Note: This is only needed for non-syncronizing components
     * @param binding name of the binding
     * @param component to resolve binding request
     * @param def default value if binding is not set
     * @return boolean resolution of the object returned from the
     *		valueForBinding request.
     */
    public static int intValueForBindingOnComponentWithDefault(String binding, WOComponent component, int def) {
        int result=def;
        if (component!=null) {
            if (component.canGetValueForBinding(binding)) {
                Object value=component.valueForBinding(binding);
                result=value==null ? def : intValueWithDefault(value, def);
            }
        }
        return result;
    }

    /**
     * Basic utility method for reading int values. The current
     * implementation uses {@link intValueWithDefault(Object,int)}
     * with a default of <code>0</code>.
     * @param obj object to be evaluated
     * @return boolean evaluation of the given object
     */
    public static int intValue(Object obj) {
        return intValueWithDefault(obj,0);
    }

    /**
     * Basic utility method for reading <code>int</code> values. The current
     * implementation tests if the object is an instance of
     * a String, Number and Boolean. Booleans are 1 if they equal
     * <code>true</code>. The default value is used if
     * the object is null or the boolean value is false.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return int evaluation of the given object
     */
    public static int intValueWithDefault(Object obj, int def) {
        int value = def;
        if (obj != null) {
            if (obj instanceof Number) {
                value = ((Number)obj).intValue();
            } else if(obj instanceof String) {
                try {
                    value = Integer.parseInt((String)obj);
                } catch(NumberFormatException numberformatexception) {
                    throw new RuntimeException("error parsing integer from value " + obj);
                }
            } else if (obj instanceof Boolean)
                value = ((Boolean)obj).booleanValue() ? 1 : def;
        } else {
            value = def;
        }
        return value;
    }
    
}
