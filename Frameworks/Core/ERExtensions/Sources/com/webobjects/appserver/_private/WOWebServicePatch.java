package com.webobjects.appserver._private;

import java.io.InputStream;

import org.apache.axis.server.AxisServer;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.Document;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOWSDDRegistrar;
import com.webobjects.foundation.NSLog;
import com.webobjects.webservices.support.WOXMLProvider;

/**
 * Fixes a bug in WOWebService that causes the "Resources/server.wsdd" file (for configuration Axis)
 * to fail to be loaded if contained inside a "jar framework" rather than a regular framework.
 * 
 * @author johnthuss
 *
 */
public class WOWebServicePatch {

	/*
	 * Re-implemented to support loading the server.wsdd file from a jar framework as well.
	 */
	static Document getDeploymentDocument() {
		Document document = null;
		InputStream stream = WOApplication.application().resourceManager().inputStreamForResourceNamed("server.wsdd", null, null);
		if (stream != null) {
			try {
				document = XMLUtils.newDocument(stream);
			} catch (Exception e) {
				if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupWebServices)) {
					NSLog.debug.appendln("Couldn't parse .wsdd file");
					NSLog.debug.appendln(e);
				}
			}
		}
		if (document == null) {
			if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelDetailed, NSLog.DebugGroupWebServices))
				NSLog.debug.appendln("Couldn't " + ((stream == null) ? "find" : "parse") + " .wsdd file. Using empty default.");
			document = WOWSDDRegistrar._getEmptyDeployment();
		}
		return document;
	}
	
	/*
	 * Same functionality as WOWebService, except it calls our version of getDeploymentDocument().
	 */
	public static void initServer() {
		if (WOWebService.engine == null) {
			if (WOApplication.application().resourceManager().inputStreamForResourceNamed("server.wsdd", null, null) == null)
				return; // nothing necessary to do - the default .wsdd will be loaded correctly by WO.
			
			try {
				WOWebService.provider = new WOXMLProvider(getDeploymentDocument());
				WOWebService.engine = new AxisServer(WOWebService.provider);
			} catch (Exception ex) {
				NSLog.err.appendln("Error trying to deploy Axis engine " + ex);
				if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupWebServices | NSLog.DebugGroupWebObjects))
					NSLog.err.appendln(ex);
				System.exit(1);
			}
		}
	}
	
}
