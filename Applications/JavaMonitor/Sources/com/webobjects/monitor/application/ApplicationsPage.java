package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
 SUCH DAMAGE.
 */
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.String_Extensions;

public class ApplicationsPage extends MonitorComponent {

    private static final long serialVersionUID = -2523319756655905750L;

	private int _totalInstancesConfigured = 0;
	private int _totalInstancesRunning = 0;
	
	
    public ApplicationsPage(WOContext aWocontext) {
        super(aWocontext);
        handler().updateForPage(name());
    }
 

    public MApplication currentApplication;

    public String newApplicationName;

    public NSArray<MApplication> applications() {
    	NSMutableArray<MApplication> applications = new NSMutableArray<MApplication>();
    	applications.addObjectsFromArray(mySession().siteConfig().applicationArray());
    	EOSortOrdering order= new EOSortOrdering("name", EOSortOrdering.CompareAscending);
    	EOSortOrdering.sortArrayUsingKeyOrderArray(applications, new NSArray(order));
    	
    	calculateTotals(applications);
     	return applications;
    }
    
    public String hrefToApp() {
        String aURL = siteConfig().woAdaptor();
        if (aURL != null) {
            aURL = aURL + "/" + currentApplication.name();
        }
        return aURL;
    }

    public WOComponent appDetailsClicked() {
        return AppDetailPage.create(context(), currentApplication);
    }

    public WOComponent addApplicationClicked() {
        if (String_Extensions.isValidXMLString(newApplicationName)) {
            handler().startReading();
            try {
                if (siteConfig().applicationWithName(newApplicationName) == null) {
                    MApplication newApplication = new MApplication(newApplicationName, siteConfig());
                    siteConfig().addApplication_M(newApplication);

                    if (siteConfig().hostArray().count() != 0) {
                        handler().sendAddApplicationToWotaskds(newApplication, siteConfig().hostArray());
                    }

                    AppConfigurePage aPage = AppConfigurePage.create(context(), newApplication);
                    aPage.isNewInstanceSectionVisible = true;

                    // endReading in the finally block below
                    return aPage;
                }
            } finally {
                handler().endReading();
            }
        }
        newApplicationName = null;
        return ApplicationsPage.create(context());
    }

  public static WOComponent create(WOContext context) {
		return context.page().pageWithName(ApplicationsPage.class.getName());
	}

	public WOComponent deleteClicked() {
	    
	    final MApplication application = currentApplication;
	    
	    return ConfirmationPage.create(context(), new ConfirmationPage.Delegate() {

            public WOComponent cancel() {
                return ApplicationsPage.create(context());
            }

            public WOComponent confirm() {
                handler().startWriting();
                try {
                    siteConfig().removeApplication_M(application);

                    if (siteConfig().hostArray().count() != 0) {
                        handler().sendRemoveApplicationToWotaskds(application, siteConfig().hostArray());
                    }
                } finally {
                    handler().endWriting();
                }
                return ApplicationsPage.create(context());
            }

            public String explaination() {
                return "Selecting 'Yes' will shutdown any running instances of this application, delete all instance configurations, and remove this application from the Application page.";
            }

            public int pageType() {
                return APP_PAGE;
            }

            public String question() {
                return "Are you sure you want to delete the <I>" + application.name() + "</I> Application?";
            }
	        
	    });
    }

	
	
    public WOComponent bounceClicked() {
        AppDetailPage page = AppDetailPage.create(context(), currentApplication);
        page = (AppDetailPage) page.bounceClicked();
        return page;
    }

    public WOComponent configureClicked() {
        AppConfigurePage aPage = AppConfigurePage.create(context(), currentApplication);
        aPage.isNewInstanceSectionVisible = true;
        return aPage;
    }
    
    
    /**
     * Calculates and sets the {@link #totalInstancesConfigured()} and {@link #totalInstancesRunning()}
     * for the given array of applications
     * 
     * @param applications
     */
    public void calculateTotals(NSMutableArray<MApplication> applications){
    	int totalRunningInstances = 0;
    	int totalConfiguredInstances = 0;
    	
    	// use for-loop to preserve compile-time error-checking instead of using valueForKey("runningInstancesCount.@sum")
    	for (MApplication mApplication : applications) {
    		totalRunningInstances = totalRunningInstances + mApplication.runningInstancesCount();
    		totalConfiguredInstances = totalConfiguredInstances +  mApplication.instanceArray().count();
		}
    	setTotalInstancesConfigured(totalConfiguredInstances);
    	setTotalInstancesRunning(totalRunningInstances);
    }

    
	/**
	 * @return the total number of instances configured for all applications
	 */
	public int totalInstancesConfigured() {
		return _totalInstancesConfigured;
	}

	/**
	 * Sets the total number of instances configured for all applications
	 * @param totalInstancesConfigured 
	 */
	public void setTotalInstancesConfigured(int totalInstancesConfigured) {
		_totalInstancesConfigured = totalInstancesConfigured;
	}

	/**
	 * @return the total number of running instances for all applications
	 */
	public int totalInstancesRunning() {
		return _totalInstancesRunning;
	}

	/**
	 * Sets the total number of running instances for all applications
	 * @param totalInstancesRunning
	 */
	public void setTotalInstancesRunning(int totalInstancesRunning) {
		_totalInstancesRunning = totalInstancesRunning;
	}

    
    
}
