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

    // Accented email
    public static String EMAIL = "test@domain.com";
    public static String EMAIL_PERSO = "Frédéric Dupond";
    public String content = "Ceci est le contenu du mail, il n'y a rien de particulier, juste un texte avec des accents:\n\né è ä à ù ï î ô ö ç É È Ä À Ï Î Ô Ö";

    public Main(WOContext context) {
        super(context);
    }

    public void setAdminEmail (String email) {
        ERJavaMail.sharedInstance ().setAdminEmail (email);
    }

    public String adminEmail () {
        return ERJavaMail.sharedInstance ().adminEmail ();
    }

    public void sendTextOnlyMail () {
        // Create Attachment
        ERMailTextAttachment textAttachment = new ERMailTextAttachment ("Résultats.txt", "Data goes here! ...");

        // Create an instance of an OFMailDelivery subclass
        ERMailDeliveryPlainText mail = new ERMailDeliveryPlainText ();

        try {
            mail.newMail();
            mail.setTextContent (content);
            mail.addAttachment (textAttachment);
            mail.setFromAddress (EMAIL, EMAIL_PERSO);
            mail.setReplyToAddress (EMAIL);
            mail.setToAddress (this.adminEmail ());
            mail.setSubject ("Les résultats sont arrivés !");
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
