package com.webobjects.monitor.application;

/*
 © Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

 IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

 In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.

 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MSiteConfig;

import er.extensions.components.ERXComponent;

public class MonitorComponent extends ERXComponent {

    public static final boolean isClickToOpenEnabled = Boolean.parseBoolean(System.getProperty("er.component.clickToOpen", "false"));

	protected Logger log = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = -1880897151494772932L;

    public final int APP_PAGE = 0;

    public final int HOST_PAGE = 1;

    public final int SITE_PAGE = 2;

    public final int PREF_PAGE = 3;

    public final int HELP_PAGE = 4;

    public final int MIGRATION_PAGE = 5;

    public final int MOD_PROXY_PAGE = 6;

    public Application theApplication = (Application) WOApplication.application();

    private WOTaskdHandler _handler;

    private MApplication myApplication;
    private MInstance myInstance;
    private MHost myHost;

    private String _message;

    public MonitorComponent(WOContext aWocontext) {
        super(aWocontext);
        _handler = new WOTaskdHandler(mySession());
    }

    @Override
    public void awake() {
    	super.awake();
    	_message = null;
    }

    protected NSMutableArray allHosts() {
        return siteConfig().hostArray();
    }

    protected MSiteConfig siteConfig() {
        return WOTaskdHandler.siteConfig();
    }

    public Session mySession() {
        return (Session) super.session();
    }

    public WOTaskdHandler handler() {
        return _handler;
    }

	public final MApplication myApplication() {
		return myApplication;
	}

	public void setMyApplication(MApplication application) {
		assert application != null;
		myApplication = application;
		myInstance = null;
	}

	public final MInstance myInstance() {
		return myInstance;
	}

	public void setMyInstance(MInstance instance) {
		assert instance != null;
		myInstance = instance;
		myApplication = instance.application();
	}

	public final MHost myHost() {
		return myHost;
	}

	public void setMyHost(MHost host) {
		myHost = host;
	}

	public String message() {
		if (_message == null) {
			_message = ((Session)session()).message();
		}
		return _message;
	}

	/*
    public void appendToResponse(WOResponse response, WOContext context) {
        ERXClickToOpenSupport.preProcessResponse(response, context, isClickToOpenEnabled);
        super.appendToResponse(response, context);
        ERXClickToOpenSupport.postProcessResponse(getClass(), response, context, isClickToOpenEnabled);
    }
    */

}
