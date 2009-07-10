package er.javamail;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.ERXLogger;
import er.extensions.ERXProperties;
import er.extensions.ERXValidationFactory;

/**
 * <code>ERJavaMail</code> is the prinicpal class for the ERJavaMail framework.
 *
 * @author <a href="mailto:tuscland@mac.com">Camille Troillard</a>
 * @author <a href="mailto:maxmuller@mac.com">Max Muller</a>
 * @version $Id$
 */
public class ERJavaMail extends ERXFrameworkPrincipal {

    /** Class logger */
    private static final ERXLogger log = ERXLogger.getERXLogger (ERJavaMail.class);

    static {
	setUpFrameworkPrincipalClass (ERJavaMail.class);
    }

    /**
     * ERJavaMail class singleton.
     */
    protected static ERJavaMail sharedInstance;

    /**
     * Accessor to the ERJavaMail singleton.
     *
     * @return the one <code>ERJavaMail</code> instance
     */
    public static ERJavaMail sharedInstance () {
	if(sharedInstance == null) {
	    sharedInstance = (ERJavaMail)ERXFrameworkPrincipal.sharedInstance (ERJavaMail.class);
	}
	return sharedInstance;
    }

    /**
     * <code>EMAIL_VALIDATION_PATTERN</code> is a regexp pattern that
     * is used to validate emails.
     */
    protected static final String EMAIL_VALIDATION_PATTERN =
        "^[A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)*[@][A-Za-z0-9_\\-]+([.][A-Za-z0-9_\\-]+)+$";

    /**
     * The Jakarta ORO regexp matcher.
     */
    protected Perl5Matcher _matcher;

    /**
     * The compiled form of the <code>EMAIL_VALIDATION_PATTERN</code>
     * pattern.
     */
    protected Pattern _pattern = null;
    
    private Delegate _delegate;
    
    public void setDelegate(Delegate delegate) {
		_delegate = delegate;
	}
    
    /**
     * Specialized implementation of the method from
     * ERXPrincipalClass.
     */
    public void finishInitialization () {
        if (log.isDebugEnabled ())
	    log.debug ("Initializing Framework.");

        Perl5Compiler compiler	= new Perl5Compiler ();
        _matcher = new Perl5Matcher ();

        try {
            _pattern = compiler.compile (EMAIL_VALIDATION_PATTERN);
        } catch (MalformedPatternException e) {
            throw new RuntimeException
                ("The compilation of the ORO Regexp pattern failed in ERJavaMail!");
        }

        this.initializeFrameworkFromSystemProperties ();
        log.info ("ERJavaMail: finished initialization");
    }

    /**
     * This method is used to initialize ERJavaMail from System
     * properties.  Later, we will implement a way to initialize
     * those properties everytime the propertis are changed.  The
     * observer will call this method whenever appropriate.
     */
    public void initializeFrameworkFromSystemProperties () {
        // Centralize mails ?
        boolean centralize = ERXProperties.booleanForKey ("er.javamail.centralize");
        this.setCentralize (centralize);
        log.debug ("er.javamail.centralize: " + centralize);

        // Admin Email
        String adminEmail = System.getProperty ("er.javamail.adminEmail");
        if (centralize && ((adminEmail == null) || (adminEmail.length () == 0)))
            throw new RuntimeException ("ERJavaMail: the property er.javamail.centralize is true, but er.javamail.adminEmail is not specified!");
        this.setAdminEmail (adminEmail);
        log.debug ("er.javamail.adminEmail: " + _adminEmail);

        // JavaMail Debug Enabled ?
        boolean debug = ERXProperties.booleanForKey ("er.javamail.debugEnabled");
        this.setDebugEnabled (debug);
        log.debug ("er.javamail.debugEnabled: " + debug);

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

    /**
     * Helper method to init the smtpHost property.  This method
     * first check is <code>er.javamail.smtpHost</code> is set.  If
     * it is not set, then it looks for <code>mail.smtp.host</code>
     * (standard JavaMail property) and finally the <code>WOSMTPHost</code> property.
     * When a correct property is
     * found, then it sets both properties to the found value.  If no
     * properties are found, a RuntimeException is thrown.
     * @throws RuntimeException if neither one of
     * <code>er.javamail.smtpHost</code>, <code>mail.smtp.host</code> or <code>WOSMTPHost</code> is set.
     */
    protected void setupSmtpHostSafely () {
        // Smtp host
        String smtpHost = System.getProperty ("er.javamail.smtpHost");
        if ((smtpHost == null) || (smtpHost.length () == 0)) {
            // Try to fail back to default java config
            smtpHost = System.getProperty ("mail.smtp.host");
    
            if ((smtpHost == null) || (smtpHost.length () == 0)) {
                // use the standard WO host
                smtpHost = System.getProperty ("WOSMTPHost");
                if ((smtpHost == null) || (smtpHost.length () == 0)) {
                    throw new RuntimeException ("ERJavaMail: You must specify a SMTP host for outgoing mail with the property 'er.javamail.smtpHost'");
                }
                // ... and then maybe actually do what the docs say this method is supposed to do
                System.setProperty ("mail.smtp.host", smtpHost);
                System.setProperty ("er.javamail.smtpHost", smtpHost);
            }
            else {
                System.setProperty ("er.javamail.smtpHost", smtpHost);
            }
        }
        else {
            System.setProperty ("mail.smtp.host", smtpHost);
        }

        log.debug ("er.javamail.smtpHost: " + smtpHost);
    }

    /**
     * This is the deafult JavaMail Session.  It is shared among all
     * deliverers for immediate deliveries.  Deferred deliverers, use
     * their own JavaMail session.
     */
    protected javax.mail.Session _defaultSession;
    
    private Map<String, javax.mail.Session> _sessions = new ConcurrentHashMap<String, javax.mail.Session>();

    /**
     * Sets the default JavaMail session to a particular value.  This
     * value is set by default at initialization of the framework but
     * you can specify a custom one by using this method.  Note that
     * a new deliverer need to be instanciated for changes to be
     * taken in account.
     *
     * @param session the default <code>javax.mail.Session</code>
     */
    public void setDefaultSession (javax.mail.Session session) {
        session.setDebug (this.debugEnabled ());
        _defaultSession = session;
    }

    /**
     * This is the deafult JavaMail Session accessor.  It is shared
     * among all deliverers for immediate deliveries.  Deferred
     * deliverers, use their own JavaMail session.
     *
     * @return the default <code>javax.mail.Session</code> instance
     */
    public javax.mail.Session defaultSession() {
        return _defaultSession;
    }

    /**
     * Returns a newly allocated Session object from the given
     * Properties
     * @param props a <code>Properties</code> value
     * @return a <code>javax.mail.Session</code> value initialized
     * from the given properties
     */
    public javax.mail.Session newSession(Properties props) {
        javax.mail.Session session = javax.mail.Session.getInstance (props);
        if (_delegate != null) {
        	_delegate.didCreateSession(session);
        }
        session.setDebug (this.debugEnabled ());
        return session;
    }

    /**
     * Returns a newly allocated Session object from the System
     * Properties
     * @return a <code>javax.mail.Session</code> value
     */
    public javax.mail.Session newSession() {
        return newSession(System.getProperties());
    }

    /**
     * Returns a newly allocated Session object for the given message.
     * 
     * @param message the message
     * @return a new <code>javax.mail.Session</code> value
     */
    public javax.mail.Session newSessionForMessage(ERMessage message) {
    	return newSessionForContext(message.contextString());
    }

    /**
     * Returns the Session object that is appropriate for the given message.
     * 
     * @return a <code>javax.mail.Session</code> value
     */
    public javax.mail.Session sessionForMessage(ERMessage message) {
    	return sessionForContext(message.contextString());
    }

    /**
     * Returns a new Session object that is appropriate for the given context.
     * 
     * @param contextString the message context
     * @return a new <code>javax.mail.Session</code> value
     */
    protected javax.mail.Session newSessionForContext(String contextString) {
    	javax.mail.Session session;
    	if (contextString == null || contextString.length() == 0) {
    		session = newSession(System.getProperties());
    	}
    	else {
			Properties sessionProperties = new Properties();
			sessionProperties.putAll(System.getProperties());
			sessionProperties.setProperty("mail.smtp.host", ERXProperties.stringForKeyWithDefault("er.javamail.smtpHost." + contextString, sessionProperties.getProperty("er.javamail.smtpHost")));
			session = newSession(sessionProperties);
    	}
		return session;
    }
    
    /**
     * Returns the Session object that is appropriate for the given context.
     * 
     * @param contextString the message context
     * @return a <code>javax.mail.Session</code> value
     */
    protected javax.mail.Session sessionForContext(String contextString) {
    	javax.mail.Session session;
    	if (contextString == null || contextString.length() == 0) {
    		session = defaultSession();
    	}
    	else {
	    	session = _sessions.get(contextString);
	    	if (session == null) {
	    		session = newSessionForContext(contextString);
	    		_sessions.put(contextString, session);
	    	}
    	}
    	return session;
    }

    /**
     * email address used when centralizeMails == true <BR>
     * Needed when debugging application so that mails are always
     * sent to only one destination.
     */
    protected String _adminEmail;

    /**
     * admin email accessor.  The admin email is the email address
     * where centralized mail go to.
     *
     * @return a <code>String</code> value
     */
    public String adminEmail () {
        return _adminEmail;
    }

    /**
     * Sets the admin email to another value.  This value is set at
     * initialization from the <code>er.javamail.adminEmail</code>
     * Property.
     *
     * @param adminEmail a <code>String</code> value
     */
    public void setAdminEmail (String adminEmail) {
	if (this.isValidEmail (adminEmail))
	    _adminEmail = adminEmail;
    }


    /** This property specify wether JavaMail is debug enabled or not. */
    protected boolean _debugEnabled = true;

    /**
     * Returns <code>true</code> if JavaMail is debug enabled.
     *
     * @return a <code>boolean</code> value
     */
    public boolean debugEnabled () {
        return _debugEnabled;
    }

    /**
     * Sets the debug mode of JavaMail.
     *
     * @param debug a <code>boolean</code> value sets JavaMail in
     * debug mode
     */
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

    /**
     * Sets the default value of the XMailer header used when sending
     * mails.
     *
     * @param header a <code>String</code> value
     */
    public void setDefaultXMailerHeader (String header) {
        _defaultXMailerHeader = header;
    }


    /** Used to send mail to adminEmail only.  Useful for debugging issues */
    protected boolean _centralize = true;

    /**
     * Centralize is used to send all the outbound email to a single
     * address which is useful when debugging.
     *
     * @return a <code>boolean</code> value
     */
    public boolean centralize () {
        return _centralize;
    }

    /**
     * Sets the value of the <code>er.javamail.centralize</code>
     * Property.
     *
     * @param centralize if the boolean value is true, then all the
     * outbound mails will be sent to <code>adminEmail</code> email
     * address.
     */
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

    /**
     * This method return the time spent waiting if the mail queue if
     * overflowed.  During that time, mails are sent and the queue
     * lowers.  When the duration is spent, and the queue is under
     * the overflow limit, the mails are being sent again.
     * @return an <code>int</code> value
     */
    public int milliSecondsWaitIfSenderOverflowed () {
        return _milliSecondsWaitIfSenderOverflowed;
    }

    /**
     * Sets the value of the
     * <code>er.javamail.milliSecondsWaitIfSenderOverflowed</code>
     * Property.
     *
     * @param value an <code>int</code> value in milli-seconds.
     */
    public void  setMilliSecondsWaitIfSenderOverflowed (int value) {
        _milliSecondsWaitIfSenderOverflowed = value;
    }


    /**
     * Validates an enterprise object's email attribute (accessed via
     * key).
     * @param object the object to be validated
     * @param key the attribute's name
     * @param email the email value
     * @return the email if the validation didn't failed
     */
    public String validateEmail (EOEnterpriseObject object, String key, String email) {
        if (email != null) {
            if (!this.isValidEmail(email))
                throw ERXValidationFactory.defaultFactory ().createException
                    (object, key, email, "malformedEmail");
        }

        return email;
    }

    /**
     * Predicate used to validate email well-formness.
     * @return true if the email is valid
     * @param email the email String value to validate
     * @return a <code>boolean</code> value
     */
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
    public boolean hasWhiteList () {
        return this.whiteListEmailAddressPatterns ().count () > 0;
    }

    /**
     * Determines if a black list has been specified
     * @return if the black list has any elements in it
     */    
    public boolean hasBlackList () {
        return this.blackListEmailAddressPatterns ().count () > 0;
    }

    /**
     * Gets the array of white list email address
     * patterns.
     * @return array of white list email address patterns
     */
    public NSArray whiteListEmailAddressPatterns () {
        if (whiteListEmailAddressPatterns == null) {
            whiteListEmailAddressPatterns =
            ERXProperties.arrayForKeyWithDefault
                ("er.javamail.WhiteListEmailAddressPatterns", NSArray.EmptyArray);
        }
        return whiteListEmailAddressPatterns;
    }

    /**
     * Gets the array of black list email address
     * patterns.
     * @return array of black list email address patterns
     */
    public NSArray blackListEmailAddressPatterns () {
        if (blakListEmailAddressPatterns == null) {
            blakListEmailAddressPatterns =
                ERXProperties.arrayForKeyWithDefault
                ("er.javamail.BlackListEmailAddressPatterns", NSArray.EmptyArray);
        }
        return blakListEmailAddressPatterns;
    }

    /**
     * Whilte list Or qualifier to match any of the
     * patterns in the white list.
     * @return Or qualifier for the white list
     */
    public EOOrQualifier whiteListQualifier () {
        if (whiteListQualifier == null) {
            whiteListQualifier =
                this.qualifierArrayForEmailPatterns (this.whiteListEmailAddressPatterns ());
        }
        return whiteListQualifier;
    }

    /**
     * Gets the Or qualifier to match any of the patterns
     * in the black list.
     * @return or qualifier
     */
    public EOOrQualifier blackListQualifier () {
        if (blackListQualifier == null) {
            blackListQualifier =
                this.qualifierArrayForEmailPatterns (this.blackListEmailAddressPatterns ());
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
    protected EOOrQualifier qualifierArrayForEmailPatterns (NSArray emailPatterns) {
        NSMutableArray patternQualifiers = new NSMutableArray ();
        for (Enumeration patternEnumerator = emailPatterns.objectEnumerator();
                 patternEnumerator.hasMoreElements();) {
            String pattern = (String) patternEnumerator.nextElement ();
            patternQualifiers.addObject
                (EOQualifier.qualifierWithQualifierFormat
                 ("toString caseInsensitiveLike '" + pattern + "'", null));
        }
        return new EOOrQualifier (patternQualifiers);
    }

    /**
     * Filters an array of email addresses by the black and white
     * lists.
     * @param emailAddresses array of email addresses to be filtered
     * @return array of filtered email addresses
     */
    public NSArray filterEmailAddresses (NSArray emailAddresses) {
        NSMutableArray filteredAddresses = null;
        if ((emailAddresses != null) &&
            (emailAddresses.count () > 0) &&
            (this.hasWhiteList () || this.hasBlackList ())) {
            filteredAddresses = new NSMutableArray (emailAddresses);

            if (log.isDebugEnabled ()) {
                log.debug ("Filtering email addresses: " + filteredAddresses);
            }
            
            if (this.hasWhiteList ()) {
                EOQualifier.filterArrayWithQualifier (filteredAddresses,
                                                      whiteListQualifier ());
                if (log.isDebugEnabled ()) {
                    log.debug ("White list qualifier: " + whiteListQualifier ()
                               + " after filtering: " + filteredAddresses);
                }                    
            }

            if (this.hasBlackList ()) {
                NSArray filteredOutAddresses =
                    EOQualifier.filteredArrayWithQualifier (filteredAddresses,
                                                            blackListQualifier ());
                if (filteredOutAddresses.count () > 0)
                    filteredAddresses.removeObjectsInArray (filteredOutAddresses);
                if (log.isDebugEnabled ()) {
                    log.debug ("Black list qualifier: " + blackListQualifier ()
                               + " filtering: " + filteredAddresses);
                }                
            }
        }

        return (filteredAddresses != null) ?
            filteredAddresses.immutableClone () :
            emailAddresses;
    }
    
    public static interface Delegate {
    	public void didCreateSession(javax.mail.Session session);
    }
}
