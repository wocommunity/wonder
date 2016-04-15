/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.foundation.ERXArrayUtilities;

/**
 * General purpose constant class, useful when you want reference object that are not
 * bytes or strings in the DB like what you get with the factory classes.
 * If you use objects of this class, you might be able to completely remove the EOSharedEditingContext
 * (the google search term for "why does my app lock up").
 * <p>
 * To use the Number constants, you need to add an entry <code>ERXConstantClassName=Test.Status</code> to the attribute's userInfo 
 * in question and your EO's class description needs to be a {@link ERXEntityClassDescription}, also
 * you must enable the {@link er.extensions.jdbc.ERXJDBCAdaptor}.
 * <p>
 * The String and Byte based constants can be used with a custom class type:<pre><code>
 * 
 * ERCMailMessage.plist:
 * ...
 * {
 *     columnName = MAIL_STATE_ID;
 *     name = state;
 *     prototypeName = osType;
 *     adaptorValueConversionMethodName = value;
 *     factoryMethodArgumentType = EOFactoryMethodArgumentIsNSString;
 *     valueClassName = er.corebusinesslogic.ERCMailState;
 *     valueFactoryMethodName = mailState;
 * }
 * ...
 * 
 * public class ERCMailMessage extends EOGenericRecord {
 * ...
 *   public ERCMailState state() {
 *       return (ERCMailState) storedValueForKey("state");
 *   }
 *   
 *   public void setState(ERCMailState value) {
 *       takeStoredValueForKey(value, "state");
 *   }
 * ...
 * }
 * 
 * public class ERCMailState extends ERXConstant.StringConstant {
 *
 *     public ERCMailState(String key, String name) {
 *         super(key, name);
 *     }
 *     
 *     public static ERCMailState mailState(String key) {
 *      return (ERCMailState) constantForClassNamed(key, ERCMailState.class.getName());
 *     }
 * 
 *     public static ERCMailState EXCEPTION_STATE = new ERCMailState ("xcpt", "Exception");
 *     public static ERCMailState READY_TO_BE_SENT_STATE = new ERCMailState("rtbs", "Ready to be sent");
 *     public static ERCMailState SENT_STATE = new ERCMailState("sent", "Sent");
 *     public static ERCMailState RECEIVED_STATE = new ERCMailState("rcvd", "Received");
 *     public static ERCMailState WAIT_STATE = new ERCMailState("wait", "Wait");
 *     public static ERCMailState PROCESSING_STATE = new ERCMailState("proc", "Processing");
 * }
 * </code></pre>
 * An example would be:
 * <pre><code>
 * public class Test extends EOGenericRecord {
 * 	// your "status" attribute need a userInfo entry 
 * 	// "ERXConstantClassName" = "Test.Status";
 *  // Normally, the class name would be "Test$Status", this form is used to help you use EOGenerator
 * 	public static class Status extends ERXConstant.NumberConstant {
 * 		protected Status(int value, String name) {
 * 			super(value, name);
 * 		}
 * 	}
 * 	
 * 	public Status OFF = new Status(0, "Off");
 * 	public Status ON = new Status(1, "On");
 * 	
 *     public Test() {
 *         super();
 *     }
 * 
 *     public Status status() {
 *         return (Status)storedValueForKey("status");
 *     }
 * 
 *     public void setStatus(Constant aValue) {
 *         takeStoredValueForKey(aValue, "status");
 *     }
 *     
 *     public boolean isOn() {
 *     	return status() == ON;
 *     }
 * }
 * 
 * Test test = (Test)EOUtilities.createAndInsertInstance(ec, "Test");
 * test.setTest(Test.Status.OFF);
 * test = (Test)EOUtilities.createAndInsertInstance(ec, "Test");
 * test.setStatus(Test.Status.ON);
 * ec.saveChanges();
 * 
 * NSArray objects;
 * NSArray all = EOUtilities.objectsForEntityNamed(ec, "Test");
 * EOQualifier q;
 * 
 * objects = EOUtilities.objectsMatchingKeyAndValue(ec, "Test", "status", Test.Status.OFF);
 * log.info("Test.Status.OFF: " + objects);
 * q = new EOKeyValueQualifier("status", EOQualifier.QualifierOperatorEqual, Test.Status.OFF);
 * log.info("Test.Status.OFF: " + EOQualifier.filteredArrayWithQualifier(all, q));
 * 
 * // this might be a problem: equal number values match in the DB, but not in memory
 * objects = EOUtilities.objectsMatchingKeyAndValue(ec, "Test", "status", ERXConstant.OneInteger);
 * log.info("Number.OFF: " + objects);
 * q = new EOKeyValueQualifier("status", EOQualifier.QualifierOperatorEqual, ERXConstant.OneInteger);
 * log.info("Number.OFF: " + EOQualifier.filteredArrayWithQualifier(all, q));
 * 
 * // you can compare by equality
 * test.getStatus() == Test.Status.ON
 * </code></pre>
 * Note that upon class initialization 2500 Integers will be created and cached, from 0 - 2499.
 */
public abstract class ERXConstant {
	private static final Logger log = LoggerFactory.getLogger(ERXConstant.class);

	/**
	 * Holds the value store, grouped by class name.
	 */
	private static final Map _store = new HashMap();

	/**
	 * Retrieves all constants for the given class name ordered by value. An empty 
	 * NSArray is returned if the class isn't found.
	 * @param clazzName
	 */
	public static NSArray constantsForClassName(String clazzName) {
		synchronized (_store) {
			Map map = keyMap(clazzName, false);
			NSMutableArray array = new NSMutableArray(map.values().toArray());
			ERXArrayUtilities.sortArrayWithKey(array, "sortOrder");
			return array;
		}
	}

	public static interface Constant {
		public int sortOrder();
		public String name();
		public Object value();
	}
    
    /**
     * Retrieves the constant for the given class name and value. Null is returned
     * if either class or value isn't found.
     * @param value
     * @param clazzName
     */
    public static Constant constantForClassNamed(Object value, String clazzName) {
        synchronized (_store) {
            Map classMap = keyMap(clazzName, false);
            Constant result = (Constant) classMap.get(value);
            log.debug("Getting {} for {} and {}", result, clazzName, value);
            return result;
        }
    }

	private static int globalSortOrder = 0;
	/**
	 * Retrieves the key map for the class name.
	 * @param name
	 * @param create
	 */
	private static Map keyMap(String name, boolean create) {
		Map map = (Map) _store.get(name);
		if(map == null) {
			if(create) {
				map = new HashMap();
				_store.put(name, map);
				name = name.replace('$', '.');
				_store.put(name, map);
			} else {
				map = Collections.EMPTY_MAP;
			}
		}
		return map;
	}
	
    private static void registerConstant(Object key, Constant value, Class clazz) {
        synchronized (_store) {
            String className = clazz.getName();
            Map classMap = keyMap(className, true);
            log.debug("Putting {} for {}", key, className);
            classMap.put(key, value);
        }
    }
    
	public static class NumberConstant extends Number implements Constant {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Holds the value.
		 */
		private int _value;
		
		/**
		 * Holds the name.
		 */
		private String _name;
		
		/**
		 * Holds the name.
		 */
		private int _sortOrder;


		/**
		 * Sets the value and puts the object in the store keyed by class name and value.
		 * @param value
		 */
		protected NumberConstant(int value, String name) {
			_value = value;
			_name = name;
			_sortOrder = globalSortOrder++;
            ERXConstant.registerConstant(integerForInt(value), this, getClass());
		}
		
		/**
		 * Returns the sort order of the value.
		 */
		public int sortOrder() {
			return _sortOrder;
		}

		/**
		 * Number interface implementation, returns the value.
		 */
		@Override
		public final double doubleValue() {
			return intValue();
		}

		/**
		 * Number interface implementation, returns the value.
		 */
		@Override
		public final float floatValue() {
			return intValue();
		}

		/**
		 * Number interface implementation, returns the value.
		 */
		@Override
		public final int intValue() {
			return _value;
		}

		/**
		 * Number interface implementation, returns the value.
		 */
		@Override
		public final long longValue() {
			return intValue();
		}

		/**
		 * Returns the value.
		 */
		@Override
		public final int hashCode() {
			return _value;
		}

		
		public String name() {
			return _name;
		}
		
		public String userPresentableDescription() {
			return name() + " (" + intValue() +  ")";
		}
		
		@Override
		public String toString() {
			return getClass().getName() + ": " + userPresentableDescription();
		}

		public Number value() {
			return integerForInt(intValue());
		}
		
		/**
		 * Overridden to compare by value.
		 */
		@Override
		public final boolean equals(Object otherObject) {
			if(otherObject == null) {
				return false;
			}/* AK: we would violate the equals contract here, but we may need this with D2W later?
		if((otherObject instanceof Number) && !(otherObject instanceof ERXConstant)) {
			return ((Number)otherObject).intValue() == intValue();
		}*/
			if(otherObject.getClass() != getClass()) {
				return false;
			}
			return ((NumberConstant)otherObject).intValue() == intValue();
		}

		/**
		 * Retrieves the constant for the given class name and value. Null is returned
		 * if either class or value isn't found. Note that in case of inner classes, the
	     * name should be <code>Test.Status</code>, not <code>Test$Status</code>.
		 * @param value
		 * @param clazzName
		 */
		public static NumberConstant constantForClassNamed(int value, String clazzName) {
			return constantForClassNamed(integerForInt(value), clazzName);
		}
		
		/**
		 * Retrieves the constant for the given class name and value. Null is returned
		 * if either class or value isn't found.
		 * @param value
		 * @param clazzName
		 */
		public static NumberConstant constantForClassNamed(Number value, String clazzName) {
            return (NumberConstant) ERXConstant.constantForClassNamed(value, clazzName);
		}
		
	}
	
	/**
	 * Constant class that can be used with strings in the DB.
	 * @author ak
	 *
	 */
	public static abstract class StringConstant implements Constant {
		private String _value;
		private String _name;
		private int _sortOrder;
		
		public StringConstant(String value, String name) {
			_value = value;
			_name = name;
			_sortOrder = globalSortOrder++;
            ERXConstant.registerConstant(value, this, getClass());
		}
		
		public String name() {
			return _name;
		}
		
		public int sortOrder() {
			return _sortOrder;
		}
		
		public String value() {
			return _value;
		}
		
		public String userPresentableDescription() {
			return name() + " (" + value() +  ")";
		}
		
		@Override
		public String toString() {
			return getClass().getName() + ": " + userPresentableDescription();
		}
		
		/**
		 * Retrieves the constant for the given class name and value. Null is returned
		 * if either class or value isn't found.
		 * @param value
		 * @param clazzName
		 */
		public static StringConstant constantForClassNamed(String value, String clazzName) {
            return (StringConstant) ERXConstant.constantForClassNamed(value, clazzName);
		}
	}
	
	/**
	 * Constant class that can be used with bytes or NSData in the DB.
	 * @author ak
	 *
	 */
	public static abstract class ByteConstant implements Constant {
		private NSData _value;
		private String _name;
		private int _sortOrder;
		
		public ByteConstant(String value, String name) {
			//AK: making a lot of assumptions here... the value is a <hex data>, the result is a valid NSData
			this((NSData)NSPropertyListSerialization.propertyListFromString(value.toString()),name);
		}
		
		public ByteConstant(byte value[], String name) {
			this(new NSData(value),name);
		}
		
		public ByteConstant(NSData value, String name) {
			_value = value;
			_name = name;
			_sortOrder = globalSortOrder++;
            ERXConstant.registerConstant(value, this, getClass());
		}
		
		public String name() {
			return _name;
		}
		
		public int sortOrder() {
			return _sortOrder;
		}
		
		public NSData value() {
			return _value;
		}
		
		public String userPresentableDescription() {
			return name();
		}
		
		@Override
		public String toString() {
			return getClass().getName() + ": " + userPresentableDescription();
		}
        
        /**
         * Retrieves the constant for the given class name and value. Null is returned
         * if either class or value isn't found.
         * @param value
         * @param clazzName
         */
        public static ByteConstant constantForClassNamed(NSData value, String clazzName) {
            return (ByteConstant) ERXConstant.constantForClassNamed(value, clazzName);
        }
        
        /**
         * Retrieves the constant for the given class name and value. Null is returned
         * if either class or value isn't found.
         * @param value
         * @param clazzName
         */
        public static ByteConstant constantForClassNamed(byte value[], String clazzName) {
            return (ByteConstant) ERXConstant.constantForClassNamed(new NSData(value), clazzName);
        }
	}

    public static final int MAX_INT=2500;
    
    protected static Integer[] INTEGERS=new Integer[MAX_INT];
    static {
        for (int i=0; i<MAX_INT; i++) INTEGERS[i]=Integer.valueOf(i);
    }

    public static final Object EmptyObject = new Object();
    public static final String EmptyString = "";
    public static final NSArray EmptyArray = NSArray.EmptyArray;
    public static final NSArray SingleNullValueArray = new NSArray(NSKeyValueCoding.NullValue);
    public static final NSDictionary EmptyDictionary = NSDictionary.EmptyDictionary;
    public static final Integer MinusOneInteger = Integer.valueOf(-1);
    public static final Integer OneInteger = integerForInt(1);
    public static final Integer ZeroInteger = integerForInt(0);
    public static final Integer TwoInteger = integerForInt(2);
    public static final Integer ThreeInteger = integerForInt(3);
    public static final BigDecimal ZeroBigDecimal = new BigDecimal(0.00);
    public static final BigDecimal OneBigDecimal = new BigDecimal(1.00); 
    public static final Class[] EmptyClassArray = new Class[0];
    public static final Class[] NotificationClassArray = { com.webobjects.foundation.NSNotification.class };
    public static final Class[] ObjectClassArray = { Object.class };
    public static final Class[] StringClassArray = new Class[] { String.class };
    public static final Object[] EmptyObjectArray = new Object[] {};
    /** an empty gif image */
    public static final NSData EmptyImage = (NSData) NSPropertyListSerialization.propertyListFromString("<47494638396101000100800000ffffff00000021f90401000000002c00000000010001000002024401003b00>");
    
    /**
     * Returns an Integer for a given int
     * @return potentially cache Integer for a given int
     */
    public static Integer integerForInt(int i) {
        return (i>=0 && i<MAX_INT) ? INTEGERS[i] : Integer.valueOf(i);
    }

}
