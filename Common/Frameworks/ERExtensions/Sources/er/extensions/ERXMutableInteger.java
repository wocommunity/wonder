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

package er.extensions;

/** 
 * ERXMutableInteger is like Integer but mutable, to avoid the
 * excess object creation involved in 
 * <code>i = new Integer(i.getInt() + 1)</code>
 * which can get expensive if done a lot.<br>
 * Not subclassed from Integer, since Integer is final (for performance) <br><br>
 *
 * Original Author: Ian F. Darwin   (Author of "Java Coolbook" ISBN: 0-596-00170-3)  
 */
public class ERXMutableInteger {
    private int _value = 0;

    public ERXMutableInteger(int value) { _value = value; }

    public void increment() {
        _value++;
    }

    public void increment(int amount) {
        _value += amount;
    }

    public void decrement() {
        _value--;
    }

    public void decrement(int amount) {
        _value -= amount;
    }
    
    public void setIntValue(int value) {
        _value = value;
    }

    public int intValue() {
        return _value;
    }

    public String toString() {
        return Integer.toString(_value);
    }

    public static String toString(int value) {
        return Integer.toString(value);
    }

    public static int parseInt(String str) {
        return Integer.parseInt(str);
    }
}
