//
// StringUtilities.java
// Project linksadmin
//
// Created by ak on Mon Nov 05 2001
//
package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.io.*;
import java.util.*;

/**
 * Collection of {@lin String} utilities. Contains
 * the base localization support.
 */
public class ERXStringUtilities extends Object {

    /** Holds the default display language, which is English */
    private static final String DEFAULT_TARGET_DISPLAY_LANGUAGE = "English";
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
     *  After calculating these costs, we choose the least one of them (since
                                                                        *                                                          we want to use the best solution.)
     *
     *  Instead of doing this recursively (i.e. calculating ourselves "back"
                                           *                             from the final value), we build a cost-matrix c containing the optimal
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
    // MOVEME: ERXStringUtilities
    public static double distance( String a, String b){
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
     * @param comparisonString can be either 'asc' or 'desc' to tell how the results
     *		should be sorted. Bad design, this will change.
     * @return an array of objects that match in a fuzzy manner the name passed in.
     */
    // FIXME: This needs to be made more generic, i.e. right now it depends on having a field 'distance' on the
    //	      enterprise object. Also right now it fetches *all* of the attributes for *all* of the entities.
    //	      that is very costly. Should only be getting the attribute and pk.
    // FIXME: Bad api design with the comparisonString, should just pass in an EOSortOrdering
    // MOVEME: Not sure, maybe it's own class and put the interface as a static inner interface
    public static NSArray fuzzyMatch(String name,
                                     String entityName,
                                     String propertyKey,
                                     String synonymsKey,
                                     EOEditingContext ec,
                                     ERXFuzzyMatchCleaner cleaner,
                                     String comparisonString){
        NSMutableArray results = new NSMutableArray();
        NSArray rawRows = EOUtilities.rawRowsMatchingValues( ec, entityName, null);
        if(name == null)
            name = "";
        name = name.toUpperCase();
        String cleanedName = cleaner.cleanStringForFuzzyMatching(name);
        for(Enumeration e = rawRows.objectEnumerator(); e.hasMoreElements(); ){
            NSDictionary dico = (NSDictionary)e.nextElement();
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
                    ERXGenericRecord object = (ERXGenericRecord)EOUtilities.objectFromRawRow( ec, entityName, dico);
                    object.takeValueForKey(new Double(distance(name, comparedString)), "distance");
                    results.addObject(object);
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
                            ERXGenericRecord object = (ERXGenericRecord)EOUtilities.objectFromRawRow( ec, entityName, dico);
                            object.takeValueForKey(new Double(distance(name, comparedString)), "distance");
                            results.addObject(object);
                            break;
                        }
                    }
                }

            }
        }
        if(comparisonString != null){
            NSArray sortOrderings = new NSArray();
            if(comparisonString.equals("asc")){
                sortOrderings = new NSArray(new Object [] { new EOSortOrdering("distance",
                                                                               EOSortOrdering.CompareAscending) });
            }else if(comparisonString.equals("desc")){
                sortOrderings = new NSArray(new Object [] { new EOSortOrdering("distance",
                                                                               EOSortOrdering.CompareDescending) });
            }
            results = (NSMutableArray)EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)results, sortOrderings);
        }
        return results;
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
            InputStream in = new FileInputStream(path);
            
            if (null == in)
                throw new RuntimeException("The file '"+ path + "' can not be opened.");
            int length = in.available();
            if (length == 0) {
                return "";
            }
            byte buffer[] = new byte[length];
            in.read(buffer);
            in.close();
            return new String(buffer);
        } catch(Throwable t) {
            // log.debug(t.toString());
        }
        return null;
    }

    /**
     * Calculates an Integer for a given string. The
     * only advantage that this method has is to not
     * throw a number format exception if the string
     * is not correctly formatted.
     * @param s string to caclulate an Integer from
     * @return parsed Integer from the string or null
     *		if the string is not correctly formed.
     */
    public static Integer integerWithString(String s) {
        try {
            return new Integer(Integer.parseInt(s));
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
    // CHECKME: The bundle isn't being used. Is this right?
    public static String stringFromResource(String name, String extension, NSBundle bundle) {
         return stringWithContentsOfFile(WOApplication.application().resourceManager().pathForResourceNamed(name +"." + extension, null, null));
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

    /**
     * Calculates a default display name for a given
     * key path. For instance for the key path:
     * "foo.bar" the display name would be "Bar".
     * @param key to calculate the display name
     * @return display name for the given key
     */
    public static String displayNameForKey(String key) {
        StringBuffer finalString = new StringBuffer();
        if (key != null) {
            NSArray keys=NSArray.componentsSeparatedByString(key,".");
            String lastHop=(String)keys.objectAtIndex(keys.count()-1);
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
                    finalString.append(tempString);
                    if (i>0) finalString.append(' ');
                    tempString = new StringBuffer();
                }
                tempString.append(tempChar.toString());
            }
            finalString.append(tempString);
            finalString.append(nextChar);
        }
        return finalString.toString();
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

}
