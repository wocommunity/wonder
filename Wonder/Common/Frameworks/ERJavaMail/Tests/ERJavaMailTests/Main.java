//
// Main.java: Class file for WO Component 'Main'
// Project OdaikoJavaMailTests
//
// Created by camille on Thu Jul 04 2002
//
 
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;
import er.javamail.*;
import java.util.*;

public class Main extends WOComponent {

    public static String EMAIL = "test@domain.com";
    public Main(WOContext context) {
        super(context);
    }


    public void sendTextOnlyMail () {
        // Create Attachment
        ERMailTextAttachment textAttachment = new ERMailTextAttachment ("stockReport.txt", "Data goes here! ...");

        // Create an instance of an OFMailDelivery subclass
        ERMailDeliveryPlainText mail = new ERMailDeliveryPlainText ();

        try {
            mail.newMail();
            mail.setTextContent ("Please see attachment!");
            mail.addAttachment (textAttachment);
            mail.setFromAddress (EMAIL);
            mail.setReplyToAddress (EMAIL);
            mail.setToAddress (EMAIL);
            mail.setSubject ("Testing Stock Report via Email");
			mail.sendMail ();
		} catch (Exception e) {
			e.printStackTrace ();
		}
    }

    public void sendHTMLMail () {
		ERMailUtils.sendHTMLMail ("Home", null,
							EMAIL,
							EMAIL,
							EMAIL, "HTML Test");
    }
}
