/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.interfaces;

/**
 * Nice interface implemented by all ER edit pages.  In a next page delegate you can ask the sender if the object was saved.  Useful for determining which button the user hit, ie save or cancel.
 */

public interface ERDObjectSaverInterface {
    public boolean objectWasSaved();
}
