package er.bugtracker.tasks;

import java.util.Enumeration;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOMailDelivery;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;

import er.bugtracker.People;
import er.bugtracker.pages.BugReportEmail;
import er.extensions.ERXEC;
import er.extensions.ERXWOContext;
/**
 * Sends email reminders. 
 * Call up with <code>ERXMainRunner -mainClass er.bugtracker.tasks.BatchReporter -mainMethod runBatchReport</code>
 * @author ak
 *
 */
public class BatchReporter {

    /**
     * we run over all people in the DB and send them a summary email if they
     * have unread bugs
     */
    public void runBatchReport() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            NSArray everybody = People.clazz.allObjects(ec);
            for (Enumeration e = everybody.objectEnumerator(); e.hasMoreElements();) {
                People person = (People) e.nextElement();
                NSDictionary bindings = new NSDictionary(new Object[] { person }, new Object[] { "user" });
                NSArray unreadBugs = person.unreadBugs();
                String email = person.email();
                if (unreadBugs.count() > 0 && email != null && email.length() != 0) {
                    BugReportEmail emailBody = (BugReportEmail)WOApplication.application().pageWithName("BugReportEmail", ERXWOContext.newContext());
                    emailBody.takeValueForKey(unreadBugs, "unreadBugs");
                    emailBody.takeValueForKey(person, "owner");
                    WOMailDelivery.sharedInstance().composeComponentEmail("bugtracker@netstruxr.com", new NSArray(email), null,
                            "You have " + unreadBugs.count() + " unread bug(s)", emailBody, true);
                    NSLog.debug.appendln("Sending report to " + email + ": " + unreadBugs.count() + " unread bugs");
                }
            }
        } finally {
            ec.unlock();
        }
    }

}
