/*
 * WOStatsPage.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

public class WOStatsPage extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
        sessionMemoryDict = new NSDictionary((Map)sessionsDict.removeObjectForKey("Avg. Memory Per Session"), true);
        sessionStats = (NSArray)sessionsDict.removeObjectForKey("Last Session's Statistics");
        maxSessionsDate = (NSTimestamp) sessionsDict.removeObjectForKey("Peak Active Sessions Date");


        maxPageCount = 0;
        maxActionCount = 0;
        maxPageCount = _maxServedForDictionary(pagesDict);
        maxActionCount = _maxServedForDictionary(directActionsDict);
    }

    @Override
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
        double aDetailCount = detailCount();
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
