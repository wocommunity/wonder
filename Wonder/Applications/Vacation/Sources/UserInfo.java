import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** This is the normal user information page (not seen by Admin users).
It displays the user's info and events in non-editable form.
A user can request a new event in this page.  If they do request a new event, an email is sent to all administrators of their group.
*/
public class UserInfo extends VacationComponent {
    protected VacationEvent newDate;
    protected VacationEvent currentDate;

    /** Constructor.  Initialize a blank event for any requests */
    public UserInfo(WOContext context) {
        super(context);
        newDate = new VacationEvent();
    }

    /** Method called when a user requests a new event */
    public WOComponent requestDate() {

        session().defaultEditingContext().insertObject(newDate);

        // put the new date in the user's dates
        newDate.setPerson(session.user());
        session.user().addToDates(newDate);

        try
        {
            if (session.user().group()==null) throw new Exception("Request was not processed. You are not a member of a group.  Please contact your administrator to be added, then try again.");

            // save the new event
            session().defaultEditingContext().saveChanges();

            // get the admin users for a group
            NSArray adminusers = session.user().adminUsers(session.defaultEditingContext(), session.user().group());

            if (adminusers != null) {
                // send an email to the admin users
                WOMailDelivery mailer = WOMailDelivery.sharedInstance();
                NSMutableArray adminEmails = new NSMutableArray();
                String adminEmailString = "";

                NSTimestampFormatter fm = new NSTimestampFormatter((String) application.settings.objectForKey("fullCalendarDateFormat"));

                // for each admin user...
                for (int i=0; i<adminusers.count(); i++) {

                    Person adminUser = (Person) adminusers.objectAtIndex(i);

                    if (adminUser.email()!=null && session.user().email()!=null) {

                        adminEmailString = adminEmailString + " " + adminUser.name() + ";";

                        if (((String) application.settings.objectForKey("emailActivated")).equals("true")) {
                            mailer.composePlainTextEmail(session.user().email(), new NSArray(adminUser.email()), null,
                                                         "Request from " + session.user().name(), session.user().name() +
                                                         " has requested some leave from " + fm.format(newDate.fromDate()) + " to " + fm.format(newDate.toDate())
                                                         + " (" + newDate.comment() + ") for " + newDate.totalTime() + " day(s).\n\n" +
                                                         "Please check the Leave Management software.\n\nClick here to login and view the request: "
                                                         + application.settings.objectForKey("appURL") + "?edit=" + session.user().userID(), true);
                        }
                    }

                }

                this.setMessage ("Request added and email sent to" + adminEmailString);
            }

        }
        catch (Exception e) {
            this.setMessage(e.getMessage());
        }

        // initialize a new Date object
        newDate = new VacationEvent();

        // invalidate objects
        session().defaultEditingContext().invalidateAllObjects();

        return null;
    }

    /** Save changes the default editing context */
    public WOComponent saveChanges() {

        try {
            session().defaultEditingContext().saveChanges();
        }
        catch (Exception e) {
            this.setMessage(e.getMessage());
        }

        session().defaultEditingContext().invalidateAllObjects();

        return null;
    }

    public boolean isInFuture() {
        NSTimestamp temptime = new NSTimestamp();
        if (currentDate.fromDate().after(temptime)) temptime = currentDate.fromDate();

        return temptime.equals(currentDate.fromDate());
    }

    public WOComponent cancelEvent() {
        currentDate.setType("Cancel Requested");
        return saveChanges();
    }

    public WOComponent refresh() {
        session.user().selectedYear = new Integer(session.selectedYear.intValue());
        return null;
    }

}
