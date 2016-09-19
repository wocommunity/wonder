/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
package com.webobjects.monitor._private;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;

public class MObject implements NSKeyValueCoding {

    /********** Useful Statics **********/
    public static NSArray<Object> loadSchedulerArray = new NSArray<>(new Object[]{"Default" , "Round Robin" , "Random" , "Load Average", "Custom"});
    public static NSArray<Object> loadSchedulerArrayValues = new NSArray<>(new Object[]{"DEFAULT" , "ROUNDROBIN" , "RANDOM" , "LOADAVERAGE", "CUSTOM"});
    
    public static NSArray<Object> hostTypeArray = new NSArray<>(new Object[]{"MacOSX" , "Windows" , "Unix"});
    
    public static NSArray<Object> urlVersionArray = new NSArray<>(new Object[]{Integer.valueOf(4), Integer.valueOf(3)});
    
    protected static String[] weekNames = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    public static NSArray<String> weekArray = new NSArray<>(weekNames);

    public static String[] timesOfDay =
        new String[]{"0000", "0100", "0200", "0300", "0400", "0500", "0600", "0700", "0800", "0900", "1000", "1100",
                     "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100", "2200", "2300"};
    public static NSArray<String> timeOfDayArray = new NSArray<>(timesOfDay);

    protected static Integer[] schedulingIntervals =
        new Integer[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4),
                      Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(12) };
    public static NSArray<Integer> schedulingIntervalArray = new NSArray<>(schedulingIntervals);
    
    public static NSArray<Object> schedulingTypeArray = new NSArray<>(new Object[]{"HOURLY", "DAILY", "WEEKLY"});

    /*
                         1 Hour  =    60 minutes =   3600 seconds =   3600000 ms
              1 day  =  24 hours =  1440 minutes =  86400 seconds =  86400000 ms
     1 week = 7 days = 168 hours = 10080 minutes = 604800 seconds = 604800000 ms
     */
    public static long halfHourAsSeconds = 1800;
    

    public static String _POST = "POST";
    public static String _GET = "GET";
    public static String _HTTP1 = "HTTP/1.0";

    
    public static String[] stateArray = new String[]{"UNKNOWN", "STARTING", "ALIVE", "STOPPING", "DEAD", "CRASHING"};
    public static int UNKNOWN = 0;
    public static int STARTING = 1;
    public static int ALIVE = 2;
    public static int STOPPING = 3;
    public static int DEAD = 4;
    public static int CRASHING = 5;

    public static String _emptyString = "";
    public static Integer _zeroInteger = Integer.valueOf(0);

    public static String directActionString = "/cgi-bin/WebObjects/wotaskd.woa/wa/monitorRequest";
    public static String adminActionStringPrefix = "/cgi-bin/WebObjects/";
    public static String adminActionStringPostfix = ".woa/womp/instanceRequest";
    /**********/

    

    /********** 'values' validators **********/
    MSiteConfig _siteConfig;
    public MSiteConfig siteConfig() { return _siteConfig; }

    protected NSMutableDictionary values;
    protected _NSThreadsafeMutableDictionary adaptorValues = new _NSThreadsafeMutableDictionary(new NSMutableDictionary());

    public NSMutableDictionary values() { return values; }
    public void setValues(NSMutableDictionary newValues) {
        values = newValues;
        _siteConfig.dataHasChanged();
    }
    public void updateValues(NSDictionary aDict) {
        values = new NSMutableDictionary(aDict);
        _siteConfig.dataHasChanged();
    }

    public static Integer validatedInteger(Integer value) {
        if (value == null) {
            return value;
        }
        return Integer.valueOf(Math.abs(value.intValue()));
    }

    public static Integer validatedUrlVersion(Integer version) {
        if (version != null) {
            int intVal = version.intValue();
            if (intVal != 3 && intVal != 4) {
                return Integer.valueOf(4);
            }
        }
        return version;
    }

    public static String validatedHostType(String value) {
        if (value != null) {
            if ( (value.equals("UNIX") ) ||
                 (value.equals("WINDOWS") ) ||
                 (value.equals("MACOSX") ) ) {
                return value;
            }
        }
        return null;
    }

    public static String validatedOutputPath(String value) {
        if ( (value == null) || (value.length() == 0) ) {
            return "/dev/null";
        }
        return value;
    }

    public static Integer validatedLifebeatInterval(Integer value) {
        int intVal = 0;
        try {
            intVal = value.intValue();
        } catch (Exception e) {}

        if (intVal < 1) {
            return Integer.valueOf(30);
        }
        return value;
    }

    public static String validatedSchedulingType(String value) {
        if (value != null) {
            if ( (value.equals("HOURLY") ) ||
                 (value.equals("DAILY") ) ||
                 (value.equals("WEEKLY") ) ) {
                return value;
            }
        }
        return null;
    }

    public static Integer validatedSchedulingStartTime(Integer value) {
        if (value != null) {
            int intVal = value.intValue();
            if ( (intVal >= 0) && (intVal <= 23) ) {
                return value;
            }
        }
        return null;
    }

    // Our array is from 0-23, but the display is for '12 AM' to '11 PM'
    public static Integer morphedSchedulingStartTime(String value) {
        int i = MObject.timeOfDayArray.indexOfObject(value);
        if (i != NSArray.NotFound) {
            return Integer.valueOf(i);
        }
        return null;
    }

    public static String morphedSchedulingStartTime(Integer value) {
        if (value != null) {
            Object aString = MObject.timeOfDayArray.objectAtIndex(value.intValue());
            if (aString != null) {
                return aString.toString();
            }
        }
        return null;
    }

    public static Integer validatedSchedulingStartDay(Integer value) {
        if (value != null) {
            int intVal = value.intValue();
            if ( (intVal >= 0) && (intVal <= 6) ) {
                return value;
            }
        }
        return null;
    }

    // Java normally returns 1-7, ObjC returned 0-6, JavaFoundation will return 0-6
    // Our array is from 0-6
    public static Integer morphedSchedulingStartDay(String value) {
        int i = MObject.weekArray.indexOfObject(value);
        if (i != NSArray.NotFound) {
            return Integer.valueOf(i);
        }
        return null;
    }

    public static String morphedSchedulingStartDay(Integer value) {
        if (value != null) {
            Object aString = MObject.weekArray.objectAtIndex(value.intValue());
            if (aString != null) {
                return aString.toString();
            }
        }
        return null;
    }

    public static String validatedStats(String value) {
        if (value == null) return "0";

        int i = value.indexOf('.');
        int sLen = value.length()-1;
        if (i == -1) {
            return value;
        }
        if ( (i+3) > sLen) {
            return value;
        }
        return value.substring(0, (i+4));
    }        
    /**********/
    


    /********** NSKeyValueCoding/NSKeyValueCoding.ErrorHandling/NSKeyValueCodingAdditions methods **********/
    public static boolean canAccessFieldsDirectly() {
        return true;
    }

    public Object valueForKey(String key) {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
   }

    public void takeValueForKey(Object value, String key) {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
    }

    public Object handleQueryWithUnboundKey(String key) {
        return NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, key);
    }

    public void handleTakeValueForUnboundKey(Object value, String key) {
        NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, value, key);
    }

    public void unableToSetNullForKey(String key) {
        NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
    }

    public Object valueForKeyPath(String keyPath){
        return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
    }

    public void takeValueForKeyPath(Object value, String keyPath) {
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
    }
    /**********/
}
