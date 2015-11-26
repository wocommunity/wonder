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
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

public class StatsUtilities {
    public static NSTimestampFormatter dateFormatter = new NSTimestampFormatter("%Y:%m:%d:%H:%M:%S %Z");

    static public Integer totalTransactionsForApplication(MApplication anApp) {
        int aTotalTrans = 0;
        NSArray anInstArray = anApp.instanceArray();
        int i;
        int anInstArrayCount = anInstArray.count();

        for (i = 0; i < anInstArrayCount; i++) {
            MInstance anInstance = (MInstance)anInstArray.objectAtIndex(i);
            NSDictionary aStatsDict = anInstance.statistics();

            if (aStatsDict != null) {
                try {
                    String aValue = (String) aStatsDict.valueForKey("transactions");
                    aTotalTrans = aTotalTrans + Integer.parseInt(aValue);
                } catch (Throwable ex) {
                    // do nothing
                }
            }
        }
        return Integer.valueOf(aTotalTrans);
    }
    
    
    /**
     * Returns the total number of transactions for running instances of the given monitored application
     * If the monitored application has no running instances, returns Integer.valueOf(0)
     * 
     * @param monitoredApplication
     * @return
     */
    public static Integer totalTransactionsForActiveInstancesOfApplication(MApplication monitoredApplication) {
    	Integer totalTransactions = sumStatisticOfActiveInstances(monitoredApplication, "transactions");
        return totalTransactions;
    }


    static public Integer totalActiveSessionsForApplication(MApplication anApp) {
        NSArray anInstArray = anApp.instanceArray();
        int aTotalActiveSessions = 0;
        int i;
        int anInstArrayCount = anInstArray.count();

        for (i = 0; i < anInstArrayCount; i++) {
            MInstance anInstance = (MInstance)anInstArray.objectAtIndex(i);
            NSDictionary aStatsDict = anInstance.statistics();

            if (aStatsDict != null) {
                try {
                    String aValue = (String) aStatsDict.valueForKey("activeSessions");
                    aTotalActiveSessions = aTotalActiveSessions + Integer.parseInt(aValue);
                } catch (Throwable ex) {
                    // do nothing
                }
            }
        }
        return Integer.valueOf(aTotalActiveSessions);
    }

    
    /**
     * Returns the total number of active sessions for running instances of the given monitored application
     * If the monitored application has no running instances, returns Integer.valueOf(0)
     * 
     * @param monitoredApplication
     * @return
     */
    public static Integer totalActiveSessionsForActiveInstancesOfApplication(MApplication monitoredApplication) {
    	Integer totalActiveSessions = sumStatisticOfActiveInstances(monitoredApplication, "activeSessions");
        return totalActiveSessions;
    }


    /**
     * Calculates and returns the sum of the statistic indicated by the given statisticsKey for 
     * the running instances of the given monitored application.
     * 
     * @param monitoredApplication
     * @param statisticsKey
     * @return
     */
    protected static Integer sumStatisticOfActiveInstances(MApplication monitoredApplication, String statisticsKey){
        int sum = 0;
        NSArray<MInstance> instances = monitoredApplication.instanceArray();
        for (MInstance anInstance : instances) {
        	if (anInstance.isRunning_M()){
	            NSDictionary statistics = anInstance.statistics();
	            if (statistics != null) {
	                try {
	                    String aValue = (String) statistics.valueForKey(statisticsKey);
	                    sum = sum + Integer.parseInt(aValue);
	                } catch (Throwable ex) {
	                    // do nothing
	                }
	            }	
        	}
		}
        Integer sumAsInteger = Integer.valueOf(sum);
        return sumAsInteger;
    }
    
    

    static public Float totalAverageTransactionForApplication(MApplication anApp) {
        NSArray anInstArray = anApp.instanceArray();
        float aTotalTime = (float)0.0;
        int aTotalTrans = 0;
        float aTotalAvg = (float)0.0;
        int i;
        int anInstArrayCount = anInstArray.count();

        for (i = 0; i < anInstArrayCount; i++) {
            MInstance anInstance = (MInstance)anInstArray.objectAtIndex(i);
            NSDictionary aStatsDict = anInstance.statistics();

            if (aStatsDict != null) {
                try {
                    String aValue = (String)aStatsDict.valueForKey("transactions");
                    Integer aTrans = Integer.valueOf(aValue);

                    if (aTrans.intValue() > 0) {
                        aValue = (String)aStatsDict.valueForKey("avgTransactionTime");
                        Float aTime = Float.valueOf(aValue);
                        aTotalTime = aTotalTime + (aTrans.intValue() * aTime.floatValue());
                        aTotalTrans = aTotalTrans + (aTrans.intValue());
                    }
                } catch (Throwable ex) {
                    // do nothing
                }
            }
        }

        if (aTotalTrans > 0) {
            aTotalAvg = aTotalTime / aTotalTrans;
        }

        return Float.valueOf(aTotalAvg);
    }


    static public Float totalAverageIdleTimeForApplication(MApplication anApp) {
        NSArray anInstArray = anApp.instanceArray();
        float aTotalTime = (float)0.0;
        int aTotalTrans = 0;
        float aTotalAvg = (float)0.0;
        int i;
        int instArrayCount = anInstArray.count();

        for (i = 0; i < instArrayCount; i++) {
            MInstance anInstance = (MInstance)anInstArray.objectAtIndex(i);
            NSDictionary aStatsDict = anInstance.statistics();

            if (aStatsDict != null) {
                try {
                    String aValue = (String)aStatsDict.valueForKey("transactions");
                    Integer aTrans = Integer.valueOf(aValue);

                     if (aTrans.intValue() > 0) {
                         String idleString = (String)aStatsDict.valueForKey("averageIdlePeriod");
                         Float aTime = Float.valueOf(idleString);
                         aTotalTime = aTotalTime + (aTrans.intValue() * aTime.floatValue());
                         aTotalTrans = aTotalTrans + (aTrans.intValue());
                     }
                } catch (Throwable ex) {
                    // do nothing
                }
            }
        }

        if (aTotalTrans > 0) {
            aTotalAvg = aTotalTime / aTotalTrans;
        }

        return Float.valueOf(aTotalAvg);
    }

    static public Float actualTransactionsPerSecondForApplication(MApplication anApp) {
        float anOverallRate = (float)0.0;
        NSArray anInstArray = anApp.instanceArray();
        int i;
        int anInstArrayCount = anInstArray.count();

        for (i = 0; i < anInstArrayCount; i++) {
            MInstance anInstance = (MInstance)anInstArray.objectAtIndex(i);
            NSDictionary aStatsDict = anInstance.statistics();
            String aStartDate = "";
            float anInstRate = (float)0.0;
            Integer aTrans;

            if (aStatsDict != null) {
                aStartDate = (String)aStatsDict.valueForKey("startedAt");
                try {
                    aTrans = Integer.valueOf((String) aStatsDict.valueForKey("transactions"));
                } catch (Throwable ex) {
                    aTrans = null;
                }
                if (aTrans != null && (aTrans.intValue() > 0)) {
                    NSTimestamp aDate;
                    float aRunningTime;

                    try {
                        // Important! This relies on the fact that the stats will deliver startdate based on GMT, since new NSTimestamp is also base on GMT!
                        aDate = (NSTimestamp)StatsUtilities.dateFormatter.parseObject(aStartDate);
                        aRunningTime = (aDate.getTime() - System.currentTimeMillis()) / 1000;
                    } catch (java.text.ParseException ex) {
                        aRunningTime = (float) 0.0;
                        NSLog.err.appendln("Format error in StatsUtilities: " + aStartDate);
                        NSLog.err.appendln(ex.getErrorOffset());
                        NSLog.err.appendln("Actual Transactions Per Second rate is inaccurate.");
                    }
                    if (aRunningTime > 0.0) {
                        anInstRate = (aTrans.floatValue()) / aRunningTime;
                    }
                }
            }
            anOverallRate = anOverallRate + anInstRate;
        }
        return Float.valueOf(anOverallRate);
    }
}
