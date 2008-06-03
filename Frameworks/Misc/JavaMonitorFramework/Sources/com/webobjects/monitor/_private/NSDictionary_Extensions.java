/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (“Apple”) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple’s copyrights in this original Apple software (the “Apple Software”), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;


public class NSDictionary_Extensions {

    private static NSMutableDictionary replaceSpaceWithUnderscoreCache = new NSMutableDictionary();
    private static NSMutableDictionary replaceUnderscoreWithSpaceCache = new NSMutableDictionary();

    public static NSMutableDictionary replaceSpaceWithUnderscore(NSDictionary aDict) {
        // This will recursively iterate through the dictionary; anywhere a key has spaces, it will replace them with underscores
        if (aDict == null) return null;

        NSMutableDictionary newDict = new NSMutableDictionary(aDict.count());
        String key = null;
        Object value = null;
        String newKey = null;

        Enumeration allKeys = aDict.keyEnumerator();
        while (allKeys.hasMoreElements()) {
            key = (String)allKeys.nextElement();
            value = aDict.valueForKey(key);
            if (value instanceof NSDictionary) {
                value = replaceSpaceWithUnderscore((NSDictionary)value);
            }
            if (value instanceof NSArray) {
                value = replaceSpaceWithUnderscore((NSArray)value);
            }
            newKey = (String)replaceSpaceWithUnderscoreCache.valueForKey(key);
            if (newKey == null) {
                newKey = key.replace(' ', '_');
                replaceSpaceWithUnderscoreCache.takeValueForKey(newKey, key);
            }
            newDict.takeValueForKey(value, newKey);
        }
        return newDict;
    }

    public static NSMutableDictionary replaceUnderscoreWithSpace(NSDictionary aDict) {
        // This will recursively iterate through the dictionary; anywhere a key has underscores, it will replace them with spaces
        if (aDict == null) return null;

        NSMutableDictionary newDict = new NSMutableDictionary(aDict.count());
        String key = null;
        Object value = null;
        String newKey = null;

        Enumeration allKeys = aDict.keyEnumerator();
        while (allKeys.hasMoreElements()) {
            key = (String)allKeys.nextElement();
            value = aDict.valueForKey(key);
            if (value instanceof NSDictionary) {
                value = replaceUnderscoreWithSpace((NSDictionary)value);
            }
            if (value instanceof NSArray) {
                value = replaceUnderscoreWithSpace((NSArray)value);
            }
            newKey = (String)replaceUnderscoreWithSpaceCache.valueForKey(key);
            if (newKey == null) {
                newKey = key.replace('_', ' ');
                replaceUnderscoreWithSpaceCache.takeValueForKey(newKey, key);
            }
            newDict.takeValueForKey(value, newKey);
        }
        return newDict;
    }


    public static NSMutableArray replaceSpaceWithUnderscore(NSArray anArray) {
        // This will recursively iterate through the array, looking for NSDictionaries, and calling replaceSpaceWithUnderscore on them.
        if (anArray == null) return null;

        NSMutableArray newArray = new NSMutableArray(anArray.count());

        Enumeration allObjects = anArray.objectEnumerator();
        while (allObjects.hasMoreElements()) {
            Object value = allObjects.nextElement();
            if (value instanceof NSDictionary) {
                value = replaceSpaceWithUnderscore((NSDictionary)value);
            }
            if (value instanceof NSArray) {
                value = replaceSpaceWithUnderscore((NSArray)value);
            }
            newArray.addObject(value);
        }
        return newArray;
    }

    public static NSMutableArray replaceUnderscoreWithSpace(NSArray anArray) {
        // This will recursively iterate through the array, looking for NSDictionaries, and calling replaceUnderscoreWithSpace on them.
        if (anArray == null) return null;

        NSMutableArray newArray = new NSMutableArray(anArray.count());

        Enumeration allObjects = anArray.objectEnumerator();
        while (allObjects.hasMoreElements()) {
            Object value = allObjects.nextElement();
            if (value instanceof NSDictionary) {
                value = replaceUnderscoreWithSpace((NSDictionary)value);
            }
            if (value instanceof NSArray) {
                value = replaceUnderscoreWithSpace((NSArray)value);
            }
            newArray.addObject(value);
        }
        return newArray;
    }

    public static NSDictionary dictionaryForArchive(NSDictionary tempDict) {
        NSMutableDictionary newDict = new NSMutableDictionary(tempDict.count());

        Enumeration allKeys = tempDict.keyEnumerator();
        while (allKeys.hasMoreElements()) {
            String key = (String)allKeys.nextElement();
            Object value = tempDict.valueForKey(key);
            if ((value instanceof String) && (((String)value).equals("true"))) {
                newDict.takeValueForKey("YES", key);
            } else if ((value instanceof String) && (((String)value).equals("false"))) {
                newDict.takeValueForKey("NO", key);
            } else {
                newDict.takeValueForKey(value, key);
            }
        }
        return newDict;
    }

    public static NSDictionary dictionaryFromArchive(NSDictionary tempDict) {
        NSMutableDictionary newDict = new NSMutableDictionary(tempDict.count());

        Enumeration allKeys = tempDict.keyEnumerator();
        while (allKeys.hasMoreElements()) {
            String key = (String)allKeys.nextElement();
            Object value = tempDict.valueForKey(key);
            if ((value instanceof String) && (((String)value).equals("YES"))) {
                newDict.takeValueForKey("true", key);
            } else if ((value instanceof String) && (((String)value).equals("NO"))) {
                newDict.takeValueForKey("false", key);
            } else {
                newDict.takeValueForKey(value, key);
            }
        }
        return newDict;
    }


    public static void verbosePrint(NSDictionary aDict) {
        Enumeration allKeys = aDict.keyEnumerator();
        while (allKeys.hasMoreElements()) {
            String key = (String)allKeys.nextElement();
            Object value = aDict.valueForKey(key);
            NSLog.debug.appendln("\tkey = " + key + "\t\tvalue = " + value + " (" + value.getClass().getName() + ")");
        }
    }
}
