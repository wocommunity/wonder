/*
 $Id$

 ERMailDeliveryJapanesePlainText.java - Tatsuya Kawano - tatsuya@mac.com
*/

package er.javamail;

import java.util.*;
import java.io.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

public class ERMailDeliveryJapanesePlainText extends ERMailDelivery {

    /** String Message content */
    private String _iso2022jpTextContent;

    /** Sets the subject for the current message instance */
    public void setSubject (String subject) throws MessagingException {
        String encoded = null;
        try {
            encoded = MimeUtility.encodeText (subject, "ISO-2022-JP", "B");
        } catch (Exception e) {
            encoded = subject;
        }
        this.mimeMessage ().setSubject  (encoded);
    }

    /** Sets the text content of the current message. */
    public void setTextContent (String text) {
        _iso2022jpTextContent = unicodeToISO2022JP (text);
    }

    /** Pre-processes the mail before it gets sent.
        @see ERMailDelivery#prepareMail */
    // protected DataHandler prepareMail() {
    public DataHandler prepareMail() {
        return new DataHandler(_iso2022jpTextContent, "text/plain; charset=\"ISO-2022-JP\"");
    }

    /** Encodes Unicode strings into ISO-2022-JP string. */
    public static String unicodeToISO2022JP (String unicodeString) {
        String iso2022jpString = null;
        
        try {
            byte[] byteArray = unicodeString.getBytes("ISO2022JP");
            iso2022jpString = new String(byteArray);
        } catch (java.io.UnsupportedEncodingException ex) {
            iso2022jpString = unicodeString;
        }

        return iso2022jpString;
    }

}

