/*
 * WOStatsPage.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (ÒAppleÓ) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under AppleÕs copyrights in this original Apple 
 * software (the ÒApple SoftwareÓ), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import java.util.*;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import java.net.UnknownHostException;

public class WOStatsPage extends WOComponent {
    public NSDictionary detailsDict;
    public NSDictionary pagesDict;
    public NSDictionary directActionsDict;
    public NSDictionary sessionMemoryDict;
    public NSDictionary transactions;
    public NSDictionary statsDict;
    public NSDictionary memoryDict;
    public NSArray sessionStats;
    public NSMutableDictionary sessionsDict;
    public long maxPageCount;
    public long maxActionCount;
    public NSTimestamp maxSessionsDate;
    public String userName;
    public Object currentKey;
    public Object currentItem;
    public String password;

    public WOStatsPage(WOContext aContext)  {
        super(aContext);
    }
    
    public WOComponent submit()  {
        session().validateStatisticsLogin(password, userName);
        return this;
    }

    public String host() throws UnknownHostException {
        if (WOApplication.application().host() != null) {
            return WOApplication.application().host();
        } 
        return java.net.InetAddress.getLocalHost().getHostName();
    }

    public String instance()  {
        int instance = context().request().applicationNumber();
        return ""+instance;
    }

    protected long _maxServedForDictionary(NSDictionary aDictionary) {
        long aMaxServedCount = 0;
        NSDictionary aPage = null;
        Enumeration aPageEnumerator = aDictionary.objectEnumerator();
        while (aPageEnumerator.hasMoreElements()) {
            aPage = (NSDictionary)aPageEnumerator.nextElement();
            long newCount = ((Long)aPage.objectForKey("Served")).longValue();
            aMaxServedCount += newCount;
        }
        return aMaxServedCount;
    }

    public void _initIvars() {
        statsDict = WOApplication.application().statistics();
        pagesDict = (NSDictionary)statsDict.objectForKey("Pages");
        directActionsDict = (NSDictionary)statsDict.objectForKey("DirectActions");
        detailsDict = (NSDictionary)statsDict.objectForKey("Details");
        transactions = (NSDictionary)statsDict.objectForKey("Transactions");
        memoryDict = (NSDictionary)statsDict.objectForKey("Memory");
        sessionsDict = ((NSDictionary)statsDict.objectForKey("Sessions")).mutableClone();
        sessionMemoryDict = (NSDictionary)sessionsDict.removeObjectForKey("Avg. Memory Per Session");
        sessionStats = (NSArray)sessionsDict.removeObjectForKey("Last Session's Statistics");
        maxSessionsDate = (NSTimestamp) sessionsDict.removeObjectForKey("Peak Active Sessions Date");


        maxPageCount = 0;
        maxActionCount = 0;
        long currentCount;
        int i;

        maxPageCount = _maxServedForDictionary(pagesDict);
        maxActionCount = _maxServedForDictionary(directActionsDict);
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        // ** This should probably be somewhere else.
        _initIvars();
        super.appendToResponse(aResponse, aContext);
    }

    public long pageCount() {
        return ((Long)((NSDictionary)currentItem).objectForKey("Served")).longValue();
    }

    public double pageAvg() {
        return ((Double)((NSDictionary)currentItem).objectForKey("Avg Resp. Time")).doubleValue();
    }

    public double pageMin() {
        return ((Double)((NSDictionary)currentItem).objectForKey("Min Resp. Time")).doubleValue();
    }

    public double pageMax() {
        return ((Double)((NSDictionary)currentItem).objectForKey("Max Resp. Time")).doubleValue();
    }

    public long detailCount() {
        return ((Long)detailsDict.objectForKey(currentKey)).longValue();
    }

    public void setDetailPercent(String aValue) {
    }

    public long detailPercent() {
        double aTransactionsCount = ((Number)transactions.objectForKey("Transactions") ).doubleValue();
        double aDetailCount = (double)detailCount();
        if (aTransactionsCount > 0L) {
            return (long)((aDetailCount / aTransactionsCount) * 100);
        } else {
            return 0;
        }
    }

    public Long componentActionTransactions() {
        return (Long)transactions.objectForKey("Component Action Transactions");
    }

    public Long directActionTransactions() {
        return (Long)transactions.objectForKey("Direct Action Transactions");
    }

    public Double avgComponentActionTransactions() {
        return (Double)transactions.objectForKey("Component Action  Avg. Transaction Time");
    }

    public Double avgDirectActionTransactions() {
        return (Double)transactions.objectForKey("Direct Action Avg. Transaction Time");
    }

    public Double avgTransactionTime() {
        return (Double)transactions.objectForKey("Avg. Transaction Time");
    }

    public Double avgIdleTime() {
        return (Double)transactions.objectForKey("Avg. Idle Time");
    }

    public Double movingAvgTransactionTime() {
        return (Double)transactions.objectForKey("Moving Avg. Transaction Time");
    }

    public Double movingAvgIdleTime() {
        return (Double)transactions.objectForKey("Moving Avg. Idle Time");
    }

    public Long movingAvgSampleSize() {
        return (Long)transactions.objectForKey("Sample Size For Moving Avg.");
    }

    public String runningTime() {
        long aRunningTime = System.currentTimeMillis()-((NSTimestamp)statsDict.objectForKey("StartedAt")).getTime();
        String aRunningTimeString = WOStatsPage._timeIntervalDescription(aRunningTime);
        return aRunningTimeString;
    }

    public boolean isLogPath() {
        if (statsDict.objectForKey("LogFile")!=null) {
            return true;
        }	
        return false;
    }

    public boolean isLastUser() {
        if (sessionStats!=null && (sessionStats.count() != 0)) {
            return true;
        }
        return false;
    }

    public long actionCount() {
        return ((Long)((NSDictionary)currentItem).objectForKey("Served")).longValue();
    }

    public double actionAvg() {
        return ((Double)((NSDictionary)currentItem).objectForKey("Avg Resp. Time")).doubleValue();
    }

    public double actionMin() {
        return ((Double)((NSDictionary)currentItem).objectForKey("Min Resp. Time")).doubleValue();
    }

    public double actionMax() {
        return ((Double)((NSDictionary)currentItem).objectForKey("Max Resp. Time")).doubleValue();
    }

    protected static String _timeIntervalDescription(long aTimeInterval) {
        String aTimeIntervalString;
        long timeInterval = aTimeInterval / 1000;
        long days;
        long hours;
        long minutes;
        long seconds;

        days = (timeInterval / (60*60*24));
        timeInterval = timeInterval - (days * (60*60*24));

        hours = (timeInterval / (60*60));
        timeInterval = timeInterval - (hours * (60*60));

        minutes = (timeInterval / (60));
        timeInterval = timeInterval - (minutes * (60));

        seconds = timeInterval;

        aTimeIntervalString = days+" days, "+hours+" hours, "+minutes+" minutes, "+seconds+" seconds";
        return aTimeIntervalString;
    }

    public String vendorDescription() {
        return System.getProperty("java.vendor");
    }

    public String vendorURL() {
        return System.getProperty("java.vendor.url");
    }

    public boolean vendorURLdisabled() {
        String url = vendorURL();
        if (url == null || url.length() == 0)
            return true;
        return false;
    }

    public String jdkVersion() {
        return System.getProperty("java.version");
    }
    
    public String operatingSystem() {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
    }
    
}
