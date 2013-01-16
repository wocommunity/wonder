/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSValidation;


/**
 * This class is pulled directly out of David Neumann's
 * ChangeNotification framework.  The only changes made are
 * to use log4j instead of System.out.println.<br/>
 * <br/>
 * The tolerant saver provides a way to save an editing context in
 * a tolerant fashion. By tolerant we mean that you can have the
 * option to save an editing context and have the exception ignored,
 * hvae the changes merged from the database or stomp all the changes
 * the database regardless of locking. The entry point for using this
 * class is the <code>save</code> method.
 * @deprecated use {@link ERXEC}
 */
// MOVEME: All of these methods could move to something like ERXEOFUtilities
@Deprecated
public class ERXTolerantSaver {

    /** logging support */
    public final static Logger log = Logger.getLogger(ERXTolerantSaver.class);

    /**
     * Cover method for calling the method <code>save</code> with the
     * third parameter (merge) set to true. See the description of
     * the three parameter version for a detailed explanation.
     * @param ec editing context to be saved.
     * @param writeAnyWay boolean flag to determine if the editing
     *		context should be resaved after a general adaptor
     *		exception
     * @return string representation of the exception that happened.
     *		This will be changed in the future.
     */
    public static String save(EOEditingContext ec, boolean writeAnyWay) {
        return save(ec, writeAnyWay, true);
    }

    /**
     * Entry point for saving an editing context in a tolerant
     * manner. The two flags for this method are <code>writeAnyWay</code>
     * and <code>merge</code>. The writeAnyWay flag controls if a second
     * save should be performed if the first operation fails due to a general
     * adaptor operation. Note that even if this option is specified as
     * false the object will be refetched and optionally have the new changes
     * merged into. This means that the objects that failed saving to the
     * database will be ready to be saved if writeAnyWay is false. The second
     * option is to merge the previous changes if a failure occurs. If this
     * is set to true then when a locking failure occurs the object is refetched
     * and then the previous changes are re-applied to the object.
     * @param ec editing context to be saved
     * @param writeAnyWay boolean flag to determine if an editing context should
     *		be saved again after a failure.
     * @param merge boolean flag that determines if changes should be re-applied
     *		if a locking failure occurs when the first save happens
     * @return string indicating the exception that happened, null if everything
     *		went smooth. This should be changed in the future.
     */
    // FIXME: returning those strings for error conditions is not very good
    // we should probably return ints for status and re-throw the original exception
    // in some cases
    private static String _save(EOEditingContext ec, boolean writeAnyWay, boolean merge) {
        String result = null;
        if (log.isDebugEnabled()) log.debug("TolerantSaver: save...");
        try {
            //if (log.isDebugEnabled()) log.debug("about to save changes...");
            ec.saveChanges();
        } catch(NSValidation.ValidationException eov) {
            log.info("TolerantSaver: Caught EOValidationException: " + eov.getMessage());
            throw eov;
        } catch(EOGeneralAdaptorException e) {
            if(ERXEOAccessUtilities.isOptimisticLockingFailure(e)) {
                EOEnterpriseObject failedEO = ERXEOAccessUtilities.refetchFailedObject(ec, e);
                if(merge) {
                    ERXEOAccessUtilities.reapplyChanges(failedEO, e);
                }
                if(writeAnyWay) {
                    _save(ec, writeAnyWay, merge);
                }
                result = "EOAdaptorOptimisticLockingFailure";
            } else {
                result = "Error: No EOAdaptorFailureKey, reason ";
                result += errorFromException(e);
            }
        }
        return result;
    }

    public static String save(EOEditingContext ec, boolean writeAnyWay, boolean merge) {
        int tries = 0;
        String re = "";
        while (tries++ < 20) {
            re = _save(ec, writeAnyWay, merge);
            if (re == null || re.indexOf("deadlock") == -1) {
                break;
            }
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            log.error("got deadlock, trying to save again");
        }
        return re;
    }

    private static String errorFromException(Exception e) {
        String stackTrace = NSLog.throwableAsString(e);
        //this works for frontbase, add other indexOf statements for db's like oracle, ...
        if (stackTrace.indexOf("multiple transaction conflict detected") != -1) {
            return "deadlock";
        }
        return "";
    }
}
