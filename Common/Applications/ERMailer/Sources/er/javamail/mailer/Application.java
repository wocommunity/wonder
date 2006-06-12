//
// Application.java
// Project ERMailer
//
// Created by Max Muller on Tue Oct 22 2002
//
package er.javamail.mailer;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.ERXApplication;
import er.extensions.ERXEC;
import er.extensions.ERXExtensions;
import er.extensions.ERXLogger;
import er.extensions.ERXProperties;
import er.extensions.ERXUtilities;

import er.corebusinesslogic.ERCMailDelivery;
import er.corebusinesslogic.ERCMailMessage;

import er.javamail.ERJavaMail;

import java.util.Timer;
import java.util.TimerTask;

import java.util.Enumeration;

public class Application extends ERXApplication {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(Application.class);

    /**
     * Main initialization function.
     * @param argv command line arguements
     */
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    /** holds a reference to the mail timer when launched in daemon mode */
    protected Timer mailTimer;
    
    /**
     * Public application constructor.
     */
    public Application() {
        super();
    }

    /**
     * Method invoked when the application has finished launching.
     * Either processes the outgoing mail and then exits or sets up
     * a daemon process to process the outgoing mail at the specified
     * daemon frequency which is specified in the property:
     * <b>er.javamail.mailer.ERBatchMailerDaemonFrequency</b>
     */
    public void didFinishLaunching() {
        if (ERXProperties.booleanForKey("er.javamail.mailer.ERTestSendingMail"));
            testSendingMail();
        int frequency = ERXProperties.intForKey("er.javamail.mailer.ERBatchMailerDaemonFrequency");
        if (frequency > 0) {
            log.debug("Scheduling timer for frequency: " + frequency + "(s)");
            mailTimer = new Timer(true);
            mailTimer.schedule(new ERMailerTimerTask(), frequency*1000l, frequency*1000l);
        } else {
            ERMailer.instance().processOutgoingMail();
            log.debug("Done processing mail. Exiting.");
            System.exit(0);            
        }
    }

    public void testSendingMail() {
        log.info("Sending test mail");
        try {
            EOEditingContext ec = ERXEC.newEditingContext();
            
            ERCMailMessage message = ERCMailDelivery.sharedInstance().composeEmail(ERJavaMail.sharedInstance().adminEmail(),
                                                          new NSArray(ERJavaMail.sharedInstance().adminEmail()),
                                                          new NSArray(ERJavaMail.sharedInstance().adminEmail()),
                                                          null,
                                                          "This is a test",
                                                          "This is the body",
                                                          ec);
            ec.saveChanges();
        } catch (Exception e) {
            log.error("Caught exception: " + e.getMessage() + " stack: " + ERXUtilities.stackTrace(e));
        }
        log.info("Done.");
    }    
}
