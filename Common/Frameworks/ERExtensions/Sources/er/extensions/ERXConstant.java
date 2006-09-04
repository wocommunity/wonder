/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.math.*;
import java.util.*;

import com.webobjects.foundation.*;

/**
 * Numerical contant class, usefull when you want reference object that are not
 * bytes or strings in the DB like what you get with the factory classes.
 * <pre><code>

public abstract class Test extends ERXGenericRecord {

	public static class Status extends ERXConstant {
		private String _name;
	
		protected Status(int value, String name) {
			super(value);
			_name = name;
		}
	
		public String name() {
			return _name;
		}
	}
	
	public Status OFF = new Status(0, "Off");
	public Status ON = new Status(1, "On");
	
    public Test() {
        super();
    }

    public Status status() {
        return (Status)storedValueForKey("status");
    }

    public void setStatus(Constant aValue) {
        takeStoredValueForKey(aValue, "status");
    }
    
    public boolean isOn() {
    	return status() == ON;
    }
}
Test test = (Test)EOUtilities.createAndInsertInstance(ec, "Test");
test.setTest(Test.Status.OFF);
test = (Test)EOUtilities.createAndInsertInstance(ec, "Test");
test.setStatus(Test.Status.ON);
ec.saveChanges();
NSArray objects;
objects = EOUtilities.objectsMatchingKeyAndValue(ec, "Test", "status", Test.Status.OFF);
log.info("Test.Status.OFF: " + objects);
objects = EOUtilities.objectsMatchingKeyAndValue(ec, "Test", "status", ERXConstant.OneInteger);
log.info("Number.OFF: " + objects);
objects = EOUtilities.objectsForEntityNamed(ec, "Test");
EOQualifier q;
q = new EOKeyValueQualifier("test", EOQualifier.QualifierOperatorEqual, Test.Status.OFF);
log.info(" Test.Status.OFF: " + EOQualifier.filteredArrayWithQualifier(objects, q));
q = new EOKeyValueQualifier("test", EOQualifier.QualifierOperatorEqual, ERXConstant.OFFInteger);
log.info("Number.OFF: " + EOQualifier.filteredArrayWithQualifier(objects, q));

 * </pre></code>
 * You need to add an entry <code>ERXConstantClassName</code> to the attribute in question
 * and your EO's class description needs to be a ERXEntityClassDescription.
 * <br />
 * Note that upon class initialization
 * 2500 Integers will be created and cached, from 0 - 2499.
 */
public abstract class ERXConstant extends Number {

	private static final Map _store = new HashMap();

	public static ERXConstant constantForClassNamed(int value, String clazzName) {
		return constantForClassNamed(integerForInt(value), clazzName);
	}
	
	public static ERXConstant constantForClassNamed(Number value, String clazzName) {
		synchronized (_store) {
			Map classMap = keyMap(clazzName, false);
			return (ERXConstant) classMap.get(value);
		}
	}
	
	private static Map keyMap(String name, boolean create) {
		Map map = (Map) _store.get(name);
		if(map == null) {
			if(create) {
				map = new HashMap();
			} else {
				map = Collections.EMPTY_MAP;
			}
		}
		return map;
	}

	private int _value;
	
	protected ERXConstant(int value) {
		_value = value;
		synchronized (_store) {
			Map classMap = keyMap(getClass().getName(), true);
			Integer key = integerForInt(value);
			classMap.put(key, this);
		}
	}

	public final double doubleValue() {
		return intValue();
	}

	public final float floatValue() {
		return intValue();
	}

	public final int intValue() {
		return _value;
	}

	public final long longValue() {
		return intValue();
	}
	
	public final int hashCode() {
		return _value;
	}

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
		return ((ERXConstant)otherObject).intValue() == intValue();
	}
	
    public static final int MAX_INT=2500;
    
    protected static Integer[] INTEGERS=new Integer[MAX_INT];
    static {
        for (int i=0; i<MAX_INT; i++) INTEGERS[i]=new Integer(i);
    }

    public static final Object EmptyObject = new Object();
    public static final NSArray EmptyArray = NSArray.EmptyArray;
    public static final NSArray SingleNullValueArray = new NSArray(NSKeyValueCoding.NullValue);
    public static final NSDictionary EmptyDictionary = NSDictionary.EmptyDictionary;
    public static final Integer MinusOneInteger = new Integer(-1);
    public static final Integer OneInteger = integerForInt(1);
    public static final Integer ZeroInteger = integerForInt (0);
    public static final Integer TwoInteger = integerForInt (2);
    public static final Integer ThreeInteger = integerForInt (3);
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
        return (i>=0 && i<MAX_INT) ? INTEGERS[i] : new Integer(i);
    }

    /**
     * Returns an Integer for a given String
     * @throws NumberFormatException forwarded from the
     *		parseInt method off of Integer
     * @return potentially cache Integer for a given String
     */
    // MOVEME: ERXStringUtilities
    public static Integer integerForString(String s) throws NumberFormatException {
        return integerForInt(Integer.parseInt(s));
    }
}
