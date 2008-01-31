package er.extensions;
import java.math.BigDecimal;

import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * ERXValueUtilities has usefull conversion methods for
 * reading and transforming <code>boolean</code>,
 * <code>int</code> and <code>float</code>values.
 * Unless otherwise stated, when an empty string
 * (or one containing only whitespace) is given, then
 * the string is assumed to be null. This is because
 * D2W is not able to give back null values anymore.
 * @created ak on Mon Oct 28 2002
 * @project ERExtensions
 */

public class ERXValueUtilities {
    /**
     * @deprecated use ERXComponentUtilities.booleanValueForBinding(component, binding, def)
     */
    public static boolean booleanValueForBindingOnComponentWithDefault(String binding, WOComponent component, boolean def) {
        return ERXComponentUtilities.booleanValueForBinding(component, binding, def);
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
     * a <code>String</code>, a <code>Number</code> or a
     * <code>ERXUtilities.BooleanOperation</code>. 
     * Numbers are false if they equal <code>0</code>,
     * Strings are false if they equal (case insensitive)
     * 'no', 'false' or parse to 0. 
     * <code>ERXUtilities.BooleanOperation</code> are false if <code>value</code>
     * returns <code>false</code>.
     * The default value is used if the object is null.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return boolean evaluation of the given object
     */
    public static boolean booleanValueWithDefault(Object obj, boolean def) {
        boolean flag = true;
        if (obj != null) {
            if (obj instanceof Number) {
                if (((Number)obj).intValue() == 0)
                    flag = false;
            } else if(obj instanceof String) {
                String s = ((String)obj).trim();
                if (s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("n"))
                    flag = false;
                else if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y"))
                    flag = true;
                else
                    try {
                        if (s.length() > 0 && Integer.parseInt(s) == 0)
                            flag = false;
                    } catch(NumberFormatException numberformatexception) {
                        throw new RuntimeException("Error parsing boolean from value \"" + s + "\"");
                    }
            } else if (obj instanceof Boolean) {
                flag = ((Boolean)obj).booleanValue();
            } else if( obj instanceof ERXUtilities.BooleanOperation )
                flag = ((ERXUtilities.BooleanOperation) obj ).value();
        } else {
            flag = def;
        }
        return flag;
    }

    /**
     * Basic utility method for reading int values. The current
     * implementation uses {@link #intValueWithDefault(Object, int)}
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
                    String s = ((String)obj).trim(); // Need to trim trailing space
                    if(s.length() > 0)
                        value = Integer.parseInt(s);
                } catch(NumberFormatException numberformatexception) {
                    throw new IllegalStateException("Error parsing integer from value : <" + obj + ">");
                }
            } else if (obj instanceof Boolean)
                value = ((Boolean)obj).booleanValue() ? 1 : def;
        } else {
            value = def;
        }
        return value;
    }

    /**
     * This method resolves bindings from WOComponents to
     * <code>long</code> values.
     * Note: This is only needed for non-syncronizing components
     * @param binding name of the binding
     * @param component to resolve binding request
     * @param def default value if binding is not set
     * @return boolean resolution of the object returned from the
     *		valueForBinding request.
     */
    public static long longValueForBindingOnComponentWithDefault(String binding, WOComponent component, long def) {
        long result=def;
        if (component!=null) {
            if (component.canGetValueForBinding(binding)) {
                Object value=component.valueForBinding(binding);
                result=value==null ? def : longValueWithDefault(value, def);
            }
        }
        return result;
    }

    /**
     * Basic utility method for reading long values. The current
     * implementation uses {@link #longValueWithDefault(Object, long)}
     * with a default of <code>0</code>.
     * @param obj object to be evaluated
     * @return boolean evaluation of the given object
     */
    public static long longValue(Object obj) {
        return longValueWithDefault(obj,0);
    }

    /**
     * Basic utility method for reading <code>long</code> values. The current
     * implementation tests if the object is an instance of
     * a String, Number and Boolean. Booleans are 1 if they equal
     * <code>true</code>. The default value is used if
     * the object is null or the boolean value is false.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return long evaluation of the given object
     */
    public static long longValueWithDefault(Object obj, long def) {
        long value = def;
        if (obj != null) {
            if (obj instanceof Number) {
                value = ((Number)obj).longValue();
            } else if(obj instanceof String) {
                try {
                    String s = ((String)obj).trim(); // Need to trim trailing space
                    if(s.length() > 0)
                        value = Long.parseLong(s);
                } catch(NumberFormatException numberformatexception) {
                    throw new IllegalStateException("Error parsing long from value : <" + obj + ">");
                }
            } else if (obj instanceof Boolean)
                value = ((Boolean)obj).booleanValue() ? 1 : def;
        } else {
            value = def;
        }
        return value;
    }

    /**
     * Basic utility method for reading NSArray values which works also with Strings.
     * The current implementation uses {@link #arrayValueWithDefault(Object, NSArray)}
     * with a default of <code>null</code>.
     * @param obj object to be evaluated
     * @return NSArray evaluation of the given object
     */
    public static NSArray arrayValue(Object obj) {
        return arrayValueWithDefault(obj,null);
    }

    /**
     * Basic utility method for reading <code>NSArray</code> values
     * which also works with serialzed NSArrays and comma seperated items. The default value is used if
     * the object is null.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return int evaluation of the given object
     */
    public static NSArray arrayValueWithDefault(Object obj, NSArray def) {
        NSArray value = def;
        if (obj != null) {
            if (obj instanceof NSArray) {
                value =(NSArray)obj;
            } else if(obj instanceof String) {
            	String s = (String)obj;
            	if(s.length() > 0 && s.charAt(0) != '(') {
            		s = "(" + s + ")";
            	}
                value = (NSArray)NSPropertyListSerialization.propertyListFromString(s);
            } else {
                throw new RuntimeException("Not a String or NSArray " + obj);
            }
        }
        return value;
    }

    /**
     * Basic utility method for reading NSDictionary values which works also with Strings.
     * The current implementation uses {@link #dictionaryValueWithDefault(Object, NSDictionary)}
     * with a default of <code>null</code>.
     * @param obj object to be evaluated
     * @return NSDictionary evaluation of the given object
     */
    public static NSDictionary dictionaryValue(Object obj) {
        return dictionaryValueWithDefault(obj,null);
    }

    /**
     * Basic utility method for reading <code>NSDictionary</code> values
     * which also works with serialzed NSDictionarys. The default value is used if
     * the object is null.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return int evaluation of the given object
     */
    public static NSDictionary dictionaryValueWithDefault(Object obj, NSDictionary def) {
        NSDictionary value = def;
        if (obj != null) {
            if (obj instanceof NSDictionary) {
                value =(NSDictionary)obj;
            } else if(obj instanceof String) {
                value = (NSDictionary)NSPropertyListSerialization.propertyListFromString((String)obj);
            } else {
                throw new RuntimeException("Not a String or NSDictionary " + obj);
            }
        }
        return value;
    }


    /**
     * Basic utility method for reading BigDecimal values which works also with Strings.
     * The current implementation uses {@link #bigDecimalValueWithDefault(Object, BigDecimal)}
     * with a default of <code>null</code>.
     * @param obj object to be evaluated
     * @return BigDecimal evaluation of the given object
     */
    public static BigDecimal bigDecimalValue(Object obj) {
        return bigDecimalValueWithDefault(obj,null);
    }

    /**
     * Basic utility method for reading <code>BigDecimal</code> values.
     * The default value is used if the object is null.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return int evaluation of the given object
     */
    public static BigDecimal bigDecimalValueWithDefault(Object obj, BigDecimal def) {
        BigDecimal value = def;
        if (obj != null) {
            if (obj instanceof BigDecimal) {
                value =(BigDecimal)obj;
            } else if(obj instanceof String) {
                String s = ((String)obj).trim();
                if(s.length() > 0)
                    value = new BigDecimal(s);
            } else if(obj instanceof Number) {
                value = new BigDecimal(((Number)obj).doubleValue());
            } else if(obj instanceof Boolean) {
                value = new BigDecimal(((Boolean)obj).booleanValue() ? 0.0D : 1.0D);
            } else {
                throw new RuntimeException("Not a String or Number " + obj);
            }
        }
        return value;
    }    
}
