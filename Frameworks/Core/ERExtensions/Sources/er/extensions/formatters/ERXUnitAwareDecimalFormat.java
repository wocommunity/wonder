//
// ERXUnitAwareDecimalFormat.java
// Project ERExtensions
//
// Created by tatsuya on Sun Oct 19 2002
//
package er.extensions.formatters;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 * <code>ERXUnitAwareDecimalFormat</code> extends {@link java.text.DecimalFormat} 
 * to add an automatic unit conversion feature for 
 * the given unit. Convenient to display friendly values 
 * for file size, elapsed time, etc.
 * 
 * <strong>Examples:</strong>
 * <pre>
 * 
 * import java.text.NumberFormat;
 * import er.extensions.ERXUnitAwareDecimalFormat
 * 
 * double smallValue = 123.0d;
 * double largeValue = 1234567890.0d;
 * NumberFormat formatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
 * formatter.setMaximumFractionDigits(2);
 * 
 * // Will display "123 bytes"
 * System.out.println(formatter.format(smallValue)); 
 * 
 * // Will display "1.15 GB"
 * System.out.println(formatter.format(largeValue));
 * 
 * </pre>
 */
public class ERXUnitAwareDecimalFormat extends DecimalFormat implements Cloneable, Serializable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** Predefined computer mass unit; supports: bytes, KB, MB, GB, TB */
    public static final String BYTE = "byte";

    /** Predefined metric length unit; supports: nm, micrometer, mm, cm, m, km */
    public static final String METER = "meter";

    /** Predefined metric weight unit; supports: mg, g, kg, ton, kiloton */
    public static final String GRAM = "gram";

    /** Predefined time unit; supports: ps, ns, microsecond, ms, sec, min, hour, day  */
    public static final String SECOND = "second";

    /** UnitPrefix is an inner class */
    public static class UnitPrefix implements NSKeyValueCoding {

        private static NSArray _bytePrefixArray;
        private static NSArray _meterPrefixArray;
        private static NSArray _gramPrefixArray;
        private static NSArray _secondPrefixArray;

        protected final String unitSymbol;
        protected final String unitName; 
        protected final double multiplyingFactor;
        
        public UnitPrefix(String unitSymbol, String unitName, double multiplyingFactor) {
            this.unitSymbol = unitSymbol;
            this.unitName = unitName;
            this.multiplyingFactor = multiplyingFactor;
        }
        
        public String unitSymbol() {
            return unitSymbol;
        }
        
        public String unitName() {
            return unitName;
        }

        public double multiplyingFactor() {
            return multiplyingFactor;
        }
        
        public double adjustScale(double number) {
            return number / multiplyingFactor;
        }
        
        public double adjustScale(long number) {
            return adjustScale((double)number);
        }

        public static NSArray unitPrefixArrayForUnit(String unitName) {
            NSArray unitPrefixArray = NSArray.EmptyArray;
            if (BYTE.equals(unitName)) {
                if (_bytePrefixArray == null) 
                    _bytePrefixArray = new NSArray(new Object[] {
                            new UnitPrefix("bytes", "byte", 1.0d), 
                            new UnitPrefix("KB", "kilobyte", 1024.0d), 
                            new UnitPrefix("MB", "megabyte", 1024.0d * 1024.0d),
                            new UnitPrefix("GB", "gigabyte", 1024.0d * 1024.0d * 1024.0d),
                            new UnitPrefix("TB", "terabyte", 1024.0d * 1024.0d * 1024.0d * 1024.0d) });
                unitPrefixArray = _bytePrefixArray;

            } else if (METER.equals(unitName)) {
                if (_meterPrefixArray == null) 
                    _meterPrefixArray = new NSArray(new Object[] {
                            new UnitPrefix("nm", "nanometer", 1.0d / 1000.0d / 1000.0d / 1000.0d),
                            new UnitPrefix("micrometer", "micrometer", 1.0d / 1000.0d / 1000.0d),
                            new UnitPrefix("mm", "millimeter", 1.0d / 1000.0d),
                            new UnitPrefix("cm", "centimeter", 1.0d / 100.0d),
                            new UnitPrefix("m", "meter", 1.0d), 
                            new UnitPrefix("km", "kilometer", 1000.0d) }); 
                unitPrefixArray = _meterPrefixArray;

            } else if (GRAM.equals(unitName)) {
                if (_gramPrefixArray == null) 
                    _gramPrefixArray = new NSArray(new Object[] {
                            new UnitPrefix("mg", "milligram", 1.0d / 1000.0d), 
                            new UnitPrefix("g", "gram", 1.0d), 
                            new UnitPrefix("kg", "kilogram", 1000.0d), 
                            new UnitPrefix("ton", "metric ton", 1000.0d * 1000.0d),
                            new UnitPrefix("kiloton", "metric kiloton", 1000.0d * 1000.0d * 1000.0d) }); 
                unitPrefixArray = _gramPrefixArray;

            } else if (SECOND.equals(unitName)) {
                if (_secondPrefixArray == null) 
                    _secondPrefixArray = new NSArray(new Object[] {
                            new UnitPrefix("ps", "picosecond", 1.0d / 1000.0d / 1000.0d / 1000.0d / 1000.0d),
                            new UnitPrefix("ns", "nanosecond", 1.0d / 1000.0d / 1000.0d / 1000.0d),
                            new UnitPrefix("microsecond", "microsecond", 1.0d / 1000.0d / 1000.0d),
                            new UnitPrefix("ms", "millisecond", 1.0d / 1000.0d),
                            new UnitPrefix("sec", "second", 1.0d), 
                            new UnitPrefix("min", "minute", 60.0d), 
                            new UnitPrefix("hour", "hour", 60.0d * 60.0d), 
                            new UnitPrefix("day", "day", 60.0d * 60.0d * 24.0d) }); 
                unitPrefixArray = _secondPrefixArray;
            }
            return unitPrefixArray;
        }

        public static UnitPrefix findAppropriatePrefix(double number, NSArray unitPrefixArray) {
            UnitPrefix unitPrefix = null;
            Enumeration e = unitPrefixArray.reverseObjectEnumerator();
            while (e.hasMoreElements()) {
                unitPrefix = (UnitPrefix)e.nextElement();
                if (number >= unitPrefix.multiplyingFactor())   break;
            }
            return unitPrefix;
        }   

        public Object valueForKey(String key) {
            return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
        }
        
        public void takeValueForKey(Object value, String key) {
            throw new NSKeyValueCoding.UnknownKeyException("Can't take the value " + value 
                        + " for the key " + key 
                        + " since " + getClass().getName() + " is immutable.", 
                        value, key);
        }

        private String _toString;
        @Override
        public String toString() {
            if (_toString == null)
                _toString = "<" + getClass().getName() + " "
                                + unitName + "(" + unitSymbol + ") [" + multiplyingFactor + "] >";
            return _toString;
        }
    }
    
    protected final NSArray unitPrefixArray;
    
    public ERXUnitAwareDecimalFormat() {
        super();
        unitPrefixArray = NSArray.EmptyArray;
    }
    
    public ERXUnitAwareDecimalFormat(String unitName) {
        super();
        unitPrefixArray = UnitPrefix.unitPrefixArrayForUnit(unitName);
    }
    
    public ERXUnitAwareDecimalFormat(NSArray unitPrefixArray) {
        super();
        this.unitPrefixArray = unitPrefixArray;
    }

    public ERXUnitAwareDecimalFormat(String pattern, DecimalFormatSymbols symbols) {
        super(pattern, symbols);
        unitPrefixArray = NSArray.EmptyArray;
    }

    public ERXUnitAwareDecimalFormat(String pattern, DecimalFormatSymbols symbols, String unitName) {
        super(pattern, symbols);
        unitPrefixArray = UnitPrefix.unitPrefixArrayForUnit(unitName);
    }

    public ERXUnitAwareDecimalFormat(String pattern, DecimalFormatSymbols symbols, NSArray unitPrefixArray) {
        super(pattern, symbols);
        this.unitPrefixArray = unitPrefixArray;
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        StringBuffer result = toAppendTo;
        UnitPrefix unitPrefix = UnitPrefix.findAppropriatePrefix(number, unitPrefixArray);
        if (unitPrefix == null) {
            result = super.format(number, toAppendTo, fieldPosition);
        } else {
            double convertedNumber = unitPrefix.adjustScale(number);
            result = super.format(convertedNumber, toAppendTo, fieldPosition);
            // ENHANCEME: Would be nice to be able to specify the place for  
            //            the unit symbol via the format string. 
            result.append(' ').append(unitPrefix.unitSymbol());
        }
        return result; 
    }
    
    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return format((double)number, toAppendTo, fieldPosition);
    }

}
