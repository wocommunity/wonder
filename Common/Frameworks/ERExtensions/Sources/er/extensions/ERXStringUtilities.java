//
// StringUtilities.java
// Project linksadmin
//
// Created by ak on Mon Nov 05 2001
//
package er.extensions;
import java.io.*;
import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Collection of {@link java.lang.String String} utilities. Contains
 * the base localization support.
 */
public class ERXStringUtilities {
    
    /** Holds the default display language, which is English */
    private static final String DEFAULT_TARGET_DISPLAY_LANGUAGE = "English";
    /** Holds the chars for hex enconding */
    public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    /** Holds the distance key */
    private static final String _DISTANCE = "distance";
    /** Holds the ascending <code>EOSortOrdering</code>s */
    public static final NSArray SORT_ASCENDING = 
        new NSArray(new Object [] { new EOSortOrdering(_DISTANCE, EOSortOrdering.CompareAscending) });
    /** Holds the ascending <code>EOSortOrdering</code>s */
    public static final NSArray SORT_DESCENDING = 
        new NSArray(new Object [] { new EOSortOrdering(_DISTANCE, EOSortOrdering.CompareDescending) });
    
    /** 
     * Holds the array of default display languages. Holds
     * a single entry for English.
     */
    private static NSArray _defaultTargetDisplayLanguages = new NSArray(DEFAULT_TARGET_DISPLAY_LANGUAGE);
    
    /**
     * Java port of the distance algorithm.
     *
     * The code below comes from the following post on http://mail.python.org
     * Fuzzy string matching
     *   Magnus L. Hetland mlh@idt.ntnu.no
     *   27 Aug 1999 15:51:03 +0200
     *
     *  Explanation of the distance algorithm...
     *
     *  The algorithm:
     *
     *  def distance(a,b):
     *   c = {}
     *  n = len(a); m = len(b)
     *
     *  for i in range(0,n+1):
     *  c[i,0] = i
     *  for j in range(0,m+1):
     *  c[0,j] = j
     *
     *  for i in range(1,n+1):
     *  for j in range(1,m+1):
     *  x = c[i-1,j]+1
     *  y = c[i,j-1]+1
     *  if a[i-1] == b[j-1]:
     *    z = c[i-1,j-1]
     *  else:
     *    z = c[i-1,j-1]+1
     *  c[i,j] = min(x,y,z)
     *  return c[n,m]
     *
     *  It calculates the following: Given two strings, a and b, and three
     *  operations, adding, subtracting and exchanging single characters, what
     *  is the minimal number of steps needed to translate a into b?
     *
     *  The method is based on the following idea:
     *
     *  We want to find the distance between a[:x] and b[:y]. To do this, we
     *  first calculate
     *
     *  1) the distance between a[:x-1] and b[:y], adding the cost of a
     *  subtract-operation, used to get from a[:x] to a[:z-1];
     *
     *  2) the distance between a[:x] and b[:y-1], adding the cost of an
     *  addition-operation, used to get from b[:y-1] to b[:y];
     *
     *  3) the distance between a[:x-1] and b[:y-1], adding the cost of a
     *  *possible* exchange of the letter b[y] (with a[x]).
     *
     *  The cost of the subtraction and addition operations are 1, while the
     *  exchange operation has a cost of 1 if a[x] and b[y] are different, and
     *  0 otherwise.
     *
     *  After calculating these costs, we choose the least one of them
     * (since we want to use the best solution.)
     *
     *  Instead of doing this recursively, i.e. calculating ourselves "back"
     *  from the final value, we build a cost-matrix c containing the optimal
     *  costs, so we can reuse them when calculating the later values. The
     *  costs c[i,0] (from string of length n to empty string) are all i, and
     *  correspondingly all c[0,j] (from empty string to string of length j)
     *  are j.
     *
     *  Finally, the cost of translating between the full strings a and b
     *  (c[n,m]) is returned.
     *
     *  I guess that ought to cover it...
     * --------------------------
     * @param a first string
     * @param b second string
     * @return the distance between the two strings
     */
    public static double distance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int c[][] = new int[n+1][m+1];
        for(int i = 0; i<=n; i++){
            c[i][0] = i;
        }
        for(int j = 0; j<=m; j++){
            c[0][j] = j;
        }
        for(int i = 1; i<=n; i++){
            for(int j = 1; j<=m; j++){
                int x = c[i-1][j] + 1;
                int y = c[i][j-1] + 1;
                int z = 0;
                if(a.charAt(i-1) == b.charAt(j-1))
                    z = c[i-1][j-1];
                else
                    z = c[i-1][j-1] + 1;
                int temp = Math.min(x,y);
                c[i][j] = Math.min(z, temp);
            }
        }
        return c[n][m];
    }

    /** holds the base adjustment for fuzzy matching */
    // FIXME: Not thread safe
    // MOVEME: Needs to go with the fuzzy matching stuff
    protected static double adjustement = 0.5;
    private static ERXLogger log = ERXLogger.getERXLogger(ERXStringUtilities.class);

    /**
     * Sets the base adjustment used for fuzzy matching
     * @param newAdjustment factor to be used.
     */
    // FIXME: Not thread safe.
    // MOVEME: fuzzy matching stuff
    public static void setAdjustement(double newAdjustement) {
        adjustement = newAdjustement;
    }

    /**
     * Fuzzy matching is useful for catching user entered typos. For example
     * if a user is search for a company named 'Aple' within your application
     * they aren't going to find it. Thus the idea of fuzzy matching, meaning you
     * can define a threshold of 'how close can they be' type of thing.
     *
     * @param name to be matched against
     * @param entityName name of the entity to perform the match against.
     * @param proertyKey to be matched against
     * @param synonymsKey allows objects to have additional values to be matched
     * 		against in addition to just the value of the propertyKey
     * @param ec context to fetch data in
     * @param cleaner object used to clean a string, for example the cleaner might
     *		strip out the words 'The' and 'Inc.'
     * @param orderings can be either <code>SORT_ASCENDING</code> or <code>SORT_DESCENDING</code> 
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
        NSMutableArray results = new NSMutableArray();
        EOFetchSpecification fs = new EOFetchSpecification( entityName, null, null );
        fs.setFetchesRawRows( true );
        NSArray pks = EOUtilities.entityNamed( ec, entityName ).primaryKeyAttributeNames();
        NSMutableArray keyPaths = new NSMutableArray(pks);
        keyPaths.addObject( propertyKey );
        if( synonymsKey != null ) 
            keyPaths.addObject( synonymsKey );
        //we use only the strictly necessary keys.
        fs.setRawRowKeyPaths( keyPaths );
        NSArray rawRows = ec.objectsWithFetchSpecification( fs );
        if(name == null)
            name = "";
        name = name.toUpperCase();
        String cleanedName = cleaner.cleanStringForFuzzyMatching(name);
        for(Enumeration e = rawRows.objectEnumerator(); e.hasMoreElements(); ){
            NSMutableDictionary dico = ((NSDictionary)e.nextElement()).mutableClone();
            Object value = dico.valueForKey(propertyKey);
            boolean trySynonyms = true;
            //First try to match with the name of the eo
            if( value!=null && value instanceof String){
                String comparedString = ((String)value).toUpperCase();
                String cleanedComparedString = cleaner.cleanStringForFuzzyMatching(comparedString);
                if( (distance(name, comparedString) <=
                     Math.min((double)name.length(), (double)comparedString.length())*adjustement ) ||
                    (distance(cleanedName, cleanedComparedString) <=
                     Math.min((double)cleanedName.length(), (double)cleanedComparedString.length())*adjustement)){
                    dico.setObjectForKey( new Double(distance(name, comparedString)), _DISTANCE );
                    NSDictionary pkValues = new NSDictionary( dico.objectsForKeys( pks, NSKeyValueCoding.NullValue ), pks );
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
                        if((distance(name, comparedString) <=
                            Math.min((double)name.length(), (double)comparedString.length())*adjustement) ||
                           (distance(cleanedName, comparedString) <=
                            Math.min((double)cleanedName.length(), (double)comparedString.length())*adjustement)){
                            dico.setObjectForKey( new Double(distance(name, comparedString)), _DISTANCE );
                            NSDictionary pkValues = new NSDictionary( dico.objectsForKeys( pks, NSKeyValueCoding.NullValue ), pks );
                            dico.setObjectForKey( EOUtilities.faultWithPrimaryKey( ec, entityName, pkValues ), eoKey );
                            results.addObject( dico );
                            break;
                        }
                    }
                }
            }
        }
        if( sortOrderings != null ) {
            results = (NSMutableArray)EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)results, sortOrderings);
        }
        return (NSArray) results.valueForKey( eoKey );        
    }
    
    /** @deprecated use 
        <code>fuzzyMatch(String name, String entityName, String propertyKey,
                         String synonymsKey, EOEditingContext ec,
                         ERXFuzzyMatchCleaner cleaner, NSArray sortOrderings )</code>
        instead*/
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
     * @param path to the file in the file system
     * @return the contents of the file in a string
     */
    public static String stringWithContentsOfFile(String path) {
        try {
            if(path != null)
                return ERXFileUtilities.stringFromFile(new File(path));
        } catch (IOException e) {
        	log.error(e, e);
        }
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
     * @see ERXConstant#integerForString(String)
     */
    public static Integer integerWithString(String s) {
        try {
            return ERXConstant.integerForString(s);
        } catch (Exception e) {
        }
        return null;
    } 

    /**
     * Retrives a given string for a given name, extension
     * and bundle.
     * @param name of the resource
     * @param extension of the resource, example: txt or rtf
     * @param bundle to look for the resource in
     * @return string of the given file specified in the bundle
     */
    public static String stringFromResource(String name, String extension, NSBundle bundle) {
        String path = null;
        if(bundle != null) {
            path = bundle.resourcePathForLocalizedResourceNamed(name + (extension == null || extension.length() == 0 ? "" : "." + extension), null);
            try {
                InputStream stream = bundle.inputStreamForResourcePath(path);
                byte bytes[] = ERXFileUtilities.bytesFromInputStream(stream);
                return new String(bytes);
            } catch (IOException e) {
                log.warn("IOException when stringFromResource(" + name + "." + extension + " in bundle " + bundle.name());
            }
        } else {
            log.warn("Not found: " + name + "." + extension + " in bundle " + bundle.name());
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
        StringBuffer finalString = null;
        if (!stringIsNullOrEmpty(key) && !key.trim().equals("")) {
            finalString = new StringBuffer();
            String lastHop=key.indexOf(".") == -1 ? key : key.endsWith(".") ? "" : key.substring(key.lastIndexOf(".") + 1);
            StringBuffer tempString = new StringBuffer();
            char[] originalArray = lastHop.toCharArray();
            originalArray[0] = Character.toUpperCase(originalArray[0]);
            Character tempChar = null;
            Character nextChar = null;
            for(int i=0;i<(originalArray.length-1);i++){
                tempChar = new Character(originalArray[i]);
                nextChar = new Character(originalArray[i+1]);
                if(Character.isUpperCase(originalArray[i]) &&
                   Character.isLowerCase(originalArray[i+1])) {
                    finalString.append(tempString.toString());
                    if (i>0) finalString.append(' ');
                    tempString = new StringBuffer();
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
     * @param string to scan
     * @return position in int. -1 for not found. 
     */ 
    public static int indexOfNumericInString(String str) {
        return indexOfNumericInString(str, 0);
    }
        
    /** 
     * Locate the the first numeric character 
     * after <code>fromIndex</code> in the given string. 
     * @param string to scan
     * @return position in int. -1 for not found. 
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
     * @param separator character to add to potentially
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
     * Replaces a given string by another string in a string.
     * @param old string to be replaced
     * @param newString to be inserted
     * @param buffer string to have the replacement done on it
     * @return string after having all of the replacement done.
     */
    public static String replaceStringByStringInString(String old, String newString, String buffer) {
        int begin, end;
        int oldLength = old.length();
        int length = buffer.length();
        StringBuffer convertedString = new StringBuffer(length + 100);

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
     * Removes the spaces in a given String
     * @return string removing all spaces in it.
     */
    public static String escapeSpace(String aString){
        NSArray parts = NSArray.componentsSeparatedByString(aString," ");
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
        if (different == -1)
            return str;
        else {
            char[] chars = new char[len];
            str.getChars(0, len, chars, 0);
            // (Note we start at different, not at len.)
            for(int j = different; j >= 0; j--) {
                chars[j] = Character.toLowerCase(chars[j]);
            }

            return new String(chars);
        }
    }

    /**
     * String multiplication.
     * @param n the number of times to concatinate a given string
     * @param s string to be multipled
     * @return multiplied string
     */
    public static String stringWithNtimesString(int n, String s) {
        StringBuffer sb=new StringBuffer();
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
     * @return "" if the string is null else the string
     */
    public static String emptyStringForNull(String s) {
        return s==null ? "" : s;
    }

    public static String escapeNonXMLChars(String str) {
        if (str == null) return null;

        StringBuffer result = new StringBuffer(str.length());
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

    public static String escapeNonBasicLatinChars(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        if (block != null  &&  Character.UnicodeBlock.BASIC_LATIN.equals(block)) 
            return String.valueOf(c);
        else 
            return toHexString(c);
    }

    public static String escapeNonBasicLatinChars(String str) {
        if (str == null) return null;

        StringBuffer result = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); i++) 
            result.append(escapeNonBasicLatinChars(str.charAt(i)));
            
        return result.toString();
    }

    public static String toHexString(char c) {
        StringBuffer result = new StringBuffer("\u005C\u005Cu9999".length());
        String u = Long.toHexString((int) c).toUpperCase();
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

        StringBuffer result = new StringBuffer("\u005C\u005Cu9999".length() * str.length());
        for (int i = 0; i < str.length(); i++) 
            result.append(toHexString(str.charAt(i)));

        return result.toString();
    }

    /*
     * Converts a byte array to hex string.
     * @param block byte array
     * @return hex string
     */
    public static String byteArrayToHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            int high = ((block[i] & 0xf0) >> 4);
            int low  =  (block[i] & 0x0f);
            buf.append(HEX_CHARS[high]);
            buf.append(HEX_CHARS[low]);
        }
        return buf.toString();
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
            String minorVersion = ERXStringUtilities.replaceStringByStringInString(".", "", 
                                        version.substring(floatingPointIndex + 1));
            version = version.substring(0, floatingPointIndex + 1) + minorVersion;
        }
        return version;
    }

    /**
     * Capitalizes a given string.
     * @param value to be capitalized
     * @return capitalized string
     */
    public static String capitalize(String value) {
        String capital = null;
        if (value != null && value.length() > 0) {
            StringBuffer buffer = new StringBuffer(value);

            buffer.setCharAt(0, Character.toUpperCase(value.charAt(0)));
            capital = buffer.toString();            
        }
        return capital != null ? capital : value;
    }

    /**
     * Capitalizes all the strings in a given string.
     * @param value to be capitalized
     * @return capitalized string
     */    
    public static String capitalizeAllWords(String value) {
        String capitalize = null;
        if (value != null && value.length() > 0) {
            StringBuffer buffer = new StringBuffer();
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

    /** checks if the specified String contains only digits. 
     * 
     * @param name
     * @return
     */
    public static boolean isDigitsOnly(String name) {
        for (int i = name.length(); i-- > 0;) {
            char c = name.charAt(i);
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
    
}
