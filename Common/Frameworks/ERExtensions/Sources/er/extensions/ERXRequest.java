package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.eocontrol.*;

/** Subclass of WORequest that fixes several Bugs.
 * The ID's are #2924761 and #2961017. It can also be extended to handle
 * #2957558 ("de-at" is converted to "German" instead of "German_Austria").
 * The request is created via ${link ERXApplication$createRequest()}.
 */
public  class ERXRequest extends WORequest {
    static final ERXLogger log = ERXLogger.getLogger(ERXRequest.class);
    /** Simply call superclass constructor */
    public ERXRequest(String string, String string0, String string1,
                      NSDictionary nsdictionary, NSData nsdata,
                      NSDictionary nsdictionary2) {
        super(string, string0, string1, nsdictionary,
              nsdata, nsdictionary2);
    }

    /** NSArray to keep browserLanguages in. */
    protected  NSArray _browserLanguages;

    /** Returns a cooked version of the languages the user has set in his Browser.
     * Adds "Nonlocalized" and {link ERXLocalizer$defaultLanguage()} if not
     * already present.
     * @returns cooked version of user's languages
     */
    public NSArray browserLanguages() {
        if (_browserLanguages == null) {
            NSArray fixedLanguages = null;
            String string = this.headerForKey("accept-language");
            if (string != null) {
                NSArray rawLanguages
                = NSArray.componentsSeparatedByString(string, ",");
                fixedLanguages = fixAbbreviationArray(rawLanguages);
            }
            NSMutableArray languageKeys =
                WOProperties.TheLanguageDictionary.objectsForKeys(fixedLanguages,
                                                                  null).mutableClone();
            languageKeys.addObject("Nonlocalized");
            if(!languageKeys.containsObject(ERXLocalizer.defaultLanguage()))
                languageKeys.addObject(ERXLocalizer.defaultLanguage());
            _browserLanguages = languageKeys.immutableClone();
        }
        return _browserLanguages;
    }

    /** Translates ("de", "en-gb;q=0.66", "en-us;q=0.33") to ("de", "en_gb", "en-us", "en").
     * @param languages NSArray of Strings
     * @returns NSArray of normalized Strings
     */
    protected NSArray fixAbbreviationArray(NSArray languages) {
        NSMutableArray nsmutablearray = new NSMutableArray(languages.count());
        int cnt = languages.count();
        for (int i = cnt - 1; i >= 0; i--) {
            String string = (String) languages.objectAtIndex(i);
            int offset;
            string = string.trim();
            offset = string.indexOf(';');
            if (offset > 0)
                string = string.substring(0, offset);
            offset = string.indexOf('-');
            if (offset > 0) {
                String langPrefix = string.substring(0, 2);  //  "en" part of "en-us"
                if (!nsmutablearray.containsObject(langPrefix)) 
                    nsmutablearray.insertObjectAtIndex(langPrefix, 0);
                // converts "en-us" into "en_us";
                StringBuffer cooked = new StringBuffer(string.length());
                cooked.append(langPrefix)
                    .append("_")
                    .append(string.substring(offset+1, offset+3));
                string = cooked.toString();
            }
            nsmutablearray.insertObjectAtIndex(string, 0);
        }
        return nsmutablearray;
    }

    public NSDictionary cookieValues() {
        try {
            return super.cookieValues();
        } catch (Throwable t) {
            log.warn(t + ":" + this);
            log.warn(t);
            return new NSDictionary();
        }
    }    
}
