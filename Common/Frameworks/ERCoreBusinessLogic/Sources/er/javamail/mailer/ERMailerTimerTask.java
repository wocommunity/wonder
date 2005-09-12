//
// ERMailDaemonTask.java
// Project ERMailer
//
// Created by max on Tue Oct 22 2002
//
package er.javamail.mailer;

import java.util.TimerTask;

import er.extensions.ERXLogger;

/**
 * Timer task used when running the ERMailer
 * in daemon mode.
 */
public class ERMailerTimerTask extends TimerTask {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERMailerTimerTask.class);

    /**
     * Processes the outgoing mail.
     */
    public void run() {
        if (log.isDebugEnabled()) log.debug("Timer firing to process outgoing mail.");

        try {
            ERMailer.instance().processOutgoingMail();
        }
        catch ( Exception e ) {
            log.error("run(): caught exception: " + e);    
        }
    }
}
