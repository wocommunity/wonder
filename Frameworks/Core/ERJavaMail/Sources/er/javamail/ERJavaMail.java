package er.javamail;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXProperties;
import er.extensions.validation.ERXValidationFactory;

/**
 * ERJavaMail is the principal class for the ERJavaMail framework.
 * 
 * @property er.javamail.centralize
 * @property er.javamail.adminEmail
 * @property er.javamail.debugEnabled
 * @property er.javamail.senderQueue.size
 * @property er.javamail.milliSecondsWaitIfSenderOverflowed
 * @property er.javamail.XMailerHeader
 * @property er.javamail.smtpProtocol
 * @property er.javamail.smtpHost
 * @property mail.[smtpProtocol].host
 * @property WOSMTPHost
 * @property er.javamail.smtpPort
 * @property mail.[smtpProtocol].port
 * @property er.javamail.smtpAuth
 * @property mail.[smtpProtocol].auth
 * @property er.javamail.smtpUser
 * @property mail.[smtpProtocol].user
 * @property er.javamail.smtpPassword
 * @property mail.[smtpProtocol].password
 * @property mail.smtps.socketFactory.fallback
 * @property er.javamail.emailPattern
 * @property er.javamail.WhiteListEmailAddressPatterns
 * @property er.javamail.BlackListEmailAddressPatterns
 *
 * @author <a href="mailto:tuscland@mac.com">Camille Troillard</a>
 * @author <a href="mailto:maxmuller@mac.com">Max Muller</a>
 */
public class ERJavaMail extends ERXFrameworkPrincipal {

	public final static Class<?> REQUIRES[] = new Class[] { ERXExtensions.class };

	/** Class logger */
	private static final Logger log = Logger.getLogger(ERJavaMail.class);

	static {
		setUpFrameworkPrincipalClass(ERJavaMail.class);
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
	public static synchronized ERJavaMail sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = ERXFrameworkPrincipal.sharedInstance(ERJavaMail.class);
		}
		return sharedInstance;
	}

	/**
	 * <code>EMAIL_VALIDATION_PATTERN</code> is a regexp pattern that is used to validate emails.
	 */
	// RFC 2822 token definitions for valid email - only used together to form a java Pattern object:
	private static final String sp = "!#$%&'*+\\-/=?^_`{|}~";
	private static final String atext = "[a-zA-Z0-9" + sp + "]";
	private static final String atom = atext + "+"; // one or more atext chars
	private static final String dotAtom = "\\." + atom;
	private static final String localPart = atom + "(" + dotAtom + ")*"; // one atom followed by 0 or more dotAtoms.
	// RFC 1035 tokens for domain names:
	private static final String letter = "[a-zA-Z]";
	private static final String letDig = "[a-zA-Z0-9]";
	private static final String letDigHyp = "[a-zA-Z0-9\\-]";
	private static final String rfcLabel = letDig + "(" + letDigHyp + "{0,61}" + letDig + "){0,1}";
	private static final String domain = rfcLabel + "((\\." + rfcLabel + ")*\\." + letter + "{2,6}){0,1}";
	// Combined together, these form the allowed email regexp allowed by RFC 2822:
	private static final String EMAIL_VALIDATION_PATTERN = "^" + localPart + "@" + domain + "$";

	/**
	 * The compiled form of the <code>EMAIL_VALIDATION_PATTERN</code> pattern.
	 */
	protected Pattern _pattern = null;

	private Delegate _delegate;
	
	public void setDelegate(Delegate delegate) {
		_delegate = delegate;
	}

	/**
	 * Specialized implementation of the method from ERXPrincipalClass.
	 */
	@Override
	public void finishInitialization() {
		initializeFrameworkFromSystemProperties();
	}

	/**
	 * This method is used to initialize ERJavaMail from System properties. Later, we will implement a way to initialize
	 * those properties everytime the propertis are changed. The observer will call this method whenever appropriate.
	 */
	public void initializeFrameworkFromSystemProperties() {
		// Centralize mails ?
		boolean centralize = ERXProperties.booleanForKey("er.javamail.centralize");
		this.setCentralize(centralize);
		log.debug("er.javamail.centralize: " + centralize);

		String adminEmail = System.getProperty("er.javamail.adminEmail");
		if (isValidEmail(adminEmail)) {
			this.setAdminEmail(adminEmail);
			log.debug("er.javamail.adminEmail: " + _adminEmail);
		}
		else if (centralize) {
			throw new IllegalArgumentException("When 'er.javamail.centralize' is true (default)," + " all outgoing mails will get sent to 'er.javamail.adminEmail'" + " instead of the normal TO addresses, but you did not provide a valid email for that property.");
		}

		// JavaMail Debug Enabled ?
		boolean debug = ERXProperties.booleanForKey("er.javamail.debugEnabled");
		this.setDebugEnabled(debug);
		log.debug("er.javamail.debugEnabled: " + debug);

		// Number of messages that the sender queue can hold at a time
		int queueSize = ERXProperties.intForKey("er.javamail.senderQueue.size");
		if (queueSize >= 1)
			this.setSenderQueueSize(queueSize);
		log.debug("er.javamail.senderQueue.size: " + queueSize);

		// Time to wait when sender if overflowed
		int milliswait = ERXProperties.intForKey("er.javamail.milliSecondsWaitIfSenderOverflowed");
		if (milliswait > 1000)
			this.setMilliSecondsWaitIfSenderOverflowed(milliswait);
		log.debug("er.javamail.milliSecondsWaitIfSenderOverflowed: " + milliswait);

		// Smtp host
		this.setupSmtpHostSafely();

		this.setDefaultSession(this.newSession());

		if (this.defaultSession() == null)
			log.warn("Unable to create default mail session!");

		// Default X-Mailer header
		this.setDefaultXMailerHeader(System.getProperty("er.javamail.XMailerHeader"));
		log.debug("er.javamail.XMailHeader: " + this.defaultXMailerHeader());
	}

	/**
	 * Helper method to init the smtpHost property. This method first check is <code>er.javamail.smtpHost</code> is
	 * set. If it is not set, then it looks for <code>mail.smtp.host</code> (standard JavaMail property) and finally
	 * the <code>WOSMTPHost</code> property. When a correct property is found, then it sets both properties to the
	 * found value. If no properties are found, a RuntimeException is thrown.
	 * 
	 * @throws RuntimeException
	 *             if neither one of <code>er.javamail.smtpHost</code>, <code>mail.smtp.host</code> or
	 *             <code>WOSMTPHost</code> is set.
	 */
	protected void setupSmtpHostSafely() {
		setupSmtpProperties(System.getProperties(), null);
	}
	
	protected void setupSmtpProperties(Properties properties, String contextString) {
		String contextSuffix = contextString == null ? "" : ("." + contextString);
		
		// Smtp host
		String smtpProtocol = smtpProtocolForContext(contextString);

		String smtpHost = ERXProperties.stringForKeyWithDefault("er.javamail.smtpHost" + contextSuffix, ERXProperties.stringForKey("er.javamail.smtpHost"));
		if ((smtpHost == null) || (smtpHost.length() == 0)) {
			// Try to fail back to default java config
			smtpHost = ERXProperties.stringForKey("mail." + smtpProtocol + ".host");

			if ((smtpHost == null) || (smtpHost.length() == 0)) {
				// use the standard WO host
				smtpHost = ERXProperties.stringForKey("WOSMTPHost");
				if ((smtpHost == null) || (smtpHost.length() == 0)) {
					throw new RuntimeException("ERJavaMail: You must specify a SMTP host for outgoing mail with the property 'er.javamail.smtpHost'");
				}
                // ... and then maybe actually do what the docs say this method is supposed to do
				properties.setProperty("mail." + smtpProtocol + ".host", smtpHost);
				properties.setProperty("er.javamail.smtpHost", smtpHost);
			}
			else {
				properties.setProperty("er.javamail.smtpHost", smtpHost);
			}
		}
		else {
			properties.setProperty("mail." + smtpProtocol + ".host", smtpHost);
		}
		log.debug("er.javamail.smtpHost: " + smtpHost);
		
		String port = ERXProperties.stringForKeyWithDefault("er.javamail.smtpPort" + contextSuffix, ERXProperties.stringForKey("er.javamail.smtpPost"));
		if (port != null && port.length() > 0) {
			properties.setProperty("mail." + smtpProtocol + ".port" , port);
			log.debug("ERJavaMail will use smtp port: " + port);
		}
		
		boolean smtpAuth = ERXProperties.booleanForKeyWithDefault("er.javamail.smtpAuth" + contextSuffix, ERXProperties.booleanForKey("er.javamail.smtpAuth"));
		log.debug("ERJavaMail will use authenticated SMTP connections.");
		if (smtpAuth) {
			properties.setProperty("mail." + smtpProtocol + ".auth", String.valueOf(smtpAuth));
			String user = ERXProperties.stringForKeyWithDefault("er.javamail.smtpUser" + contextSuffix, ERXProperties.stringForKey("er.javamail.smtpUser"));
			if (user == null || user.length() == 0) {
				throw new RuntimeException("You specified er.javamail.smtpAuth=true, but you didn't specify an er.javamail.smtpUser to use as the login name.");
			}
			properties.setProperty("mail." + smtpProtocol + ".user", user);
			String password = ERXProperties.stringForKeyWithDefault("er.javamail.smtpPassword" + contextSuffix, ERXProperties.stringForKey("er.javamail.smtpPassword"));
			if (password == null || password.length() == 0) {
				log.warn("You specified er.javamail.smtpAuth=true, but you didn't set er.javamail.smtpPassword for the " + user + " mail user.");
			}
			if (password != null) {
				properties.setProperty("mail." + smtpProtocol + ".password", password);
			}
		}
		if ("smtps".equals(smtpProtocol)) {
			properties.setProperty("mail.smtps.socketFactory.fallback", "false");
		}
	}

	/**
	 * This is the default JavaMail Session. It is shared among all deliverers for immediate deliveries. Deferred
	 * deliverers, use their own JavaMail session.
	 */
	protected javax.mail.Session _defaultSession;
	private final Map<String, javax.mail.Session> _sessions = new ConcurrentHashMap<String, javax.mail.Session>();

	/**
	 * Sets the default JavaMail session to a particular value. This value is set by default at initialization of the
	 * framework but you can specify a custom one by using this method. Note that a new deliverer need to be
	 * instanciated for changes to be taken in account.
	 * 
	 * @param session
	 *            the default <code>javax.mail.Session</code>
	 */
	public void setDefaultSession(javax.mail.Session session) {
		session.setDebug(this.debugEnabled());
		_defaultSession = session;
	}

	/**
	 * This is the deafult JavaMail Session accessor. It is shared among all deliverers for immediate deliveries.
	 * Deferred deliverers, use their own JavaMail session.
	 * 
	 * @return the default <code>javax.mail.Session</code> instance
	 */
	public javax.mail.Session defaultSession() {
		return _defaultSession;
	}
	
	/**
	 * Returns a newly allocated Session object from the given Properties
	 * 
	 * @param props
	 *            a <code>Properties</code> value
	 * @return a <code>javax.mail.Session</code> value initialized from the given properties
	 */
	public javax.mail.Session newSession(Properties props) {
		return newSessionForContext(props, null);
	}

	/**
	 * Returns a newly allocated Session object from the System Properties
	 * 
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
			session = newSessionForContext(System.getProperties(), contextString);
		}
		else {
			Properties sessionProperties = new Properties();
			sessionProperties.putAll(System.getProperties());
			setupSmtpProperties(sessionProperties, contextString);
			session = newSessionForContext(sessionProperties, contextString);
		}
		return session;
	}

	/**
	 * Returns a newly allocated Session object from the given Properties
	 * 
	 * @param properties
	 *            a <code>Properties</code> value
	 * @return a <code>javax.mail.Session</code> value initialized from the given properties
	 */
	public javax.mail.Session newSessionForContext(Properties properties, String contextString) {
		if (_delegate != null) {
			_delegate.willCreateSessionWithPropertiesForContext(properties, contextString);
		}
		javax.mail.Session session = javax.mail.Session.getInstance(properties);
		if (_delegate != null) {
			_delegate.didCreateSession(session);
		}
		session.setDebug(this.debugEnabled());
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
	 * Needed when debugging application so that mails are always sent to only one destination.
	 */
	protected String _adminEmail;

	/**
	 * admin email accessor. The admin email is the email address where centralized mail go to.
	 * 
	 * @return a <code>String</code> value
	 */
	public String adminEmail() {
		return _adminEmail;
	}

	/**
	 * Sets the admin email to another value. This value is set at initialization from the
	 * <code>er.javamail.adminEmail</code> Property.
	 * 
	 * @param adminEmail
	 *            a <code>String</code> value
	 */
	public void setAdminEmail(String adminEmail) {
		if (!(isValidEmail(adminEmail) || (adminEmail != null && adminEmail.trim().length() > 0))) {
			throw new IllegalArgumentException("You specified an invalid admin email address '" + adminEmail + "'.");
		}
		_adminEmail = adminEmail;
	}

	/** This property specify wether JavaMail is debug enabled or not. */
	protected boolean _debugEnabled = true;

	/**
	 * Returns <code>true</code> if JavaMail is debug enabled.
	 * 
	 * @return a <code>boolean</code> value
	 */
	public boolean debugEnabled() {
		return _debugEnabled;
	}

	/**
	 * Sets the debug mode of JavaMail.
	 * 
	 * @param debug
	 *            a <code>boolean</code> value sets JavaMail in debug mode
	 */
	public void setDebugEnabled(boolean debug) {
		_debugEnabled = debug;
	}

	/** This property sets the default header for the X-Mailer property */
	protected String _defaultXMailerHeader = null;

	/**
	 * Gets the default X-Mailer header to use for sending mails. Pulls the value out of the property:
	 * er.javamail.XMailerHeader
	 * 
	 * @return default X-Mailer header
	 */
	public String defaultXMailerHeader() {
		return _defaultXMailerHeader;
	}

	/**
	 * Sets the default value of the XMailer header used when sending mails.
	 * 
	 * @param header
	 *            a <code>String</code> value
	 */
	public void setDefaultXMailerHeader(String header) {
		_defaultXMailerHeader = header;
	}

	/** Used to send mail to adminEmail only. Useful for debugging issues */
	protected boolean _centralize = true;

	/**
	 * Centralize is used to send all the outbound email to a single address which is useful when debugging.
	 * 
	 * @return a <code>boolean</code> value
	 */
	public boolean centralize() {
		return _centralize;
	}

	/**
	 * Sets the value of the <code>er.javamail.centralize</code> Property.
	 * 
	 * @param centralize
	 *            if the boolean value is true, then all the outbound mails will be sent to <code>adminEmail</code>
	 *            email address.
	 */
	public void setCentralize(boolean centralize) {
		_centralize = centralize;
	}

	/**
	 * Returns the SMTP protocol to use for connections.
	 */
	public String smtpProtocolForContext(String contextString) {
		String contextSuffix = (contextString == null) ? "" : ("." + contextString);
		return ERXProperties.stringForKeyWithDefault("er.javamail.smtpProtocol" + contextSuffix, ERXProperties.stringForKeyWithDefault("er.javamail.smtpProtocol", ERXProperties.stringForKeyWithDefault("mail.smtp.protocol", "smtp")));
	}

	/**
	 * Number of messages that the sender queue can hold at a time; default to 50 messages and can be configured by
	 * <code>er.javamail.senderQueue.size</code> system property.
	 */
	protected int _senderQueueSize = 50;

	public int senderQueueSize() {
		return _senderQueueSize;
	}

	public void setSenderQueueSize(int value) {
		_senderQueueSize = value;
	}

	/** Wait n milliseconds (by default this value is 6000) if the mail sender is overflowed */
	protected int _milliSecondsWaitIfSenderOverflowed = 6000;

	/**
	 * This method return the time spent waiting if the mail queue if overflowed. During that time, mails are sent and
	 * the queue lowers. When the duration is spent, and the queue is under the overflow limit, the mails are being sent
	 * again.
	 * 
	 * @return an <code>int</code> value
	 */
	public int milliSecondsWaitIfSenderOverflowed() {
		return _milliSecondsWaitIfSenderOverflowed;
	}

	/**
	 * Sets the value of the <code>er.javamail.milliSecondsWaitIfSenderOverflowed</code> Property.
	 * 
	 * @param value
	 *            an <code>int</code> value in milli-seconds.
	 */
	public void setMilliSecondsWaitIfSenderOverflowed(int value) {
		_milliSecondsWaitIfSenderOverflowed = value;
	}

	/**
	 * Validates an enterprise object's email attribute (accessed via key).
	 * 
	 * @param object
	 *            the object to be validated
	 * @param key
	 *            the attribute's name
	 * @param email
	 *            the email value
	 * @return the email if the validation didn't failed
	 */
	public String validateEmail(EOEnterpriseObject object, String key, String email) {
		if (email != null) {
			if (!this.isValidEmail(email))
				throw ERXValidationFactory.defaultFactory().createException(object, key, email, "malformedEmail");
		}

		return email;
	}

	/**
	 * Predicate used to validate email well-formness.
	 * 
	 * @return true if the email is valid
	 * @param email
	 *            the email String value to validate
	 * @return a <code>boolean</code> value
	 */
	public synchronized boolean isValidEmail(String email) {
		if (_pattern == null) {
			String patternString = ERXProperties.stringForKey("er.javamail.emailPattern");
			if(patternString == null || patternString.trim().length() == 0) {
				patternString = EMAIL_VALIDATION_PATTERN;
			}

			try {
				_pattern = Pattern.compile(patternString);
			}
			catch (PatternSyntaxException e) {
				throw new RuntimeException("The compilation of the email pattern '" + patternString + "' failed.", e);
			}
		}
		if (email != null) {
			return _pattern.matcher(email).matches();
		}
		return false;
	}

	// ===========================================================================
	// Black and White list email address filtering support
	// ---------------------------------------------------------------------------

	/** holds the array of white list email addresses */
	protected NSArray<String> whiteListEmailAddressPatterns;

	/** holds the array of black list email addresses */
	protected NSArray<String> blakListEmailAddressPatterns;

	/** holds the white list qualifier */
	protected EOOrQualifier whiteListQualifier;

	/** holds the black list qualifier */
	protected EOOrQualifier blackListQualifier;

	/**
	 * Determines if a white list has been specified
	 * 
	 * @return if the white list has any elements in it
	 */
	public boolean hasWhiteList() {
		return this.whiteListEmailAddressPatterns().count() > 0;
	}

	/**
	 * Determines if a black list has been specified
	 * 
	 * @return if the black list has any elements in it
	 */
	public boolean hasBlackList() {
		return this.blackListEmailAddressPatterns().count() > 0;
	}

	/**
	 * Gets the array of white list email address patterns.
	 * 
	 * @return array of white list email address patterns
	 */
	@SuppressWarnings("unchecked")
	public NSArray<String> whiteListEmailAddressPatterns() {
		if (whiteListEmailAddressPatterns == null) {
			whiteListEmailAddressPatterns = ERXProperties.arrayForKeyWithDefault("er.javamail.WhiteListEmailAddressPatterns", NSArray.EmptyArray);
		}
		return whiteListEmailAddressPatterns;
	}

	/**
	 * Gets the array of black list email address patterns.
	 * 
	 * @return array of black list email address patterns
	 */
	@SuppressWarnings("unchecked")
	public NSArray<String> blackListEmailAddressPatterns() {
		if (blakListEmailAddressPatterns == null) {
			blakListEmailAddressPatterns = ERXProperties.arrayForKeyWithDefault("er.javamail.BlackListEmailAddressPatterns", NSArray.EmptyArray);
		}
		return blakListEmailAddressPatterns;
	}

	/**
	 * Whilte list Or qualifier to match any of the patterns in the white list.
	 * 
	 * @return Or qualifier for the white list
	 */
	public EOOrQualifier whiteListQualifier() {
		if (whiteListQualifier == null) {
			whiteListQualifier = this.qualifierArrayForEmailPatterns(this.whiteListEmailAddressPatterns());
		}
		return whiteListQualifier;
	}

	/**
	 * Gets the Or qualifier to match any of the patterns in the black list.
	 * 
	 * @return or qualifier
	 */
	public EOOrQualifier blackListQualifier() {
		if (blackListQualifier == null) {
			blackListQualifier = this.qualifierArrayForEmailPatterns(this.blackListEmailAddressPatterns());
		}
		return blackListQualifier;
	}

	/**
	 * Constructs an Or qualifier for filtering an array of strings that might have the * wildcard character. Will be
	 * nice when we have regex in Java 1.4.
	 * 
	 * @param emailPatterns
	 *            array of email patterns
	 * @return or qualifier to match any of the given patterns
	 */
	protected EOOrQualifier qualifierArrayForEmailPatterns(NSArray<String> emailPatterns) {
		NSMutableArray<EOQualifier> patternQualifiers = new NSMutableArray<EOQualifier>();
		for (String pattern : emailPatterns) {
			patternQualifiers.addObject(EOQualifier.qualifierWithQualifierFormat("toString caseInsensitiveLike '" + pattern + "'", null));
		}
		return new EOOrQualifier(patternQualifiers);
	}

	/**
	 * Filters an array of email addresses by the black and white lists.
	 * 
	 * @param emailAddresses
	 *            array of email addresses to be filtered
	 * @return array of filtered email addresses
	 */
	public NSArray<String> filterEmailAddresses(NSArray<String> emailAddresses) {
		NSMutableArray<String> filteredAddresses = null;
		if ((emailAddresses != null) && (emailAddresses.count() > 0) && (this.hasWhiteList() || this.hasBlackList())) {
			filteredAddresses = new NSMutableArray<String>(emailAddresses);

			if (log.isDebugEnabled()) {
				log.debug("Filtering email addresses: " + filteredAddresses);
			}

			if (this.hasWhiteList()) {
				EOQualifier.filterArrayWithQualifier(filteredAddresses, whiteListQualifier());
				if (log.isDebugEnabled()) {
					log.debug("White list qualifier: " + whiteListQualifier() + " after filtering: " + filteredAddresses);
				}
			}

			if (this.hasBlackList()) {
				@SuppressWarnings("unchecked")
				NSArray<String> filteredOutAddresses = EOQualifier.filteredArrayWithQualifier(filteredAddresses, blackListQualifier());
				if (filteredOutAddresses.count() > 0)
					filteredAddresses.removeObjectsInArray(filteredOutAddresses);
				if (log.isDebugEnabled()) {
					log.debug("Black list qualifier: " + blackListQualifier() + " filtering: " + filteredAddresses);
				}
			}
		}

		return (filteredAddresses != null) ? filteredAddresses.immutableClone() : emailAddresses;
	}
	
	public static interface Delegate {
		public void willCreateSessionWithPropertiesForContext(Properties properties, String contextString);
		
		public void didCreateSession(javax.mail.Session session);
	}
}
