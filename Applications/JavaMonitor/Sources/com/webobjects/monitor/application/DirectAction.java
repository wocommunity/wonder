package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.application.WOTaskdHandler.ErrorCollector;

import er.extensions.appserver.ERXResponse;

public class DirectAction extends WODirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    @Override
    public WOActionResults defaultAction() {
    	if (request().stringFormValueForKey("pw") != null ) {
    		Main loginPage = (Main) pageWithName(Main.class.getName());
    		loginPage.setPassword(request().stringFormValueForKey("pw"));
    		return loginPage.loginClicked();
    	}
    	return super.defaultAction();
    }
    
    public WOComponent MainAction() {
        return pageWithName("Main");
    }

    protected MSiteConfig siteConfig() {
        return WOTaskdHandler.siteConfig();
    }
    
    private Object nonNull(Object value) {
        if(value == null) {
            return "";
        }
        return value;
    }

    private NSDictionary historyEntry(MApplication app) {
        NSMutableDictionary<String, Object> result = new NSMutableDictionary<>();
        result.setObjectForKey(app.name(), "applicationName");
        NSArray<MInstance> allInstances = app.instanceArray();
        result.setObjectForKey(Integer.valueOf(allInstances.count()), "configuredInstances");
        
        int runningInstances = 0;
        int refusingInstances = 0;
        NSMutableArray instances = new NSMutableArray();
        for (MInstance instance : allInstances) {
            if (instance.isRunning_M()) {
                runningInstances++;
                instances.addObject(instance);
            }
            if (instance.isRefusingNewSessions()) {
                refusingInstances++;
            }
        }
        result.setObjectForKey(Integer.valueOf(runningInstances), "runningInstances");
        result.setObjectForKey(Integer.valueOf(refusingInstances), "refusingInstances");
        
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@sum.activeSessionsValue")), "sumSessions");
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@max.activeSessionsValue")), "maxSessions");
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@avg.activeSessionsValue")), "avgSessions");
        
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@sum.transactionsValue")), "sumTransactions");
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@max.transactionsValue")), "maxTransactions");
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@avg.transactionsValue")), "avgTransactions");
        
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@max.avgTransactionTimeValue")), "maxAvgTransactionTime");
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@avg.avgTransactionTimeValue")), "avgAvgTransactionTime");

        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@max.avgIdleTimeValue")), "maxAvgIdleTime");
        result.setObjectForKey(nonNull(app.instanceArray().valueForKeyPath("@avg.avgIdleTimeValue")), "avgAvgIdleTime");
          
        return result;
    }
    
    public WOResponse statisticsAction() {
        ERXResponse response = new ERXResponse();
        String pw = context().request().stringFormValueForKey("pw");
        if(siteConfig().compareStringWithPassword(pw)) {
            WOTaskdHandler handler = new WOTaskdHandler(new ErrorCollector() {

                public void addObjectsFromArrayIfAbsentToErrorMessageArray(NSArray<String> aErrors) {
                    
                }});
            handler.startReading();
            try {
                NSMutableArray stats = new NSMutableArray();
                for (MApplication app : siteConfig().applicationArray()) {
                    handler.getInstanceStatusForHosts(app.hostArray());
                    NSDictionary appStats = historyEntry(app);
                    stats.addObject(appStats);
                }
                response.appendContentString(NSPropertyListSerialization.stringFromPropertyList(stats));
            } finally {
                handler.endReading();
            }
        }
        return response;
    }
}
