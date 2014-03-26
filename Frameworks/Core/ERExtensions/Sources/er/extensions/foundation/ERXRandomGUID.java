package er.extensions.foundation;

// This class was copied from www.javaexchange.com, under the terms of
// the author's license.  See the Javadocs for more information.

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * <p>
 * In the multitude of java GUID generators, I found none that guaranteed
 * randomness. GUIDs are guaranteed to be globally unique by using ethernet
 * MACs, IP addresses, time elements, and sequential numbers. GUIDs are not
 * expected to be random and most often are easy/possible to guess given a
 * sample from a given generator. SQL Server, for example generates GUID that
 * are unique but sequential within a given instance.
 * </p>
 * 
 * <p>
 * GUIDs can be used as security devices to hide things such as files within a
 * filesystem where listings are unavailable (e.g. files that are served up from
 * a Web server with indexing turned off). This may be desirable in cases where
 * standard authentication is not appropriate. In this scenario, the RandomGUIDs
 * are used as directories. Another example is the use of GUIDs for primary keys
 * in a database where you want to ensure that the keys are secret. Random GUIDs
 * can then be used in a URL to prevent hackers (or users) from accessing
 * records by guessing or simply by incrementing sequential numbers.
 * </p>
 * 
 * <p>
 * There are many other possibilities of using GUIDs in the realm of security and
 * encryption where the element of randomness is important. This class was
 * written for these purposes but can also be used as a general purpose GUID
 * generator as well.
 * </p>
 * 
 * <p>
 * {@code RandomGUID} generates truly random GUIDs by using the system's IP
 * address (name/IP), system time in milliseconds (as an integer), and a very
 * large random number joined together in a single String that is passed through
 * an MD5 hash. The IP address and system time make the MD5 seed globally unique
 * and the random number guarantees that the generated GUIDs will have no
 * discernable pattern and cannot be guessed given any number of previously
 * generated GUIDs. It is generally not possible to access the seed information
 * (IP, time, random number) from the resulting GUIDs as the MD5 hash algorithm
 * provides one way encryption.
 * </p>
 * 
 * <h2>Security of {@code RandomGUID}</h2>
 * <p>
 * {@code RandomGUID} can be called one of two ways -- with the basic java
 * {@link Random} number generator or a cryptographically strong random
 * generator ({@link SecureRandom}). The choice is offered because the secure
 * random generator takes about 3.5 times longer to generate its random numbers
 * and this performance hit may not be worth the added security especially
 * considering the basic generator is seeded with a cryptographically strong
 * random seed.
 * </p>
 * 
 * <p>
 * Seeding the basic generator in this way effectively decouples the random
 * numbers from the time component making it virtually impossible to predict the
 * random number component even if one had absolute knowledge of the System
 * time. Thanks to Ashutosh Narhari for the suggestion of using the static
 * method to prime the basic random generator.
 * </p>
 * 
 * <p>
 * Using the secure random option, this class complies with the statistical
 * random number generator tests specified in FIPS 140-2, Security Requirements
 * for Cryptographic Modules, section 4.9.1.
 * </p>
 * 
 * <p>
 * I converted all the pieces of the seed to a {@code String} before handing it
 * over to the MD5 hash so that you could print it out to make sure it contains
 * the data you expect to see and to give a nice warm fuzzy. If you need better
 * performance, you may want to stick to {@code byte[]} arrays.
 * </p>
 * 
 * <p>
 * I believe that it is important that the algorithm for generating random GUIDs
 * be open for inspection and modification. This class is free for all uses.
 * </p>
 * 
 * <h2>History</h2>
 * <table>
 * <tr>
 * <td>11/05/02</td>
 * <td>Performance enhancement from Mike Dubman. Moved {@code InetAddr.getLocal}
 * to static block. Mike has measured a 10 fold improvement in run time.</td>
 * </tr>
 * <tr>
 * <td>01/29/02</td>
 * <td>Bug fix: Improper seeding of nonsecure Random object caused duplicate
 * GUIDs to be produced. Random object is now only created once per JVM.</td>
 * </tr>
 * <tr>
 * <td>01/19/02</td>
 * <td>Modified random seeding and added new constructor to allow secure random
 * feature.</td>
 * </tr>
 * <tr>
 * <td>01/14/02</td>
 * <td>Added random function seeding with JVM run time</td>
 * </tr>
 * </table>
 * 
 * <h2>License</h2>
 * <p>
 * From www.JavaExchange.com, Open Software licensing
 * </p>
 * 
 * @version 1.2.1 11/05/02
 * @author Marc A. Mnich
 */
public class ERXRandomGUID {
    private static Logger log = Logger.getLogger(ERXRandomGUID.class);
    public String valueBeforeMD5 = "";
    public String valueAfterMD5 = "";
    private static Random myRand;
    private static SecureRandom mySecureRand;

    private static String s_id;

    /**
	 * Static block to take care of one time {@link SecureRandom} seed. It takes
	 * a few seconds to initialize {@code SecureRandom}. You might want to
	 * consider removing this static block or replacing it with a
	 * "time since first loaded" seed to reduce this time. This block will run
	 * only once per JVM instance.
	 */
    static {
        mySecureRand = new SecureRandom();
        long secureInitializer = mySecureRand.nextLong();
        myRand = new Random(secureInitializer);
        try {
            s_id = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            log.error(e, e);
        }

    }


    /**
	 * Default constructor. With no specification of security option, this
	 * constructor defaults to lower security, high performance.
	 */
    public ERXRandomGUID() {
        getRandomGUID(false);
    }

    /**
	 * Constructor with security option. Setting {@code secure} {@code true}
	 * enables each random number generated to be cryptographically strong.
	 * {@code secure} {@code false} defaults to the standard {@link Random}
	 * function seeded with a single cryptographically strong random number.
	 * 
	 * @param secure
	 *            {@code true} use a random number from {@link SecureRandom}, or
	 *            {@code false} use a random number from {@link Random}
	 */
    public ERXRandomGUID(boolean secure) {
        getRandomGUID(secure);
    }

    /**
	 * Method to generate the random GUID
	 * 
	 * @param secure
	 *            {@code true} use a random number from {@link SecureRandom}, or
	 *            {@code false} use a random number from {@link Random}
	 */
    private void getRandomGUID(boolean secure) {
        MessageDigest md5 = null;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error(e, e);
            valueBeforeMD5 = "";
            valueAfterMD5 = "";
            return;
        }

        try {
            StringBuilder sbValueBeforeMD5 = new StringBuilder();
            long time = System.currentTimeMillis();
            long rand = 0;

            if (secure) {
                rand = mySecureRand.nextLong();
            } else {
                rand = myRand.nextLong();
            }

            // This StringBuilder can be a long as you need; the MD5
            // hash will always return 128 bits. You can change
            // the seed to include anything you want here.
            // You could even stream a file through the MD5 making
            // the odds of guessing it at least as great as that
            // of guessing the contents of the file!
            sbValueBeforeMD5.append(s_id);
            sbValueBeforeMD5.append(':');
            sbValueBeforeMD5.append(Long.toString(time));
            sbValueBeforeMD5.append(':');
            sbValueBeforeMD5.append(Long.toString(rand));

            valueBeforeMD5 = sbValueBeforeMD5.toString();
            md5.update(valueBeforeMD5.getBytes());

            byte[] array = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < array.length; ++j) {
                int b = array[j] & 0xFF;
                if (b < 0x10) sb.append('0');
                sb.append(Integer.toHexString(b));
            }

            valueAfterMD5 = sb.toString();

        } catch (Exception e) {
            log.error(e, e);
        }
    }


    /**
	 * Convert to the standard format for GUID (Useful for SQL Server
	 * UniqueIdentifiers, etc.) Example: C2FEEEAC-CFCD-11D1-8B05-00600806D9B6
	 * 
	 * @return a {@code String} representation of this object
	 */
    @Override
    public String toString() {
        String raw = valueAfterMD5.toUpperCase();
        StringBuilder sb = new StringBuilder();
        sb.append(raw.substring(0, 8));
        sb.append('-');
        sb.append(raw.substring(8, 12));
        sb.append('-');
        sb.append(raw.substring(12, 16));
        sb.append('-');
        sb.append(raw.substring(16, 20));
        sb.append('-');
        sb.append(raw.substring(20));

        return sb.toString();
    }

    /**
	 * Demonstration and self test of class.
	 * 
	 * @param args
	 *            No arguments
	 */
    public static void main(String args[]) {
        for (int i=0; i< 100; i++) {
        ERXRandomGUID myGUID = new ERXRandomGUID();
        System.out.println("Seeding String=" + myGUID.valueBeforeMD5);
        System.out.println("rawGUID=" + myGUID.valueAfterMD5);
        System.out.println("RandomGUID=" + myGUID.toString());
        }
    }

    /**
	 * Returns the {@code String} representation of a new {@code ERXRandomGUID}
	 * object.
	 * 
	 * @return the {@code String} representation of a new {@code ERXRandomGUID}
	 *         object
	 */
    public static String newGid() {
        return new ERXRandomGUID().toString();
    }
}
