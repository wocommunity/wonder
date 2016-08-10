/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

public class _NSObjectUtilities  {
    private _NSObjectUtilities() {}
    
    /**
     * These methods will eventually do an exhaustive dump of the Object in question using Reflection APIs.
     * This is very slow, and outputs fully qualified _everything_.
     * These are considered private API, even though they are publicly visible.
     * Also note that this may alter the state of the object to be dumped - it is _not_ guaranteed to be free of side-effects.
     * @param o object to dump
     * @param showInterfaces - will show implemented interfaces
     * @param showConstructors - will show available constructors
     * @param showFields - will show ivars
     * @param getFields - will attempt to get ivar values (only if showFields)
     * @param showMethods - will show method signatures
     * @param getMethodsJava - will attempt to invoke all methods starting with "get" and taking no arguments (only if showMethods)
     * @param getMethodsNext - will attempt to invoke all methods with the same name as an ivar (ignoring starting '_') and taking no arguments (only if showMethods)
     * @return serialized object
     * @throws IOException if something goes wrong
     */
    public static String dumpObjectAsString(Object o,
                                    boolean showInterfaces, boolean showConstructors,
                                    boolean showFields, boolean getFields,
                                    boolean showMethods, boolean getMethodsJava, boolean getMethodsNext) throws IOException {
        StringWriter outputWriter = new StringWriter();
        String CR = Character.valueOf('\n').toString();
        outputWriter.write(CR);
        outputWriter.write("This object's hashCode is = " + o.hashCode() + CR);

        Class us = o.getClass();
        String ourName = us.getName();
        outputWriter.write("This object is of Class = ");
        boolean areWeAnArray = us.isArray();
        if (areWeAnArray) {
            Class ourComponentType = us.getComponentType();	// null if not an array
            outputWriter.write("Array with Component Type = " + ourComponentType.getName());
            int ourLength = -1;
            try {
                ourLength = Array.getLength(o);
            } catch (IllegalArgumentException e) {}
            outputWriter.write(" and size " + ourLength + CR);
            // Walk through the array, writing out the state of all the elements in the array?
        } else {
            outputWriter.write(ourName + CR);
        }

        Class ourSuper = us.getSuperclass();	// null if none or if interface
        String ourSuperName = null;
        if (ourSuper != null) {
            ourSuperName = ourSuper.getName();
            outputWriter.write("The Superclass is of Class = " + ourSuperName + CR);
        } else {
            outputWriter.write("This object has no Superclass" + CR);
        }

        if (showInterfaces) {
            Class[] ourInterfaces = us.getInterfaces();	// empty array if none
            if (ourInterfaces.length == 0) {
                outputWriter.write("This object implements no interfaces" + CR);
            } else {
                outputWriter.write("Interfaces: " + CR);
                for (int i=0; i<ourInterfaces.length; i++) {
                    outputWriter.write("\t" + ourInterfaces[i].getName() + CR);
                }
            }
        }
        if (showConstructors) {
            Constructor[] ourConstructors = {};
            Constructor[] ourDeclaredConstructors = {};
            try {
                    ourConstructors = us.getConstructors();	// returns empty array if none
            } catch (SecurityException e) {}
            try {
                    ourDeclaredConstructors = us.getDeclaredConstructors();	// returns empty array if none
            } catch (SecurityException e) {}

            NSMutableSet<Object> mergeConstructors = new NSMutableSet<Object>(ourConstructors);
            mergeConstructors.unionSet(new NSSet<Object>(ourDeclaredConstructors));
            Object[] allOurConstructors = mergeConstructors._allObjects();

            if (allOurConstructors.length != 0) {
                outputWriter.write("Constructors: " + CR);
                for (int i = 0; i< allOurConstructors.length; i++) {
                    outputWriter.write("\t" + allOurConstructors[i] + CR);
                }
            } else {
                outputWriter.write("No Visible Constructors" + CR);
            }
        }
        if (showFields) {
            Field[] ourFields = {};
            Field[] ourDeclaredFields = {};
            try {
                    ourFields = us.getFields();	// returns empty array if none
            } catch (SecurityException e) {}
            try {
                    ourDeclaredFields = us.getDeclaredFields();	// returns empty array if none
            } catch (SecurityException e) {}

            NSMutableSet<Object> mergeFields = new NSMutableSet <Object>(ourFields);
            mergeFields.unionSet(new NSSet <Object> (ourDeclaredFields));
            Object[] allOurFields = mergeFields._allObjects();

            if (allOurFields.length != 0) {
                outputWriter.write("Fields: " + CR);
                for (int i = 0; i< allOurFields.length; i++) {
                    outputWriter.write("\t" + allOurFields[i]);
                    if (getFields) {
                        try {
                            Object got =  ((Field)allOurFields[i]).get(o);
                            outputWriter.write(" = " + got + CR);
                        } catch (Exception f1) {
                            outputWriter.write(CR);
                        }
                    } else {
                        outputWriter.write(CR);
                    }
                }
            } else {
                outputWriter.write("No Visible Fields" + CR);
            }
        }
        if (showMethods) {
            Method[] ourMethods = {};
            Method[] ourDeclaredMethods = {};
            try {
                    ourMethods = us.getMethods();	// returns empty array if none
            } catch (SecurityException e) {}
            try {
                    ourDeclaredMethods = us.getDeclaredMethods();	// returns empty array if none
            } catch (SecurityException e) {}

            NSMutableSet<Object> mergeMethods = new NSMutableSet<Object>(ourMethods);
            mergeMethods.unionSet(new NSSet <Object>(ourDeclaredMethods));
            Object[] allOurMethods = mergeMethods._allObjects();

            if (allOurMethods.length != 0) {
                boolean alreadyInvoked = false;
                outputWriter.write("Methods: " + CR);
                for (int i = 0; i< allOurMethods.length; i++) {
                    alreadyInvoked = false;
                    Method aMethod = (Method)allOurMethods[i];
                    outputWriter.write("\t" + aMethod);
                    if ( (getMethodsJava) && (aMethod.getName().startsWith("get")) ) {
                        try {
                            Object returnVal = aMethod.invoke(o);
                            outputWriter.write(" = " + returnVal + CR);
                            alreadyInvoked = true;
                        } catch (Exception m1) {
                            outputWriter.write(CR);
                            alreadyInvoked = true;
                        }
                    }
                    if (getMethodsNext && !alreadyInvoked) {
                        String methodName = aMethod.getName();
                        try {
                            us.getField(methodName);
                        } catch (Exception e1) {
                            try {
                                us.getDeclaredField(methodName);
                            } catch (Exception e2) {
                                try {
                                    us.getField("_" + methodName);
                                } catch (Exception e3) {
                                    try {
                                        us.getDeclaredField("_" + methodName);
                                    } catch (Exception e4) {
                                        // Didn't find anything
                                        outputWriter.write(CR);
                                        alreadyInvoked = true;
                                    }
                                }
                            }
                        }
                        if (!alreadyInvoked) {
                            // we match (sortof) an ivar
                            try {
                                Object returnVal2 = aMethod.invoke(o);
                                outputWriter.write(" = " + returnVal2 + CR);
                                alreadyInvoked = true;
                            } catch (Exception m2) {
                                outputWriter.write(CR);
                                alreadyInvoked = true;
                            }
                        }
                    }
                    if (!alreadyInvoked) {
                        outputWriter.write(CR);
                    }
                }
            } else {
                outputWriter.write("No Visible Methods" + CR);
            }
            outputWriter.write(CR);
        }
        String retVal = outputWriter.toString();
        outputWriter.close();
        outputWriter = null;
        return retVal;
    }

}
