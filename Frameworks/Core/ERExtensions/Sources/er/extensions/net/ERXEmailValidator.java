package er.extensions.net;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Email validation class inspired by <a
 * href="http://leshazlewood.com/2006/11/06/emailaddress-java-class/">Les
 * Hazlewood's email validator.</a> This class is immutable and thread safe.
 * 
 * @author Les Hazlewood (regular expressions)
 * @author Ramsey Gurley (threaded domain validation)
 */
public final class ERXEmailValidator implements Serializable {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ERXEmailValidator.class);

	// RFC 2822 2.2.2 Structured Header Field Bodies
	private static final String wsp = "[ \\t]"; // space or tab
	private static final String fwsp = wsp + "*";

	// RFC 2822 3.2.1 Primitive tokens
	private static final String dquote = "\\\"";
	// ASCII Control characters excluding white space:
	private static final String noWsCtl = "\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F";
	// all ASCII characters except CR and LF:
	private static final String asciiText = "[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]";

	// RFC 2822 3.2.2 Quoted characters:
	// single backslash followed by a text char
	private static final String quotedPair = "(\\\\" + asciiText + ")";

	// RFC 2822 3.2.4 Atom:
	private static final String atext = "[a-zA-Z0-9\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~]";
	private static final String atom = fwsp + atext + "+" + fwsp;
	private static final String dotAtomText = atext + "+" + "(" + "\\." + atext + "+)*";
	private static final String dotAtom = fwsp + "(" + dotAtomText + ")" + fwsp;

	// RFC 2822 3.2.5 Quoted strings:
	// noWsCtl and the rest of ASCII except the doublequote and backslash
	// characters:
	private static final String qtext = "[" + noWsCtl + "\\x21\\x23-\\x5B\\x5D-\\x7E]";
	private static final String qcontent = "(" + qtext + "|" + quotedPair + ")";
	private static final String quotedString = dquote + "(" + fwsp + qcontent + ")*" + fwsp + dquote;

	// RFC 2822 3.2.6 Miscellaneous tokens
	private static final String word = "((" + atom + ")|(" + quotedString + "))";
	private static final String phrase = word + "+"; // one or more words.

	// RFC 1035 tokens for domain names:
	private static final String letter = "[a-zA-Z]";
	private static final String letDig = "[a-zA-Z0-9]";
	private static final String letDigHyp = "[a-zA-Z0-9-]";
	private static final String rfcLabel = letDig + "(" + letDigHyp + "{0,61}" + letDig + ")?";
	private static final String rfc1035DomainName = rfcLabel + "(\\." + rfcLabel + ")*\\." + letter + "{2,6}";

	// RFC 2822 3.4 Address specification
	// domain text - non white space controls and the rest of ASCII chars not
	// including [, ], or \:
	private static final String dtext = "[" + noWsCtl + "\\x21-\\x5A\\x5E-\\x7E]";
	private static final String dcontent = dtext + "|" + quotedPair;
	private static final String domainLiteral = "\\[" + "(" + fwsp + dcontent + "+)*" + fwsp + "\\]";
	private static final String rfc2822Domain = "(" + dotAtom + "|" + domainLiteral + ")";

	private static final String localPart = "((" + dotAtom + ")|(" + quotedString + "))";

	private final String domain;
	private final String addrSpec;
	private final String angleAddr;
	private final String nameAddr;
	private final String mailbox;
	private final String patternString;
	private final Pattern validPattern;
	
	/**
	 * This second validator exists because there is an issue with validating
	 * addresses that allowQuotedIdentifiers that have no quoting and a long
	 * mailbox name. Example: blahblahblahblahblahblahblah@blah.com
	 * 
	 * It seems that after about 25 chars, the regular expression matching
	 * takes exponentially longer to match the string. The same address with
	 * quoting does not exhibit the problem. 
	 * Ex. "Blah blah" &lt;blahblahblahblahblahblahblah@blah.com&gt;
	 * 
	 * Nor does using a validator that does not allow quoted identifiers. In
	 * order to work around this problem, a second internal validator is
	 * created when allowQuotedIdentifiers is true. This internal validator
	 * does not allow quoted identifiers. It is tried first and only if it
	 * returns false is the full regular expression used.
	 */
	private final ERXEmailValidator _internal;

	/**
	 * 
	 * @param allowQuotedIdentifiers
	 *            if true, quoted identifiers are allowed (using quotes and
	 *            angle brackets around the raw address) are allowed, e.g.:
	 *            "John Smith" &lt;john.smith@somewhere.com&gt; The RFC says
	 *            this is a valid mailbox. If you don't want to allow this,
	 *            because for example, you only want users to enter in a raw
	 *            address (john.smith@somewhere.com - no quotes or angle
	 *            brackets), then set this to false.
	 * 
	 * @param allowDomainLiterals
	 *            if true, domain literals are allowed in the email address,
	 *            e.g.: someone@[192.168.1.100] or john.doe@[23:33:A2:22:16:1F]
	 *            or me@[my computer] The RFC says these are valid email
	 *            addresses, but most people don't like allowing them. If you
	 *            don't want to allow them, and only want to allow valid domain
	 *            names (RFC 1035, x.y.z.com, etc), set this to false.
	 */
	public ERXEmailValidator(boolean allowQuotedIdentifiers, boolean allowDomainLiterals) {
		domain = allowDomainLiterals ? rfc2822Domain : rfc1035DomainName;
		addrSpec = localPart + "@" + domain;
		angleAddr = "<" + addrSpec + ">";
		nameAddr = "(" + phrase + ")?" + fwsp + angleAddr;
		mailbox = nameAddr + "|" + addrSpec;
		patternString = allowQuotedIdentifiers ? mailbox : addrSpec;
		validPattern = Pattern.compile(patternString);
		
		/*
		 * See javadoc for the _internal ivar
		 */
		_internal = allowQuotedIdentifiers?new ERXEmailValidator(false, allowDomainLiterals):null;
	}

	/**
	 * Utility method that checks to see if the specified string is a valid
	 * email address according to the * RFC 2822 specification.
	 * 
	 * @param email
	 *            the email address string to test for validity.
	 * @return true if the given text valid according to RFC 2822, false
	 *         otherwise.
	 */
	public boolean isValidEmailString(String email) {
		/*
		 * See javadoc for the _internal ivar
		 */
		if(_internal != null && _internal.isValidEmailString(email)) {
			return true;
		}
		return email != null && validPattern.matcher(email).matches();
	}

	/**
	 * The thread pool
	 */
	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	/**
	 * Callable to actually validate the email domain.
	 */
	private static class DomainValidator implements Callable<Boolean> {
		private final String _hostName;

		/**
		 * @param hostName
		 *            the host name to validate
		 */
		DomainValidator(String hostName) {
			_hostName = hostName;
		}

		public Boolean call() {
			Hashtable env = new Hashtable();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			try {
				DirContext ictx = new InitialDirContext(env);
				Attributes attrs = ictx.getAttributes(_hostName, new String[] { "MX" });
				Attribute attr = attrs.get("MX");
				return attr != null ? Boolean.TRUE : Boolean.FALSE;
			}
			catch (NameNotFoundException e) {
				return Boolean.FALSE;
			}
			catch (NamingException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}

	}

	/**
	 * Checks to see if the hostName is a valid email domain. A timeout is
	 * specified which limits the time spent waiting for the DNS lookup. If the
	 * timeout is exceeded, the method returns null.
	 * 
	 * @param hostName
	 *            the email hostName
	 * @param timeout
	 *            the timeout in milliseconds
	 * @return true if the hostName is valid, false if no hostName or MX record
	 *         is found, null if lookup times out
	 * @throws NamingException
	 * 
	 * @deprecated this method will throw mysterious NullPointerExceptions if used
	 * in a loop. Evidently, something about the DirContext is not as thread safe
	 * as the javadocs claim. Do not use it.
	 */
	public static Boolean isValidDomainString(String hostName, long timeout) {
		if (timeout < 1) {
			return null;
		}
		DomainValidator domainValidator = new DomainValidator(hostName);
		Future<Boolean> future = executorService.submit(domainValidator);

		try {
			Boolean result = future.get(timeout, TimeUnit.MILLISECONDS);
			return result;
		}
		catch (InterruptedException e) {
			// This really shouldn't happen
			log.info("Domain validation thread interrupted.");
			return null;
		}
		catch (ExecutionException e) {
			// Threw some naming exception?
			log.warn("Exception thrown validating domain.", e);
			return null;
		}
		catch (TimeoutException e) {
			// If the future timed out, return null.
			log.debug("Timeout validating email domain.");
			return null;
		}
	}

	/**
	 * Convenience method to validate email address string and domain. If a
	 * timeout occurs, the default boolean value is returned.
	 * 
	 * @param email
	 *            the email string to test
	 * @param timeout
	 *            the timeout in milliseconds
	 * @param def
	 *            default value if timeout occurs
	 * @return true if the email passes both validations
	 * 
	 * @deprecated Deprecated because it relies on {@link ERXEmailValidator#isValidDomainString(String, long)}
	 */
	public boolean isValidEmailAddress(String email, long timeout, boolean def) {
		if (isValidEmailString(email)) {
			String hostName = hostNameForEmailString(email);
			Boolean value = ERXEmailValidator.isValidDomainString(hostName, timeout);
			return ERXValueUtilities.booleanValueWithDefault(value, def);
		}
		return false;
	}

	/**
	 * Parses the host name from the email string
	 * 
	 * @param email
	 *            the email address
	 * @return the hostName portion of the email address
	 */
	public static String hostNameForEmailString(String email) {
		String hostName = StringUtils.substringAfterLast(email, "@");
		// handle domain literals and quoted identifiers
		hostName = StringUtils.trimToEmpty(hostName);
		if(hostName.isEmpty()) { return hostName; }
		int lastIndex = hostName.length() - 1;
		if (hostName.lastIndexOf('>') == lastIndex) {
			hostName = hostName.substring(0, lastIndex);
		}
		hostName = StringUtils.trimToEmpty(hostName);
		lastIndex = hostName.length() - 1;
		if (hostName.indexOf('[') == 0 && hostName.lastIndexOf(']') == lastIndex) {
			hostName = hostName.substring(1, lastIndex);
		}
		hostName = StringUtils.trimToEmpty(hostName);
		return hostName;
	}
}
