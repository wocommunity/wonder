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

    /** Translates ("de"," en;q=0.66") to ("de","en").
     * @param languages NSArray of Strings
     * @returns NSArray of normalized Strings
     */
    protected NSArray fixAbbreviationArray(NSArray languages) {
        NSMutableArray nsmutablearray = new NSMutableArray(languages.count());
        int cnt = languages.count();
        for (int i = 0; i < cnt; i++) {
            String string = (String) languages.objectAtIndex(i);
            int offset;
            string = string.trim();
            offset = string.indexOf(';');
            if (offset > 0)
                string = string.substring(0, offset);
            offset = string.indexOf('-');
            if (offset > 0) {
                String cooked;
                cooked = string.substring(0, 2);
                cooked = cooked + "_";
                cooked = cooked + string.substring(offset+1, offset+3);
                string = cooked;
            }
            nsmutablearray.addObject(string);
        }
        return nsmutablearray;
    }
}
