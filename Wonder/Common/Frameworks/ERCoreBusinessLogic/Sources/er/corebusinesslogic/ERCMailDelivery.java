/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import java.util.*;
import er.extensions.*;

public class ERCMailDelivery {

    ///////////////////////////////////////////////////// Static Methods //////////////////////////////////////////////////////////
    protected static ERCMailDelivery _sharedInstance;
    public static ERCMailDelivery sharedInstance() {
        if (_sharedInstance == null)
            _sharedInstance = new ERCMailDelivery();
        return _sharedInstance;
    }

    public static String commaSeparatedListFromArray(NSArray a) {
        StringBuffer result=new StringBuffer();
        if (a!=null) {
            for (Enumeration e=a.objectEnumerator(); e.hasMoreElements(); ) {
                String address=(String)e.nextElement();
                if (address.indexOf("\"")!=-1) {
                    address=address.replace('\"', '\''); 
                }
                if (address.indexOf(",")!=-1) { // FIXME I am sure other characters than comma will cause problems
                    address=address.replace(',', ' ');                    
                    // address='\"'+address+'\"';
                }
                result.append(address);
                if (e.hasMoreElements()) {
                    result.append(',');
                    result.append(' ');
                }
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////// Instance Methods ////////////////////////////////////////////////////////

    public ERCMailMessage composeEmail(String from,
                                     NSArray to,
                                     NSArray cc,
                                     NSArray bcc,
                                     String title,
                                     String message,
                                     EOEditingContext ec) {
        ERCMailMessage mailMessage = (ERCMailMessage)ERCMailMessage.mailMessageClazz().createAndInsertObject(ec);
        String safeTitle=title!=null ? ( title.length() > 200 ? title.substring(0,198) : title ) : null;
        mailMessage.setTitle(safeTitle);
        mailMessage.setFromAddress(from);
        mailMessage.setToAddresses(commaSeparatedListFromArray(to));
        mailMessage.setCcAddresses(commaSeparatedListFromArray(cc));
        mailMessage.setBccAddresses(commaSeparatedListFromArray(bcc));
        mailMessage.setText(message);
        return mailMessage;
    }

    public ERCMailMessage composeEmailWithAttachments (String from,
                                     NSArray to,
                                     NSArray cc,
                                     NSArray bcc,
                                     String title,
                                     String message,
                                     NSArray filePaths,
                                     EOEditingContext ec) {
        ERCMailMessage mailMessage = this.composeEmail(from, to, cc, bcc, title, message, ec);
        Enumeration enm = filePaths.objectEnumerator();

        while(enm.hasMoreElements()) {
            String filePath = (String)enm.nextElement();
            ERCMessageAttachment attachment = (ERCMessageAttachment)ERCMessageAttachment.messageAttachmentClazz().createAndInsertObject(ec);
            attachment.setFilePath(filePath);
            mailMessage.addToBothSidesOfAttachments(attachment);
        }
        return mailMessage;
    }

    public ERCMailMessage composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 WOComponent component,
                                                 EOEditingContext ec) {
        String message=null;
        if (component!=null) {
            NSData d=component.generateResponse().content();
            message=new String(d.bytes(0, d.length())); // FIXME inefficient?
        }
        return composeEmail(from, to, cc, bcc, title, message, ec);
    }
    
}

