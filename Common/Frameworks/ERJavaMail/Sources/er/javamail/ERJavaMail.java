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

    protected static ERJavaMail sharedInstance;
    public static ERJavaMail sharedInstance () {
	if(sharedInstance == null) {
	    sharedInstance = (ERJavaMail)ERXFrameworkPrincipal.sharedInstance (ERJavaMail.class);
	}
	return sharedInstance;
    }

    // Mail Validation ivars
    protected static final String EMAIL_VALIDATION_PATTERN = "^[A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)*[@][A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)+$";
    protected Perl5Matcher _matcher;
    protected Pattern _pattern = null;

    public void finishInitialization () {
        if (log.isDebugEnabled ())
	    log.debug ("Initializing Framework.");

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
		// Admin Email
		String adminEmail = System.getProperty ("er.javamail.adminEmail");
		if ((adminEmail == null) || (adminEmail.length () == 0))
            throw new RuntimeException ("ERJavaMail: the property er.javamail.adminEmail is not specified!");
        this.setAdminEmail (adminEmail);
		log.debug ("er.javamail.adminEmail: " + _adminEmail);

		// JavaMail Debug Enabled ?
		boolean debug = ERXProperties.booleanForKey ("er.javamail.debugEnabled");
        this.setDebugEnabled (debug);
		log.debug ("er.javamail.debugEnabled: " + debug);

		// Centralize mails ?
        boolean centralize = ERXProperties.booleanForKey ("er.javamail.centralize");
		this.setCentralize (centralize);
		log.debug ("er.javamail.centralize: " + centralize);

		// Time to wait when sender if overflowed
		int milliswait = ERXProperties.intForKey ("er.javamail.milliSecondsWaitIfSenderOverflowed");
        if (milliswait > 1000)
			this.setMilliSecondsWaitIfSenderOverflowed (milliswait);
		log.debug ("er.javamail.milliSecondsWaitIfSenderOverflowed: " + milliswait);

		// Smtp host
		String smtpHost = System.getProperty ("er.javamail.smtpHost");
        if ((smtpHost == null) || (smtpHost.length () == 0))
            throw new RuntimeException ("ERJavaMail: You must specify a SMTP host for outgoing mail with the property 'er.javamail.smtpHost'");
		log.debug ("er.javamail.smtpHost: " + smtpHost);

		// With the smtp-host, we can setup the session
        Properties props = new Properties ();
        props.put ("mail.smtp.host", smtpHost);
        this.setDefaultProperties (props);
        javax.mail.Session session = javax.mail.Session.getDefaultInstance (this.defaultProperties (), null);
		this.setDefaultSession (session);

		// Default X-Mailer header
		this.setDefaultXMailerHeader (System.getProperty ("er.javamail.XMailerHeader"));
		log.debug ("er.javamail.smtpHost: " + smtpHost);
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
	if (this.isValidEmail (adminEmail))
	    _adminEmail = adminEmail;
    }

	
    protected boolean _debugEnabled = true;

    public boolean debugEnabled () {
        return _debugEnabled;
    }

    public void setDebugEnabled (boolean debug) {
        _debugEnabled = debug;
    }


	/** This property sets the default header for the X-Mailer property */
	protected String _defaultXMailerHeader = null;

	/**
	 * Gets the default X-Mailer header to use for
     * sending mails. Pulls the value out of the
     * property: er.javamail.XMailerHeader
     * @return default X-Mailer header
     */
	public String defaultXMailerHeader () {
		return _defaultXMailerHeader;
	}

	public void setDefaultXMailerHeader (String header) {
		_defaultXMailerHeader = header;
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
    /** Validates an enterprise object's attribute (accessed via key).
		@param object the object to be validated
		@param key the attribute's name
		@param email the email value
		@return the email if correct */
    public String validateEmail (EOEnterpriseObject object, String key, String email) {
        if (email != null) {
            if (!isValidEmail(email))
                throw ERXValidationFactory.defaultFactory ().createException (object, key, email, "malformedEmail");
        }

        return email;
    }

    /** @return true if the email is valid
	@param email the email value to validate */
    public boolean isValidEmail (String email) {
        if (email != null)
            return _matcher.matches (email, _pattern);
        return false;
    }

    /** Define a standard ERJavMail exception */
    public static class Exception extends java.lang.Exception {
		public Exception (String message) { super (message); }
    }
}
