/*
 $Id$

 ERJavaMail.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import er.extensions.*;
import java.util.*;
import org.apache.oro.text.regex.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.EOEnterpriseObject;

public class ERJavaMail extends ERXFrameworkPrincipal {

    public static final ERXLogger log = ERXLogger.getERXLogger (ERJavaMail.class);

    static {
        setUpFrameworkPrincipalClass (ERJavaMail.class);
    }

    static ERJavaMail sharedInstance;
    public static ERJavaMail sharedInstance () {
        if(sharedInstance == null) {
            sharedInstance = (ERJavaMail)ERXFrameworkPrincipal.sharedInstance(ERJavaMail.class);
        }
        return sharedInstance;
    }

    // Mail Validation ivars
    static final String EMAIL_VALIDATION_PATTERN = "^[A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)*[@][A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)+$";
    Perl5Matcher _matcher;
    Pattern _pattern = null;

    public void finishInitialization () {
        Perl5Compiler compiler	= new Perl5Compiler ();
        _matcher = new Perl5Matcher ();
        try {
            _pattern = compiler.compile (EMAIL_VALIDATION_PATTERN);
        } catch (MalformedPatternException e) {
            throw new RuntimeException ("The compilation of the ORO Regexp pattern failed in ERJavaMail!");
        }

        this.initializeFrameworkFromSystemProperties ();
        log.info ("ERJavaMail: finished initialization");
    }

    public void initializeFrameworkFromSystemProperties () {
        if (log.isDebugEnabled ())
            log.debug ("Initializing Framework.");

        String adminEmail = ERXProperties.stringForKey ("er.javamail.adminEmail");
        if ((adminEmail == null) || (adminEmail.length () == 0))
            throw new RuntimeException ("ERJavaMail: the property er.javamail.adminEmail is not specified!");
        this.setAdminEmail (adminEmail);

        boolean debug = ERXProperties.booleanForKey ("er.javamail.debugEnabled");
        this.setDebugEnabled (debug);

        boolean centralize = ERXProperties.booleanForKey ("er.javamail.centralize");
        this.setCentralize (centralize);

        int milliswait = ERXProperties.intForKey ("er.javamail.milliSecondsWaitIfSenderOverflowed");
        if (milliswait > 1000)
            this.setMilliSecondsWaitIfSenderOverflowed (milliswait);

        // Finish intialization
        String smtpHost = ERXProperties.stringForKey ("er.javamail.smtpHost");
        if ((smtpHost == null) || (smtpHost.length () == 0))
            throw new RuntimeException ("ERJavaMail: You must specify a SMTP host for outgoing mail with the property 'er.javamail.smtpHost'");
        Properties props = new Properties ();
        props.put ("mail.smtp.host", smtpHost);
        this.setDefaultProperties (props);

        javax.mail.Session session = javax.mail.Session.getDefaultInstance (this.defaultProperties (), null);
        this.setDefaultSession (session);
    }

    protected Properties _defaultProperties;
    public void setDefaultProperties (Properties javaMailProperties) {
        _defaultProperties = javaMailProperties;
    }
    public Properties defaultProperties () {
        return _defaultProperties;
    }

    protected javax.mail.Session _defaultSession;
    public void setDefaultSession (javax.mail.Session session) {
        session.setDebug (this.debugEnabled ());
        _defaultSession = session;
    }
    public javax.mail.Session defaultSession () {
        return _defaultSession;
    }
    
    /** Return a newly allocated Session object from the given Properties */
    public javax.mail.Session newSession (Properties props) {
        javax.mail.Session session = javax.mail.Session.getInstance (props);
        session.setDebug (this.debugEnabled ());
        return session;
    }
    /** Return a newly allocated Session object from the default Properties */
    public javax.mail.Session newSession () {
        return javax.mail.Session.getInstance (this.defaultProperties ());
    }

    /** email address when centralizeMails == true <BR>
        Needed when debugging application so that mails are always sent to only one destination */
    protected String _adminEmail;
    public String adminEmail () {
        return _adminEmail;
    }
    public void setAdminEmail (String adminEmail) {
        _adminEmail = ERJavaMail.sharedInstance ().validateEmail (null, null, adminEmail);
    }

    protected boolean _debugEnabled = true;
    public boolean debugEnabled () {
        return _debugEnabled;
    }
    public void setDebugEnabled (boolean debug) {
        _debugEnabled = debug;
    }

    /** Used to send mail to adminEmail only.  Useful for debugging issues */
    protected boolean _centralize = true;
    public boolean centralize () {
        return _centralize;
    }
    public void setCentralize (boolean centralize) {
        _centralize = centralize;
    }
    /** Wait n milliseconds (by default this value is 6000) if the mail sender is overflowed */
    protected int _milliSecondsWaitIfSenderOverflowed = 6000;
    public int milliSecondsWaitIfSenderOverflowed () {
        return _milliSecondsWaitIfSenderOverflowed;
    }
    public void  setMilliSecondsWaitIfSenderOverflowed (int value) {
        _milliSecondsWaitIfSenderOverflowed = value;
    }

    // MAIL VALIDATION
    public String validateEmail (EOEnterpriseObject object, String key, String email) {
        if (email != null) {
            if (!isValidEmail(email))
                throw ERXValidationFactory.defaultFactory ().createException (object, key, email, "malformedEmail");
        }

        return email;
    }
    public boolean isValidEmail (String email) {
        if (email != null) {
            return _matcher.matches (email, _pattern);
        }
        return false;
    }
}
