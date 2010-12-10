Odaiko MailDelivery Framework
-----------------------------


IMPORTANT NOTICE:
- - - - - - - - -

These files are protected under the GNU Lesser Public License.
You should read and understand the terms of this license before using it.
The article 15 in the LICENSE read this:

	  15. BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
	WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE
	LAW.  EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS
	AND/OR OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY 
	OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT 
	LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
	FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS TO THE QUALITY AND 
	PERFORMANCE OF THE LIBRARY IS WITH YOU.  SHOULD THE LIBRARY PROVE 
	DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR 
	OR CORRECTION.



WHAT CAN I DO WITH IT ?
- - - - - - - - - - - -

This small framework is intended to send mails with the JavaMail API in conjunction to Apple WebObjects (http://www.apple.com/webobjects).  You can also use it without WebObjects but some (very little) porting is needed.  Two classes can't be used in this context: ERMailDeliveryWOComponentPlainText and ERMailDeliveryHTML.



ADVANTAGES
- - - - - -

This framework merely consists of 5 files that you may add to your project or compile as a separate framework.  Its originality comes from its ability to send mail with a separated thread.  Most of JavaMail functionalities are not hidden so it's easy to write subclasses that extend the use of these classes.

When you need to send multiple mails you don't create multiple instances of ERMailDelivery subclasses, you just create one instance and call the newMail () method to create a new message, this is useful for memory / performances issues.

I created these classes because there was a bug in WOMailDelivery in deployment mode that I cannot solve.  The other advantage is that the mails are sent within the process and JVM of the currently running application.


CONFIGURATION
- - - - - - -

Copy and customize the properties given in SampleConfiguration.txt under 'Support' group in the project files.


CODE SAMPLE
- - - - - - 

// Create an instance of an ERMailDelivery subclass
ERMailDeliveryHTML mail = new ERMailDeliveryHTML ();

// Here ERMailDeliveryHTML needs a WOComponent to render the HTML text content.
mail.setWOComponentContent (mailPage);

// Here you create a new instance of the message
// You can loop over this fragment of code, not forgetting to use newMail ()
// before you set the attributes of the message.
try {
    mail.newMail ();
    mail.setFromAddress    (emailFrom);
    mail.setReplyToAddress (emailReplyTo);
    mail.setSubject 	   (emailSubject);
    mail.setToAddresses    (new NSArray (toEmailAddresses));

    // Send the mail
    mail.sendMail ();
} catch (Exception e) {
    // handle the exception ...
}


CHANGES
- - - -

10/09/2002:
    Made API more consistent with Project Wonder.


09/19/2001:

    Packaged the framework into a WebObjects 5 framework.
    As long as ProjectBuilder for Mac OX X and ProjectBuilder for Windows are incompatible,
    the project can't be used under Windows, unless it is compiled under another platform.

    OFMaildelivery.setToAddress now takes an NSArray as argument. java.util.Vector is no
    longer used.


FEEDBACK
- - - - -

Don't hesitate to send {postcards, feedback, love letters} to Camille Troillard <tuscland@mac.com>