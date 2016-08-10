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
import com.webobjects.foundation.NSArray;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MObject;

public class HostConfigurePage extends MonitorComponent {

	public HostConfigurePage(WOContext aWocontext) {
		super(aWocontext);
	}

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -2948616033564158515L;

	private String _hostTypeSelection;

	public NSArray hostTypeList = MObject.hostTypeArray;

	public String hostTypeSelection() {
		if (_hostTypeSelection == null) {
			String type = myHost().osType();
			for (int i = hostTypeList.count() - 1; i >= 0; i--) {
				String myHostTypeSelection = (String) hostTypeList.objectAtIndex(i);
				if (type.equalsIgnoreCase(myHostTypeSelection)) {
					_hostTypeSelection = myHostTypeSelection;
				}
			}
		}
		return _hostTypeSelection;
	}

	public void setHostTypeSelection(String newType) {
		_hostTypeSelection = newType;
	}

	public WOComponent configureHostClicked() {
		handler().startWriting();
		try {
			MHost host = myHost();

			if ((_hostTypeSelection != null) && (!(_hostTypeSelection.toUpperCase().equals(host.osType())))) {
				host.setOsType(_hostTypeSelection.toUpperCase());
				handler().sendUpdateHostToWotaskds(host, siteConfig().hostArray());
			}
		} finally {
			handler().endWriting();
		}

		return HostConfigurePage.create(context(), myHost());
	}

	public WOComponent syncHostClicked() {
		MHost host = myHost();
		siteConfig().hostErrorArray.addObjectIfAbsent(host);
		handler().sendUpdateHostToWotaskds(host, new NSArray(host));

		return HostConfigurePage.create(context(), myHost());
	}

	public static HostConfigurePage create(WOContext context, MHost host) {
		HostConfigurePage page = (HostConfigurePage) context.page().pageWithName(HostConfigurePage.class.getName());
		page.setMyHost(host);
		return page;
	}

}