/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* EREOFLogEntryInterface.java created by max on Tue 19-Jun-2001 */
package er.extensions;

/////////////////////////////////////////////////////////////////////////////////////////////
//  Interface implemented by EnterpriseObjects to inialize with a logging event and optionally
//	a layout.
/////////////////////////////////////////////////////////////////////////////////////////////
public interface ERXEOFLogEntryInterface {
    
    public void intializeWithLoggingEvent(org.apache.log4j.spi.LoggingEvent event, org.apache.log4j.Layout layout);
}
