//
// Application.java
// Project OdaikoJavaMailTests
//
// Created by camille on Thu Jul 04 2002
//

import er.extensions.*;
import er.javamail.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.*;

public class Application extends ERXApplication {

	public static String EMAIL = "test@testdomain.tld";

    public static void main(String argv[]) {
        WOApplication.main(argv, Application.class);
    }

    public Application() {
        super();
	}


	public void sendTextOnlyMail () {
        // Create Attachment
        ERMailTextAttachment textAttachment = new ERMailTextAttachment ("stockReport.txt", "Data goes here! ...");

        // Create an instance of an OFMailDelivery subclass
        ERMailDeliveryPlainText mail = new ERMailDeliveryPlainText();

        try {
            mail.newMail();
            mail.setTextContent ("Please see attachment!");
            mail.addAttachment (textAttachment);
            mail.setFromAddress (EMAIL);
            mail.setReplyToAddress (EMAIL);
            mail.setToAddresses (new NSArray (EMAIL));
            mail.setSubject ("Testing Stock Report via Email");
			mail.sendMail ();
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	public void sendHTMLMail () {
		ERMailUtils.sendHTMLMail ("Main", null,
							EMAIL,
							EMAIL,
							EMAIL, "HTML Test");
	}
}
