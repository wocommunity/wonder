/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

///////////////////////////////////////////////////////////////////////////////////////
// A Session Addition is a singleton object that can be accessed via instance methods off
// of the Session object, ie session.additions.sessionAdditionName.aMethod.  A session
// addition will only ever have one session object set on it at any given time.  See
// ERXSessionAddition for an abstract implementation.
///////////////////////////////////////////////////////////////////////////////////////
public interface ERXSessionAdditionInterface {
    public void setSession(ERXSession session);
    public String sessionAdditionName();
}
