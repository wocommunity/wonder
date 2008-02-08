/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableSet;

/**
 * Used as a generic way to retain a reference to an object so that it will not
 * be collected by the garbage collector. This class is most often used to
 * retain objects used to observe {link
 * 
 * @NSNotification}s.<br/> <br/> Note that the current implementation does not
 *                    implement reference counting so calling retain multiple
 *                    times on the same object does not have any effect after
 *                    the first call.
 */
// ENHANCEME: Should implement reference counting.
public class ERXRetainer {
	private static EOEditingContext ec;

	/** set used to retain references to objects */
	private static NSMutableSet _retainerSet = new NSMutableSet();

	/**
	 * Retains a reference to the object.
	 * 
	 * @param object
	 *            object to be retained.
	 */
	public static void retain(Object object) {
		synchronized (_retainerSet) {
			/*if (object instanceof EOEnterpriseObject) {
				EOEnterpriseObject eo = (EOEnterpriseObject) object;
				if (ec == null) {
					ec = ERXEC.newEditingContext();

				}
				ec.lock();
				try {
					object = ERXEOControlUtilities.localInstanceOfObject(ec, eo);
				finally {
					ec.unlock();
				}

			}*/
			_retainerSet.addObject(object);

		}
	}

	/**
	 * Releases the reference to the object.
	 * 
	 * @param object
	 *            object to be released.
	 */
	public static void release(Object object) {
		synchronized (_retainerSet) {
			_retainerSet.removeObject(object);
		}
	}

	/**
	 * Tests if the given object is being retained by the ERXRetainer class.
	 * 
	 * @param object
	 *            object to be tested.
	 * @return returns if the given object is currently retained.
	 */
	public static boolean isObjectRetained(Object object) {
		synchronized (_retainerSet) {

			return _retainerSet.containsObject(object);
		}
	}
}
