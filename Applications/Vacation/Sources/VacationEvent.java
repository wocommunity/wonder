// Event.java
// Created on Mon Nov 05 10:54:08  2001 by Apple EOModeler Version 410

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.math.BigDecimal;

public class VacationEvent extends _VacationEvent {

    public VacationEvent() {
        super();
        setArchived(new Integer(0));
        setToDate(new NSTimestamp());
        setFromDate(new NSTimestamp());
        setFromPeriod("am");
        setToPeriod("pm");
    }

    public String legendText() {
        return legendTextForType(type());
    }

    public static String legendTextForType(String inType) {
        String returnText = inType.substring(0,1);
        if (inType.indexOf("Request")!=-1) returnText = returnText+"R";
        return returnText;
    }
    
    public void validateForSave() throws NSValidation.ValidationException {
        if (totalTime().doubleValue()<0.5) throw new NSValidation.ValidationException("Exception: The total requested duration must be a minimum of 0.5 days");
        else if (toDate().before(fromDate())) throw new NSValidation.ValidationException("Exception: The From Date must be before the To Date");
    }

    public boolean display() {
        if (type().equals("Holiday") || type().equals("Lieu Time Earned")) return false;
        else return true;
    }

    public String periodStringForDate(GregorianCalendar compareDate) {

        // conversion from NSTimestamps to GregorianCalendars
        GregorianCalendar toGregDate = new GregorianCalendar();
        GregorianCalendar fromGregDate = new GregorianCalendar();
        
        toGregDate.setTime(toDate());
        fromGregDate.setTime(fromDate());

        String returnString = "";
        
        if (toGregDate.equals(compareDate)) {
            returnString = toPeriodString();
        }
        
        if (fromGregDate.equals(compareDate)) {
            returnString = fromPeriodString();
        }

        return returnString;

    }

    public String toPeriodString() {
        if (toPeriod()!=null && toPeriod().equals("am")) return "(" + toPeriod() + ")";
        else return "";
    }

    public String fromPeriodString() {
        if (fromPeriod()!= null && fromPeriod().equals("pm")) return "(" + fromPeriod() + ")";
        else return "";
    }

    // override the standard method, so that we can send email messages
    public void setType(String value) {

        takeStoredValueForKey(value, "type");

        // send an email to the user saying telling them an event
        WOMailDelivery mailer = WOMailDelivery.sharedInstance();
        Application application = (Application) WOApplication.application();
        //application.adminEmail

        if (person()!=null) {

            if (person().editorUser==null) person().editorUser = person();
            
            NSTimestampFormatter fm = new NSTimestampFormatter((String) application.settings.objectForKey("fullCalendarDateFormat"));

            if (((String) application.settings.objectForKey("emailActivated")).equals("true")) {
            mailer.composePlainTextEmail(person().editorUser.email(), new NSArray(person().email()), null, "Your Leave Request has been updated.", "The request from " + fm.format(fromDate()) + " to " + fm.format(toDate()) + " has been updated to status: " + value + ".\n\nPlease check the Leave system.\n" + application.settings.objectForKey("appURL") +
                                                "\n\nThis email was automatically generated."
                                                         , true);

           }

        }

    }

}
