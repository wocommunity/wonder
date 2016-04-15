//
// ERMailDaemonTask.java
// Project ERMailer
//
// Created by max on Tue Oct 22 2002
//
package er.javamail.mailer;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timer task used when running the ERMailer
 * in daemon mode.
 */
public class ERMailerTimerTask extends TimerTask {
    private static final Logger log = LoggerFactory.getLogger(ERMailerTimerTask.class);

    /**
     * Processes the outgoing mail.
     */
    @Override
    public void run() {
        log.debug("Timer firing to process outgoing mail.");

        try {
            ERMailer.instance().processOutgoingMail();
        }
        catch ( Exception e ) {
            log.error("run(): caught exception", e);
        }
    }
}
