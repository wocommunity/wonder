//
// DirectAction.java
// Project Feedback
//
// Created by ravi on Fri Aug 10 2001
//
package org.svgobjects.examples.ecard;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class DirectAction extends WODirectAction {
    public static NSArray keys = new NSArray(new String[]{"from", "to", "message", "subject", "image"});
    public String from;
    public String to;
    public String subject;
    public String message;
    public String image;

    public DirectAction(WORequest aRequest) {
        super(aRequest);
	takeFormValuesForKeyArray(keys);
    }

    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }
    
    public WOActionResults pictureAction() {
	WOComponent nextPage = pageWithName("Picture");
	
	// set values
	nextPage.takeValueForKey(image, "image");
	
	return nextPage;
    }

    public WOActionResults feedbackAction() {
	WOComponent nextPage = pageWithName("Preview");
		
	// set values
	nextPage.takeValueForKey(subject, "subject");
	nextPage.takeValueForKey(message, "message");
	nextPage.takeValueForKey(image, "image");

	return nextPage;
    }
    
    public WOActionResults sendAction() {
	WOComponent nextPage = pageWithName("ThankYou");
	WOMailDelivery sharedInstance = WOMailDelivery.sharedInstance();

	// send emails
	sharedInstance.sendEmail(recipientEmail());
	sharedInstance.sendEmail(senderEmail());

	return pageWithName("ThankYou");
    }
    
    /*
    * accessors
    */
    public String recipientEmail() {
    	WOComponent nextPage = pageWithName("RecipientEmail");
	WOMailDelivery sharedInstance = WOMailDelivery.sharedInstance();

	// set values
	nextPage.takeValueForKey(subject, "subject");
	nextPage.takeValueForKey(message, "message");
	nextPage.takeValueForKey(image, "image");
	nextPage.takeValueForKey(from, "from");
	
	// compose email
	return sharedInstance.composeComponentEmail(from, new NSArray(to), null, subject, nextPage, false);
    }
    
    public String senderEmail() {
    	WOComponent nextPage = pageWithName("SenderEmail");
	WOMailDelivery sharedInstance = WOMailDelivery.sharedInstance();

	// set values
	nextPage.takeValueForKey(subject, "subject");
	nextPage.takeValueForKey(message, "message");
	nextPage.takeValueForKey(image, "image");
	nextPage.takeValueForKey(to, "to");
	
	// compose email
	return sharedInstance.composeComponentEmail("eCards", new NSArray(from), null, subject, nextPage, false);
    }
}