//
// StringUtilities.java
// Project linksadmin
//
// Created by ak on Mon Nov 05 2001
//
package er.extensions.foundation;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
import java.text.Format;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EOAdaptorOperation;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.appserver.ERXMessageEncoding;
import er.extensions.eof.ERXConstant;
import er.extensions.formatters.ERXSimpleHTMLFormatter;

/**
 * Collection of {@link java.lang.String String} utilities. Contains
 * the base localization support.
 */
public class ERXStringUtilities {
    
    /** Holds the default display language, which is English */
    private static final String DEFAULT_TARGET_DISPLAY_LANGUAGE = "English";
    /** Holds the chars for hex encoding */
    public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    /** Holds the distance key */
    private static final String _DISTANCE = "distance";
    /** Holds the ascending <code>EOSortOrdering</code>s */
    public static final NSArray SORT_ASCENDING = 
        new NSArray(new Object [] { new EOSortOrdering(_DISTANCE, EOSortOrdering.CompareAscending) });
    /** Holds the ascending <code>EOSortOrdering</code>s */
    public static final NSArray SORT_DESCENDING = 
        new NSArray(new Object [] { new EOSortOrdering(_DISTANCE, EOSortOrdering.CompareDescending) });
    /** Holds characters that have special meaning for regex */
    public static final String SpecialRegexCharacters        = ".*[]{}()?\\+%$!^";
    
    /** 
     * Holds the array of default display languages. Holds
     * a single entry for English.
     */
    private static NSArray _defaultTargetDisplayLanguages = new NSArray(DEFAULT_TARGET_DISPLAY_LANGUAGE);

	/**
	 * Returns the <a
	 * href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein
	 * distance</a> between {@code a} and {@code b} as a {@code double}. (This
	 * method is being retained for backwards compatibility, and will be removed
	 * at some future point. New code should use
	 * {@link #levenshteinDistance(String, String)}.)
	 * 
	 * @param a
	 *            first string
	 * @param b
	 *            second string
	 * @return Levenshtein distance between {@code a} and {@code b}
	 * @deprecated Use {@link #levenshteinDistance(String, String)}, which
	 *             correctly returns an {@code int} result
	 */
    @Deprecated
    public static double distance(String a, String b) {
    	return levenshteinDistance(a, b);
    }

	/**
	 * <p>
	 * Returns the <a
	 * href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein
	 * distance</a> between {@code a} and {@code b}. This code is based on <a
	 * href
	 * ="http://mail.python.org/pipermail/python-list/1999-August/006031.html"
	 * >some Python code posted to a mailing list</a> by Magnus L. Hetland
	 * &lt;mlh@idt.ntnu.no&gt;, and assumed to be in the public domain.
	 * </p>
	 * 
	 * <h3>Algorithm</h3>
	 * 
	 * <pre>
	 * <code>def distance(a,b):
	 *   c = {}
	 *   n = len(a); m = len(b)
	 * 
	 *   for i in range(0,n+1):
	 *     c[i,0] = i
	 *   for j in range(0,m+1):
	 *     c[0,j] = j
	 * 
	 *   for i in range(1,n+1):
	 *     for j in range(1,m+1):
	 *       x = c[i-1,j]+1
	 *       y = c[i,j-1]+1
	 *       if a[i-1] == b[j-1]:
	 *         z = c[i-1,j-1]
	 *       else:
	 *         z = c[i-1,j-1]+1
	 *       c[i,j] = min(x,y,z)
	 *   return c[n,m]</code>
	 * </pre>
	 * 
	 * <p>
	 * It calculates the following: Given two strings, {@code a} and {@code b},
	 * and three operations, adding, subtracting and exchanging single
	 * characters, what is the minimal number of steps needed to translate
	 * {@code a} into {@code b}? The method is based on the following idea. We
	 * want to find the distance between {@code a[:x]} and {@code b[:y]}. To do
	 * this, we first calculate:
	 * </p>
	 * 
	 * <ol>
	 * <li>the distance between {@code a[:x-1]} and {@code b[:y]}, adding the
	 * cost of a subtract-operation, used to get from {@code a[:x]} to
	 * {@code a[:z-1]};</li>
	 * <li>the distance between {@code a[:x]} and {@code b[:y-1]}, adding the
	 * cost of an addition-operation, used to get from {@code b[:y-1]} to
	 * {@code b[:y]};</li>
	 * <li>the distance between {@code a[:x-1]} and {@code b[:y-1]}, adding the
	 * cost of a <em>possible</em> exchange of the letter {@code b[y]} (with
	 * {@code a[x]}).</li>
	 * </ol>
	 * 
	 * <p>
	 * The cost of the subtraction and addition operations are 1, while the
	 * exchange operation has a cost of 1 if {@code a[x]} and {@code b[y]} are
	 * different, and 0 otherwise. After calculating these costs, we choose the
	 * least one of them (since we want to use the best solution.)
	 * </p>
	 * 
	 * <p>
	 * Instead of doing this recursively, i.e. calculating ourselves "back" from
	 * the final value, we build a cost-matrix {@code c} containing the optimal
	 * costs, so we can reuse them when calculating the later values. The costs
	 * {@code c[i,0]} (from string of length {@code n} to empty string) are all
	 * {@code i}, and correspondingly all {@code c[0,j]} (from empty string to
	 * string of length {@code j}) are {@code j}. Finally, the cost of
	 * translating between the full strings {@code a} and {@code b} (
	 * {@code c[n,m]}) is returned.
	 * </p>
	 * 
	 * @param a
	 *            first string
	 * @param b
	 *            second string
	 * @return the distance between the two strings
	 * @deprecated use {@link StringUtils#getLevenshteinDistance(String, String)} instead
	 */
	@Deprecated
	public static int levenshteinDistance(String a, String b) {
		int n = a.length();
		int m = b.length();
		int c[][] = new int[n + 1][m + 1];
		for (int i = 0; i <= n; i++) {
			c[i][0] = i;
		}
		for (int j = 0; j <= m; j++) {
			c[0][j] = j;
		}
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				int x = c[i - 1][j] + 1;
				int y = c[i][j - 1] + 1;
				int z = 0;
				if (a.charAt(i - 1) == b.charAt(j - 1))
					z = c[i - 1][j - 1];
				else
					z = c[i - 1][j - 1] + 1;
				int temp = Math.min(x, y);
				c[i][j] = Math.min(z, temp);
			}
		}
		return c[n][m];
	}
    
    /** holds the base adjustment for fuzzy matching */
    // FIXME: Not thread safe
    // MOVEME: Needs to go with the fuzzy matching stuff
    protected static double adjustement = 0.5;
    private static Logger log = Logger.getLogger(ERXStringUtilities.class);

    /**
     * Sets the base adjustment used for fuzzy matching
     * @param newAdjustement factor to be used.
     */
    // FIXME: Not thread safe.
    // MOVEME: fuzzy matching stuff
    public static void setAdjustement(double newAdjustement) {
        adjustement = newAdjustement;
    }

    /**
     * Fuzzy matching is useful for catching user entered typos. For example
     * if a user is searching for a company named 'Aple' within your application
     * they aren't going to find it. Thus the idea of fuzzy matching, meaning you
     * can define a threshold of 'how close can they be' type of thing.
     *
     * @param name to be matched against
     * @param entityName name of the entity to perform the match against.
     * @param propertyKey to be matched against
     * @param synonymsKey allows objects to have additional values to be matched
     * 		against in addition to just the value of the propertyKey
     * @param ec context to fetch data in
     * @param cleaner object used to clean a string, for example the cleaner might
     *		strip out the words 'The' and 'Inc.'
     * @param sortOrderings can be either <code>SORT_ASCENDING</code> or <code>SORT_DESCENDING</code> 
     *          to tell how the results should be sorted.
     * @return an array of objects that match in a fuzzy manner the name passed in.
     */
    // FIXME: Should move all of fuzzy matching stuff in ERXFuzzyMatchCleaner and then add a static inner inerface
    // IMPROVEME: too many parameters.
    public static NSArray fuzzyMatch(String name,
                                     String entityName,
                                     String propertyKey,
                                     String synonymsKey,
                                     EOEditingContext ec,
                                     ERXFuzzyMatchCleaner cleaner,
                                     NSArray sortOrderings ){
        String eoKey = "eo";
        NSMutableArray<NSMutableDictionary<String, Object>> results = new NSMutableArray<NSMutableDictionary<String, Object>>();
        EOFetchSpecification fs = new EOFetchSpecification( entityName, null, null );
        fs.setFetchesRawRows( true );
        NSArray<String> pks = EOUtilities.entityNamed( ec, entityName ).primaryKeyAttributeNames();
        NSMutableArray<String> keyPaths = new NSMutableArray<String>(pks);
        keyPaths.addObject( propertyKey );
        if( synonymsKey != null ) 
            keyPaths.addObject( synonymsKey );
        //we use only the strictly necessary keys.
        fs.setRawRowKeyPaths( keyPaths );
        NSArray<NSDictionary<String, Object>> rawRows = ec.objectsWithFetchSpecification( fs );
        if(name == null)
            name = "";
        name = name.toUpperCase();
        String cleanedName = cleaner.cleanStringForFuzzyMatching(name);
        for(Enumeration e = rawRows.objectEnumerator(); e.hasMoreElements(); ){
            NSMutableDictionary<String, Object> dico = ((NSDictionary)e.nextElement()).mutableClone();
            Object value = dico.valueForKey(propertyKey);
            boolean trySynonyms = true;
            //First try to match with the name of the eo
            if( value!=null && value instanceof String){
                String comparedString = ((String)value).toUpperCase();
                String cleanedComparedString = cleaner.cleanStringForFuzzyMatching(comparedString);
                if( (levenshteinDistance(name, comparedString) <=
                     Math.min((double)name.length(), (double)comparedString.length())*adjustement ) ||
                    (levenshteinDistance(cleanedName, cleanedComparedString) <=
                     Math.min((double)cleanedName.length(), (double)cleanedComparedString.length())*adjustement)){
                    dico.setObjectForKey( Double.valueOf(levenshteinDistance(name, comparedString)), _DISTANCE );
                    NSDictionary<String, Object> pkValues = new NSDictionary<String, Object>(dico.objectsForKeys(pks, NSKeyValueCoding.NullValue ), pks);
                    dico.setObjectForKey( EOUtilities.faultWithPrimaryKey( ec, entityName, pkValues ), eoKey );
                    results.addObject( dico );
                    trySynonyms = false;
                }
            }
            //Then try to match using the synonyms vector
            if(trySynonyms && synonymsKey != null){
                Object synonymsString = dico.valueForKey(synonymsKey);
                if(synonymsString != null && synonymsString instanceof String){
                    Object plist  = NSPropertyListSerialization.propertyListFromString((String)synonymsString);
                    Vector v = (Vector)plist;
                    for(int i = 0; i< v.size(); i++){
                        String comparedString = ((String)v.elementAt(i)).toUpperCase();
                        if((levenshteinDistance(name, comparedString) <=
                            Math.min((double)name.length(), (double)comparedString.length())*adjustement) ||
                           (levenshteinDistance(cleanedName, comparedString) <=
                            Math.min((double)cleanedName.length(), (double)comparedString.length())*adjustement)){
                            dico.setObjectForKey( Double.valueOf(levenshteinDistance(name, comparedString)), _DISTANCE );
                            NSDictionary<String, Object> pkValues = new NSDictionary<String, Object>(dico.objectsForKeys(pks, NSKeyValueCoding.NullValue ), pks);
                            dico.setObjectForKey( EOUtilities.faultWithPrimaryKey( ec, entityName, pkValues ), eoKey );
                            results.addObject( dico );
                            break;
                        }
                    }
                }
            }
        }
        if( sortOrderings != null ) {
            results = (NSMutableArray<NSMutableDictionary<String, Object>>) EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
        }
        return (NSArray) results.valueForKey( eoKey );
    }
    
    /**
     * @param name
     * @param entityName
     * @param propertyKey
     * @param synonymsKey
     * @param ec
     * @param cleaner
     * @param comparisonString
     * @return an array of objects that match in a fuzzy manner the name passed in.
     * @deprecated use {@link #fuzzyMatch(String, String, String, String, EOEditingContext, ERXFuzzyMatchCleaner, NSArray)}
     */
    @Deprecated
    public static NSArray fuzzyMatch(String name,
                                     String entityName,
                                     String propertyKey,
                                     String synonymsKey,
                                     EOEditingContext ec,
                                     ERXFuzzyMatchCleaner cleaner,
                                     String comparisonString){
        NSArray sortOrderings = null;
            if("asc".equals(comparisonString)){
                sortOrderings = SORT_ASCENDING;
            }else if ("desc".equals(comparisonString)){
                sortOrderings = SORT_DESCENDING;
            }
        return fuzzyMatch( name, entityName, propertyKey, synonymsKey, ec, cleaner, sortOrderings );
    }

    /**
     * Gets a localized string for a given key in the application's
     * Localizable strings file for the default language (English).
     * @param key to be lookup in the strings file
     * @return string value of the looked up key
     */
    // CHECKME: Should this be using the framework search order?
    public static String localizedStringForKey(String key) {
        return localizedStringForKey(key, null, null);
    }

    /**
     * Gets a localized string for a given key in a given framework's
     * Localizable strings file for the default language (English).
     * @param key to be lookup in the strings file
     * @param framework name, specify app or null to perform the
     *		lookup in the application's resources.
     * @return string value of the looked up key
     */    
    public static String localizedStringForKey(String key, String framework) {
        return localizedStringForKey(key, framework, null);
    }

    /**
     * Gets a localized string for a given key in a given framework's
     * Localizable strings file using the array of languages as the
     * search order for the key.
     * @param key to be lookup in the strings file
     * @param framework name, specify app or null to perform the
     *		lookup in the application's resources.
     * @param languages array to search for the key in
     * @return string value of the looked up key
     */    
    public static String localizedStringForKey(String key, String framework, NSArray languages) {
        languages = languages != null && languages.count() > 0 ? languages : _defaultTargetDisplayLanguages;
        String result = WOApplication.application().resourceManager().stringForKey( key, "Localizable", key, framework, languages);
        return result;
    }

    /**
     * Uses the method <code>localizedStringForKey</code> to retreive
     * a template that is then parsed using the passed in object to
     * produce a resulting string. The template parser used is
     * {@link ERXSimpleTemplateParser}.
     * @param o object used to resolve keys in the localized template
     * @param key to be lookup in the strings file
     * @param framework name, specify app or null to perform the
     *		lookup in the application's resources.
     * @param languages array to search for the key in
     * @return localized template parsed and resolved with the given
     *		object.
     */    
    public static String localizedTemplateStringWithObjectForKey(Object o, String key, String framework, NSArray languages) {
        String template = localizedStringForKey(key, framework, languages);
        return ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(template, null, o);
    }

    /**
     * Reads the contents of a file given by a path
     * into a string.
     * @param file path to the file in the file system
     *
     * @return the contents of the file in a string
     */
    public static String stringWithContentsOfFile(File file) {
        try {
            if(file != null)
                return ERXFileUtilities.stringFromFile(file);
        } catch (IOException e) {
            log.error(e, e);
        }
        return null;
    }
    /**
     * Reads the contents of a file given by a path
     * into a string.
     * @param path to the file in the file system
     * @return the contents of the file in a string
     */
    public static String stringWithContentsOfFile(String path) {
        if(path != null)
            return ERXStringUtilities.stringWithContentsOfFile(new File(path));
        return null;
    }

    /**
     * Calculates an Integer for a given string. The
     * only advantage that this method has is to not
     * throw a number format exception if the string
     * is not correctly formatted.  This method makes
     * use of the ERXConstant.integerForString caching
     * logic.
     * @param s string to caclulate an Integer from
     * @return parsed Integer from the string or null
     *		if the string is not correctly formed.
     * @see er.extensions.eof.ERXConstant#integerForString(String)
     */
    public static Integer integerWithString(String s) {
        try {
            return ERXConstant.integerForInt(Integer.parseInt(s));
        } catch (NumberFormatException e) {
        	// ignore
        }
        return null;
    }
    
    /**
     * Tests if a given string object can be parsed into
     * an integer.
     * @param s string to be parsed
     * @return <code>true</code> if the string is not <code>null</code>
     *      and can be parsed to an int
     */
    public static boolean stringIsParseableInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Wrapper for {@link Integer#valueOf(String)} that catches
     * the NumberFormatException.
     * 
     * @param s string to convert to an Integer
     * @return Integer or <code>null</code> if the string could
     *      not be parsed
     */
    public static Integer safeInteger(String s) {
    	try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
        	// ignore
        }
        return null;
    }
    
    /**
     * Wrapper for {@link Long#valueOf(String)} that catches
     * the NumberFormatException.
     * 
     * @param s string to convert to a Long
     * @return Long or <code>null</code> if the string could
     *      not be parsed
     */
    public static Long safeLong(String s) {
    	try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
        	// ignore
        }
        return null;
    }

    /**
     * Retrieves a given string for a given name, extension
     * and bundle.
     * @param name of the resource
     * @param extension of the resource, example: txt or rtf
     * @param bundle to look for the resource in
     * @return string of the given file specified in the bundle
     */
    public static String stringFromResource(String name, String extension, NSBundle bundle) {
        String path = null;
        if(bundle == null) {
            bundle = NSBundle.mainBundle();
        }
        path = bundle.resourcePathForLocalizedResourceNamed(name + (extension == null || extension.length() == 0 ? "" : "." + extension), null);
        if(path != null) {
        	InputStream stream = null;
            try {
                stream = bundle.inputStreamForResourcePath(path);
                byte bytes[] = ERXFileUtilities.bytesFromInputStream(stream);
                return new String(bytes);
            } catch (IOException e) {
                log.warn("IOException when stringFromResource(" + name + "." + extension + " in bundle " + bundle.name());
            } finally {
            	if (stream != null) {
            		try { stream.close(); } catch (IOException e) {}
            	}
            }
        }
        return null;
    }
    
    public static final String firstPropertyKeyInKeyPath(String keyPath) {
        String part = null;
        if (keyPath != null) {
            int index = keyPath.indexOf(".");
            if (index != -1)
                part = keyPath.substring(0, index);
            else
                part = keyPath;
        }
        return part;
    }

    public static final String lastPropertyKeyInKeyPath(String keyPath) {
        String part = null;
        if (keyPath != null) {
            int index = keyPath.lastIndexOf(".");
            if (index != -1)
                part = keyPath.substring(index + 1);
            else
                part = keyPath;
        }
        return part;
    }

    public static final String keyPathWithoutLastProperty(String keyPath) {
        String part = null;
        if(keyPath != null) {
            int index = keyPath.lastIndexOf(".");
            if (index != -1)
                part = keyPath.substring(0, index);
        }
        return part;
    }

    public static final String keyPathWithoutFirstProperty(String keyPath) {
        String part = null;
        if(keyPath != null) {
            int index = keyPath.indexOf(".");
            if (index != -1)
                part = keyPath.substring(index + 1);
        }
        return part;
    }

    /**
     * Calculates a default display name for a given
     * key path. For instance for the key path:
     * "foo.bar" the display name would be "Bar".
     * @param key to calculate the display name
     * @return display name for the given key
     */
    public static String displayNameForKey(String key) {
        StringBuilder finalString = null;
        if (!stringIsNullOrEmpty(key) && !key.trim().equals("")) {
            finalString = new StringBuilder();
            String lastHop=key.indexOf(".") == -1 ? key : key.endsWith(".") ? "" : key.substring(key.lastIndexOf(".") + 1);
            StringBuilder tempString = new StringBuilder();
            char[] originalArray = lastHop.toCharArray();
            originalArray[0] = Character.toUpperCase(originalArray[0]);
            Character tempChar = null;
            Character nextChar = null;
            for(int i=0;i<(originalArray.length-1);i++){
                tempChar = Character.valueOf(originalArray[i]);
                nextChar = Character.valueOf(originalArray[i+1]);
                if(Character.isUpperCase(originalArray[i]) &&
                   Character.isLowerCase(originalArray[i+1])) {
                    finalString.append(tempString.toString());
                    if (i>0) finalString.append(' ');
                    tempString = new StringBuilder();
                }
                tempString.append(tempChar.toString());
            }
            finalString.append(tempString.toString());
            finalString.append(nextChar);
        }
        return finalString == null ? "" : finalString.toString();
    }

    /** 
     * Locate the the first numeric character in the given string.
     * 
     * @param str string to scan
     * @return position in string or -1 if no numeric found 
     */ 
    public static int indexOfNumericInString(String str) {
        return indexOfNumericInString(str, 0);
    }
        
    /** 
     * Locate the the first numeric character 
     * after <code>fromIndex</code> in the given string.
     * 
     * @param str string to scan
     * @param fromIndex index position from where to start
     * @return position in string or -1 if no numeric found
     */ 
    public static int indexOfNumericInString(String str, int fromIndex) {
        if (str == null)  throw new IllegalArgumentException("String cannot be null.");
    
        int pos = -1;
        for (int i = fromIndex; i < str.length(); i++) {
            char c = str.charAt(i);
            if ('0' <= c  &&  c <= '9') {
                pos = i;
                break;
            }
        }
        return pos;
    }

    /**
     * Utility method to append a character to a
     * StringBuffer if the last character is not
     * a certain character. Useful for determining
     * if you need to add an '&' to the end of a
     * form value string.
     * @param separator character to potentially
     *		add to the StringBuffer.
     * @param not character to test if the given
     *		StringBuffer ends in it.
     * @param sb StringBuffer to test and potentially
     *		append to.
     */
    public static void appendSeparatorIfLastNot(char separator, char not, StringBuffer sb) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != not)
            sb.append(separator);
    }

    /**
     * Utility method to append a character to a
     * StringBuilder if the last character is not
     * a certain character. Useful for determining
     * if you need to add an '&' to the end of a
     * form value string.
     * @param separator character to potentially
     *		add to the StringBuilder.
     * @param not character to test if the given
     *		StringBuilder ends in it.
     * @param sb StringBuilder to test and potentially
     *		append to.
     */
    public static void appendSeparatorIfLastNot(char separator, char not, StringBuilder sb) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != not)
            sb.append(separator);
    }

    /**
     * Replaces a given string by another string in a string.
     * @param old string to be replaced
     * @param newString to be inserted
     * @param buffer string to have the replacement done on it
     * @return string after having all of the replacement done.
     * @deprecated use {@link StringUtils#replace(String, String, String)} instead
     */
    @Deprecated
    public static String replaceStringByStringInString(String old, String newString, String buffer) {
        int begin, end;
        int oldLength = old.length();
        int length = buffer.length();
        StringBuilder convertedString = new StringBuilder(length + 100);

        begin = 0;
        while(begin < length)
        {
            end = buffer.indexOf(old, begin);
            if(end == -1)
            {
                convertedString.append(buffer.substring(begin));
                break;
            }
            if(end == 0)
                convertedString.append(newString);
            else {
                convertedString.append(buffer.substring(begin, end));
                convertedString.append(newString);
            }
            begin = end+oldLength;
        }
        return convertedString.toString();
    }
    
    /**
     * Replaces the first occurrence of a string with another string in a string.
     *
     * @param sourceString string to use on which to perform the replacement
     * @param stringToReplace string to replace in sourceString if it exists.
     * @param replacementString the string with which to replace stringToReplace.
     * @return sourceString with stringToReplace replaced with replacementString if it
     *         existed in sourceString.  otherwise, sourceString is returned.
     * @deprecated use {@link StringUtils#replaceOnce(String, String, String)} instead
     */
    @Deprecated
    public static String stringByReplacingFirstOccurrenceOfStringWithString(final String sourceString, final String stringToReplace, final String replacementString) {
        final int indexOfMatch = sourceString.indexOf(stringToReplace);
        final String result;
        
        if ( indexOfMatch >= 0 ) {
            final int sourceStringLength = sourceString.length();
            final int stringToReplaceLength = stringToReplace.length();
            final int replacementStringLength = replacementString.length();
            final StringBuilder buffer = new StringBuilder(sourceStringLength - stringToReplaceLength + replacementStringLength);
            
            buffer.append(sourceString.substring(0, indexOfMatch));
            buffer.append(replacementString);
            buffer.append(sourceString.substring(indexOfMatch + stringToReplaceLength, sourceStringLength));
            
            result = buffer.toString();
        }
        else {
            result = sourceString;
        }
        
        return result;
    }    

    /**
     * Removes the spaces in a given string.
     * 
     * @param aString string to remove spaces from
     * @return string without spaces
     */
    public static String escapeSpace(String aString) {
        NSArray<String> parts = NSArray.componentsSeparatedByString(aString, " ");
        return parts.componentsJoinedByString("");
    }

    /** This method runs about 20 times faster than
     * java.lang.String.toLowerCase (and doesn't waste any storage
     * when the result is equal to the input).  Warning: Don't use
     * this method when your default locale is Turkey.
     * java.lang.String.toLowerCase is slow because (a) it uses a
     * StringBuffer (which has synchronized methods), (b) it
     * initializes the StringBuffer to the default size, and (c) it
     * gets the default locale every time to test for name equal to
     * "tr".
     * @see <a href="http://www.norvig.com/java-iaq.html#tolower">tolower</a> 
     * @author Peter Norvig **/
    public static String toLowerCase(String str) {
        if (str == null)
            return null;

        int len = str.length();
        int different = -1;
        // See if there is a char that is different in lowercase
        for(int i = len-1; i >= 0; i--) {
            char ch = str.charAt(i);
            if (Character.toLowerCase(ch) != ch) {
                different = i;
                break;
            }
        }

        // If the string has no different char, then return the string as is,
        // otherwise create a lowercase version in a char array.
        if (different == -1) {
            return str;
        }
        char[] chars = new char[len];
        str.getChars(0, len, chars, 0);
        // (Note we start at different, not at len.)
        for(int j = different; j >= 0; j--) {
            chars[j] = Character.toLowerCase(chars[j]);
        }

        return new String(chars);
    }

    /**
     * String multiplication.
     * @param n the number of times to concatenate a given string
     * @param s string to be multiplied
     * @return multiplied string
     */
    public static String stringWithNtimesString(int n, String s) {
    	StringBuilder sb = new StringBuilder(n);
        for (int i=0; i<n; i++) sb.append(s);
        return sb.toString();
    }

    /**
     * Counts the number of occurrences of a particular
     * <code>char</code> in a given string.
     * @param c char to count in string
     * @param s string to look for specified char in.
     * @return number of occurences of a char in the string
     */
    public static int numberOfOccurrencesOfCharInString(char c, String s) {
        int result=0;
        if (s!=null) {
            for (int i=0; i<s.length();)
                if (s.charAt(i++)==c) result++;
        }
        return result;
    }
    
    /**
     * Simple test if the string is either null or
     * equal to "".
     * @param s string to test
     * @return result of the above test
     */
    public static boolean stringIsNullOrEmpty(String s) {
        return ((s == null) || (s.length() == 0));
    }
    
    /**
     * Simple utility method that will return null
     * if the string passed in is equal to ""
     * otherwise it will return the passed in
     * string.
     * @param s string to test
     * @return null if the string is "" else the string.
     */
    public static String nullForEmptyString(String s) {
        return s==null ? null : (s.length()==0 ? null : s);
    }
    
    /**
     * Simple utility method that will return the
     * string "" if the string passed in is null
     * otherwise it will return the passed in
     * string.
     * @param s string to test
     * @return the empty string if the string is null, else the string
     */
    public static String emptyStringForNull(String s) {
        return s==null ? "" : s;
    }

    public static String escapeNonXMLChars(String str) {
        if (str == null) return null;

        StringBuilder result = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
        	char c = str.charAt(i);
        	switch(c) {
        		case '<': result.append("&lt;"); break;
        		case '>': result.append("&gt;"); break;
        		case '&': result.append("&amp;"); break;
        		case '"': result.append("&quot;"); break;
        		default:
        			result.append(c);
        	}
        }
        
        return result.toString();
    }

    /**
     * XML entities to unescape.
     */
	public static final NSDictionary XML_UNESCAPES;
	
	/**
	 * ISO entities to unescape.
	 */
	public static final NSDictionary ISO_UNESCAPES;
	
	/**
	 * Symbol entities to unescape.
	 */
	public static final NSDictionary SYMBOL_UNESCAPES; 

	/**
	 * Safe HTML entities to unescape (SYMBOL+ISO). This still prevents injection attacks.
	 */
	public static final NSDictionary HTML_SAFE_UNESCAPES; 

	/**
	 * HTML entities to unescape (XML+SYMBOL+ISO).
	 */
	public static final NSDictionary HTML_UNESCAPES; 

	static {
		// NOTE AK I used: 
		// http://www.w3schools.com/tags/ref_symbols.asp
		// http://www.w3schools.com/tags/ref_entities.asp
		// as apache commons lang didn't really work for me?!?
		
		Object[] xml = new Object[] { '<', "lt", '>', "gt", '&', "amp", '\"', "quot" };
		NSMutableDictionary dict = new NSMutableDictionary();
		for (int i = 0; i < xml.length; i+=2) {
			Character charValue = ((Character) xml[i]);
			String key = (String) xml[i+1];
			dict.setObjectForKey(charValue+"", key);
			dict.setObjectForKey(charValue+"", "#"+charValue);
		}
		XML_UNESCAPES = dict.immutableClone();
		
		Object[] iso = { 160, "nbsp", 161, "iexcl", 162, "cent", 163, "pound", 164, "curren", 165, "yen", 166, "brvbar", 167, "sect", 168, "uml", 169, "copy", 170, "ordf", 171, "laquo", 172, "not", 173, "shy", 174, "reg", 175, "macr", 176, "deg", 177, "plusmn", 178, "sup2", 179, "sup3", 180, "acute", 181, "micro", 182, "para", 183, "middot", 184, "cedil", 185, "sup1", 186, "ordm", 187, "raquo", 188, "frac14", 189, "frac12", 190, "frac34", 191, "iquest", 215, "times", 247, "divide", 192, "Agrave", 193, "Aacute", 194, "Acirc", 195, "Atilde", 196, "Auml", 197, "Aring", 198, "AElig", 199, "Ccedil", 200, "Egrave", 201, "Eacute", 202, "Ecirc", 203, "Euml", 204, "Igrave", 205, "Iacute", 206, "Icirc", 207, "Iuml", 208, "ETH", 209, "Ntilde", 210, "Ograve", 211, "Oacute", 212, "Ocirc", 213, "Otilde", 214, "Ouml", 216, "Oslash", 217, "Ugrave", 218, "Uacute", 219, "Ucirc", 220, "Uuml", 221, "Yacute", 222, "THORN", 223, "szlig", 224, "agrave", 225, "aacute", 226, "acirc", 227, "atilde", 228,
				"auml", 229, "aring", 230, "aelig", 231, "ccedil", 232, "egrave", 233, "eacute", 234, "ecirc", 235, "euml", 236, "igrave", 237, "iacute", 238, "icirc", 239, "iuml", 240, "eth", 241, "ntilde", 242, "ograve", 243, "oacute", 244, "ocirc", 245, "otilde", 246, "ouml", 248, "oslash", 249, "ugrave", 250, "uacute", 251, "ucirc", 252, "uuml", 253, "yacute", 254, "thorn", 255, "yuml" };
		
		dict = new NSMutableDictionary();
		for (int i = 0; i < iso.length; i+=2) {
			Integer charValue = ((Integer) iso[i]);
			String key = (String) iso[i+1];
			dict.setObjectForKey(Character.toChars(charValue)[0]+"", key);
			dict.setObjectForKey(Character.toChars(charValue)[0]+"", "#"+charValue);
		}
		ISO_UNESCAPES = dict.immutableClone();
		
		Object[] symbols = new Object[] { 8704, "forall", 8706, "part", 8707, "exists", 8709, "empty", 8711, "nabla", 8712, "isin", 8713, "notin", 8715, "ni", 8719, "prod", 8721, "sum", 8722, "minus", 8727, "lowast", 8730, "radic", 8733, "prop", 8734, "infin", 8736, "ang", 8743, "and", 8744, "or", 8745, "cap", 8746, "cup", 8747, "int", 8756, "there4", 8764, "sim", 8773, "cong", 8776, "asymp", 8800, "ne", 8801, "equiv", 8804, "le", 8805, "ge", 8834, "sub", 8835, "sup", 8836, "nsub", 8838, "sube", 8839, "supe", 8853, "oplus", 8855, "otimes", 8869, "perp", 8901, "sdot", 913, "Alpha", 914, "Beta", 915, "Gamma", 916, "Delta", 917, "Epsilon", 918, "Zeta", 919, "Eta", 920, "Theta", 921, "Iota", 922, "Kappa", 923, "Lambda", 924, "Mu", 925, "Nu", 926, "Xi", 927, "Omicron", 928, "Pi", 929, "Rho", 931, "Sigma", 932, "Tau", 933, "Upsilon", 934, "Phi", 935, "Chi", 936, "Psi", 937, "Omega", 945, "alpha", 946, "beta", 947, "gamma", 948, "delta", 949, "epsilon", 950, "zeta", 951, "eta", 952, "theta",
				953, "iota", 954, "kappa", 955, "lambda", 956, "mu", 957, "nu", 958, "xi", 959, "omicron", 960, "pi", 961, "rho", 962, "sigmaf", 963, "sigma", 964, "tau", 965, "upsilon", 966, "phi", 967, "chi", 968, "psi", 969, "omega", 977, "thetasym", 978, "upsih", 982, "piv", 338, "OElig", 339, "oelig", 352, "Scaron", 353, "scaron", 376, "Yuml", 402, "fnof", 710, "circ", 732, "tilde", 8194, "ensp", 8195, "emsp", 8201, "thinsp", 8204, "zwnj", 8205, "zwj", 8206, "lrm", 8207, "rlm", 8211, "ndash", 8212, "mdash", 8216, "lsquo", 8217, "rsquo", 8218, "sbquo", 8220, "ldquo", 8221, "rdquo", 8222, "bdquo", 8224, "dagger", 8225, "Dagger", 8226, "bull", 8230, "hellip", 8240, "permil", 8242, "prime", 8243, "Prime", 8249, "lsaquo", 8250, "rsaquo", 8254, "oline", 8364, "euro", 8482, "trade", 8592, "larr", 8593, "uarr", 8594, "rarr", 8595, "darr", 8596, "harr", 8629, "crarr", 8968, "lceil", 8969, "rceil", 8970, "lfloor", 8971, "rfloor", 9674, "loz", 9824, "spades", 9827, "clubs", 9829, "hearts",
				9830, "diams" };
		dict = new NSMutableDictionary();
		for (int i = 0; i < symbols.length; i+=2) {
			Integer charValue = ((Integer) symbols[i]);
			String key = (String) symbols[i+1];
			dict.setObjectForKey(Character.toChars(charValue)[0]+"", key);
			dict.setObjectForKey(Character.toChars(charValue)[0]+"", "#"+charValue);
		}
		SYMBOL_UNESCAPES = dict.immutableClone();

		dict = new NSMutableDictionary();
		dict.addEntriesFromDictionary(ISO_UNESCAPES);
		dict.addEntriesFromDictionary(SYMBOL_UNESCAPES);
		HTML_SAFE_UNESCAPES = dict.immutableClone();
		
		dict.addEntriesFromDictionary(XML_UNESCAPES);
		HTML_UNESCAPES = dict.immutableClone();
	}
  
    /**
     * Util to unescape entities. Entities not found in the set will be left intact.
     * @param string string to unescape
     * @param map map of entities
     * @return unescaped string
     */
    public static String unescapeEntities(String string, Map<String, String> map) {
        if(string != null) {
        	StringBuilder result = new StringBuilder();
            int len = string.length();
            for(int start = 0; start < len; start++) {
                char c1 = string.charAt(start);
                if(c1 == '&') {
                	StringBuilder entity = new StringBuilder();
                    for(int end = start+1; end < len; end++) {
                        char c2 = string.charAt(end);
                        if(c2 == ';') {
                            String key = entity.toString();
							String replacement = map.get(key);
                            if(replacement == null) {
                            	replacement = map.get(key.toUpperCase());
                            }
                            if(replacement == null) {
                            	replacement = "&" + key + ";";
                            }
   							result.append(replacement);
                            start = end;
                            break;
                        }
                        entity.append(c2);
                    }
                } else {
                    result.append(c1);
                }
            }
            string = result.toString();
        }
        return string;
    }

    /**
     * Escapes the given PCDATA string as CDATA.
     * @param pcdata The string to escape
     * @return the escaped string
     */
    public static String escapePCData(String pcdata) {
    	if(pcdata == null) { return null; }
    	
    	int start = 0;
    	int end = 0;
    	String close = "]]>";
    	String escape = "]]]]><![CDATA[>";

    	StringBuilder sb = new StringBuilder("<![CDATA[");
    	
    	do {
        	end = pcdata.indexOf(close, start);
    		sb.append(pcdata.substring(start, (end==-1?pcdata.length():end)));
    		if(end != -1) { sb.append(escape); }
    		start = end;
    		start += 3;
    	} while (end != -1);
    	
    	sb.append(close);
    	
    	return sb.toString();
    }

    public static String escapeNonBasicLatinChars(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        if (block != null  &&  Character.UnicodeBlock.BASIC_LATIN.equals(block)) 
            return String.valueOf(c);
        return toHexString(c);
    }

    public static String escapeNonBasicLatinChars(String str) {
        if (str == null) return null;

        StringBuilder result = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) 
            result.append(escapeNonBasicLatinChars(str.charAt(i)));
            
        return result.toString();
    }

    /**
     * Escapes the apostrophes in a Javascript string with a backslash.
     * 
     * @param sourceString the source string to escape
     * @return the escaped javascript string
     */
    public static String escapeJavascriptApostrophes(String sourceString) {
    	return ERXStringUtilities.escape(new char[] { '\'' }, '\\', sourceString);
    }
    
    /**
     * Escapes the given characters with the given escape character in _sourceString.  This 
     * implementation is specifically designed for large strings.  In the event that no characters 
     * are escaped, the original string will be returned with no new object creation.  A null
     * _sourceString will return null.
     * 
     * Example: sourceString = Mike's, escape chars = ', escape with = \, returns Mike\'s
     * 
     * @param _escapeChars the list of characters to escape
     * @param _escapeWith the escape character to use
     * @param _sourceString the string to escape the characters in.
     * @return the escaped string
     */
    public static String escape(char[] _escapeChars, char _escapeWith, String _sourceString) {
      String targetString;
      if (_sourceString == null) {
        targetString = null;
      }
      else {
    	StringBuilder targetBuffer = null;
        int lastMatch = 0;
        int length = _sourceString.length();
        for (int sourceIndex = 0; sourceIndex < length; sourceIndex++) {
          char ch = _sourceString.charAt(sourceIndex);
          boolean escape = false;
          for (int escapeNum = 0; !escape && escapeNum < _escapeChars.length; escapeNum++) {
            if (ch == _escapeChars[escapeNum]) {
              escape = true;
            }
          }
          if (escape) {
            if (targetBuffer == null) {
              targetBuffer = new StringBuilder(length + 100);
            }
            if (sourceIndex - lastMatch > 0) {
              targetBuffer.append(_sourceString.substring(lastMatch, sourceIndex));
            }
            targetBuffer.append(_escapeWith);
            lastMatch = sourceIndex;
          }
        }
        if (targetBuffer == null) {
          targetString = _sourceString;
        }
        else {
          targetBuffer.append(_sourceString.substring(lastMatch, length));
          targetString = targetBuffer.toString();
        }
      }
      return targetString;
    }

    public static String toHexString(char c) {
    	StringBuilder result = new StringBuilder("\u005C\u005Cu9999".length());
        String u = Long.toHexString(c).toUpperCase();
        switch (u.length()) {
            case 1:   result.append("\u005C\u005Cu000");  break;
            case 2:   result.append("\u005C\u005Cu00");   break;
            case 3:   result.append("\u005C\u005Cu0");    break;
            default:  result.append("\u005C\u005Cu");     break;
        }
        result.append(u);
        return result.toString();
    }

    public static String toHexString(String str) {
        if (str == null) return null;

        StringBuilder result = new StringBuilder("\u005C\u005Cu9999".length() * str.length());
        for (int i = 0; i < str.length(); i++) 
            result.append(toHexString(str.charAt(i)));

        return result.toString();
    }

    /**
     * Converts a byte array to hex string.
     * @param block byte array
     * @return hex string
     */
    public static String byteArrayToHexString(byte[] block) {
        int len = block.length;
        StringBuilder buf = new StringBuilder(2 * len);
        for (int i = 0; i < len; ++i) {
            int high = ((block[i] & 0xf0) >> 4);
            int low  =  (block[i] & 0x0f);
            buf.append(HEX_CHARS[high]);
            buf.append(HEX_CHARS[low]);
        }
        return buf.toString();
    }
    
    /**
     * Converts a even-length, hex-encoded String to a byte array.
     * 
     * @param hexString hex string to convert
     * @return byte array of given hex string
     */
    public static byte[] hexStringToByteArray(String hexString) {
    	int length = hexString.length();
    	if(length % 2 == 1) {
    		throw new IllegalArgumentException("String must have even length: " + length);
    	}
    	byte array[] = new byte[length/2];

		for(int i = 0; i < array.length; i++) {
			char c1 = hexString.charAt(i*2);
			char c2 = hexString.charAt(i*2+1);
			byte b = 0;
            if(c1 >= '0' && c1 <= '9')
                b += (c1 - 48) * 16;
            else if(c1 >= 'a' && c1 <= 'f')
                b += ((c1 - 97) + 10) * 16;
            else if(c1 >= 'A' && c1 <= 'F')
                b += ((c1 - 65) + 10) * 16;
            else
                throw new IllegalArgumentException("Illegal Character: '" + c1 + "' in " + hexString);
            if(c2 >= '0' && c2 <= '9')
                b += c2 - 48;
            else if(c2 >= 'a' && c2 <= 'f')
                b += (c2 - 97) + 10;
            else if(c2 >= 'A' && c2 <= 'F')
                b += (c2 - 65) + 10;
            else
                throw new IllegalArgumentException("Illegal Character: '" + c2 + "' in " + hexString);
            array[i] = b;

		}
		return array;
    }
    
    /** 
     * Cleans up the given version string by removing extra 
     * dots(.), for example, 5.1.3 becomes 5.13, so that 
     * the string can be converted to a double or BigDecimal 
     * type easily. 
     * 
     * @param version string 
     * @return cleaned-up string that only contains the 
     *           first dot(.) as the floating point indicator. 
     */
    public static String removeExtraDotsFromVersionString(String version) {
        int floatingPointIndex = version.indexOf("."); 
        if (floatingPointIndex >= 0  &&  floatingPointIndex + 1 < version.length()) {
            String minorVersion = StringUtils.replace(version.substring(floatingPointIndex + 1), ".", "");
            version = version.substring(0, floatingPointIndex + 1) + minorVersion;
        }
        return version;
    }

    /**
     * Capitalizes a given string. That is, the first character of the returned
     * string will be upper case, and other characters will be unchanged. For
     * example, for the input string "{@code you have a dog}", this method would
     * return "{@code You have a dog}".
     * 
     * @param value to be capitalized
     * @return capitalized string
     */
    public static String capitalize(String value) {
        String capital = null;
        if (value != null && value.length() > 0) {
        	StringBuilder buffer = new StringBuilder(value);

            buffer.setCharAt(0, Character.toUpperCase(value.charAt(0)));
            capital = buffer.toString();            
        }
        return capital != null ? capital : value;
    }

    /**
     * Uncapitalizes a given string.
     * @param value to be uncapitalized
     * @return capitalized string
     */
    public static String uncapitalize(String value) {
        String capital = null;
        if (value != null) {
        	int length = value.length();
        	if (length > 0) {
        		StringBuilder buffer = new StringBuilder(value);
	            for (int i = 0; i < length; i ++) {
	            	char ch = value.charAt(i);
	            	if (i == 0 || i == length - 1 || (i < length - 1 && Character.isUpperCase(value.charAt(i + 1)))) {
	                    buffer.setCharAt(i, Character.toLowerCase(ch));
	            	}
	            	else {
	            		break;
	            	}
	            }
	            capital = buffer.toString();
        	}
        }
        return capital != null ? capital : value;
    }
    
    /**
     * Capitalizes all the strings in a given string. That is, the first
     * character of each (whitespace-delimited) word in the input string will be
     * upper case, and other characters will be unchanged. Additionally, each
     * region of contiguous whitespace in the original string is converted to a
     * single space in the result. For example, for the input string
     * "{@code you  have  a  dog}" (with two spaces between each word), this
     * method would return "{@code You Have A Dog}".
     * 
     * @param value to be capitalized
     * @return capitalized string
     */    
    public static String capitalizeAllWords(String value) {
        String capitalize = null;
        if (value != null && value.length() > 0) {
        	StringBuilder buffer = new StringBuilder();
            boolean first = true;
            for (StringTokenizer tokenizer = new StringTokenizer(value); tokenizer.hasMoreElements();) {
                String token = tokenizer.nextToken();
                if (!first) {
                    buffer.append(' ');
                } else {
                    first = false;
                }
                buffer.append(capitalize(token));
            }
            capitalize = buffer.toString();
        }
        return capitalize != null ? capitalize : value;
    }
    
    /**
     * Converts this_is_a_test to ThisIsATest
     * @param underscoreString the string_with_underscores
     * @param capitalize if true, the first letter is capitalized
     * @return the StringWithoutUnderscores
     */
    public static String underscoreToCamelCase(String underscoreString, boolean capitalize) {
    	StringBuilder camelCase = new StringBuilder();
    	String[] underscoreStrings = underscoreString.split("_");
    	for (int i = 0; i < underscoreStrings.length; i ++) {
    		String word;
    		if (i > 0 || capitalize) {
    			word = ERXStringUtilities.capitalize(underscoreStrings[i]);
    		}
    		else {
    			word = underscoreStrings[i];
    		}
			camelCase.append(word);
    	}
    	return camelCase.toString();
    }
    
    /**
     * Converts a string in camel case to an underscore representation.
     * 
     * @param camelString string to convert
     * @param lowercase if all uppercase characters should be converted to lowercase
     * @return the string_with_underscores
     */
    public static String camelCaseToUnderscore(String camelString, boolean lowercase) {
    	StringBuilder underscore = new StringBuilder();
    	boolean lastCharacterWasWordBreak = false;
    	boolean lastCharacterWasCapital = false;
    	int length = camelString.length();
    	for (int i = 0; i < length; i ++) {
    		char ch = camelString.charAt(i);
    		if (Character.isUpperCase(ch)) {
    			boolean isLastCharacter = (i == length - 1); 
    			boolean nextCharacterIsCapital =  (!isLastCharacter && Character.isUpperCase(camelString.charAt(i + 1)));
    			if (i > 0 && ((!lastCharacterWasWordBreak && !lastCharacterWasCapital) || (!nextCharacterIsCapital && !isLastCharacter))) {
    				underscore.append("_");
    				lastCharacterWasWordBreak = true;
    			}
    			else {
    				lastCharacterWasWordBreak = false;
    			}
    			lastCharacterWasCapital = true;
    		}
    		else if (ch == '_') {
    			lastCharacterWasWordBreak = true;
    			lastCharacterWasCapital = false;
    		}
    		else {
    			lastCharacterWasWordBreak = false;
    			lastCharacterWasCapital = false;
    		}
    		if (lowercase) {
    			underscore.append(Character.toLowerCase(ch));
    		}
    		else {
    			underscore.append(ch);
    		}
    	}
    	return underscore.toString();
    }
    
    public static boolean stringEqualsString(String s1, String s2) {
        if (s1 == s2) return true;
        if (s1 != null && s2 != null && s1.equals(s2)) return true;
        if (s1 == null && s2 == null) return true;
        return false;
    }
    
    /**
     * Tests if the string starts with the specified prefix ignoring case.  This method is
     * optimized so that it only converts the relevant substring of stringToSearch to lowercase
     * before comparing it to the lowercase version of prefix.
     * @param stringToSearch string to check
     * @param prefix prefix to look for
     * @return true if stringToSearch case-insensitively starts with prefix
     */
    public static boolean caseInsensitiveStartsWith(String stringToSearch, String prefix) {
        return caseInsensitiveStartsWith(stringToSearch, prefix, 0);
    }
    
    /**
     * Tests if the string starts with the specified prefix starting at the specified index ignoring case.
     * This method is optimized so that it only converts the relevant substring of stringToSearch to lowercase
     * before comparing it to the lowercase version of prefix.
     * @param stringToSearch string to check
     * @param prefix prefix to look for
     * @param toffset starting offset to perform the search
     * @return true if stringToSearch case-insensitively starts with prefix starting at toffset
     */
    public static boolean caseInsensitiveStartsWith(String stringToSearch, String prefix, int toffset) {
        boolean result = false;
        final int stringToSearchLength = stringToSearch.length();
        final int prefixLength = prefix.length();
        
        if ( (toffset + prefixLength) <= stringToSearchLength ) {
            final String slice = stringToSearch.substring(toffset, toffset+prefixLength);
            
            result = toLowerCase(slice).equals(toLowerCase(prefix));
        }
        
        return result;
    }
    
    /**
     * This method takes a string and returns a string which is the first string such that the
     * result byte length in the specified encoding does not exceed the byte limit.  This tends
     * to be an issue with UTF-8 and Japanese characters because they're double- or triple-byte
     * in UTF-8 and you need to be careful not to split in the middle of a multi-byte sequence.
     *
     * <p>
     *
     * This method is optimized for the UTF-8 case.  If <code>encoding</code> is either "UTF-8" or "UTF8",
     * the optimized case will kick in.
     *
     * @param inputString string to truncate
     * @param byteLength maximum byte length
     * @param encoding encoding to use
     * @return string truncated appropriately.
     */
    public static String stringByTruncatingStringToByteLengthInEncoding(final String inputString, final int byteLength, final String encoding) {
        String result = null;
        
        if ( log.isDebugEnabled() ) {
            log.debug("stringByTruncatingStringToByteLengthInEncoding: encoding='" + encoding + "', byteLength=" +
                      byteLength + ", inputString='" + inputString + "'");
        }
        
        if ( inputString != null ) {
            final byte[] bytes = toBytes(inputString, encoding);
            
            if ( bytes != null ) {
                if ( bytes.length > byteLength ) {
                    // we gotta do some work
                    if ( "UTF-8".equals(encoding) || "UTF8".equals(encoding) ) {
                        // if the encoding is UTF-8, we can be smarter about this than in the general case.
                        // UTF-8 defines three types of bytes: those that begin in 0, those that begin in 11 and
                        // those that begin in 10.  0 means ASCII, 11 means the beginning of a multi-byte sequence,
                        // 10 means in the middle or end of a multi-byte sequence.
                        //
                        // so, we hit the byte at index byteLength which is one past our target byte length.  while
                        // that byte begins with 10, we walk backwards until the next byte doesn't begin with 10.  at that
                        // point, we're at the end of a character.
                        if ( byteLength >= 1 ) {
                            int index = byteLength - 1;
                            
                            while ( index >= 0 && ((bytes[index+1] & 0xC0) == 0x80) )
                                index--;
                            
                            if ( index > 0 ) {
                                try {
                                    result = new String(bytes, 0, index+1, encoding);
                                }
                                catch ( UnsupportedEncodingException e ) {
                                    throw new RuntimeException("Got " + e.getClass() + " exception.  byteLength=" + byteLength + ", encoding='" +
                                                               encoding + "', inputString='"+ inputString + "'.", e);
                                }
                            }
                        }
                        
                        result = result != null ? result : "";
                    }
                    else {
                        final Charset charset = Charset.forName(encoding);
                        final CharsetDecoder decoder = charset.newDecoder();
                        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                        int currentLength = byteLength;
                        CharBuffer charBuffer = null;
                        
                        do {
                            byteBuffer.position(0);
                            byteBuffer.limit(currentLength);
                            
                            try {
                                charBuffer = decoder.reset().decode(byteBuffer);
                            }
                            catch ( MalformedInputException e ) {
                                // this exception we expect to get if when we slice the byte buffer to
                                // the intended byte length, we end up mid-way through a byte sequence.
                                currentLength--;
                            }
                            catch (CharacterCodingException e ) {
                                // we're not expecting to get this exception.  the javadoc doesn't say
                                // under what circumstances it happens and it would be surprising it this happened
                                // and the conversion to byte array worked earlier.
                                log.error("Got " + e.getClass() + " exception. byteLength=" + byteLength + ", encoding='" + encoding +
                                          "', inputString='"+ inputString + "'.", e);
                                break;
                            }
                        } while ( charBuffer == null && currentLength > 0 );
                        
                        result = charBuffer != null ? charBuffer.toString() : "";
                    }
                }
                else {
                    result = inputString;
                }
            }
        }
        
        if ( log.isDebugEnabled() )
            log.debug("stringByTruncatingStringToByteLengthInEncoding: result='" + result + "'");
        
        return result;
    }    

    /** checks if the specified String contains only digits. 
     * 
     * @param aString the string to check
     * @return true if the string contains only digits, false otherwise
     */
    public static boolean isDigitsOnly(String aString) {
        for (int i = aString.length(); i-- > 0;) {
            char c = aString.charAt(i);
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /** checks if the specified String contains only Letters. 
     * 
     * @param aString the string to check
     * @return true if the string contains only Letters, false otherwise
     */
    public static boolean isLettersOnly(String aString) {
        for (int i = aString.length(); i-- > 0;) {
            char c = aString.charAt(i);
            if (!Character.isLetter(c)) return false;
        }
        return true;
    }

    /** checks if the String contains a character that has a special meaning
     * in regex. This could used to ensure that username and passwords have no
     * such characters.
     * 
     * @param s the string to check
     * @return <code>true</code> if s contains one or multiple characters that have special
     * meanings in regex.
     */
    public static boolean stringContainsSpecialRegexCharacters(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (SpecialRegexCharacters.indexOf(s.charAt(i)) > -1) { return true; }
        }
        return false;
    }

    /**
     * Returns a string from the contents of the given URL.
     * 
     * @param url the URL to read from
     * @return the string that was read
     * @throws IOException if the connection fails
     */
    public static String stringFromURL(URL url) throws IOException {
    	InputStream is = url.openStream();
    	try {
    		return ERXStringUtilities.stringFromInputStream(is);
    	}
    	finally {
    		is.close();
    	}
    }

    /**
     * Returns a string from the contents of the given URL.
     * 
     * @param url the URL to read from
     * @param encoding the string encoding to read with
     *
     * @return the string that was read
     * @throws IOException if the connection fails
     */
    public static String stringFromURL(URL url, String encoding) throws IOException {
    	InputStream is = url.openStream();
    	try {
    		return ERXStringUtilities.stringFromInputStream(is, encoding);
    	}
    	finally {
    		is.close();
    	}
    }

    /**
         * Returns a string from the input stream using the default
          * encoding.
          * @param in stream to read
          * @return string representation of the stream.
     * @throws IOException if things go wrong
      */
     public static String stringFromInputStream(InputStream in) throws IOException {
         return new String(ERXFileUtilities.bytesFromInputStream(in));
     }

     /**
      * Returns a string from the input stream using the default
       * encoding.
       * @param in stream to read
       * @param encoding to be used, null will use the default
       * @return string representation of the stream.
      * @throws IOException if things go wrong
   */
     public static String stringFromInputStream(InputStream in, String encoding) throws IOException {
         return new String(ERXFileUtilities.bytesFromInputStream(in), encoding);
     }

  
      /**
       * Returns a String by invoking toString() on each object from the array. After each toString() call
       * the separator is appended to the buffer.
       * 
       * @param array an object array from which to get a nice String representation
       * @param separator a separator which is displayed between the objects toString() value
       *
       * @return a string representation from the array
       */
    public static String toString(Object[] array, String separator) {
    	StringBuilder buf = new StringBuilder();
          for (int i = 0; i < array.length; i++) {
              Object o = array[i];
              buf.append(o.toString());
              buf.append(separator);
          }
          return buf.toString();
      }

    /**
     * Creates a readable debug string for some data types (dicts, arrays, adaptorOperations, databaseOperations).
     * 
     * @param object the object to dump
     * @return string representation of the given object
     */
    public static String dumpObject(Object object) {
		StringBuffer sb = new StringBuffer(4000);
		dumpObject(sb, object, 0);
		return sb.toString();
	}

	/**
	 * Checks if any of the characters specified in characters is contained in the string
	 * specified by source.
	 * 
	 * @param source the String which might contain characters
	 * @param characters the characters to check
	 * @return true if any character from characters is in source, false otherwise
	 */
	public static boolean containsAnyCharacter(String source, String characters) {
		for (int i = source.length(); i-- > 0;) {
			char c = source.charAt(i);
			if (characters.indexOf(c) > -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes any character which is not in characters from the source string.
	 * 
	 * @param source the string which will be modified
	 * @param characters the characters that are allowed to be in source
	 * @return a new string only with characters from the characters argument
	 */
	public static String removeExceptCharacters(String source, String characters) {
		StringBuilder buf = new StringBuilder();
		int l = source.length();
		for (int i = 0; i < l; i++) {
			char c = source.charAt(i);
			if (characters.indexOf(c) > -1) {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	/**
	 * Removes any character which is in characters from the source string.
	 * 
	 * @param source the string which will be modified
	 * @param characters the characters that are not allowed to be in source
	 * @return a new string without any characters from the characters argument
	 */
	public static String removeCharacters(String source, String characters) {
		StringBuilder buf = new StringBuilder();
		int l = source.length();
		for (int i = 0; i < l; i++) {
			char c = source.charAt(i);
			if (characters.indexOf(c) == -1) {
				buf.append(c);
			}
		}
		return buf.toString();
	}

  /**
   * Matches strings like Quicksilver (NullPointerException is matched by "NPE").  The rule
   * is basically just that all the letters of the search string must appear in the original
   * string in the same order as the search string, but they are not required to be contiguous
   * or case-matched.
   * 
   * @param _str the string to search in
   * @param _searchString the search string to look for
   * @return whether or not _str contains _searchString
   */
  public static boolean quicksilverContains(String _str, String _searchString) {
    boolean equals;
    if (_str == null) {// || _searchString == null || _searchString.length() == 0) {
      equals = false;
    }
    else {
      equals = true;
      if (_searchString != null && _searchString.length() > 0) {
        int searchStringLength = _searchString.length();
        int strLength = _str.length();
        int strPos = 0;
        for (int searchStringPos = 0; equals && searchStringPos < searchStringLength; searchStringPos++) {
          char searchStringCh = Character.toLowerCase(_searchString.charAt(searchStringPos));
          boolean searchStringChFound = false;
          for (; !searchStringChFound && strPos < strLength; strPos++) {
            char strCh = _str.charAt(strPos);
            searchStringChFound = (Character.toLowerCase(strCh) == searchStringCh);
          }
          if (!searchStringChFound) {
            equals = false;
          }
        }
      }
    }
    return equals;
  }

  /**
   * Generate an MD5 hash from a String.
   *
   * @param str the string to hash
   * @param encoding MD5 operates on byte arrays, so we need to know the encoding to getBytes as
   * @return the MD5 sum of the bytes
   * @exception IOException
   */
  public static byte[] md5(String str, String encoding) {
	byte[] bytes;
	if (str == null) {
		bytes = new byte[0];
	}
	else {
	  	try {
	  		if(encoding == null) {
	  			encoding = CharEncoding.UTF_8;
	  		}
			bytes = ERXFileUtilities.md5(new ByteArrayInputStream(str.getBytes(encoding)));
		}
		catch (UnsupportedEncodingException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		catch (IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
	return bytes;
  }

  /**
   * Generate an MD5 hash as hex from a String.
   *
   * @param str the string to hash
   * @param encoding MD5 operates on byte arrays, so we need to know the encoding to getBytes as
   * @return the MD5 sum of the bytes in a hex string
   * @exception IOException
   */
  public static String md5Hex(String str, String encoding) {
	  String hexStr;
	  if (str == null) {
		  hexStr = null;
	  }
	  else {
		  hexStr = ERXStringUtilities.byteArrayToHexString(ERXStringUtilities.md5(str, encoding));
	  }
	  return hexStr;
  }
  
  /**
   * Returns a string case-matched against the original string.  For instance, if originalString
   * is "Mike" and newString is "john", this returns "John".  If originalString is "HTTP" and
   * newString is "something", this returns "SOMETHING". 
   * 
   * @param originalString the original string to analyze the case of
   * @param newString the new string
   * @return the case-matched variant of newString
   */
  public static String matchCase(String originalString, String newString) {
	  String matchedCase = newString;
	  if (matchedCase != null) {
		  int length = originalString.length();
		  if (length > 0) {
			  boolean uppercase = true;
			  boolean lowercase = true;
			  boolean capitalize = true;
			  
			  for (int i = 0; i < length; i ++) {
				  char ch = originalString.charAt(i);
				  if (Character.isUpperCase(ch)) {
					  lowercase = false;
					  if (i > 0) {
						  capitalize = false;
					  }
				  }
				  else {
					  uppercase = false;
					  if (i == 0) {
						  capitalize = false;
					  }
				  }
			  }
			  
			  if (capitalize) {
				  matchedCase = ERXStringUtilities.capitalize(newString);
			  }
			  else if (uppercase) {
				  matchedCase = newString.toUpperCase();
			  }
			  else if (lowercase) {
				  matchedCase = newString.toLowerCase();
			  }
		  }
	  }
	  return matchedCase;
  }

	// ##########################################################################################
    // private methods
    // ##########################################################################################
    
  public static void indent(PrintWriter writer, int level) {
  	for (int i = 0; i < level; i++) {
  	  writer.append("  ");
	}
  }
  
    public static void indent(StringBuffer sb, int level) {
    	for (int i = 0; i < level; i++) {
			sb.append("  ");
		}
    }
    
   private static void dumpArray(StringBuffer sb, NSArray array, int level) {
    	sb.append("(\n");
    	for(Enumeration e = array.objectEnumerator(); e.hasMoreElements();) {
    		Object value = e.nextElement();
    		dumpObject(sb, value, level+1);
    		sb.append(",\n");
    	}
   		indent(sb, level);
    	sb.append(")");
    }
    
    private static void dumpDictionary(StringBuffer sb, NSDictionary dict, int level) {
    	sb.append("{\n");
    	for(Enumeration e = dict.keyEnumerator(); e.hasMoreElements();) {
    		Object key = e.nextElement();
    		Object value = dict.objectForKey(key);
    		indent(sb, level+1);
    		sb.append(key).append(" = ");
    		dumpObject(sb, value, level+1);
    		sb.append(";\n");
    	}
    	indent(sb, level);
    	sb.append("}");
    }
    
    private static NSDictionary databaseOperationAsDictionary(EODatabaseOperation op) {
    	NSMutableDictionary dict = new NSMutableDictionary(8);
    	int operator = op.databaseOperator();
    	if(operator == 0) {
    		dict.setObjectForKey("EODatabaseNothingOperator", "_databaseOperator");
     	} else if(operator == 1) {
    		dict.setObjectForKey("EODatabaseInsertOperator", "_databaseOperator");
     	} else if(operator == 3) {
    		dict.setObjectForKey("EODatabaseDeleteOperator", "_databaseOperator");
     	} else if(operator == 2) {
    		dict.setObjectForKey("EODatabaseUpdateOperator", "_databaseOperator");
    	} else {
    		dict.setObjectForKey("<unrecognized value>", "_databaseOperator");
    	}
    	if(op.newRow() != null)
    		dict.setObjectForKey(op.newRow(), "_newRow");
    	if(op.dbSnapshot() != null)
    		dict.setObjectForKey(op.dbSnapshot(), "_dbSnapshot");
    	if(op.globalID() != null)
    		dict.setObjectForKey(op.globalID(), "_globalID");
    	if(op.entity() != null)
    		dict.setObjectForKey(op.entity().name(), "_entity");
    	if(op.adaptorOperations() != null)
    		dict.setObjectForKey(op.adaptorOperations(), "_adaptorOps");
    	if(op.object() != null)
    		dict.setObjectForKey(op.object().toString(), "_object");
       return dict;  	
    }
    
    /**
     * Debug method to get the EOAdaptorOperation as a dictionary that can be pretty-printed later.
     * The output from a EOGeneralAdaptorException.userInfo.toString is pretty much unreadable.
     * @param op
     */
    private static NSDictionary adaptorOperationAsDictionary(EOAdaptorOperation op) {
    	NSMutableDictionary dict = new NSMutableDictionary();
    	int operator = op.adaptorOperator();
    	if(operator == 0) {
    		dict.setObjectForKey("EOAdaptorLockOperator", "_adaptorOperator");
       	} else if (operator == 1) {
    		dict.setObjectForKey("EOAdaptorInsertOperator", "_adaptorOperator");
       	} else if (operator == 3) {
    		dict.setObjectForKey("EOAdaptorDeleteOperator", "_adaptorOperator");
       	} else if (operator == 2) {
    		dict.setObjectForKey("EOAdaptorUpdateOperator", "_adaptorOperator");
       	} else {
    		dict.setObjectForKey("<unrecognized value>", "_adaptorOperator");
    	}
    	if(op.entity() != null)
    		dict.setObjectForKey(op.entity(), "_entity");
    	if(op.qualifier() != null)
    		dict.setObjectForKey(op.qualifier().toString(), "_qualifier");
    	if(op.changedValues() != null)
    		dict.setObjectForKey(op.changedValues(), "_changedValues");
    	if(op.exception() != null)
    		dict.setObjectForKey(op.exception(), "_exception");
    	return dict;
    }
    
    private static void dumpObject(StringBuffer sb, Object value, int level) {
    	if(value instanceof NSDictionary) {
    		dumpDictionary(sb, (NSDictionary)value, level);
    	} else if (value instanceof NSArray) {
    		dumpArray(sb, (NSArray)value, level);
    	} else if (value instanceof NSData) {
    		NSData data = (NSData)value;
			sb.append(byteArrayToHexString(data.bytes()));
    	} else if (value instanceof EODatabaseOperation) {
    		dumpDictionary(sb, databaseOperationAsDictionary((EODatabaseOperation)value), level);
    	} else if (value instanceof EOAdaptorOperation) {
    		dumpDictionary(sb, adaptorOperationAsDictionary((EOAdaptorOperation)value), level);
     	} else {
       		indent(sb, level);
    		sb.append(value);
    	}
    }

    /**
     * "Borrowed" from 1.5's Class.getSimpleBinaryName
     */
	private static String getSimpleBinaryName(Class clazz) {
		Class declaringClass = clazz.getDeclaringClass();
		if (declaringClass == null) {
			return null;
		}
		try {
			return clazz.getName().substring(declaringClass.getName().length());
		}
		catch (IndexOutOfBoundsException e) {
			throw new InternalError("Malformed class name");
		}
	}

    /**
     * "Borrowed" from 1.5's Class.isAsciiDigit
     */
    private static boolean isAsciiDigit(char c) {
    	return '0' <= c && c <= '9';
    }


    /**
     * "Borrowed" from 1.5's Class.getSimpleClassName
     */
	public static String getSimpleClassName(Class clazz) {
		if (clazz.isArray()) {
			return ERXStringUtilities.getSimpleClassName(clazz.getComponentType()) + "[]";
		}
		String declaringClassName = ERXStringUtilities.getSimpleBinaryName(clazz);
		if (declaringClassName == null) {
			declaringClassName = clazz.getName();
			return declaringClassName.substring(declaringClassName.lastIndexOf(".") + 1);
		}
		int i = declaringClassName.length();
		if (i < 1 || declaringClassName.charAt(0) != '$') {
			throw new InternalError("Malformed class name");
		}
		int j;
		for (j = 1; j < i && ERXStringUtilities.isAsciiDigit(declaringClassName.charAt(j)); j++) {
		}
		return declaringClassName.substring(j);
	}

	/**
	 * Same as NSPropertySerialization except it sorts on keys first.
	 * @param dict
	 */
	public static String stringFromDictionary(NSDictionary dict) {
		NSArray orderedKeys = dict.allKeys();
		orderedKeys = ERXArrayUtilities.sortedArraySortedWithKey(orderedKeys, "toString.toLowerCase");
		StringBuilder result = new StringBuilder();
		for (Enumeration keys = orderedKeys.objectEnumerator(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			Object value = dict.objectForKey(key);
			String stringValue = NSPropertyListSerialization.stringFromPropertyList(value);
			String stringKey = NSPropertyListSerialization.stringFromPropertyList(key);
			if(!(value instanceof String)) {
				stringValue = stringValue.replaceAll("\n", "\n\t");
			}
			result.append("\t");
			result.append(stringKey);
			result.append(" = ");
			result.append(stringValue);
			result.append(";\n");
		}
		return "{\n" + result + "}\n";
	}
	
	/**
	 * It's ridiculous that StringBuffer doesn't have a .regionMatches like String.  This is
	 * stolen from String and re-implemented on top of StringBuffer.  It's slightly slower than
	 * String's because we have to call charAt instead of just accessing the underlying array,
	 * but so be it.
	 * 
	 * @param str the StringBuffer to compare a region of
     * @param toffset the starting offset of the sub-region in this string.
     * @param other the string argument.
     * @param ooffset the starting offset of the sub-region in the string argument.
     * @param len the number of characters to compare.
     * @return <code>true</code> if the specified sub-region of this string
     *         exactly matches the specified sub-region of the string argument;
     *         <code>false</code> otherwise.
	 */
    public static boolean regionMatches(StringBuffer str, int toffset, String other, int ooffset, int len) {
		int to = toffset;
		int po = ooffset;
		// Note: toffset, ooffset, or len might be near -1>>>1.
		int count = str.length();
		int otherCount = other.length();
		if ((ooffset < 0) || (toffset < 0) || (toffset > (long)count - len) || (ooffset > (long)otherCount - len)) {
		   return false;
		}
		while (len-- > 0) {
		   if (str.charAt(to++) != other.charAt(po++)) {
		       return false;
		   }
		}
		return true;
    }

    /**
     * Converts source to be suitable for use as an identifier in JavaScript.  prefix is prefixed to source
     * if the first character of source is not suitable to start an identifier (e.g. a number).  Any characters
     * in source that are not allowed in an identifier are replaced with replacement.
     * 
     * @see Character#isJavaIdentifierStart(char)
     * @see Character#isJavaIdentifierPart(char)
     * 
     * @param source String to make into a identifier name
     * @param prefix String to prefix source with to make it a valid identifier name
     * @param replacement character to use to replace characters in source that are no allowed in an identifier name
     * @return source converted to a name suitable for use as an identifier in JavaScript
     */
    public static String safeIdentifierName(String source, String prefix, char replacement)
    {
    	StringBuilder b;
    	// Add prefix if source does not start with valid character
        if (source == null || source.length() == 0 || Character.isJavaIdentifierStart(source.charAt(0))) {
            b = new StringBuilder(source);
        } else {
        	b = new StringBuilder(prefix);
        	b.append(source);
        }
    	
        for (int i = 0; i < b.length(); i++) {
            char c = b.charAt(i);
            if ( ! Character.isJavaIdentifierPart(c)) {
                b.setCharAt(i, replacement);
            }
        }

        return b.toString();
    }

    /**
     * Convenience method to call safeIdentifierName(source, prefix, '_')
     * 
     * @see #safeIdentifierName(String, String, char)
     * 
     * @param source String to make into a identifier name
     * @param prefix String to prefix source with to make it a valid identifier name
     * @return source converted to a name suitable for use as an identifier in JavaScript
     */
    public static String safeIdentifierName(String source, String prefix) {
    	return safeIdentifierName(source, prefix, '_');

    }
    
    /**
     * Convenience method to call safeIdentifierName(source, "_", '_')
     *
     * @see #safeIdentifierName(String, String, char)
     * 
     * @param source String to make into a identifier name
     * @return source converted to a name suitable for use as an identifier in JavaScript
     */
    public static String safeIdentifierName(String source) {
    	return safeIdentifierName(source, "_", '_');

    }
    
    
    /**
     * Utility to encode an URL without the try/catch. Throws an NSForwardException in the unlikely case that ERXMessageEncoding.defaultEncoding() can't be found.
     * @param string
     */
    public static String urlEncode(String string) {
    	try {
			return URLEncoder.encode(string, ERXMessageEncoding.defaultEncoding());
		}
		catch (UnsupportedEncodingException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
    }
    
    /**
     * Utility to decode an URL without the try/catch. Throws an NSForwardException in the unlikely case that ERXMessageEncoding.defaultEncoding() can't be found.
     * @param string
     */
    public static String urlDecode(String string) {
    	try {
			return URLDecoder.decode(string, ERXMessageEncoding.defaultEncoding());
		}
		catch (UnsupportedEncodingException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
    }

    /**
     * Utility to convert to UTF-8 bytes without the try/catch. Throws an NSForwardException in the unlikely case that your encoding can't be found.
     * @param string string to convert
     */
    public static byte[] toUTF8Bytes(String string) {
    	return toBytes(string, CharEncoding.UTF_8);
    }

    /**
     * Utility to convert to bytes without the try/catch. Throws an NSForwardException in the unlikely case that your encoding can't be found.
     * @param string string to convert
     * @param encoding
     */
    public static byte[] toBytes(String string, String encoding) {
    	if(string == null) {
    		return null;
    	}
    	try {
			return string.getBytes(encoding);
		}
		catch (UnsupportedEncodingException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
    }


    /**
     * Utility to convert from UTF-8 bytes without the try/catch. Throws an NSForwardException in the unlikely case that your encoding can't be found.
     * @param bytes string to convert
     */
    public static String fromUTF8Bytes(byte bytes[]) {
    	return fromBytes(bytes, CharEncoding.UTF_8);
    }

    /**
     * Utility to convert from bytes without the try/catch. Throws an NSForwardException in the unlikely case that your encoding can't be found.
     * @param bytes string to convert
     * @param encoding
     */
    public static String fromBytes(byte bytes[], String encoding) {
    	if(bytes == null) {
    		return null;
    	}
    	try {
			return new String(bytes, encoding);
		}
		catch (UnsupportedEncodingException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
    }

    /**
     * Pads a string to the specified number of chars by adding the the given pad char on the right side.  If the
     * string is longer than paddedLength, it is returned unchanged.
     *
     * @param string the string to pad
     * @param padChar the character to pad with
     * @param paddedLength the length to pad to
     * @return the padded string
     */
    public static String rightPad(String string, char padChar, int paddedLength) {
    	if (string.length() >= paddedLength) {
    		return string;
    	}
    	
    	StringBuilder buffer = new StringBuilder(string);
        for (int i = string.length(); i < paddedLength; i++) {
            buffer.append(padChar);
        }
        return buffer.toString();
    }
    
    
    /**
     * Pads a string to the specified number of chars by adding the the given pad char on the left side.  If the
     * string is longer than paddedLength, it is returned unchanged.
     *
     * @param string the string to pad
     * @param padChar the character to pad with
     * @param paddedLength the length to pad to
     * @return the padded string
     */
    public static String leftPad(String string, char padChar, int paddedLength) {
    	if (string.length() >= paddedLength) {
    		return string;
    	}
    	
    	StringBuilder buffer = new StringBuilder();
        for (int i = string.length(); i < paddedLength; i++) {
            buffer.append(padChar);
        }
        buffer.append(string);
        return buffer.toString();
    }
    
    /**
     * Inserts the a string into a nother string at a particular offset.
     * 
     * @param destinationString the string to insert into
     * @param contentToInsert the string to insert
     * @param insertOffset the offset in destinationString to insert
     * @return the resulting string
     */
    public static String insertString(String destinationString, String contentToInsert, int insertOffset) {
    	String result;
    	if (destinationString == null) {
    		if (insertOffset > 0) {
    			throw new IndexOutOfBoundsException("You attempted to insert '" + contentToInsert + "' into an empty string at the offset " + insertOffset + ".");
    		}
    		result = contentToInsert;
    	}
    	else {
			StringBuilder sb = new StringBuilder(destinationString.length() + contentToInsert.length());
			sb.append(destinationString.substring(0, insertOffset));
			sb.append(contentToInsert);
			sb.append(destinationString.substring(insertOffset));
			result = sb.toString();
    	}
		return result;
    }
    
    /**
    * Null-safe wrapper for java.lang.String.trim
    * @param s string to trim
    * @return trimmed string or null if s was null
    */
    public static String trimString(String s) {
    	if (s == null) {
    		return s;
    	}
    	return s.trim();
    }
    
    /**
     * Removes line breaks and quotes the string if necessary
     * 
     * @param s
     * 
     * @return the string in Excel save CSV format
     */
    public static String excelSafeCsvString(String s) {
		if (s != null) {
			boolean mustQuote = false;
			s = unquote(s, "\"");
			s = s.replaceAll("\r", "");
			s = s.replaceAll("\n", "");
			if (s.contains("\"")) {
				s = s.replaceAll("\"", "\"\"");
				mustQuote = true;
			}
			if (s.contains(","))
				mustQuote = true;
			if (mustQuote)
				s = quote(s, "\"");
		}
		return s;
	}

	/**
	 * Remove the quote symbols from the given string
	 * 
	 * @param s 
	 * @param quoteSymbol
	 * 
	 * @return the string unquoted
	 */
	public static String unquote(String s, String quoteSymbol) {
		if (s == null || quoteSymbol == null)
			throw new IllegalArgumentException("Neither the string nor the quote symbol are allowed to be null");
		if (s.startsWith(quoteSymbol) && s.endsWith(quoteSymbol)) {
			s = s.substring(1);
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	/**
	 * Quote the given string with the provided quote symbols
	 * 
	 * @param s the string to quote
	 * @param quoteSymbol - the quote symbol
	 * 
	 * @return quoted string
	 */
	public static String quote(String s, String quoteSymbol) {
		if (s == null || quoteSymbol == null) {
			throw new IllegalArgumentException("Neither the string nor the quote symbol are allowed to be null");
		}

		s = new StringBuilder().append(quoteSymbol).append(s).append(quoteSymbol).toString();
		return s;
	}
	
	/**
	 * Appends a CSS class to an existing (possibly null) CSS class string.
	 * 
	 * @param originalString the original string
	 * @param cssClass the new CSS class to append
	 * @return the CSS classes appended together (with a space between if originalString is non-empty)
	 */
	public static String stringByAppendingCSSClass(String originalString, String cssClass) {
		String newString;
		if (cssClass == null || cssClass.length() == 0) {
			newString = originalString;
		}
		else if (originalString == null || originalString.length() == 0) {
			newString = cssClass;
		}
		else {
			newString = originalString + " " + cssClass;
		}
		return newString;
	}

	/**
	 * Removes HTML characters from the given string.
	 * 
	 * @param str the string to remove HTML from
	 * @param convertChars set to true if you want html special chars to be converted ( ex. &copy; to (C) ), false otherwise
	 * @return the string without HTML characters in it
	 */
	public static String stripHtml(String str, boolean convertChars) {
 		String stripped = str;
 		if (stripped != null) {
 			stripped = stripped.replaceAll("<[^>]*>", " ");
			if(convertChars) {
				stripped = stripped.replaceAll("\\s+", " ");
				stripped = stripped.replaceAll("&#8217;", "'");
				stripped = stripped.replaceAll("&#169;", "(C)");
				stripped = stripped.replaceAll("&#215;", " x ");
				stripped = stripped.replaceAll("&#8230;", "...");
				stripped = stripped.replaceAll("&#8212;", " -- ");
				stripped = stripped.replaceAll("&#8211;", " - ");
				stripped = stripped.replaceAll("&#8220;", "\"");
				stripped = stripped.replaceAll("&#8221;", "\"");
				stripped = stripped.replaceAll("&#174;", "(C)");
				stripped = stripped.replaceAll("&#174;", "(R)");
				stripped = stripped.replaceAll("&#8482;", "(TM)");
			stripped = stripped.trim();
			}
		}
		return stripped;
	}
	
    /**
     * Removes all of the HTML tags from a given string.
     * Note: this is a very simplistic implementation
     * and will most likely not work with complex HTML.
     * Note: for actual conversion of HTML tags into regular
     * strings have a look at {@link ERXSimpleHTMLFormatter}
     * @param s html string
     * @return string with all of its html tags removed
     */
    // FIXME: this is so simplistic it will break if you sneeze
    public static String removeHTMLTagsFromString(String s) {
        StringBuffer result=new StringBuffer();
        if (s != null && s.length()>0) {
            int position=0;
            while (position<s.length()) {
                int indexOfOpeningTag=s.indexOf("<",position);
                if (indexOfOpeningTag!=-1) {
                    if (indexOfOpeningTag!=position)
                        result.append(s.substring(position, indexOfOpeningTag));
                    position=indexOfOpeningTag+1;
                } else {
                    result.append(s.substring(position, s.length()));
                    position=s.length();
                }
                int indexOfClosingTag=s.indexOf(">",position);
                if (indexOfClosingTag!=-1) {
                    position= indexOfClosingTag +1;
                } else {
                    result.append(s.substring(position, s.length()));
                    position=s.length();
                }
            }
        }
        return StringUtils.replace(result.toString(), "&nbsp;"," ");
    }
    
    /**
     * Returns the value stripped from HTML tags if <b>escapeHTML</b> is false.
     * This makes sense because it is not terribly useful to have half-finished tags in your code.
     * Note that the "length" of the resulting string is not very exact.
     * FIXME: we could remove extra whitespace and character entities here
     * @return value stripped from tags.
     */
    public static String strippedValue(String value, int length) {
        if(value == null || value.length() < 1)
            return null;
        StringTokenizer tokenizer = new StringTokenizer(value, "<", false);
        int token = value.charAt(0) == '<' ? 0 : 1;
        String nextPart = null;
        StringBuffer result = new StringBuffer();
        int currentLength = result.length();
        while (tokenizer.hasMoreTokens() && currentLength < length && currentLength < value.length()) {
            if(token == 0)
                nextPart = tokenizer.nextToken(">");
            else {
                nextPart = tokenizer.nextToken("<");
                if(nextPart.length() > 0  && nextPart.charAt(0) == '>')
                    nextPart = nextPart.substring(1);
            }
            if (nextPart != null && token != 0) {
                result.append(nextPart);
                currentLength += nextPart.length();
            }
            token = 1 - token;
        }
        return result.toString();
    }
	
	/**
	 * @deprecated use {@link #stripHtml(String, boolean)}
	 */
	@Deprecated
	public static String stripHtml(String str) {
		return stripHtml(str, false);
	}

	/**
	 * Attempts to convert string values for attributes into the appropriate
	 * value class for the attribute. If the method is unable to convert the
	 * value, it returns null.
	 * 
	 * @param attr The attribute for the value in question.
	 * @param strVal The string value to be coerced.
	 * @param encoding The encoding used if the attribute value class is custom
	 * and the factory method does not accept a string.
	 * @param formatter The formatter used if the value class is NSTimestamp.
	 * @return The coerced object value or null.
	 */
	public static Object attributeValueFromString(EOAttribute attr, String strVal, String encoding, Format formatter) {
		Object val = null;
		Class attrValueClass = null;
		try {
			attrValueClass = Class.forName(attr.className());
		} catch (ClassNotFoundException cnfe) {
			//An attribute has a className that is not in the classpath
			throw NSForwardException._runtimeExceptionForThrowable(cnfe);
		}
		
    	// If value is a date, parse using the formatter.
    	if(NSTimestamp.class.equals(attrValueClass)) {

    		Date parseResult = null;
    		try {
    			parseResult = (Date)formatter.parseObject(strVal);
        		val = new NSTimestamp(parseResult);
    		} catch(ParseException pe) {
    			// If the user mangles the date format in the URL, we probably 
    			// want to feed them an error page rather than handle it here.
    			throw NSForwardException._runtimeExceptionForThrowable(pe);
    		}
    		    		
    	// If number, convert string to number type with reflection.
    	} else if(Number.class.isAssignableFrom(attrValueClass)) {
    		val = attributeNumberValueFromString(attr, strVal);
				
    	// If string, it's a direct assignment
    	} else if (String.class.equals(attrValueClass)) {
    		val = strVal;
    	
    	// If none of the above, check for a custom factory method.
    	} else if(attr.valueFactoryMethod()!=null) {
    		val = attributeCustomValueFromString(attr, strVal, encoding);
    	}
		
		return val;
	}


	/**
	 * Attempts to convert string values for attributes into the appropriate
	 * value class for the attribute. If the method is unable to convert the
	 * value, it returns null.
	 * 
	 * @param attr The attribute for the value in question.
	 * @param strVal The string value to be coerced.
	 * @return The coerced object value or null.
	 */
	public static Number attributeNumberValueFromString(EOAttribute attr, String strVal) {
		Number val = null;
		// Determine the date class required
		String typeString = attr.valueType();
		if (typeString != null) {
    		char key = typeString.charAt(0);
			String numberType = null;

			switch (key) {
			case EOAttribute._VTByte:
				numberType = Byte.class.getName();
				break;
			
			case EOAttribute._VTShort:
				numberType = Short.class.getName();
				break;
			
			case EOAttribute._VTInteger:
				numberType = Integer.class.getName();
				break;
				
			case EOAttribute._VTLong:
				numberType = Long.class.getName();
				break;
				
			case EOAttribute._VTFloat:
				numberType = Float.class.getName();
				break;
				
			case EOAttribute._VTDouble:
				numberType = Double.class.getName();
				break;
				
			case EOAttribute._VTBigDecimal:
				numberType = BigDecimal.class.getName();
				break;
				
			case EOAttribute._VTBoolean:
				numberType = Boolean.class.getName();
				break;
				
			default:
				break;
			}
    		
			// Generate value through reflection
			if(numberType!=null) {
	    		try {
	    			Class numberClass = Class.forName(numberType);
	    			Constructor numberConstructor = numberClass.getConstructor(new Class[] {String.class});
	    			val = (Number)numberConstructor.newInstance(strVal);
	    		} catch(Exception e) {
	    			throw NSForwardException._runtimeExceptionForThrowable(e);
	    		}
			}
		}
		return val;
	}
	

	/**
	 * Attempts to convert string values for attributes into the appropriate
	 * value class for the attribute. If the method is unable to convert the
	 * value, it returns null.
	 * 
	 * @param attr The attribute for the value in question.
	 * @param strVal The string value to be coerced.
	 * @param encoding The encoding used if the attribute value class is custom
	 * and the factory method does not accept a string.
	 * @return The coerced object value or null.
	 */
	public static Object attributeCustomValueFromString(EOAttribute attr, String strVal, String encoding) {
		Object val = null;
		Class attrValueClass = null;
		try {
			attrValueClass = Class.forName(attr.className());
		} catch (ClassNotFoundException cnfe) {
			//An attribute has a className that is not in the classpath
			NSForwardException._runtimeExceptionForThrowable(cnfe);
		}
		NSSelector sel = attr.valueFactoryMethod();
		
		try {
			Method m = sel.methodOnClass(attrValueClass);
			
    		switch (attr.factoryMethodArgumentType()) {
			case EOAttribute.FactoryMethodArgumentIsBytes:
				if(encoding==null){throw new NullPointerException();}
				byte[] b = strVal.getBytes(encoding);
				val = m.invoke(null, new Object[] {b});
				break;

			case EOAttribute.FactoryMethodArgumentIsData:
				if(encoding==null){throw new NullPointerException();}
				NSData d = new NSData(strVal, encoding);
				val = m.invoke(null, new Object[] {d});
				break;
				
			case EOAttribute.FactoryMethodArgumentIsString:
				val = m.invoke(null, new Object[] {strVal});
				break;
				
			default:
				break;
			}
		} catch (NullPointerException npe) {
			throw npe;
		} catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}

		return val;
	}

	/**
	 * Returns whether the given value falls in a range defined by the given string, which is 
	 * in the format "1-5,100,500,800-1000".
	 * 
	 * @param value the value to check for
	 * @param rangeString the range string to parse
	 * @return whether or not the value falls within the given ranges
	 */
	public static boolean isValueInRange(int value, String rangeString) {
		boolean rangeMatches = false;
		if (rangeString != null && rangeString.length() > 0) {
			String[] ranges = rangeString.split(",");
			for (String range : ranges) {
				range = range.trim();
				int dashIndex = range.indexOf('-');
				if (dashIndex == -1) {
					int singleValue = Integer.parseInt(range);
					if (value == singleValue) {
						rangeMatches = true;
						break;
					}
				}
				else {
					int lowValue = Integer.parseInt(range.substring(0, dashIndex).trim());
					int highValue = Integer.parseInt(range.substring(dashIndex + 1).trim());
					if (value >= lowValue && value <= highValue) {
						rangeMatches = true;
						break;
					}
				}
			}
		}
		return rangeMatches;
	}
	
	/**
	 * Masks a given string with a single character in the substring specified by the
	 * begin and end indexes.  Negative indexes count from the end of the string 
	 * beginning with -1.  For example,
	 * <code>maskStringWithCharacter("Visa 4111111111111111", '*', 5, -4);</code> will
	 * result in a string value of "Visa ************1111" 
	 * 
	 * @param arg The string value to mask
	 * @param mask The character mask
	 * @param beginIndex The string index where masking begins. 
	 * Negative numbers count down from the end of the string.
	 * @param endIndex The index where masking ends.
	 * Negative numbers count down from the end of the string
	 * @return The masked string result
	 */
	public static String maskStringWithCharacter(String arg, char mask, int beginIndex, int endIndex) {
		int length = arg.length();
		
		//Get the actual begin and end index.
		int begin = (beginIndex < 0)?length + beginIndex:beginIndex;
		int end = (endIndex < 0)?length + endIndex:endIndex;
		int sub = end - begin;
		if(sub < 0) {
			throw new StringIndexOutOfBoundsException(sub);
		}
				
		StringBuilder sb = new StringBuilder(arg.substring(0, begin));
		for(int i = 0; i < sub; i++) { 
			sb.append(mask);
		}
		sb.append(arg.substring(end, length));
		return sb.toString();
	}
	
	/**
	 * A fast Luhn algorithm check. This method only verifies that the string
	 * argument validates with the Luhn algorithm. It does not attempt to verify
	 * if the number conforms to ISO/IEC 7812 specifications.
	 * 
	 * @param value
	 *            A string value consisting of numeric digits between 0-9. If
	 *            the number contains hyphens, spaces, or anything other than a
	 *            string of digits between 0-9 the method returns false.
	 * 
	 * @return true if the value passes a luhn check, false otherwise.
	 */
	public static boolean luhnCheck(String value) {
		final int length = value.length(), parity = length % 2;
		final char zero = '0';
		int sum = 0;
		for (int i = 0, tmp = 0; i < length; i++) {
			tmp = value.charAt(i) - zero;
			if (tmp < 0 || tmp > 9) { return false; }
			sum += (i % 2 == parity) ? ((2 * tmp) / 10) + ((2 * tmp) % 10) : tmp;
		}
		return sum % 10 == 0;
	}

	/**
	* Returns a string trimmed about at the max lenght you define without truncating the last word and adding "..." (if necessary)
	* 
	* @param trimmingString the string you would like to trim
	* @param maxLenght the max lenght you need
	* @return the string trimmed
	*/
	public static String wordSafeTrimmedString(String trimmingString, int maxLenght) {
		String cuttedString = trimmingString;
		if ( ( trimmingString != null ) && ( trimmingString.length() > maxLenght ) ) {
			trimmingString = stripHtml(trimmingString,false);
			if( trimmingString.length() > maxLenght) {
				int space = trimmingString.indexOf(" ",(maxLenght - 20));
				try {
					cuttedString = trimmingString.substring(0, space)+" ...";
				} catch ( Exception e ) {
					//GIVE UP
				}
			}
		}
		return cuttedString;
	}

	/**
	 * <span class="en">
	 * 	trim leading 0 from a (Number) String 
	 *
	 * 	@param str - the String
	 * 
	 * 	@return Result String
	 * </span>
	 * 
	 * <span class="ja">
	 * 	半角数字の前の0を取る処理。
	 *
	 * 	@param str - 処理対象の文字列
	 * 
	 * 	@return 処理済みの文字列
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static String trimZeroInFrontOfNumbers(String str){
		// 文字有無確認
		if(stringIsNullOrEmpty(str)) 
			return str;

		// 不必要番号削除処理
		int loopIdxMax =  str.length() -1;
		StringBuilder retStr = new StringBuilder(loopIdxMax +1);
		char targetChar;
		boolean alladdFlg = false;
		for(int loopIdx = 0; loopIdx < loopIdxMax; loopIdx++){
			targetChar = str.charAt(loopIdx);
			if(alladdFlg || ((targetChar >= '1') && (targetChar <= '9')) ){
				retStr.append ( targetChar );	// 文字コードが0以外なら残りを全て保存
				alladdFlg = true;
			}
		}
		retStr.append (str.charAt(loopIdxMax));	// 最後のコードを追加

		return retStr.toString();
	}

    /**
     * Given an initial string and an array of substrings, 
     * Removes any occurrences of any of the substrings
     * from the initial string. Used in conjunction with
     * fuzzy matching.
     * @param newString initial string from which to remove other strings
     * @param toBeCleaneds array of substrings to be removed from the initial string.
     * @return cleaned string.
     */
    // FIXME: Should use a StringBuffer instead of creating strings all over the place.
    public static String cleanString(String newString, NSArray<String> toBeCleaneds) {
        String result=newString;
        if (newString!=null) {
            for(Enumeration e = toBeCleaneds.objectEnumerator(); e.hasMoreElements();){
                String toBeCleaned = (String)e.nextElement();
                if(newString.toUpperCase().indexOf(toBeCleaned)>-1){
                    result=newString.substring(0, newString.toUpperCase().indexOf(toBeCleaned));
                }
            }
        }
        return result;
    }
	
	public static boolean isBlank(String value) {
		boolean isBlank = false;
		if (value == null || value.trim().length() == 0) {
			isBlank = true;
		}
		return isBlank;
	}
	
	public static boolean isNotBlank(String value) {
		return ! isBlank(value);
	}
}
