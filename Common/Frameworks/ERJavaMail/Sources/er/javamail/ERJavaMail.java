/*
  $Id$

  ERJavaMail.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOEnterpriseObject;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.ERXLogger;
import er.extensions.ERXProperties;
import er.extensions.ERXValidationFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import java.util.Enumeration;
import java.util.Properties;

public class ERJavaMail extends ERXFrameworkPrincipal {

    private static final ERXLogger log = ERXLogger.getERXLogger (ERJavaMail.class);

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
    protected static final String EMAIL_VALIDATION_PATTERN =
        "^[A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)*[@][A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)+$";
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

         // Number of messages that the sender queue can hold at a time
        int queueSize = ERXProperties.intForKey ("er.javamail.senderQueue.size");
        if (queueSize >= 1)
            this.setSenderQueueSize (queueSize);
        log.debug ("er.javamail.senderQueue.size: " + queueSize);

       // Time to wait when sender if overflowed
        int milliswait = ERXProperties.intForKey ("er.javamail.milliSecondsWaitIfSenderOverflowed");
        if (milliswait > 1000)
            this.setMilliSecondsWaitIfSenderOverflowed (milliswait);
        log.debug ("er.javamail.milliSecondsWaitIfSenderOverflowed: " + milliswait);

        // Smtp host
        this.setupSmtpHostSafely ();

        this.setDefaultSession (this.newSession ());

        if (this.defaultSession () == null)
            log.warn("Unable to create default mail session!");
        
        // Default X-Mailer header
        this.setDefaultXMailerHeader (System.getProperty ("er.javamail.XMailerHeader"));
        log.debug ("er.javamail.XMailHeader: " + this.defaultXMailerHeader ());
    }

    protected void setupSmtpHostSafely () {
        // Smtp host
        String smtpHost = System.getProperty ("er.javamail.smtpHost");
        if ((smtpHost == null) || (smtpHost.length () == 0)) {
            // Try to fail back to default java config
            smtpHost = System.getProperty ("mail.smtp.host");
    
            if ((smtpHost == null) || (smtpHost.length () == 0)) {
                throw new RuntimeException ("ERJavaMail: You must specify a SMTP host for outgoing mail with the property 'er.javamail.smtpHost'");
            } else
                System.setProperty ("er.javamail.smtpHost", smtpHost);
        } else
            System.setProperty ("mail.smtp.host", smtpHost);

        log.debug ("er.javamail.smtpHost: " + smtpHost);
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

    /** Return a newly allocated Session object from the System Properties */
    public javax.mail.Session newSession () {
        return javax.mail.Session.getInstance (System.getProperties ());
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

    /** 
     * Number of messages that the sender queue can hold at a time; 
     * default to 50 messages and can be configured by 
     * <code>er.javamail.senderQueue.size</code> system property. 
     */
    protected int _senderQueueSize = 50; 
    
    public int senderQueueSize () {
        return _senderQueueSize;
    }
    
    public void setSenderQueueSize (int value) {
        _senderQueueSize = value;
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
            if (!this.isValidEmail(email))
                throw ERXValidationFactory.defaultFactory ().createException
                    (object, key, email, "malformedEmail");
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

    //	===========================================================================
    //	Black and White list email address filtering support
    //	---------------------------------------------------------------------------    
    
    /** holds the array of white list email addresses */
    protected NSArray whiteListEmailAddressPatterns;

    /** holds the array of black list email addresses */
    protected NSArray blakListEmailAddressPatterns;

    /** holds the white list qualifier */
    protected EOOrQualifier whiteListQualifier;

    /** holds the black list qualifier */
    protected EOOrQualifier blackListQualifier;
    
    /**
     * Determines if a white list has been specified
     * @return if the white list has any elements in it
     */
    public boolean hasWhiteList() {
        return whiteListEmailAddressPatterns().count() > 0;
    }

    /**
     * Determines if a black list has been specified
     * @return if the black list has any elements in it
     */    
    public boolean hasBlackList() {
        return blackListEmailAddressPatterns().count() > 0;
    }

    /**
     * Gets the array of white list email address
     * patterns.
     * @return array of white list email address patterns
     */
    public NSArray whiteListEmailAddressPatterns() {
        if (whiteListEmailAddressPatterns == null) {
            whiteListEmailAddressPatterns =
            ERXProperties.arrayForKeyWithDefault("er.javamail.WhiteListEmailAddressPatterns", NSArray.EmptyArray);
        }
        return whiteListEmailAddressPatterns;
    }

    /**
     * Gets the array of black list email address
     * patterns.
     * @return array of black list email address patterns
     */
    public NSArray blackListEmailAddressPatterns() {
        if (blakListEmailAddressPatterns == null) {
            blakListEmailAddressPatterns =
            ERXProperties.arrayForKeyWithDefault("er.javamail.BlackListEmailAddressPatterns", NSArray.EmptyArray);
        }
        return blakListEmailAddressPatterns;
    }

    /**
     * Whilte list Or qualifier to match any of the
     * patterns in the white list.
     * @return Or qualifier for the white list
     */
    public EOOrQualifier whiteListQualifier() {
        if (whiteListQualifier == null) {
            whiteListQualifier = qualifierArrayForEmailPatterns(whiteListEmailAddressPatterns());
        }
        return whiteListQualifier;
    }

    /**
     * Gets the Or qualifier to match any of the patterns
     * in the black list.
     * @return or qualifier
     */
    public EOOrQualifier blackListQualifier() {
        if (blackListQualifier == null) {
            blackListQualifier = qualifierArrayForEmailPatterns(blackListEmailAddressPatterns());
        }
        return blackListQualifier;
    }

    /**
     * Constructs an Or qualifier for filtering an array of
     * strings that might have the * wildcard character.
     * Will be nice when we have regex in Java 1.4.
     * @param emailPatterns array of email patterns
     * @return or qualifier to match any of the given patterns
     */
    protected EOOrQualifier qualifierArrayForEmailPatterns(NSArray emailPatterns) {
        NSMutableArray patternQualifiers = new NSMutableArray();
        for (Enumeration patternEnumerator = emailPatterns.objectEnumerator(); patternEnumerator.hasMoreElements();) {
            String pattern = (String)patternEnumerator.nextElement();
            patternQualifiers.addObject(EOQualifier.qualifierWithQualifierFormat("toString caseInsensitiveLike '" + pattern + "'", null));
        }
        return new EOOrQualifier(patternQualifiers);
    }

    /**
     * Filters an array of email addresses by the black and white
     * lists.
     * @param emailAddresses array of email addresses to be filtered
     * @return array of filtered email addresses
     */
    public NSArray filterEmailAddresses(NSArray emailAddresses) {
        NSMutableArray filteredAddresses = null;
        if (emailAddresses != null && emailAddresses.count() > 0 && (hasWhiteList() || hasBlackList())) {
            filteredAddresses = new NSMutableArray(emailAddresses);
            if (hasWhiteList()) {
                EOQualifier.filterArrayWithQualifier(filteredAddresses,
                                                    whiteListQualifier());
            }
            if (hasBlackList()) {
                NSArray filteredOutAddresses = EOQualifier.filteredArrayWithQualifier(filteredAddresses,
                                                                                     blackListQualifier());
                if (filteredOutAddresses.count() > 0)
                    filteredAddresses.removeObjectsInArray(filteredOutAddresses);
            }
        }
        return filteredAddresses != null ? filteredAddresses.immutableClone() : emailAddresses;
    }
}
