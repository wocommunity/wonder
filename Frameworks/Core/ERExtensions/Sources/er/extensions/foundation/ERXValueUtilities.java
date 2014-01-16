package er.extensions.foundation;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSet;

import er.extensions.components.ERXComponentUtilities;

/**
 * ERXValueUtilities has useful conversion methods for
 * reading and transforming <code>boolean</code>,
 * <code>int</code> and <code>float</code>values.
 * Unless otherwise stated, when an empty string
 * (or one containing only whitespace) is given, then
 * the string is assumed to be null. This is because
 * D2W is not able to give back null values anymore.
 * 
 * @author ak on Mon Oct 28 2002
 */
public class ERXValueUtilities {
	/**
	 * Returns whether or not the given object is null or NSKVC.Null.
	 * 
	 * @param obj the object to check
	 * @return true if the object is null or NSKVC.Null
	 */
	public static boolean isNull(Object obj) {
		return obj == null || obj == NSKeyValueCoding.NullValue || obj instanceof NSKeyValueCoding.Null;
	}
	
    /**
     * @param binding the binding to parse
     * @param component the component to evaluate the binding on
     * @param def the default value if the binding value is null
     * @return the boolean value of the binding
     * @deprecated use {@link er.extensions.components.ERXComponentUtilities#booleanValueForBinding(WOComponent, String, boolean)}
     */
    @Deprecated
    public static boolean booleanValueForBindingOnComponentWithDefault(String binding, WOComponent component, boolean def) {
        return ERXComponentUtilities.booleanValueForBinding(component, binding, def);
    }

    /**
	 * Basic utility method for determining if an object represents either a
	 * true or false value. The current implementation tests if the object is an
	 * instance of a String or a Number. Numbers are false if they equal
	 * <code>0</code>, Strings are false if they equal (case insensitive) 'no',
	 * 'false' or parse to 0.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return boolean evaluation of the given object
	 * 
	 */
	public static boolean booleanValue(Object obj) {
		return ERXValueUtilities.booleanValueWithDefault(obj, false);
	}

	/**
	 * Basic utility method for determining if an object represents either a
	 * true or false value. The current implementation tests if the object is an
	 * instance of a <code>String</code>, or a <code>Number</code>. Numbers are
	 * false if they equal <code>0</code>, Strings are false if they equal (case
	 * insensitive) 'no', 'false' or parse to 0. The default value is used if
	 * the object is null.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return boolean evaluation of the given object
	 * 
	 */
	public static boolean booleanValueWithDefault(Object obj, boolean def) {
		return ERXValueUtilities.isNull(obj) ? def : BooleanValueWithDefault(obj, Boolean.valueOf(def));
	}

	/**
	 * Basic utility method for determining if an object represents either a
	 * true or false value. The current implementation tests if the object is an
	 * instance of a <code>String</code>, or a <code>Number</code>. Numbers are
	 * false if they equal <code>0</code>, Strings are false if they equal (case
	 * insensitive) 'no', 'false' or parse to 0. The default value is used if
	 * the object is null.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return boolean evaluation of the given object
	 * 
	 */
	public static Boolean BooleanValueWithDefault(Object obj, Boolean def) {
		Boolean flag = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof Number) {
				if (((Number) obj).intValue() == 0) {
					flag = Boolean.FALSE;
				} else {
					flag = Boolean.TRUE;
				}
			} else if (obj instanceof String) {
				String strValue = ((String) obj).trim();
				if (strValue.length() > 0) {
					if (strValue.equalsIgnoreCase("no") || strValue.equalsIgnoreCase("false") || strValue.equalsIgnoreCase("n")) {
						flag = Boolean.FALSE;
					} else if (strValue.equalsIgnoreCase("yes") || strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("y")) {
						flag = Boolean.TRUE;
					} else {
						try {
							if (Integer.parseInt(strValue) == 0) {
								flag = Boolean.FALSE;
							} else {
								flag = Boolean.TRUE;
							}
						} catch (NumberFormatException numberformatexception) {
							throw new IllegalArgumentException("Failed to parse a boolean from the value '" + strValue + "'.");
						}
					}
				}
			} else if (obj instanceof Boolean) {
				flag = (Boolean) obj;
			// MS: Nothing actually implements BooleanOperation ...
			} else if( obj instanceof ERXUtilities.BooleanOperation ) {
                flag = ((ERXUtilities.BooleanOperation) obj ).value();
			} else {
				throw new IllegalArgumentException("Failed to parse a boolean from the value '" + obj + "'.");
			}
		}
		return flag;
	}

	/**
	 * Basic utility method for reading int values. The current implementation
	 * uses {@link #intValueWithDefault(Object, int)} with a default of
	 * <code>0</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return integer evaluation of the given object
	 * 
	 */
	public static int intValue(Object obj) {
		return ERXValueUtilities.intValueWithDefault(obj, 0);
	}

	/**
	 * Basic utility method for reading <code>int</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return integer evaluation of the given object
	 * 
	 */
	public static int intValueWithDefault(Object obj, int def) {
		return ERXValueUtilities.isNull(obj) ? def : IntegerValueWithDefault(obj, Integer.valueOf(def));
	}

	/**
	 * Basic utility method for reading <code>Integer</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return Integer evaluation of the given object
	 * 
	 */
	public static Integer IntegerValueWithDefault(Object obj, Integer def) {
		Integer value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof Integer) {
				value = ((Integer) obj).intValue();
			} else if (obj instanceof Number) {
				value = Integer.valueOf(((Number) obj).intValue());
			} else if (obj instanceof String) {
				try {
					String strValue = ((String) obj).trim(); // Need to trim
																// trailing
																// space
					if (strValue.length() > 0) {
						value = Integer.valueOf(strValue);
					}
				} catch (NumberFormatException numberformatexception) {
					throw new IllegalArgumentException("Failed to parse an integer from the value '" + obj + "'.", numberformatexception);
				}
			} else if (obj instanceof Boolean) {
				value = ((Boolean) obj).booleanValue() ? Integer.valueOf(1) : def;
			}
		} else {
			value = def;
		}
		return value;
	}

	/**
	 * Basic utility method for reading float values. The current implementation
	 * uses {@link #floatValueWithDefault(Object, float)} with a default of
	 * <code>0</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return float evaluation of the given object
	 * 
	 */
	public static float floatValue(Object obj) {
		return ERXValueUtilities.floatValueWithDefault(obj, 0);
	}

	/**
	 * Basic utility method for reading <code>float</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return float evaluation of the given object
	 * 
	 */
	public static float floatValueWithDefault(Object obj, float def) {
		return ERXValueUtilities.isNull(obj) ? def : FloatValueWithDefault(obj, Float.valueOf(def));
	}

	/**
	 * Basic utility method for reading <code>Float</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return Float evaluation of the given object
	 * 
	 */
	public static Float FloatValueWithDefault(Object obj, Float def) {
		Float value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof Float) {
				value = (Float) obj;
			} else if (obj instanceof Number) {
				value = Float.valueOf(((Number) obj).floatValue());
			} else if (obj instanceof String) {
				try {
					String strValue = ((String) obj).trim(); // Need to trim
																// trailing
																// space
					if (strValue.length() > 0) {
						value = Float.valueOf(strValue);
					}
				} catch (NumberFormatException numberformatexception) {
					throw new IllegalArgumentException("Failed to parse a float from the value '" + obj + "'.", numberformatexception);
				}
			} else if (obj instanceof Boolean) {
				value = ((Boolean) obj).booleanValue() ? Float.valueOf(1.0f) : def;
			}
		} else {
			value = def;
		}
		return value;
	}

	/**
	 * Basic utility method for reading double values. The current
	 * implementation uses {@link #doubleValueWithDefault(Object, double)} with
	 * a default of <code>0</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return double evaluation of the given object
	 * 
	 */
	public static double doubleValue(Object obj) {
		return ERXValueUtilities.doubleValueWithDefault(obj, 0);
	}

	/**
	 * Basic utility method for reading <code>double</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return double evaluation of the given object
	 * 
	 */
	public static double doubleValueWithDefault(Object obj, double def) {
		return ERXValueUtilities.isNull(obj) ? def : DoubleValueWithDefault(obj, Double.valueOf(def));
	}

	/**
	 * Basic utility method for reading <code>Double</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return Double evaluation of the given object
	 * 
	 */
	public static Double DoubleValueWithDefault(Object obj, Double def) {
		Double value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof Double) {
				value = (Double) obj;
			} else if (obj instanceof Number) {
				value = Double.valueOf(((Number) obj).doubleValue());
			} else if (obj instanceof String) {
				try {
					String strValue = ((String) obj).trim(); // Need to trim
																// trailing
																// space
					if (strValue.length() > 0) {
						value = Double.valueOf(strValue);
					}
				} catch (NumberFormatException numberformatexception) {
					throw new IllegalArgumentException("Failed to parse a double from the value '" + obj + "'.", numberformatexception);
				}
			} else if (obj instanceof Boolean) {
				value = ((Boolean) obj).booleanValue() ? Double.valueOf(1.0) : def;
			}
		} else {
			value = def;
		}
		return value;
	}

	/**
	 * Basic utility method for reading long values. The current implementation
	 * uses {@link #longValueWithDefault(Object, long)} with a default of
	 * <code>0</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return long evaluation of the given object
	 * 
	 */
	public static long longValue(Object obj) {
		return ERXValueUtilities.longValueWithDefault(obj, 0);
	}

	/**
	 * Basic utility method for reading <code>long</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return long evaluation of the given object
	 * 
	 */
	public static long longValueWithDefault(Object obj, long def) {
		return ERXValueUtilities.isNull(obj) ? def : LongValueWithDefault(obj, Long.valueOf(def));
	}

	/**
	 * Basic utility method for reading <code>Long</code> values. The current
	 * implementation tests if the object is an instance of a String, Number and
	 * Boolean. Booleans are 1 if they equal <code>true</code>. The default
	 * value is used if the object is null or the boolean value is false.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return Long evaluation of the given object
	 * 
	 */
	public static Long LongValueWithDefault(Object obj, Long def) {
		Long value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof Long) {
				value = (Long) obj;
			} else if (obj instanceof Number) {
				value = Long.valueOf(((Number) obj).longValue());
			} else if (obj instanceof String) {
				try {
					String strValue = ((String) obj).trim(); // Need to trim
																// trailing
																// space
					if (strValue.length() > 0) {
						value = Long.valueOf(strValue);
					}
				} catch (NumberFormatException numberformatexception) {
					throw new IllegalArgumentException("Failed to parse a long from the value '" + obj + "'.", numberformatexception);
				}
			} else if (obj instanceof Boolean) {
				value = ((Boolean) obj).booleanValue() ? Long.valueOf(1L) : def;
			}
		} else {
			value = def;
		}
		return value;
	}

	/**
	 * Basic utility method for reading NSArray values which works also with
	 * Strings. The current implementation uses
	 * {@link #arrayValueWithDefault(Object, NSArray)} with a default of
	 * <code>null</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return NSArray evaluation of the given object
	 * 
	 */
	public static NSArray arrayValue(Object obj) {
		return ERXValueUtilities.arrayValueWithDefault(obj, null);
	}

	/**
	 * Basic utility method for reading <code>NSArray</code> values which also
	 * works with serialized NSArrays and comma separated items. The default
	 * value is used if the object is null.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return NSArray evaluation of the given object
	 * 
	 */
	public static NSArray arrayValueWithDefault(Object obj, NSArray def) {
		NSArray value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof NSArray) {
				value = (NSArray) obj;
			} else if (obj instanceof String) {
				String strValue = ((String) obj).trim();
				if (strValue.length() > 0) {
					if (strValue.charAt(0) != '(') {
						strValue = "(" + strValue + ")";
					}
					value = (NSArray) NSPropertyListSerialization.propertyListFromString(strValue);
					if (value == null) {
						throw new IllegalArgumentException("Failed to parse an array from the value '" + obj + "'.");
					}
				}
			} else {
				throw new IllegalArgumentException("Failed to parse an array from the value '" + obj + "'.");
			}
		}
		return value;
	}

	/**
	 * Basic utility method for reading NSSet values which works also with
	 * Strings. The current implementation uses
	 * {@link #setValueWithDefault(Object, NSSet)} with a default of
	 * <code>null</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return NSSet evaluation of the given object
	 * 
	 */
	public static NSSet setValue(Object obj) {
		return ERXValueUtilities.setValueWithDefault(obj, null);
	}

	/**
	 * Basic utility method for reading <code>NSSet</code> values which also
	 * works with serialized NSSets and comma separated items. The default value
	 * is used if the object is null.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return NSSet evaluation of the given object
	 * 
	 */
	public static NSSet setValueWithDefault(Object obj, NSSet def) {
		NSSet value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof NSSet) {
				value = (NSSet) obj;
			} else if (obj instanceof NSArray) {
				value = new NSSet((NSArray) obj);
			} else if (obj instanceof String) {
				NSArray array = arrayValueWithDefault(obj, null);
				if (array != null) {
					value = new NSSet(array);
				}
			} else {
				throw new IllegalArgumentException("Failed to parse a set from the value '" + obj + "'.");
			}
		}
		return value;
	}

	/**
	 * Basic utility method for reading NSDictionary values which works also
	 * with Strings. The current implementation uses
	 * {@link #dictionaryValueWithDefault(Object, NSDictionary)} with a default
	 * of <code>null</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return NSDictionary evaluation of the given object
	 * 
	 */
	public static NSDictionary dictionaryValue(Object obj) {
		return ERXValueUtilities.dictionaryValueWithDefault(obj, null);
	}

	/**
	 * Basic utility method for reading <code>NSDictionary</code> values which
	 * also works with serialized NSDictionarys. The default value is used if
	 * the object is null.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return NSDictionary evaluation of the given object
	 * 
	 */
	public static NSDictionary dictionaryValueWithDefault(Object obj, NSDictionary def) {
		NSDictionary value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof NSDictionary) {
				value = (NSDictionary) obj;
			} else if (obj instanceof String) {
				String strValue = ((String) obj).trim();
				if (strValue.length() > 0) {
					Object objValue = NSPropertyListSerialization.propertyListFromString((String) obj);
					if (objValue == null || !(objValue instanceof NSDictionary)) {
						throw new IllegalArgumentException("Failed to parse a dictionary from the value '" + obj + "'.");
					}
					value = (NSDictionary) objValue;
				}
			} else {
				throw new IllegalArgumentException("Failed to parse a dictionary from the value '" + obj + "'.");
			}
		}
		return value;
	}

	/**
	 * Basic utility method for reading NSData values which works also with
	 * Strings. The current implementation uses
	 * {@link #dataValueWithDefault(Object, NSData)} with a default of
	 * <code>null</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return NSData evaluation of the given object
	 * 
	 */
	public static NSData dataValue(Object obj) {
		return ERXValueUtilities.dataValueWithDefault(obj, null);
	}

	/**
	 * Basic utility method for reading <code>NSData</code> values which also
	 * works with serialized NSData. The default value is used if the object is
	 * null.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return NSData evaluation of the given object
	 * 
	 */
	public static NSData dataValueWithDefault(Object obj, NSData def) {
		NSData value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof NSData) {
				value = (NSData) obj;
			} else if (obj instanceof byte[]) {
				byte[] byteValue = (byte[]) obj;
				value = new NSData(byteValue, new NSRange(0, byteValue.length), true);
			} else if (obj instanceof String) {
				String strValue = ((String) obj).trim();
				if (strValue.length() > 0) {
					Object objValue = NSPropertyListSerialization.propertyListFromString(strValue); // MS:
																									// Encoding?
					if (objValue == null || !(objValue instanceof NSData)) {
						throw new IllegalArgumentException("Failed to parse data from the value '" + obj + "'.");
					}
					value = (NSData) objValue;
					if (value instanceof NSMutableData) {
						// AK: we need NSData if we want to use it for a PK, but
						// we get NSMutableData
						value = new NSData(value);
					}
				}
			} else {
				throw new IllegalArgumentException("Failed to parse data from the value '" + obj + "'.");
			}
		}
		return value;
	}

	/**
	 * Basic utility method for reading BigDecimal values which works also with
	 * Strings. The current implementation uses
	 * {@link #bigDecimalValueWithDefault(Object, BigDecimal)} with a default of
	 * <code>null</code>.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @return BigDecimal evaluation of the given object
	 * 
	 */
	public static BigDecimal bigDecimalValue(Object obj) {
		return ERXValueUtilities.bigDecimalValueWithDefault(obj, null);
	}

	/**
	 * Basic utility method for reading <code>BigDecimal</code> values. The
	 * default value is used if the object is null.
	 * 
	 * @param obj
	 *            object to be evaluated
	 * @param def
	 *            default value if object is null
	 * @return BigDecimal evaluation of the given object
	 * 
	 */
	public static BigDecimal bigDecimalValueWithDefault(Object obj, BigDecimal def) {
		BigDecimal value = def;
		if (!ERXValueUtilities.isNull(obj)) {
			if (obj instanceof BigDecimal) {
				value = (BigDecimal) obj;
			} else if (obj instanceof String) {
				String strValue = ((String) obj).trim();
				if (strValue.length() > 0) {
					value = new BigDecimal(strValue);
				}
			} else if (obj instanceof Integer) {
				value = new BigDecimal(((Integer) obj).intValue());
			} else if (obj instanceof Long) {
				value = new BigDecimal(((Long) obj).longValue());
			} else if (obj instanceof Float) {
				value = new BigDecimal(((Float) obj).floatValue());
			} else if (obj instanceof Double) {
				value = new BigDecimal(((Double) obj).doubleValue());
			} else if (obj instanceof Number) {
				value = new BigDecimal(((Number) obj).doubleValue());
			} else if (obj instanceof Boolean) {
				value = new BigDecimal(((Boolean) obj).booleanValue() ? 1 : 0);
			} else {
				throw new IllegalArgumentException("Failed to parse a BigDecimal from the value '" + obj + "'.");
			}
		}
		return value;
	}

	/**
	 * Returns the comparison value between int1 and int2 (using Comparator
	 * rules)
	 * 
	 * @param int1
	 *            value 1
	 * @param int2
	 *            value 2
	 * @return the Comparator comparison between the two values
	 */
	public static int compare(final int int1, final int int2) {
		return int1 > int2 ? 1 : (int1 < int2 ? -1 : 0);
	}

	/**
	 * Basic utility method for reading Enum values.
	 * 
	 * @param <T>
	 * 		Enum type evaluated
	 * @param obj
	 * 		object to evaluate
	 * @param enumType
	 * 		The desired enum class
	 * @return
	 * 		Enum evaluation of the given object or the default
	 */
	public static <T extends Enum<T>> T enumValue(Object obj, Class<T> enumType) {
		return ERXValueUtilities.enumValueWithDefault(obj, enumType, null);
	}
	
	/**
	 * Basic utility method for reading Enum values.
	 * 
	 * @param obj 
	 * 				object to be evaluated
	 * @param def 
	 * 				default value returned if object is null. If this value is null,
	 * 				the method throws a NullPointerException
	 * @param <T> 
	 * 				enum type evaluated
	 * @return Enum evaluation of the given object
	 */
	public static <T extends Enum<T>> T enumValueWithRequiredDefault(Object obj, T def) {
		return ERXValueUtilities.enumValueWithDefault(obj, (Class<T>)def.getClass(), def);
	}
	
	/**
	 * Basic utility method for reading Enum values.
	 * 
	 * @param <T>
	 * 		Enum type evaluated
	 * @param obj
	 * 		object to evaluate
	 * @param enumType
	 * 		The desired enum class
	 * @param def
	 * 		default value returned if obj is null.
	 * @return
	 * 		Enum evaluation of the given object or the default
	 */
	public static <T extends Enum<T>> T enumValueWithDefault(Object obj, Class<T> enumType, T def) {
		T result = def;
		if(!ERXValueUtilities.isNull(obj)) {
			if(obj instanceof Enum) {
				result = (T)obj;
			}
			else if (obj instanceof String) {
				result = Enum.valueOf(enumType, (String)obj);
			}
			else {
				throw new IllegalArgumentException("Failed to parse an enum from the value '" + obj + "'.");
			}
		}
		return result;
	}

	/**
	 * <span class="ja">
	 * 	複数の文字列を文字列配列として返す
	 * 
	 * 	@param anyStrings - 複数の文字列
	 * 
	 * 	@return String[] 指定された文字列が無いときは null
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */    
	public static String[] stringsToStringArray(String ... anyStrings) {
		int aryLen = anyStrings.length;
		if(aryLen <= 0) return null;
		ArrayList<String> strlist = new ArrayList<String>();
		String wkStr = null;
		for(int loop = 0; loop < aryLen; loop++){
			wkStr = anyStrings[loop];
			//System.out.println("***++++++******** anyStrings[" + loop + "] = " + wkStr);
			if((wkStr != null) && (wkStr.length() > 0))
				strlist.add(wkStr);
		}
		if(strlist.isEmpty()){
			return null;
		} 
		//return (String[]) strlist.toArray();		// Stringにキャスト出来ない時があるので
		aryLen = strlist.size();
		String[] wkStrs = new String[aryLen];
		for(int loop = 0; loop < aryLen; loop++){
			wkStrs[loop] = strlist.get(loop);
		}
		return wkStrs;
	}

	/**
	 * <span class="ja">
	 * 	複数のオブジェクトをオブジェクト配列として返す
	 * 
	 * 	@param anyObjects - 複数のオブジェクト
	 * 
	 * 	@return Object[] 指定された文字列が無いときはnull
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */    
	public static Object[] objectsToObjectArray(Object ... anyObjects) {
		int aryLen = anyObjects.length;
		if(aryLen <= 0) return null;
		ArrayList<Object> objlist = new ArrayList<Object>();
		Object wkObj = null;
		for(int loop = 0; loop < aryLen; loop++){
			wkObj = anyObjects[loop];
			if(wkObj != null)
				objlist.add(wkObj);
		}
		if(objlist.isEmpty()){
			return null;
		}
		return objlist.toArray();
	}

	/**
	 * <span class="ja">
	 * 	文字列配列を「,」で連結して返す。
	 * 
	 * 	@param sa - 文字列配列
	 * 
	 * 	@return 連結した文字配列
	 * </span>
	 */
	public static String stringArrayToString(String[] sa) {
		if((sa == null) || (sa.length <= 0)) return null;
		StringBuilder sbuff = new StringBuilder();
		int len = sa.length;
		for(int loop = 0; loop < len; loop++){
			sbuff.append(sa[loop]);
			if((loop +1) < len) sbuff.append(',');
		}
		return (sbuff.length() > 0)? sbuff.toString(): null;
	}

}
