//
// ERXMutableInteger.java
// Project ERExtensions
//
// Created by tatsuya on Mon Jul 23 2002
// 
// Original Author:
//      Ian F. Darwin   (Author of "Java Coolbook" ISBN: 0-596-00170-3)
//
// Original Javadoc from "com.darwinsys.util.MutableInteger.java"
// 	A MutableInteger is like an Integer but mutable, to avoid the
//  	excess object creation involved in 
//  	c = new Integer(c.getInt()+1)
//  	which can get expensive if done a lot.
//  	Not subclassed from Integer, since Integer is final (for performance :-))

package er.extensions.foundation;

/** 
 * <code>ERXMutableInteger</code> is like {@link java.lang.Integer Integer}
 * but mutable, to avoid the excess object creation involved in 
 * <code>i = new Integer(i.getInt() + 1)</code>
 * which can get expensive if done a lot.
 * <p>
 * Not subclassed from <code>Integer</code>, since <code>Integer</code> 
 * is final (for performance.) 
 * <p>
 * Original Author: Ian F. Darwin   (Author of "Java Coolbook" <code>ISBN: 0-596-00170-3</code>)  
 */
public class ERXMutableInteger {
    private int _value = 0;

    /**
     * Constructs a newly allocated <code>ERXMutableInteger</code> object 
     * that represents the primitive int argument.
     *
     * @param value   the int value to be represented by the 
     *               <code>ERXMutalbleInteger</code> object
     */
    public ERXMutableInteger(int value) { 
        _value = value; 
    }

    /**
     * Increments the int value of this <code>ERXMutalbleInteger</code> object by 1. 
     */
    public void increment() {
        _value++;
    }

    /**
     * Increments the int value of this <code>ERXMutalbleInteger</code> object by 
     * the int argument.
     * 
     * @param amount    the int amount to increment
     */
    public void increment(int amount) {
        _value += amount;
    }

    /**
     * Decrements the int value of this <code>ERXMutalbleInteger</code> object by 1. 
     */
    public void decrement() {
        _value--;
    }

    /**
     * Decrements the int value of this <code>ERXMutalbleInteger</code> object by 
     * the int argument.
     * 
     * @param amount    the int amount to decrement
     */
    public void decrement(int amount) {
        _value -= amount;
    }
    
    /**
     * Updates the int value of this <code>ERXMutalbleInteger</code> object to 
     * the int argument. 
     * 
     * @param value    the int value to set
     */ 
    public void setIntValue(int value) {
        _value = value;
    }

    /**
     * Returns the int value represented by this <code>ERXMutalbleInteger</code> object.
     *
     * @return   the int value of this object
     */
    public int intValue() {
        return _value;
    }

    /**
     * Returns a string object representing this <code>ERXMutalbeInteger</code>'s 
     * value. The value is converted to signed decimal representation and 
     * returned as a string, exactly as if the integer value were given 
     * as an argument to the {@link #toString(int)} method.
     *
     * @return   a string representation of the value of this object.
     */
    @Override
    public String toString() {
        return Integer.toString(_value);
    }

    /** 
     * Creates a string representation of the int argument.
     *
     * @param value    	the int value to convert
     * @return   	a string representation of the int argument
     */ 
    public static String toString(int value) {
        return Integer.toString(value);
    }

    /**
     * Parses the string argument as a signed decimal integer. 
     * The characters in the string must all be decimal digits, 
     * except that the first character may be an ASCII minus sign '-' 
     * to indicate a negative value. The resulting integer value 
     * is returned, exactly as if the argument was given as 
     * arguments to the {@link java.lang.Integer#parseInt(java.lang.String, int)
     * Integer.parseInt} method.
     *
     * @param str	the string to parse
     * @return 		the int represented by the argument in decimal
     * @throws NumberFormatException 	if the String cannot be parsed as an int.
     */ 
    public static int parseInt(String str) throws NumberFormatException {
        return Integer.parseInt(str);
    }
}
